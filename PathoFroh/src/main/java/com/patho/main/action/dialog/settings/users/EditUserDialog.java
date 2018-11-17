package com.patho.main.action.dialog.settings.users;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.AbstractTabDialog.AbstractTab;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.UserService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class EditUserDialog extends AbstractTabDialog implements OrganizationFunctions {

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

	private UserTab userTab;
	private SettingsTab settingsTab;
	private PersonTab personTab;

	private HistoUser user;

	private List<ContactRole> allRoles;


	private DefaultTransformer<Organization> organizationTransformer;

	private boolean roleChange;

	/**
	 * True if userdata where changed, an the dialog needs to be saved.
	 */
	private boolean saveAble;

	public EditUserDialog() {
		setUserTab(new UserTab());
		setSettingsTab(new SettingsTab());
		setPersonTab(new PersonTab());

		setTabs(getUserTab(), getSettingsTab(), getPersonTab());
	}

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

	

		// setting organization transformer for selecting default address
		setOrganizationTransformer(
				new DefaultTransformer<Organization>(user.getPhysician().getPerson().getOrganizsations()));

		super.initBean(task, Dialog.SETTINGS_USERS_EDIT);
		return true;
	}

	@Getter
	@Setter
	@Configurable
	public class UserTab extends AbstractTab {
		
		private List<HistoGroup> groups;

		private DefaultTransformer<HistoGroup> groupTransformer;
		
		private boolean roleChanged;
		
		public UserTab() {
			setTabName("UserTab");
			setName("dialog.userEdit.tab.userTab");
			setViewID("userTab");
			setCenterInclude("include/user.xhtml");
		}
		
		@Override
		public boolean initTab() {
			setGroups(groupRepository.findAll(true));
			setGroupTransformer(new DefaultTransformer<HistoGroup>(getGroups()));
			setRoleChange(false);
			return true;
		}
		
		public void roleChange() {
			setRoleChange(true);
		}
	
	}

	@Getter
	@Setter
	@Configurable
	public class SettingsTab extends AbstractTab {
		public SettingsTab() {
			setTabName("SettingsTab");
			setName("dialog.userEdit.tab.settingsTab");
			setViewID("settingsTab");
			setCenterInclude("include/settings.xhtml");
		}
	}

	@Getter
	@Setter
	@Configurable
	public class PersonTab extends AbstractTab {

		private List<ContactRole> allRoles;

		/**
		 * Array of roles which the physician should be associated
		 */
		private ContactRole[] associatedRoles;

		private DefaultTransformer<Organization> contactOrganizations;

		public PersonTab() {
			setTabName("PersonTab");
			setName("dialog.userEdit.tab.personTab");
			setViewID("personTab");
			setCenterInclude("include/person.xhtml");
		}

		@Override
		public boolean initTab() {
			setAllRoles(Arrays.asList(ContactRole.values()));
			setAssociatedRoles(getUser().getPhysician().getAssociatedRolesAsArray());
			return true;
		}
		
		@Override
		public void updateData() {
			// setting organization for selecting an default notification
			setContactOrganizations(
					new DefaultTransformer<Organization>(getUser().getPhysician().getPerson().getOrganizsations()));
			super.updateData();
		}

		/**
		 * Removes an organization from the user and updates the default address
		 * selection
		 * 
		 * @param organization
		 */
		public void removeFromOrganization(Organization organization) {
//			getPhysician().getPerson().getOrganizsations().remove(organization);
//			update();
		}

		public void onDefaultDialogReturn(SelectEvent event) {
		}

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
