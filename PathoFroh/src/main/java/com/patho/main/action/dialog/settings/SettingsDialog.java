package com.patho.main.action.dialog.settings;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.*;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.log.Log;
import com.patho.main.model.log.Log_;
import com.patho.main.model.person.Organization;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.*;
import com.patho.main.service.*;
import com.patho.main.util.dialog.event.ReloadEvent;
import com.patho.main.util.dialog.event.SettingsReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configurable
@Getter
@Setter
public class SettingsDialog extends AbstractTabDialog {

	public static final int DIAGNOSIS_LIST = 0;
	public static final int DIAGNOSIS_EDIT = 1;
	public static final int DIAGNOSIS_TEXT_TEMPLATE = 2;

	private ProgramParentTab programParentTab;
	private HistoUserTab histoUserTab;
	private HistoGroupTab histoGroupTab;
	private DiagnosisTab diagnosisTab;
	private MaterialTab materialTab;
	private StainingTab stainingTab;
	private StaticListTab staticListTab;
	private FavouriteListTab favouriteListTab;
	private PersonParentTab personParentTab;
	private PhysicianSettingsTab physicianSettingsTab;
	private OrganizationTab organizationTab;
	private LogTab logTab;

	public SettingsDialog() {
		setProgramParentTab(new ProgramParentTab());
		setHistoUserTab(new HistoUserTab());
		setHistoGroupTab(new HistoGroupTab());
		setDiagnosisTab(new DiagnosisTab());
		setMaterialTab(new MaterialTab());
		setStainingTab(new StainingTab());
		setStaticListTab(new StaticListTab());
		setFavouriteListTab(new FavouriteListTab());
		setPersonParentTab(new PersonParentTab());
		setPhysicianSettingsTab(new PhysicianSettingsTab());
		setOrganizationTab(new OrganizationTab());
		setLogTab(new LogTab());
		setTabs(programParentTab, histoUserTab, histoGroupTab, diagnosisTab, materialTab, stainingTab, staticListTab,
				favouriteListTab, personParentTab, physicianSettingsTab, organizationTab, logTab);
	}

	public SettingsDialog initAndPrepareBean() {
		return initAndPrepareBean("");
	}

	public SettingsDialog initAndPrepareBean(String tabName) {
		if (initBean(tabName))
			prepareDialog();

		return this;
	}

	public boolean initBean(String tabName) {
		return super.initBean(null, Dialog.SETTINGS, tabName);
	}

	public void hideDialog() {
		hideDialog(new SettingsReloadEvent());
	}

	public class ProgramParentTab extends AbstractTab {
		public ProgramParentTab() {
			setTabName("ProgrammParentTab");
			setName("dialog.settings.programmParent");
			setViewID("programmParentTab");
			setDisabled(true);
		}

	}

	@Getter
	@Setter
	@Configurable
	public class HistoUserTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private UserService userService;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private UserRepository userRepository;

		private List<HistoUser> users;

		private boolean showArchived;

		public HistoUserTab() {
			setTabName("HistoUserTab");
			setName("dialog.settings.user");
			setViewID("histoUser");
			setCenterInclude("include/userList.xhtml");
			setParentTab(programParentTab);
		}

		public boolean initTab() {
			setShowArchived(false);
			return true;
		}

