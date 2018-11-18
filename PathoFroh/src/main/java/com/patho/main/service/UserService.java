package com.patho.main.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Physician;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.UserRepository;

@Service
@Transactional
public class UserService extends AbstractService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PhysicianRepository physicianRepository;

	@Autowired
	private PhysicianService physicianService;

	@Autowired
	private GroupRepository groupRepository;

	/**
	 * Creates an new user, if a physician with the given username exist they will
	 * be merged.
	 * 
	 * @param physician
	 * @return
	 */
	public boolean addOrMergeUser(Physician physician) {

		if (physician == null) {
			return false;
		}

		// removing id from the list
		physician.setId(0);

		// checking if histouser exsists
		Optional<HistoUser> histoUser = userRepository.findOptionalByUsername(physician.getUid());

		if (!histoUser.isPresent()) {
			logger.info("No User found, creating new HistoUser " + physician.getPerson().getFullName());

			HistoUser newHistoUser = new HistoUser(physician.getUid());

			if (physician.getAssociatedRoles().size() == 0)
				physician.addAssociateRole(ContactRole.NONE);

			// saving or updating physician, also updating organizations
			physician = physicianService.addOrMergePhysician(physician);

			newHistoUser.setPhysician(physician);

			userRepository.save(newHistoUser, resourceBundle.get("log.user.created", histoUser));
		} else {
			physicianService.mergePhysicians(physician, histoUser.get().getPhysician());
			userRepository.save(histoUser.get(), resourceBundle.get("log.user.update", histoUser));
		}

		return true;
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

		userRepository.save(user, resourceBundle.get("log.user.update", user));
	}

	/**
	 * Disable User via group
	 * 
	 * @param user
	 */
	public void diableUser(HistoUser user) {
		// do not load settings is done by the updateGroupOfUser methode
		Optional<HistoGroup> oGroup = groupRepository.findOptionalById(HistoGroup.GROUP_DISABLED, false);

		if (oGroup.isPresent())
			updateGroupOfUser(user, oGroup.get());

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
	 * Setting the group of the user
	 * 
	 * @param user
	 * @param group
	 */
	public void updateGroupOfUser(HistoUser user, HistoGroup group) {
		user.setGroup(group);

		if (user.getSettings() == null)
			user.setSettings(new HistoSettings());

		GroupService.copyGroupSettings(user, group, false);

		logger.debug("Role of user " + user.getUsername() + " to " + user.getGroup().toString());
		userRepository.save(user, resourceBundle.get("log.user.role.changed", user.getGroup()));
	}

}