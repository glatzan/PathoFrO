package com.patho.main.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Contact;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.LDAPRepository;
import com.patho.main.repository.UserRepository;

@Service
@Transactional
public class UserService extends AbstractService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PhysicianService physicianService;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private LDAPRepository ldapRepository;

	/**
	 * Creats a new user, loads data from ldap and creates the user with the guest
	 * group
	 * 
	 * @param uid
	 * @return
	 */
	public Optional<HistoUser> createUser(String uid) {
		Optional<Physician> oPhyisican = ldapRepository.findByUid(uid);
		Optional<HistoGroup> group = groupRepository.findOptionalById(HistoGroup.GROUP_GUEST_ID);

		if (oPhyisican.isPresent() && group.isPresent()) {
			HistoUser user = addOrMergeUser(oPhyisican.get());

			if (user != null) {
				user = updateGroupOfUser(user, group.get());
				return Optional.ofNullable(user);
			}
		}

		return Optional.empty();
	}

	/**
	 * Adds a histouser and updates the passed physician.
	 * 
	 * @param physician
	 * @return
	 */
	public HistoUser addOrMergeUser(Physician physician) {
		return addOrMergeUser(physician, true);
	}

	/**
	 * Creates an new user, if a physician with the given username exist they will
	 * be merged.
	 * 
	 * @param physician
	 * @return
	 */
	public HistoUser addOrMergeUser(Physician physician, boolean checkPhysican) {

		if (physician == null) {
			return null;
		}

		// checking if histouser exsists
		Optional<HistoUser> histoUser = userRepository.findOptionalByPhysicianUid(physician.getUid());

		if (!histoUser.isPresent()) {
			logger.info("No User found, creating new HistoUser " + physician.getPerson().getFullName());

			if (checkPhysican)
				physician = physicianService.addOrMergePhysician(physician);

			HistoUser newHistoUser = new HistoUser(physician);

			if (physician.getAssociatedRoles().size() == 0)
				physician.addAssociateRole(ContactRole.NONE);

			// saving or updating physician, also updating organizations
			physician = physicianService.addOrMergePhysician(physician);

			newHistoUser.setPhysician(physician);

			return userRepository.save(newHistoUser, resourceBundle.get("log.user.created", histoUser));
		} else {
			histoUser.get().setPhysician(physicianService.mergePhysicians(physician, histoUser.get().getPhysician()));
			return userRepository.save(histoUser.get(), resourceBundle.get("log.user.update", histoUser));
		}
	}

	/**
	 * Updates the users data with data from ldap
	 * 
	 * @param user
	 */
	public HistoUser updateUserWithLdapData(HistoUser user) {
		user.setPhysician(physicianService.updatePhysicianWithLdapData(user.getPhysician()));
		return user;
	}

	/**
	 * Save user
	 * 
	 * @param user
	 */
	public void saveUser(HistoUser user) {
		if (user.getPhysician().hasNoAssociateRole())
			user.getPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

		user.setPhysician(physicianService.addOrMergePhysician(user.getPhysician()));
		userRepository.save(user, resourceBundle.get("log.user.update", user));
	}

	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrDisable(HistoUser user, boolean deletePhysician) {
		Physician physician = user.getPhysician();

		try {
			// setting dummy for deleting user
			user.setPhysician(new Physician(new Person(new Contact())));

			logger.debug("Deleting user");
			// error is only fired by the delete command
			user = userRepository.save(user);
			userRepository.delete(user,
					resourceBundle.get("llog.user.deleted", user.getPhysician().getPerson().getFullName()));

			if (deletePhysician) {
				logger.debug("deleting physician");
				physicianService.deleteOrArchive(physician);
			} else
				logger.debug("keeping physician");

			return true;
		} catch (Exception e) {
			// reverting user disable
			user.setPhysician(physician);
			logger.debug("deleting not possible, disable");
			diableUser(user);

			// only archive
			if (deletePhysician) {
				logger.debug("physician is not in use, archive");
				physicianService.archive(physician, true);
			} else
				logger.debug("physician is in use, do nothing");

			return false;
		}
	}

	/**
	 * Disable User via group
	 * 
	 * @param user
	 */
	public HistoUser diableUser(HistoUser user) {
		// do not load settings is done by the updateGroupOfUser methode
		Optional<HistoGroup> oGroup = groupRepository.findOptionalById(HistoGroup.GROUP_DISABLED, false);

		if (oGroup.isPresent())
			return updateGroupOfUser(user, oGroup.get());
		return user;

	}

	/**
	 * Archive User
	 * 
	 * @param user
	 * @param archive
	 */
	public void archiveUser(HistoUser user, boolean archive) {
		user.setArchived(archive);
		userRepository.save(user, resourceBundle.get(archive ? "log.user.archived" : "log.user.dearchived", user));
	}

	/**
	 * Updates the user group settings for a changed groups
	 * 
	 * @param user
	 * @param group
	 */
	public void updateGroupOfUser(HistoGroup group) {
		// updating user settings
		List<HistoUser> usersOfGroup = userRepository.findByGroupId(group.getId());
		usersOfGroup.forEach(p -> updateGroupOfUser(p, group));
	}

	/**
	 * Updates the group of an user
	 * 
	 * @param user
	 * @param group
	 * @return
	 */
	public HistoUser updateGroupOfUser(HistoUser user, HistoGroup group) {
		return updateGroupOfUser(user, group, true);
	}

	/**
	 * Updates the group of an user
	 * 
	 * @param user
	 * @param group
	 * @return
	 */
	public HistoUser updateGroupOfUser(HistoUser user, long groupID, boolean save) {
		Optional<HistoGroup> group = groupRepository.findOptionalById(groupID, true);
		return updateGroupOfUser(user, group.orElse(new HistoGroup(new HistoSettings())), save);
	}

	/**
	 * Setting the group of the user
	 * 
	 * @param user
	 * @param group
	 */
	public HistoUser updateGroupOfUser(HistoUser user, HistoGroup group, boolean save) {
		user.setGroup(group);

		if (user.getSettings() == null)
			user.setSettings(new HistoSettings());

		GroupService.copyGroupSettings(user, group, false);

		logger.debug("Role of user " + user.getUsername() + " to " + user.getGroup().toString());

		if (save)
			return userRepository.save(user, resourceBundle.get("log.user.role.changed", user.getGroup()));

		return user;
	}

}