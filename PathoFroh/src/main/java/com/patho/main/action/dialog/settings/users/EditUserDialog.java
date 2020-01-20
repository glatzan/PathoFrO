package com.patho.main.action.dialog.settings.users;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.Physician;
import com.patho.main.model.person.Organization;
import com.patho.main.model.person.Person;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.PrintService;
import com.patho.main.service.UserService;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialog.event.HistoUserDeleteEvent;
import com.patho.main.util.dialog.event.PhysicianSelectEvent;
import com.patho.main.util.dialog.event.UserReloadEvent;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.search.settings.SimpleListSearchOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class EditUserDialog extends AbstractTabDialog {


	private UserTab userTab;
	private SettingsTab settingsTab;
	private PersonTab personTab;

	private HistoUser user;

	private boolean newUser;

	private boolean selectPerson;

	private boolean refreshAble;

	/**
	 * True if userdata where changed, an the dialog needs to be saved.
	 */

	public EditUserDialog() {
		setUserTab(new UserTab());
		setSettingsTab(new SettingsTab());
		setPersonTab(new PersonTab());

		setTabs(getUserTab(), getSettingsTab(), getPersonTab());
	}

	public EditUserDialog initAndPrepareBean() {
		if (initBean(new HistoUser(new Physician(), new HistoSettings())))
			prepareDialog();

		return this;
	}

	public EditUserDialog initAndPrepareBean(HistoUser user) {
		if (initBean(user))
			prepareDialog();

		return this;
	}

	public boolean initBean(HistoUser user) {
		// existing histo user
		if (user.getId() != 0) {
			Optional<HistoUser> oUser = SpringContextBridge.services().getUserRepository().findById(user.getId());

			if (!oUser.isPresent())
				return false;

			setUser(oUser.get());
			setNewUser(false);
			setSelectPerson(false);
			getSettingsTab().setDisabled(false);
			getPersonTab().setDisabled(false);

			// only refresh if !not local user or uid is not empty
			if (getUser().getLocalUser() || HistoUtil.isNullOrEmpty(getUser().getUsername())) {
				setRefreshAble(false);
			} else {
				setRefreshAble(true);
			}

		} else {
			logger.debug("New User");
			setNewUser(true);
			setSelectPerson(true);
			setUser(user);
			MessageHandler.executeScript("clickButtonFromBean('dialogContent:selectPhyscianBtn')");
			getSettingsTab().setDisabled(true);
			getPersonTab().setDisabled(true);
		}

		return super.initBean(Dialog.SETTINGS_USERS_EDIT);
	}

	@Getter
	@Setter
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
			setGroups(SpringContextBridge.services().getGroupRepository().findAll(true, false));
			setGroupTransformer(new DefaultTransformer<HistoGroup>(getGroups()));
			return true;
		}

		@Override
		public String getCenterInclude() {
			return isSelectPerson() ? "include/selectPerson.xhtml" : centerInclude;
		}

		public void roleChange() {
			SpringContextBridge.services().getUserService().updateGroupOfUser(getUser(), getUser().getGroup(), false);
		}

	}

	@Getter
	@Setter
	public class SettingsTab extends AbstractTab {

		private View[] allViews;
		private SimpleListSearchOption[] allWorklistOptions;

		private ClinicPrinter clinicPrinter;
		private LabelPrinter labelPrinter;

		public SettingsTab() {
			setTabName("SettingsTab");
			setName("dialog.userEdit.tab.settingsTab");
			setViewID("settingsTab");
			setCenterInclude("include/settings.xhtml");
		}

		public boolean initTab() {
			if (!selectPerson) {
				setAllViews(new View[] { View.GUEST, View.WORKLIST_TASKS, View.WORKLIST_PATIENT,
						View.WORKLIST_DIAGNOSIS, View.WORKLIST_RECEIPTLOG, View.WORKLIST_REPORT });

				setAllWorklistOptions(
						new SimpleListSearchOption[] { SimpleListSearchOption.DIAGNOSIS_LIST, SimpleListSearchOption.STAINING_LIST,
								SimpleListSearchOption.NOTIFICATION_LIST, SimpleListSearchOption.EMPTY_LIST });

				setLabelPrinter(SpringContextBridge.services().getPrintService().getCurrentLabelPrinter(getUser()));
				setClinicPrinter(SpringContextBridge.services().getPrintService().getCurrentPrinter(getUser()));
			}
			return true;
		}

		public void voidAction() {
		}

		@Override
		public String getCenterInclude() {
			return isSelectPerson() ? "include/empty.xhtml" : centerInclude;
		}
	}

	@Getter
	@Setter
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
			if (!selectPerson) {
				setAllRoles(Arrays.asList(ContactRole.values()));
			}
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

		@Override
		public String getCenterInclude() {
			return isSelectPerson() ? "include/empty.xhtml" : centerInclude;
		}
	}

	/**
	 * Saves user data
	 */
	public void saveUser() {
		user.getSettings().setPrinter(settingsTab.getClinicPrinter());
		user.getSettings().setLabelPrinter(settingsTab.getLabelPrinter());
		SpringContextBridge.services().getUserService().saveUser(user);
	}

	public void saveAndHide() {
		logger.debug("Saving user Settings");
		saveUser();
		super.hideDialog(new UserReloadEvent(getUser()));
	}

	/**
	 * Updates the data of the physician with data from the clinic backend
	 */
	public void updateDataFromLdap() {
		try {
			HistoUser user = SpringContextBridge.services().getUserRepository().findById(getUser().getId()).get();
			user.getPhysician().getPerson().setAutoUpdate(true);
			setUser(SpringContextBridge.services().getUserService().updateUserWithLdapData(user));
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
		if (event.getObject() instanceof HistoUserDeleteEvent
				&& ((HistoUserDeleteEvent) event.getObject()).isDelete()) {
			if (SpringContextBridge.services().getUserService().deleteOrDisable(getUser(),
					((HistoUserDeleteEvent) event.getObject()).isDeletePhysician()))
				MessageHandler.sendGrowlMessagesAsResource("growl.user.deleted");
			else
				MessageHandler.sendGrowlMessagesAsResource("growl.user.archive");

			hideDialog(new UserReloadEvent());
		}
	}

	public void onSelectPerson(SelectEvent event) {
		if (event.getObject() instanceof PhysicianSelectEvent) {
			PhysicianSelectEvent returnEv = ((PhysicianSelectEvent) event.getObject());

			if (returnEv.isExtern()) {
				setUser(new HistoUser(returnEv.getObj(), new HistoSettings()));
				getUser().setLocalUser(true);
				SpringContextBridge.services().getUserService().updateGroupOfUser(getUser(), HistoGroup.GROUP_GUEST_ID, false);
			} else {
				Optional<HistoUser> oPhysician = SpringContextBridge.services().getUserRepository()
						.findOptionalByPhysicianUid(returnEv.getObj().getUid());

				if (oPhysician.isPresent()) {
					setUser(oPhysician.get());
					setNewUser(false);
					MessageHandler.sendGrowlMessages("User exsis!", "");
				} else {
					setUser(new HistoUser(returnEv.getObj(), new HistoSettings()));
					SpringContextBridge.services().getUserService().updateGroupOfUser(getUser(), HistoGroup.GROUP_GUEST_ID, false);
				}
			}

			getSettingsTab().setDisabled(false);
			getPersonTab().setDisabled(false);
			setSelectPerson(false);

			super.initBean();
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