		public void updateData() {
			setUsers(userRepository.findAllIgnoreArchived(!showArchived));
		}
	}

	@Getter
	@Setter
	@Configurable
	public class HistoGroupTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private GroupRepository groupRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private GroupService groupService;

		private List<HistoGroup> groups;

		private boolean showArchived;

		public HistoGroupTab() {
			setTabName("HistoGroupTab");
			setName("dialog.settings.group");
			setViewID("histoGroupTab");
			setCenterInclude("include/groupList.xhtml");
			setParentTab(programParentTab);
		}

		public boolean initTab() {
			setShowArchived(false);
			return true;
		}

		public void updateData() {
			setGroups(groupRepository.findAllOrderByIdAsc(!showArchived));
		}

		public void archiveOrDelete(HistoGroup g, boolean archive) {
			if (archive) {
				if (groupService.deleteOrArchive(g)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.group.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.group.archive");
			} else {
				groupService.archive(g, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.group.dearchive");
			}
			updateData();
		}
	}

	@Getter
	@Setter
	@Configurable
	public class DiagnosisTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private DiagnosisPresetService diagnosisPresetService;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private DiagnosisPresetRepository diagnosisPresetRepository;

		private List<DiagnosisPreset> diagnosisPresets;

		/**
		 * If true archived object will be shown.
		 */
		private boolean showArchived;

		public DiagnosisTab() {
			setTabName("DiagnosisTab");
			setName("dialog.settings.diagnosis");
			setViewID("diagnoses");
			setCenterInclude("include/diagnosisList.xhtml");
			setShowArchived(false);
		}

		@Override
		public void updateData() {
			setDiagnosisPresets(
					diagnosisPresetRepository.findAllIgnoreArchivedByOrderByIndexInListAsc(!isShowArchived()));
		}

		/**
		 * Is fired if the list is reordered by the user via drag and drop
		 *
		 * @param event
		 */
		public void onReorderList(ReorderEvent event) {
			logger.debug(
					"List order changed, moved material from " + event.getFromIndex() + " to " + event.getToIndex());
			diagnosisPresetService.reoderList(getDiagnosisPresets());
			updateData();
		}

		/**
		 * 
		 * @param d
		 * @param archive
		 */
		public void archiveOrDelete(DiagnosisPreset d, boolean archive) {
			if (archive) {
				if (diagnosisPresetService.deleteOrArchive(d)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.archive");
			} else {
				diagnosisPresetService.archive(d, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.dearchive");
			}
			updateData();
		}
	}

	@Getter
	@Setter
	@Configurable
	public class MaterialTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private MaterialPresetRepository materialPresetRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private MaterialPresetService materialPresetService;

		private List<MaterialPreset> materials;

		/**
		 * If true archived object will be shown.
		 */
		private boolean showArchived;

		public MaterialTab() {
			setTabName("MaterialTab");
			setName("dialog.settings.materials");
			setCenterInclude("include/materialList.xhtml");
			setViewID("material");
			setShowArchived(false);
		}

		@Override
		public void updateData() {
			setMaterials(
					materialPresetRepository.findAllIgnoreArchivedOrderByPriorityCountDesc(false, !isShowArchived()));
		}

		public void archiveOrDelete(MaterialPreset m, boolean archive) {
			if (archive) {
				if (materialPresetService.deleteOrArchive(m)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.material.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.material.archive");
			} else {
				materialPresetService.archive(m, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.material.dearchive");
			}
			updateData();
		}
	}

	@Getter
	@Setter
	@Configurable
	public class StainingTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private StainingPrototypeRepository stainingPrototypeRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private StainingPrototypeService stainingPrototypeService;

		/**
		 * Tab container
		 */
		private List<StainingContainer> container = new ArrayList<StainingContainer>();

		/**
		 * If true archived object will be shown.
		 */
		private boolean showArchived;

		public StainingTab() {
			setTabName("StainingTab");
			setName("dialog.settings.stainings");
			setViewID("staining");
			setCenterInclude("include/stainingsList.xhtml");
			setShowArchived(false);
		}

		@Override
		public void updateData() {
			getContainer().clear();
			// adding tabs dynamically
			for (StainingType type : StainingType.values()) {
				getContainer().add(new StainingContainer(type, stainingPrototypeRepository
						.findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(type, false, !isShowArchived())));
			}
		}

		public void archiveOrDelete(StainingPrototype p, boolean archive) {
			if (archive) {
				if (stainingPrototypeService.deleteOrArchive(p)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.staining.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.staining.archive");
			} else {
				stainingPrototypeService.archive(p, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.staining.dearchive");
			}
			updateData();
		}

		@Getter
		@Setter
		public class StainingContainer {
			private StainingType type;
			private List<StainingPrototype> prototpyes;

			public StainingContainer(StainingType type, List<StainingPrototype> prototypes) {
				this.type = type;
				this.prototpyes = prototypes;
			}
		}
	}

	@Getter
	@Setter
	@Configurable
	public class StaticListTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private ListItemRepository listItemRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private ListItemService listItemService;

		/**
		 * Current static list to edit
		 */
		private ListItem.StaticList selectedStaticList = ListItem.StaticList.WARDS;

		/**
		 * Content of the current static list
		 */
		private List<ListItem> staticListContent;

		/**
		 * If true archived object will be shown.
		 */
		private boolean showArchived;

		public StaticListTab() {
			setTabName("StaticListTab");
			setName("dialog.settings.staticLists");
			setViewID("staticLists");
			setCenterInclude("include/staticLists.xhtml");
			setShowArchived(false);
		}

		@Override
		public void updateData() {
			setStaticListContent(listItemRepository.findAllOrderByIndex(getSelectedStaticList(), !isShowArchived()));
		}

		/**
		 * Archvies or dearchvies ListItem depending on the given parameters.
		 *
		 * @param archive
		 */
		public void archiveOrDelete(ListItem item, boolean archive) {
			if (archive) {
				if (listItemService.deleteOrArchive(item)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.listitem.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.listitem.archive");
			} else {
				listItemService.archive(item, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.listitem.dearchive");
			}
			updateData();
		}

		public void onReorderList(ReorderEvent event) {
			listItemService.updateReoderedList(getStaticListContent(), getSelectedStaticList());
			updateData();
		}
	}

	@Getter
	@Setter
	public class PersonParentTab extends AbstractTab {
		public PersonParentTab() {
			setTabName("PersonParentTab");
			setName("dialog.settings.personParent");
			setViewID("personParentTab");
			setCenterInclude("include/physicianList.xhtml");
			setDisabled(true);
		}
	}

	@Getter
	@Setter
	@Configurable
	public class PhysicianSettingsTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private PhysicianRepository physicianRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private PhysicianService physicianService;

		/**
		 * True if archived physicians should be display
		 */
		private boolean showArchived;

		/**
		 * List containing all physicians known in the histo database
		 */
		private List<Physician> physicianList;

		/**
		 * All roles to display
		 */
		private List<ContactRole> allRoles;

		/**
		 * Array of roles for that physicians should be shown.
		 */
		private ContactRole[] showPhysicianRoles;

		public PhysicianSettingsTab() {
			setTabName("PhysicianSettingsTab");
			setName("dialog.settings.persons");
			setViewID("persons");
			setCenterInclude("include/physicianList.xhtml");
			setParentTab(personParentTab);

			setShowArchived(false);
			setAllRoles(Arrays.asList(ContactRole.values()));

			setShowPhysicianRoles(ContactRole.values());
		}

		@Override
		public boolean initTab() {
			setShowArchived(false);
			return true;
		}

		@Override
		public void updateData() {
			setPhysicianList(physicianRepository.findAllByRole(getShowPhysicianRoles(), !showArchived));
		}

		/**
		 * Return handles a version error and a selected physician
		 * 
		 * @param event
		 */
		public void onDefaultDialogReturn(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
				updateData();
			}
		}

		/**
		 * Archvies or dearchvies physicians depending on the given parameters.
		 *
		 * @param archive
		 */
		public void archiveOrDelete(Physician p, boolean archive) {
			if (archive) {
				if (physicianService.deleteOrArchive(p)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.person.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.person.archive");
			} else {
				physicianService.archive(p, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.person.dearchive");
			}
			updateData();
		}
	}

	@Getter
	@Setter
	@Configurable
	public class OrganizationTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private OrganizationRepository organizationRepository;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private OrganizationService organizationService;

		private List<Organization> organizations;

		private boolean showArchived;

		public OrganizationTab() {
			setTabName("OrganizationTab");
			setName("dialog.settings.organization");
			setViewID("organizations");
			setCenterInclude("include/organizationLists.xhtml");
			setParentTab(personParentTab);
		}

		@Override
		public boolean initTab() {
			setShowArchived(false);
			return true;
		}

		@Override
		public void updateData() {
			setOrganizations(organizationRepository.findAllOrderByIdAsc(!showArchived));
		}

		/**
		 * Archvies or dearchvies organization depending on the given parameters.
		 *
		 * @param archive
		 */
		public void archiveOrDelete(Organization organization, boolean archive) {
			if (archive) {
				if (organizationService.deleteOrArchive(organization)) {
					MessageHandler.sendGrowlMessagesAsResource("growl.organization.deleted");
				} else
					MessageHandler.sendGrowlMessagesAsResource("growl.organization.archive");
			} else {
				organizationService.archive(organization, false);
				MessageHandler.sendGrowlMessagesAsResource("growl.organization .dearchive");
			}
			updateData();
		}
	}

	@Getter
	@Setter
	@Configurable
	public class FavouriteListTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private FavouriteListRepository favouriteListRepository;

		/**
		 * Array containing all favourite listis
		 */
		private List<FavouriteList> favouriteLists;

		public FavouriteListTab() {
			setTabName("FavouriteListTab");
			setName("dialog.settings.favouriteList");
			setViewID("favouriteLists");
			setCenterInclude("include/favouriteList.xhtml");
		}

		@Override
		public void updateData() {
			setFavouriteLists(favouriteListRepository.findAll(false, true, true, true));
		}

	}

	@Getter
	@Setter
	@Configurable
	public class LogTab extends AbstractTab {

		@Autowired
		private LogRepository logRepository;

		private int logsPerPage;

		private int page;

		private int pagesCount;

		private List<Log> logs;

		public LogTab() {
			setTabName("LogTab");
			setName("dialog.settings.log");
			setViewID("logs");
			setCenterInclude("include/log.xhtml");

			setLogsPerPage(500);
			setPage(1);
		}

		@Override
		public void updateData() {

			// updating page count
			long maxLogPages = logRepository.count();

			pagesCount = (int) Math.ceil((double) maxLogPages / logsPerPage);

			if (page > pagesCount) {
				page = pagesCount;
			}

			setLogs(logRepository
					.findAll(PageRequest.of(page - 1, getLogsPerPage(), Sort.by(Log_.id.getName()).descending()))
					.getContent());
		}

	}

	public class AdminTab extends AbstractTab {

		public AdminTab() {
			setTabName("AdminTab");
			setName("dialog.settings.admin");
			setViewID("admin");
		}

		@Override
		public void updateData() {
		}

	}
}
