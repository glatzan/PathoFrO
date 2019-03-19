package com.patho.main.service

import com.patho.main.common.ContactRole
import com.patho.main.model.Physician
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.model.user.HistoGroup
import com.patho.main.model.user.HistoUser
import com.patho.main.repository.GroupRepository
import com.patho.main.repository.LDAPRepository
import com.patho.main.repository.UserRepository
import com.patho.main.util.user.HistoGroupNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service()
open class UserService @Autowired constructor(
        private val ldapRepository: LDAPRepository,
        private val groupRepository: GroupRepository,
        private val userRepository: UserRepository,
        private val physicianService: PhysicianService) : AbstractService() {

    /**
     * Checks if the session is associated with a user.
     *
     * @return
     */
    val isCurrentUserAvailable: Boolean
        get() = SecurityContextHolder.getContext().authentication.principal is HistoUser

    /**
     * Returns the current user.
     *
     * @return
     */
    val currentUser: HistoUser
        get() = SecurityContextHolder.getContext().authentication.principal as HistoUser

    /**
     * Creates a new user, loads data from ldap and creates the user with the guest
     * group
     *
     * @param uid
     * @return
     */
    @Transactional
    open fun createUser(uid: String): Optional<HistoUser> {
        val physician = ldapRepository.findByUid(uid)
        val group = groupRepository.findOptionalById(HistoGroup.GROUP_GUEST_ID)

        if (physician.isPresent && group.isPresent) {
            var user: HistoUser? = addOrMergeUser(physician.get())

            if (user != null) {
                user = updateGroupOfUser(user, group.get())
                return Optional.ofNullable(user)
            }
        }

        return Optional.empty()
    }

    /**
     * Adds a histouser and updates the passed physician.
     */
    @Transactional
    open fun addOrMergeUser(physician: Physician): HistoUser? {
        return addOrMergeUser(physician, true)
    }

    /**
     * Creates an new user, if a physician with the given username exist they will
     * be merged.
     */
    @Transactional
    open fun addOrMergeUser(physician: Physician, checkPhysician: Boolean): HistoUser {

        // checking if histouser exsists
        val histoUser = userRepository.findOptionalByPhysicianUid(physician.uid)

        if (!histoUser.isPresent) {
            var tmpPhysician = physician
            logger.info("No User found, creating new HistoUser " + tmpPhysician.person.getFullName())

            if (checkPhysician)
                tmpPhysician = physicianService.addOrMergePhysician(tmpPhysician)

            val newHistoUser = HistoUser(tmpPhysician)

            if (tmpPhysician.hasNoAssociateRole())
                tmpPhysician.addAssociatedRole(ContactRole.NONE)

            // saving or updating physician, also updating organizations
            tmpPhysician = physicianService.addOrMergePhysician(tmpPhysician)

            newHistoUser.physician = tmpPhysician

            return userRepository.save(newHistoUser, resourceBundle.get("log.user.created", histoUser))
        } else {
            histoUser.get().physician = physicianService.mergePhysicians(physician, histoUser.get().physician)
            return userRepository.save(histoUser.get(), resourceBundle.get("log.user.update", histoUser))
        }
    }

    /**
     * Updates the users data with data from ldap
     */
    @Transactional
    open fun updateUserWithLdapData(user: HistoUser): HistoUser {
        user.physician = physicianService.updatePhysicianWithLdapData(user.physician)
        return user
    }

    /**
     * Saves the user
     */
    @Transactional
    open fun saveUser(user: HistoUser) {
        if (user.physician.hasNoAssociateRole())
            user.physician.addAssociatedRole(ContactRole.OTHER_PHYSICIAN)
        user.physician = physicianService.addOrMergePhysician(user.physician)
        userRepository.save(user, resourceBundle.get("log.user.update", user))
    }

    /**
     * This method tries to delete a user, if this is not possible, the user will be archived
     */
    @Transactional(propagation = Propagation.NEVER)
    open fun deleteOrDisable(user: HistoUser, deletePhysician: Boolean): Boolean {
        var user = user
        val physician = user.physician

        try {
            // setting dummy for deleting user, this is for preserving the original physician
            user.physician = Physician(Person(Contact()))

            logger.debug("Deleting user")
            // error is only fired by the delete command
            user = userRepository.save(user)

            userRepository.delete(user,
                    resourceBundle.get("log.user.deleted", user.physician.person.getFullName()))

            if (deletePhysician) {
                logger.debug("Deleting physician")
                physicianService.deleteOrArchive(physician)
            } else
                logger.debug("Keeping physician")

            return true
        } catch (e: Exception) {
            // reverting user disable
            user.physician = physician
            logger.debug("deleting not possible, disable")
            disableUser(user)

            // only archive
            if (deletePhysician) {
                logger.debug("Physician is not in use, arching...")
                physicianService.archive(physician, true)
            } else
                logger.debug("Physician is in use, do nothing")

            return false
        }

    }

    /**
     * Disables a user via group settings
     *
     * @param user
     */
    @Transactional
    open fun disableUser(user: HistoUser): HistoUser {
        // do not load settings is done by the updateGroupOfUser methode
        val group = groupRepository.findOptionalById(HistoGroup.GROUP_DISABLED, false)

        if (!group.isPresent)
            throw HistoGroupNotFoundException()

        return updateGroupOfUser(user, group.get())

    }

    /**
     * Archives a user
     *
     * @param user
     * @param archive
     */
    @Transactional
    open fun archiveUser(user: HistoUser, archive: Boolean) {
        user.archived = archive
        userRepository.save(user, resourceBundle.get(if (archive) "log.user.archived" else "log.user.dearchived", user))
    }

    /**
     * Updates the user's group settings for a changed groups
     *
     * @param user
     * @param group
     */
    @Transactional
    open fun updateGroupOfUser(group: HistoGroup) {
        // updating user settings
        val usersOfGroup = userRepository.findByGroupId(group.id)
        usersOfGroup.forEach { p -> updateGroupOfUser(p, group) }
    }

    /**
     * Updates the group of an user
     *
     * @param user
     * @param group
     * @return
     */
    @Transactional
    open fun updateGroupOfUser(user: HistoUser, group: HistoGroup): HistoUser {
        return updateGroupOfUser(user, group, true)
    }

    /**
     * Updates the group of an user
     *
     * @param user
     * @param group
     * @return
     */
    @Transactional
    open fun updateGroupOfUser(user: HistoUser, groupID: Long, save: Boolean): HistoUser {
        val group = groupRepository.findOptionalById(groupID, true)
        if (!group.isPresent)
            throw  HistoGroupNotFoundException()
        return updateGroupOfUser(user, group.get(), save)
    }

    /**
     * Setting the group of the user
     */
    @Transactional
    open fun updateGroupOfUser(user: HistoUser, group: HistoGroup, save: Boolean): HistoUser {
        user.group = group

        GroupService.copyGroupSettings(user, group, false)

        logger.debug("Role of user " + user.username + " to " + user.group.toString())

        return if (save) userRepository.save(user, resourceBundle.get("log.user.roleChanged", user.group)) else user

    }
}