package com.patho.main.action.dialog.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog.StainingPrototypeHolder;
import com.patho.main.action.dialog.slides.AddSlidesDialog.StainingTypeContainer;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Organization;
import com.patho.main.model.Physician;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.model.log.Log;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.DiagnosisPresetRepository;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.StainingPrototypeRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.ListItemService;
import com.patho.main.service.MaterialPresetService;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.StainingPrototypeService;
import com.patho.main.service.UserService;
import com.patho.main.ui.ListChooser;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class SettingsDialog extends AbstractTabDialog {

	public static final int DIAGNOSIS_LIST = 0;
	public static final int DIAGNOSIS_EDIT = 1;
	public static final int DIAGNOSIS_TEXT_TEMPLATE = 2;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianService physicianService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisPresetRepository diagnosisPresetRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private StainingPrototypeRepository stainingPrototypeRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private StainingPrototypeService stainingPrototypeService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetRepository materialPresetRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetService materialPresetService;
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemService listItemService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private OrganizationRepository organizationRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

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

		tabs = new AbstractTab[] { programParentTab, histoUserTab, histoGroupTab, diagnosisTab, materialTab,
				stainingTab, staticListTab, favouriteListTab, personParentTab, physicianSettingsTab, organizationTab,
				logTab };
	}

	public void initAndPrepareBean() {
		initBean("");
		prepareDialog();
	}

	public void initAndPrepareBean(String tabName) {
		if (initBean(tabName))
			prepareDialog();
	}

	public boolean initBean(String tabName) {
		super.initBean(null, Dialog.SETTINGS);

		AbstractTab foundTab = null;

		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initTab();
			if (tabs[i].getTabName().equals(tabName))
				foundTab = tabs[i];
		}

		if (tabName == null || foundTab == null)
			onTabChange(tabs[1]);
		else {
			onTabChange(foundTab);
		}

		return true;
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
	public class HistoUserTab extends AbstractTab {

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

		public void addHistoUser(Physician physician) {
			if (physician != null) {
				userService.addOrMergeUser(physician);
				updateData();
			}
		}

		public void onAddHistoUser(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
				updateData();
			}
		}

	}

	@Getter
	@Setter
	public class HistoGroupTab extends AbstractTab {

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
			setGroups(groupRepository.findAll(!showArchived));
		}

	}

	public enum DiagnosisPage {
		LIST, EDIT, EDIT_TEXT_TEMPLATE;
	}

	@Getter
	@Setter
	public class DiagnosisTab extends AbstractTab {

		private DiagnosisPage page;

		private List<DiagnosisPreset> diagnosisPresets;

		private DefaultTransformer<DiagnosisPreset> diagnosisPresetsTransformer;

		private DiagnosisPreset selectedDiagnosisPreset;

		private boolean newDiagnosisPreset;

		private ContactRole[] allRoles;

		public DiagnosisTab() {
			setTabName("DiagnosisTab");
			setName("dialog.settings.diagnosis");
			setViewID("diagnoses");
			setPage(DiagnosisPage.LIST);

			setAllRoles(new ContactRole[] { ContactRole.FAMILY_PHYSICIAN, ContactRole.PATIENT, ContactRole.SURGEON,
					ContactRole.PRIVATE_PHYSICIAN, ContactRole.RELATIVES });
		}

		@Override
		public void updateData() {
			switch (getPage()) {
			case EDIT:
			case EDIT_TEXT_TEMPLATE:
				if (getSelectedDiagnosisPreset() != null && getSelectedDiagnosisPreset().getId() != 0) {
					setSelectedDiagnosisPreset(
							diagnosisPresetRepository.findById(getSelectedDiagnosisPreset().getId()).orElse(null));
				}
				break;
			default:
				setDiagnosisPresets(diagnosisPresetRepository.findAllByOrderByIndexInListAsc());
				break;
			}

		}

		public void prepareNewDiagnosisPreset() {
			prepareEditDiagnosisPreset(new DiagnosisPreset());
		}

		public void prepareEditDiagnosisPreset(DiagnosisPreset diagnosisPreset) {
			setSelectedDiagnosisPreset(diagnosisPreset);
			setPage(DiagnosisPage.EDIT);

			setNewDiagnosisPreset(diagnosisPreset.getId() == 0 ? true : false);

			updateData();
		}

		public void saveDiagnosisPreset() {
			if (getSelectedDiagnosisPreset().getId() == 0) {

				// case new, save
				logger.debug("Creating new diagnosis " + getSelectedDiagnosisPreset().getCategory());

				getDiagnosisPresets().add(getSelectedDiagnosisPreset());

				diagnosisPresetRepository.save(getSelectedDiagnosisPreset(),
						resourceBundle.get("log.settings.diagnosis.new", getSelectedDiagnosisPreset().getCategory()));

				ListOrder.reOrderList(getDiagnosisPresets());

				diagnosisPresetRepository.saveAll(getDiagnosisPresets(),
						resourceBundle.get("log.settings.diagnosis.list.reoder"));

			} else {

				// case edit: update an save
				logger.debug("Updating diagnosis " + getSelectedDiagnosisPreset().getCategory());

				diagnosisPresetRepository.save(getSelectedDiagnosisPreset(), resourceBundle
						.get("log.settings.diagnosis.update", getSelectedDiagnosisPreset().getCategory()));
			}

			discardDiagnosisPreset();

		}

		/**
		 * Discards all changes of a diagnosisPrototype
		 */
		public void discardDiagnosisPreset() {
			setPage(DiagnosisPage.LIST);
			setSelectedDiagnosisPreset(null);

			updateData();
		}

		public void prepareEditDiagnosisPresetTemplate() {
			setPage(DiagnosisPage.EDIT_TEXT_TEMPLATE);
		}

		public void discardEditDiagnosisPresetTemplate() {
			setPage(DiagnosisPage.EDIT);
		}

		/**
		 * Is fired if the list is reordered by the user via drag and drop
		 *
		 * @param event
		 */
		public void onReorderList(ReorderEvent event) {
			logger.debug(
					"List order changed, moved material from " + event.getFromIndex() + " to " + event.getToIndex());

			ListOrder.reOrderList(getDiagnosisPresets());

			diagnosisPresetRepository.saveAll(getDiagnosisPresets(),
					resourceBundle.get("log.settings.diagnosis.list.reoder"));

		}

		@Override
		public String getCenterInclude() {
			switch (getPage()) {
			case EDIT:
				return "include/diagnosisEdit.xhtml";
			case EDIT_TEXT_TEMPLATE:
				return "include/diagnosisEditTemplate.xhtml";
			default:
				return "include/diagnosisList.xhtml";
			}
		}
	}

	@Getter
	@Setter
	public class MaterialTab extends AbstractTab {

		private List<MaterialPreset> allMaterials;

		/**
		 * List for selecting staining, this list contains all stainings. They can be
		 * choosen and added to the material
		 */
		private List<ListChooser<StainingPrototype>> stainingListChooserForMaterial;

		private boolean newMaterial;
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
			setAllMaterials(
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

//		/**
//		 * Adds all selected staining prototypes to the material
//		 *
//		 * @param stainingListChoosers
//		 * @param stainingPrototypeList
//		 */
//		public void addStainingToMaterial() {
//			getStainingListChooserForMaterial().forEach(p -> {
//				if (p.isChoosen()) {
//					getEditMaterial().getStainingPrototypes().add(p.getListItem());
//				}
//			});
//		}
//
//		/**
//		 * Removes a staining from a material
//		 *
//		 * @param toRemove
//		 * @param stainingPrototypeList
//		 */
//		public void removeStainingFromStainingList(StainingPrototype toRemove) {
//			getEditMaterial().getStainingPrototypes().remove(toRemove);
//		}

	}

	@Getter
	@Setter
	public class StainingTab extends AbstractTab {

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
	public class StaticListTab extends AbstractTab {

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

		public void archiveListItem(ListItem item, boolean archive) {
			listItemService.archiveListItem(item, archive);
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
	public class PhysicianSettingsTab extends AbstractTab {

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
		 * @param physician
		 * @param archive
		 */
		public void archivePhysician(Physician physician, boolean archive) {
			physicianService.archivePhysician(physician, archive);
			updateData();
		}
	}

	@Getter
	@Setter
	public class OrganizationTab extends AbstractTab {

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

	}

	@Getter
	@Setter
	public class FavouriteListTab extends AbstractTab {

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
			setFavouriteLists(favouriteListRepository.findAll(true, true, true, true));
		}

	}

	@Getter
	@Setter
	public class LogTab extends AbstractTab {

		private int logsPerPull;

		private int selectedLogPage;

		private List<Log> logs;

		private int maxLogPages;

		public LogTab() {
			setTabName("LogTab");
			setName("dialog.settings.log");
			setViewID("logs");
			setCenterInclude("include/log.xhtml");

			setLogsPerPull(50);
			setSelectedLogPage(1);
		}

		@Override
		public void updateData() {
//			int maxPages = logDAO.countTotalLogs();
//			int pagesCount = (int) Math.ceil((double) maxPages / logsPerPull);
//
//			setMaxLogPages(pagesCount);
//
//			setLogs(logDAO.getLogs(getLogsPerPull(), getSelectedLogPage() - 1));
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
			// TODO Auto-generated method stub

		}

	}
}
