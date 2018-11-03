package com.patho.main.action.dialog.settings.users;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.dao.LogDAO;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.UserService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseConstraintViolationException;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class EditUserDialog extends AbstractDialog<EditUserDialog> implements OrganizationFunctions {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianService physicianService;

	private HistoUser user;

	private List<ContactRole> allRoles;

	private List<HistoGroup> groups;

	private DefaultTransformer<HistoGroup> groupTransformer;

	private DefaultTransformer<Organization> organizationTransformer;

	private boolean roleChange;

	/**
	 * True if userdata where changed, an the dialog needs to be saved.
	 */
	private boolean saveAble;

	public EditUserDialog initAndPrepareBean(HistoUser user) {
		if (initBean(user))
			prepareDialog();

		return this;
	}

	public boolean initBean(HistoUser user) {
		Optional<HistoUser> oUser = userRepository.findById(user.getId());

		if (!oUser.isPresent())
			return false;

		setUser(oUser.get());

		setAllRoles(Arrays.asList(ContactRole.values()));

		setSaveAble(false);

		setGroups(groupRepository.findAll(true));

		setGroupTransformer(new DefaultTransformer<HistoGroup>(getGroups()));

		// setting organization transformer for selecting default address
		setOrganizationTransformer(
				new DefaultTransformer<Organization>(user.getPhysician().getPerson().getOrganizsations()));

		super.initBean(task, Dialog.SETTINGS_USERS_EDIT);
		return true;
	}

	/**
	 * Saves user data
	 */
	public void saveUser() {
		// changes role settings and saves the user
		if (roleChange)
			userService.updateGroupOfUser(user, user.getGroup());
		else
			userService.saveUser(user);
	}

	/**
	 * Updates the data of the physician with data from the clinic backend
	 */
	public void updateDataFromLdap() {
		userService.updateUserWithLdapData(user);
	}

	/**
	 * Is called on return of the delete dialog. If true is passed into this method
	 * the edit user dialog will be closed.
	 * 
	 * @param event
	 */
	public void onDeleteDialogReturn(SelectEvent event) {
		if (event.getObject() instanceof Boolean && ((Boolean) event.getObject()).booleanValue()) {
			hideDialog();
		}
	}

	/**
	 * Opens a delete dialog for deleting the user.
	 */
	public void prepareDeleteUser() {
		prepareDialog(Dialog.SETTINGS_USERS_DELETE);
	}

	/**
	 * Tries to delete the user, if not deleteable it will show a dialog for
	 * disabling the user.
	 */
	public void deleteUser() {
		userDao.remove(user);
		hideDialog(true);

		// } catch (HistoDatabaseConstraintViolationException e) {
		logger.debug("Delete not possible, change group dialog");
		prepareDialog(Dialog.SETTINGS_USERS_DELETE_DISABLE);
	}

	/**
	 * Disables the user via group
	 */
	public void disableUser() {
		userService.diableUser(user);
	}

	/**
	 * Returning person of the user
	 * 
	 * @return
	 */
	public Person getPerson() {
		return getUser().getPhysician().getPerson();
	}
}
