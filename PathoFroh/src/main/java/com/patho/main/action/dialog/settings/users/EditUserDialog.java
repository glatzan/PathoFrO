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
import com.patho.main.action.dialog.settings.users.ConfirmUserDeleteDialog.ConfirmUserDeleteEvent;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.PrintService;
import com.patho.main.service.UserService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class EditUserDialog extends AbstractTabDialog {

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

	private boolean roleChange;

	/**
	 * True if userdata where changed, an the dialog needs to be saved.
	 */

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

		@Autowired
		private PrintService printService;

		private View[] allViews;
		private SimpleSearchOption[] allWorklistOptions;

		private ClinicPrinter clinicPrinter;
		private LabelPrinter labelPrinter;

		public SettingsTab() {
			setTabName("SettingsTab");
			setName("dialog.userEdit.tab.settingsTab");
			setViewID("settingsTab");
			setCenterInclude("include/settings.xhtml");
		}

		public boolean initTab() {
			setAllViews(new View[] { View.GUEST, View.WORKLIST_TASKS, View.WORKLIST_PATIENT, View.WORKLIST_DIAGNOSIS,
					View.WORKLIST_RECEIPTLOG, View.WORKLIST_REPORT });

			setAllWorklistOptions(
					new SimpleSearchOption[] { SimpleSearchOption.DIAGNOSIS_LIST, SimpleSearchOption.STAINING_LIST,
							SimpleSearchOption.NOTIFICATION_LIST, SimpleSearchOption.EMPTY_LIST });

			setLabelPrinter(printService.getCurrentLabelPrinter(getUser()));
			setClinicPrinter(printService.getCurrentPrinter(getUser()));

			return true;
		}

		public void voidAction() {
		}
	}

	@Getter
	@Setter
	@Configurable
	public class PersonTab extends AbstractTab implements OrganizationFunctions {

		private List<ContactRole> allRoles;

		private DefaultTransformer<Organization> organizationTransformer;

		public PersonTab() {
			setTabName("PersonTab");
			setName("dialog.userEdit.tab.personTab");
			setViewID("personTab");
			setCenterInclude("include/person.xhtml");
		}

		@Override
		public boolean initTab() {
			setAllRoles(Arrays.asList(ContactRole.values()));
			return true;
		}

		@Override
		public void updateData() {
			// setting organization for selecting an default notification
			updateOrganizationSelection();
			super.updateData();
		}

		@Override
		public Person getPerson() {
			return getUser().getPhysician().getPerson();
		}

		@Override
		public DefaultTransformer<Organization> getOrganizationTransformer() {
			return organizationTransformer;
		}

		@Override
		public void setOrganizationTransformer(DefaultTransformer<Organization> transformer) {
			organizationTransformer = transformer;
		}

	}

	/**
	 * Saves user data
	 */
	public void saveUser() {

		user.getSettings().setPrinter(settingsTab.getClinicPrinter());
		user.getSettings().setLabelPrinter(settingsTab.getLabelPrinter());

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
		try {
			HistoUser user = userRepository.findById(getUser().getId()).get();
			user.getPhysician().getPerson().setAutoUpdate(true);
			setUser(userService.updateUserWithLdapData(user));
			getSelectedTab().updateData();
			MessageHandler.sendGrowlMessagesAsResource("growl.patient.updateLadp.success",
					"growl.patient.updateLadp.success.info");
		} catch (IllegalStateException e) {
			getSelectedTab().updateData();
			MessageHandler.sendGrowlMessagesAsResource("growl.patient.updateLadp.failed",
					"growl.patient.updateLadp.failed.info");
		}
	}

	/**
	 * Is called on return of the delete dialog. If true is passed into this method
	 * the edit user dialog will be closed.
	 * 
	 * @param event
	 */
	public void onDeleteDialogReturn(SelectEvent event) {
		if (event.getObject() instanceof ConfirmUserDeleteEvent
				&& ((ConfirmUserDeleteEvent) event.getObject()).isDelete()) {
			if (userService.deleteOrDisable(getUser()))
				MessageHandler.sendGrowlMessagesAsResource("growl.user.deleted");
			else
				MessageHandler.sendGrowlMessagesAsResource("growl.user.archive");

			hideDialog();
		}
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
