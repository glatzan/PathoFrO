package com.patho.main.service

import com.patho.main.common.ContactRole
import com.patho.main.model.Physician
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.model.user.HistoGroup
import com.patho.main.model.user.HistoSettings
import com.patho.main.model.user.HistoUser
import com.patho.main.repository.GroupRepository
import com.patho.main.repository.LDAPRepository
import com.patho.main.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service()
open class CurrentUserService @Autowired constructor(
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
    fun createUser(uid: String): Optional<HistoUser> {
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
     *
     * @param physician
     * @return
     */
    @Transactional
    fun addOrMergeUser(physician: Physician): HistoUser? {
        return addOrMergeUser(physician, true)
    }

    /**
     * Creates an new user, if a physician with the given username exist they will
     * be merged.
     *
     * @param physician
     * @return
     */
    @Transactional
    fun addOrMergeUser(physician: Physician, checkPhysician: Boolean): HistoUser? {

        // checking if histouser exsists
        val histoUser = userRepository.findOptionalByPhysicianUid(physician.uid)

        if (!histoUser.isPresent) {
            var tmpPhysician = physician
            logger.info("No User found, creating new HistoUser " + tmpPhysician.person.getFullName())

            if (checkPhysician)
                tmpPhysician = physicianService.addOrMergePhysician(tmpPhysician)

            val newHistoUser = HistoUser(tmpPhysician)

            if (tmpPhysician.associatedRoles.size == 0)
                tmpPhysician.addAssociateRole(ContactRole.NONE)

            // saving or updating physician, also updating organizations
            tmpPhysician = physicianService.addOrMergePhysician(tmpPhysician)

            newHistoUser.physician = tmpPhysician

            return userRepository.save(newHistoUser, resourceBundle.get("log.user.created", histoUser))
        } else {
            histoUser.get().physician = physicianService.mergePhysicians(physician, histoUser.get().physician)
            return userRepository.save(histoUser.get(), resourceBundle.get("log.user.update", histoUser))
        }

        /**
         * Updates the users data with data from ldap
         *
         * @param user
         */
        @Transactional
        fun updateUserWithLdapData(user: HistoUser): HistoUser {
            user.physician = physicianService.updatePhysicianWithLdapData(user.physician)
            return user
        }

        /**
         * Save the user
         *
         * @param user
         */
        @Transactional
        fun saveUser(user: HistoUser) {
            if (user.physician.hasNoAssociateRole())
                user.physician.addAssociateRole(ContactRole.OTHER_PHYSICIAN)

            println(user.physician.person.contact.addressadditon)
            user.physician = physicianService.addOrMergePhysician(user.physician)
            println(user.physician.person.contact.addressadditon)
            userRepository.save(user, resourceBundle.get("log.user.update", user))
        }

        @Transactional(propagation = Propagation.NEVER)
        fun deleteOrDisable(user: HistoUser, deletePhysician: Boolean): Boolean {
            var user = user
            val physician = user.physician

            try {
                // setting dummy for deleting user
                user.physician = Physician(Person(Contact()))

                logger.debug("Deleting user")
                // error is only fired by the delete command
                user = userRepository.save(user)
                userRepository.delete(user,
                        resourceBundle.get("llog.user.deleted", user.physician.person.getFullName()))

                if (deletePhysician) {
                    logger.debug("deleting physician")
                    physicianService.deleteOrArchive(physician)
                } else
                    logger.debug("keeping physician")

                return true
            } catch (e: Exception) {
                // reverting user disable
                user.physician = physician
                logger.debug("deleting not possible, disable")
                diableUser(user)

                // only archive
                if (deletePhysician) {
                    logger.debug("physician is not in use, archive")
                    physicianService.archive(physician, true)
                } else
                    logger.debug("physician is in use, do nothing")

                return false
            }

        }

        /**
         * Disable User via group
         *
         * @param user
         */
        @Transactional
        fun diableUser(user: HistoUser): HistoUser {
            // do not load settings is done by the updateGroupOfUser methode
            val oGroup = groupRepository.findOptionalById(HistoGroup.GROUP_DISABLED, false)

            return if (oGroup.isPresent) updateGroupOfUser(user, oGroup.get()) else user

        }

        /**
         * Archive User
         *
         * @param user
         * @param archive
         */
        @Transactional
        fun archiveUser(user: HistoUser, archive: Boolean) {
            user.isArchived = archive
            userRepository.save(user, resourceBundle.get(if (archive) "log.user.archived" else "log.user.dearchived", user))
        }

        /**
         * Updates the user group settings for a changed groups
         *
         * @param user
         * @param group
         */
        @Transactional
        fun updateGroupOfUser(group: HistoGroup) {
            // updating user settings
            val usersOfGroup = userRepository.findByGroupId(group.getId())
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
        fun updateGroupOfUser(user: HistoUser, group: HistoGroup): HistoUser {
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
        fun updateGroupOfUser(user: HistoUser, groupID: Long, save: Boolean): HistoUser {
            val group = groupRepository.findOptionalById(groupID, true)
            return updateGroupOfUser(user, group.orElse(HistoGroup(HistoSettings())), save)
        }

        /**
         * Setting the group of the user
         *
         * @param user
         * @param group
         */
        @Transactional
        fun updateGroupOfUser(user: HistoUser, group: HistoGroup, save: Boolean): HistoUser {
            user.group = group

            if (user.settings == null)
                user.settings = HistoSettings()

            GroupService.copyGroupSettings(user, group, false)

            logger.debug("Role of user " + user.username + " to " + user.group.toString())

            return if (save) userRepository.save(user, resourceBundle.get("log.user.role.changed", user.group)) else user

        }
    }
}