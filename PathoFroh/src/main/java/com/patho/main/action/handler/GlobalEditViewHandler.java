package com.patho.main.action.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlPanelGroup;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.dialog.diagnosis.DiagnosisPhaseExitDialog.DiagnosisPhaseExitData;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog.StainingPhaseExitData;
import com.patho.main.action.dialog.worklist.WorklistSearchDialog.WorklistSearchReturnEvent;
import com.patho.main.action.handler.views.GenericView;
import com.patho.main.action.handler.views.ReceiptLogView;
import com.patho.main.action.handler.views.ReportView;
import com.patho.main.action.handler.views.TaskView;
import com.patho.main.common.ContactRole;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.common.View;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Person;
import com.patho.main.model.Signature;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.service.BlockService;
import com.patho.main.service.DiagnosisService;
import com.patho.main.service.FavouriteListService;
import com.patho.main.service.PatientService;
import com.patho.main.service.SampleService;
import com.patho.main.service.SlideService;
import com.patho.main.service.TaskService;
import com.patho.main.service.WorkPhaseService;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.ui.menu.MenuGenerator;
import com.patho.main.util.dialogReturn.DiagnosisPhaseUpdateEvent;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.dialogReturn.ReloadUserEvent;
import com.patho.main.util.dialogReturn.StainingPhaseUpdateEvent;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.AbstractWorklistSearch;
import com.patho.main.util.worklist.search.WorklistSimpleSearch;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class GlobalEditViewHandler extends AbstractHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorkPhaseService workPhaseService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandler dialogHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SlideService slideService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	/**
	 * Navigation Data
	 */
	private NavigationData navigationData = new NavigationData();

	/**
	 * Data for Taskview
	 */
	private TaskView taskView = new TaskView(this);

	/**
	 * Data that are shred between different vies
	 */
	private GenericView genericView = new GenericView(this);

	/**
	 * Data for the report view
	 */
	private ReportView reportView = new ReportView(this);

	/**
	 * Data for receipt log view
	 */
	private ReceiptLogView receiptLogView = new ReceiptLogView(this);

	private WorklistData worklistData = new WorklistData();

	/**
	 * Methodes for saving task data
	 */
	private CurrentTaskFunctions ct = new CurrentTaskFunctions(this);

	/**
	 * Functions for starting and ending a work phase
	 */
	private WorkPhase workPhase = new WorkPhase();

	private DialogReturnEventHandler dialogReturnEventHandler = new DialogReturnEventHandler(this);

	// ************************ Search ************************
	/**
	 * Search String for quick search
	 */
	private String quickSearch;

	/**
	 * TODO: use
	 */
	private boolean searchWorklist;

	// ************************ dynamic lists ************************

	/**
	 * MenuModel for task editing
	 */
	private MenuModel taskMenuModel;

	/**
	 * H:pannelgrid for dynamic command buttons
	 */
	private HtmlPanelGroup taskMenuCommandButtons;

	// ************************ Current Patient/Task ************************
	/**
	 * DataTable selection to change a material via overlay panel
	 */
	private MaterialPreset materialPresetToChange;

	/**
	 * Program start point, is called by the loginhandler
	 */
	public void initializeDataForSession() {
		logger.debug("Loading programm data");
		logger.debug("1. Loading worklist");
		System.out.println("asdasd ++++ " + patientRepository);
		// loading default worklist
		HistoSettings settings = userHandlerAction.getCurrentUser().getSettings();
		SimpleSearchOption defaultWorklistToLoad = settings.getWorklistToLoad();
		Worklist worklist;

		// if a default to load was provided
		if (defaultWorklistToLoad != null && defaultWorklistToLoad != SimpleSearchOption.EMPTY_LIST) {
			WorklistSimpleSearch simpleSearch = new WorklistSimpleSearch(defaultWorklistToLoad);
			worklist = new Worklist("Default", simpleSearch, settings);
		} else
			worklist = new Worklist("Default", new AbstractWorklistSearch());

		worklistViewHandlerAction.addWorklist(worklist, true);

		logger.debug("2. Loading common data");
		genericView.loadStaticData();

		logger.debug("3. Loading view data");
		navigationData.updateData();

		logger.debug("4. Setting navigation data");
		// setting start view
		worklistViewHandlerAction.goToNavigation(userHandlerAction.getCurrentUser().getSettings().getStartView());
		// setting default subview
		getNavigationData().setLastDefaultView(userHandlerAction.getCurrentUser().getSettings().getDefaultView());

		logger.debug("5. Init task data");

		// TODO check if problem, this should be allread done by goToNavigation
//		generateViewData(TaskInitilize.GENERATE_TASK_STATUS, TaskInitilize.GENERATE_MENU_MODEL,
//				TaskInitilize.RELOAD_MENU_MODEL_FAVOURITE_LISTS);

	}

	public void generateViewData(TaskInitilize... initilizes) {
		generateViewData(getNavigationData().getCurrentView(), initilizes);
	}

	public void generateViewData(View view, TaskInitilize... initilizes) {
		logger.debug("On Page load");

		switch (view) {
		case WORKLIST_RECEIPTLOG:
		case WORKLIST_DIAGNOSIS:
			logger.debug("Initilizing receipt or diagnosis view");

			boolean reload = false;

			for (TaskInitilize taskInitilize : initilizes) {
				if (taskInitilize == TaskInitilize.RELOAD) {
					getWorklistData().getWorklist().reloadSelectedPatientAndTask();

					reload = true;
					break;
				}
			}

			for (TaskInitilize taskInitilize : initilizes) {
				// updating task status
				if (taskInitilize == TaskInitilize.GENERATE_TASK_STATUS && !reload) {
					getWorklistData().getWorklist().getSelectedTask().generateTaskStatus();
					// updating menu model
				} else if (taskInitilize == TaskInitilize.GENERATE_MENU_MODEL)
					setTaskMenuModel(
							(new MenuGenerator()).generateEditMenu(getWorklistData().getWorklist().getSelectedPatient(),
									getWorklistData().getWorklist().getSelectedTask(), taskMenuCommandButtons));
			}

			if (view == View.WORKLIST_RECEIPTLOG) {
				// generating guilist for display
				receiptLogView.loadView();
			}

			genericView.loadView();

			break;
		case WORKLIST_PATIENT:
			for (TaskInitilize taskInitilize : initilizes) {
				if (taskInitilize == TaskInitilize.GENERATE_MENU_MODEL)
					setTaskMenuModel(
							(new MenuGenerator()).generateEditMenu(getWorklistData().getWorklist().getSelectedPatient(),
									getWorklistData().getWorklist().getSelectedTask(), taskMenuCommandButtons));
			}
			break;
		case WORKLIST_TASKS:
			logger.debug("Initilizing task view");
			taskView.loadView();
			break;
		case WORKLIST_REPORT:
			logger.debug("Initlize worklist report data");
			for (TaskInitilize taskInitilize : initilizes) {
				if (taskInitilize == TaskInitilize.GENERATE_MENU_MODEL)
					setTaskMenuModel(
							(new MenuGenerator()).generateEditMenu(getWorklistData().getWorklist().getSelectedPatient(),
									getWorklistData().getWorklist().getSelectedTask(), taskMenuCommandButtons));
			}

			reportView.loadView();
			break;
		default:
			break;
		}
	}

	public void reloadAllData() {
		logger.debug("Force Reload of all data");
		genericView.loadStaticData();
		navigationData.updateData();
		logger.debug("Reloading worklist");
		worklistViewHandlerAction.getCurrentWokrlistHandler().reloadCurrentWorklist();
	}

	public void reloadTask() {

	}

	public void reloadPatient() {

	}

	public void reloadGuiData() {
		genericView.loadStaticData();
	}

	@Transactional
	public void addTaskToFavouriteList(Task task, long favouriteListID) {
		try {
			favouriteListDAO.addReattachedTaskToList(task, favouriteListID);
			FavouriteList list = favouriteListDAO.getFavouriteList(favouriteListID, false, false, false);
			mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.added", "growl.favouriteList.added.text",
					new Object[] { task.getTaskID(), list.getName() });

			updateDataOfTask(true, true, false, false);
		} catch (HistoDatabaseInconsistentVersionException e) {
			worklistViewHandlerAction.replacePatientInCurrentWorklist(task.getPatient(), true);
		}
	}

	@Transactional
	public void removeTaskFromFavouriteList(Task task, Long favouriteListID) {
		try {
			favouriteListDAO.removeReattachedTaskFromList(task, favouriteListID);
			FavouriteList list = favouriteListDAO.getFavouriteList(favouriteListID, false, false, false);
			mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.removed",
					"growl.favouriteList.removed.text", new Object[] { task.getTaskID(), list.getName() });
			updateDataOfTask(true, true, false, false);
		} catch (HistoDatabaseInconsistentVersionException e) {
			worklistViewHandlerAction.replacePatientInCurrentWorklist(task.getPatient(), true);
		}
	}

	public void quickSearch() {
		quickSearch(getQuickSearch(), userHandlerAction.getCurrentUser().getSettings().isAlternatePatientAddMode());
		setQuickSearch("");
	}

	public void quickSearch(String quickSerach, boolean alternateMode) {
		logger.debug("Search for " + quickSerach + ", AlternateMode: " + alternateMode);
		// search only in selected worklist
		if (isSearchWorklist()) {
			logger.debug("Search in worklist");
			// TODO: implement
		} else {
			// removing spaces
			quickSerach = quickSerach.trim();

			if (quickSerach.matches("^\\d{6}$")) { // task
				// serach for task (6 digits)

				Optional<Task> task = taskRepository.findOptionalByTaskId(quickSerach, false, true, true, true, true);

				if (task.isPresent()) {
					logger.debug("Task found, adding to worklist");
					worklistViewHandlerAction.addTaskToCurrentWorklist(task.get(), true);
					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.task",
							"growl.search.patient.task.text");
				} else {
					// no task was found
					logger.debug("No task with the given id found");
					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.notFound.task", "general.blank",
							FacesMessage.SEVERITY_ERROR);
				}

			} else if (quickSerach.matches("^\\d{8}$")) { // piz
				// searching for piz (8 digits)
				logger.debug("Search for piz: " + quickSerach);

				// Searching for patient in pdv and local database
				Optional<Patient> patient;
				try {
					patient = patientService.findPatientByPizInDatabaseAndPDV(quickSerach,
							!userHandlerAction.currentUserHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC), true,
							true);
				} catch (Exception e) {
					patient = Optional.empty();
				}

				if (patient.isPresent()) {
					logger.debug("Found patient " + patient + " and adding to currentworklist");

					worklistViewHandlerAction.addPatientToCurrentWorkList(patient.get(), true, true, false);
					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.piz",
							"growl.search.patient.piz.text");

					// if alternate mode the create Task dialog will be
					// shown after the patient is added to the worklist
					if (alternateMode)
						dialogHandler.getCreateTaskDialog().initAndPrepareBean(patient.get());

				} else {
					// no patient was found for piz
					logger.debug("No Patient found with piz " + quickSerach);

					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.notFound.piz", "general.blank",
							FacesMessage.SEVERITY_ERROR);
				}
			} else if (quickSerach.matches("^\\d{9}$")) { // slide id
				// searching for slide (9 digits)
				logger.debug("Search for SlideID: " + quickSerach);

				String taskId = quickSerach.substring(0, 6);
				String uniqueSlideIDinTask = quickSerach.substring(6, 9);

				Optional<Task> task = taskRepository.findOptionalBySlideID(taskId,
						Integer.parseInt(uniqueSlideIDinTask), false, true, true, true, true);

				if (task.isPresent()) {
					logger.debug("Slide found");
					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.slide",
							"growl.search.patient.slide");
					worklistViewHandlerAction.addTaskToCurrentWorklist(task.get(), true);
				} else {
					// no slide was found
					logger.debug("No slide with the given id found");
					MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.notFount.slide", "general.blank",
							FacesMessage.SEVERITY_ERROR);
				}

			} else if (quickSerach.matches("^(.+)(, )(.+)$")) {
				logger.debug("Search for name, first name");
				// name, surename; name surename
				String[] arr = quickSerach.split(", ");
				dialogHandler.getSearchPatientDialog().initAndPrepareBean().inititalValues(arr[0], arr[1], "", null);
			} else if (quickSerach.matches("^(.+) (.+)$")) {
				logger.debug("Search for firstname, name");
				// name, surename; name surename
				String[] arr = quickSerach.split(" ");
				dialogHandler.getSearchPatientDialog().initAndPrepareBean().inititalValues(arr[1], arr[0], "", null);
			} else if (quickSerach.matches("^[\\p{Alpha}\\-]+")) {
				logger.debug("Search for name");
				dialogHandler.getSearchPatientDialog().initAndPrepareBean().inititalValues(quickSerach, "", "", null);
			} else {
				logger.debug("No search match found");
				MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.notFount.general", "general.blank",
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	/**
	 * Executes a dynamic command e.g. for opening a dialog
	 * 
	 * @param command
	 * @param task
	 */
	public void executeCommand(String command, Task task) {
		if (HistoUtil.isNotNullOrEmpty(command)) {
			if (command.matches("dialog:dialog.councilDialog")) {
				MessageHandler.executeScript("clickButtonFromBean('headerForm:councilBtn')");
			}
		}
	}

	@Configurable
	public class DialogReturnEventHandler {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		private GlobalEditViewHandler globalEditViewHandler;

		public DialogReturnEventHandler(GlobalEditViewHandler globalEditViewHandler) {
			this.globalEditViewHandler = globalEditViewHandler;
		}

		/**
		 * 
		 * Patient add dialog return function
		 * 
		 * @param event
		 */
		public void onSearchForPatientReturn(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof PatientReturnEvent) {
				logger.debug("Patient was selected, adding to database and worklist");
				Patient p = ((PatientReturnEvent) event.getObject()).getPatien();

				patientService.addPatient(p, false);
				// reload if patient is known to database, and may is associated with tasks
				worklistViewHandlerAction.addPatientToCurrentWorkList(p, true, true, p.getId() == 0 ? false : true);
			} else {
				logger.debug("No Patient was selected");
			}
		}

		/**
		 * Dialog return function, reloads the selected task
		 * 
		 * @param event
		 */
		public void onDefaultDialogReturn(SelectEvent event) {
			if (event.getObject() != null) {
				// Patient reload event
				if (event.getObject() instanceof PatientReturnEvent) {
					logger.debug("Patient add event reload event.");
					if (((PatientReturnEvent) event.getObject()).getTask() != null)
						globalEditViewHandler.getWorklistData().getWorklist()
								.setSelectedTask(((PatientReturnEvent) event.getObject()).getTask());
					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();

					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
					// staining phase reload event
				} else if (event.getObject() instanceof StainingPhaseUpdateEvent) {
					logger.debug("Update Stating phase");
					// reload task
					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
					workPhase.updateStainingPhase(
							globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask());
				} else if (event.getObject() instanceof DiagnosisPhaseUpdateEvent) {
					logger.debug("Update Diagnosis phase");
					// reload task
					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
					workPhase.updateDiagnosisPhase(
							globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask());
				} else if (event.getObject() instanceof ReloadTaskEvent || event.getObject() instanceof ReloadEvent) {
					logger.debug("Task reload event.");
					if (event.getObject() instanceof ReloadTaskEvent
							&& ((ReloadTaskEvent) event.getObject()).getTask() != null)
						globalEditViewHandler.getWorklistData().getWorklist()
								.setSelectedTask(((ReloadTaskEvent) event.getObject()).getTask());
					else
						globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
				} else if (event.getObject() instanceof StainingPhaseExitData) {
					logger.debug("Staining phase exit dialog return");
					StainingPhaseExitData data = (StainingPhaseExitData) event.getObject();
					globalEditViewHandler.getWorkPhase().endStainingPhase(data.getTask(), data.isEndStainingPhase(),
							data.isRemoveFromStainingList(), data.isGoToDiagnosisPhase(), data.isRemoveFromWorklist());

					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);
				} else if (event.getObject() instanceof DiagnosisPhaseExitData) {
					logger.debug("Diagnosis phase exit dialog return");

					DiagnosisPhaseExitData data = (DiagnosisPhaseExitData) event.getObject();
					globalEditViewHandler.getWorkPhase().endDiagnosisPhase(data.getTask(), data.getSelectedRevision(),
							data.isEndDiangosisPhase(), data.isRemoveFromDiangosisList(),
							data.isGoToNotificationPhase(), data.isRemoveFromWorklist());

					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);
				} else if (event.getObject() instanceof ReloadUserEvent) {
					logger.debug("Updating user");
					userHandlerAction.updateCurrentUser();
				}

			}
		}

		public void onWorklistSelectReturn(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof WorklistSearchReturnEvent) {
				logger.debug("Setting new worklist");
				worklistViewHandlerAction.addWorklist(((WorklistSearchReturnEvent) event.getObject()).getWorklist(), true,true);
				return;
			}
			onDefaultDialogReturn(event);
		}

	}

	@Getter
	@Setter
	public static class WorklistData {
		/**
		 * Containing all worklists
		 */
		private List<Worklist> worklists = new ArrayList<Worklist>();

		/**
		 * Current worklist
		 */
		private Worklist worklist;

		public boolean isSelected(Patient patient) {
			return getWorklist().getSelectedPatient() != null && getWorklist().getSelectedPatient().equals(patient);
		}

		public boolean isSelected(Task task) {
			return getWorklist().getSelectedTask() != null && getWorklist().getSelectedTask().equals(task);
		}

		public Task getSelectedTask() {
			return worklist.getSelectedTask();
		}
	}

	/**
	 * Navigation Data
	 */
	@Getter
	@Setter
	@Configurable
	public class NavigationData {

		/**
		 * View options, dynamically generated depending on the users role
		 */
		private List<View> navigationPages;

		/**
		 * Selected View in the menu
		 */
		private View displayView;

		/**
		 * Current view which is displayed
		 */
		private View currentView;

		/**
		 * Can be Diagnosis or Receiptlog
		 */
		private View lastDefaultView;

		public NavigationData() {

		}

		/**
		 * Returns the center view if present, otherwise an empty page will be
		 * returned**@return
		 */
		public String getCenterView() {
			if (getDisplayView() != null)
				return getDisplayView().getPath();
			else
				return View.WORKLIST_BLANK.getPath();
		}

		/**
		 * Loading all data from Backend
		 */
		public void updateData() {
			setNavigationPages(
					new ArrayList<View>(userHandlerAction.getCurrentUser().getSettings().getAvailableViews()));
		}
	}

	public static enum StainingListAction {
		NONE, PERFORMED, NOT_PERFORMED, PRINT, ARCHIVE;
	}

	@Getter
	@Setter
	@Configurable
	public class CurrentTaskFunctions {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private ResourceBundle resourceBundle;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private TaskService taskService;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private SampleService sampleService;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private BlockService blockService;

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private AssociatedContactService associatedContactService;

		private GlobalEditViewHandler globalEditViewHandler;

		public CurrentTaskFunctions(GlobalEditViewHandler globalEditViewHandler) {
			this.globalEditViewHandler = globalEditViewHandler;
		}

		private Worklist worklist() {
			return globalEditViewHandler.getWorklistData().getWorklist();
		}

		private Task getSelectedTask() {
			return worklist().getSelectedTask();
		}

		private void setSelectedTask(Task task) {
			worklist().setSelectedTask(task);
		}

		public void saveData(String resourcesKey, Object... arr) {
			save(false, resourcesKey, arr);
		}

		/**
		 * Saves dynamically changed data of the views. Error-handling is done via
		 * global error Handler.
		 * 
		 * @param toSave
		 * @param resourcesKey
		 * @param arr
		 */
		public Task save(String resourcesKey, Object... arr) {
			return save(false, resourcesKey, arr);
		}

		/**
		 * Saves dynamically changed data of the views. Error-handling is done via
		 * global error Handler.
		 * 
		 * @param toSave
		 * @param resourcesKey
		 * @param arr
		 */
		public Task save(boolean reload, String resourcesKey, Object... arr) {
			return save(getSelectedTask(), reload, resourcesKey, arr);
		}

		/**
		 * Saves dynamically changed data of the views. Error-handling is done via
		 * global error Handler.
		 * 
		 * @param toSave
		 * @param resourcesKey
		 * @param arr
		 */
		public Task save(Task task, boolean reload, String resourcesKey, Object... arr) {
			logger.debug("Saving task " + task.getTaskID());
			task = taskRepository.save(task, resourceBundle.get(resourcesKey, arr), task.getPatient());
			if (globalEditViewHandler.getWorklistData().isSelected(task)) {
				if (!reload) {
					setSelectedTask(task);
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
				} else {
					globalEditViewHandler.getWorklistData().getWorklist().reloadSelectedPatientAndTask();
					globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
				}
			}

			return task;
		}

		public void updateCaseHistory(ListItem selectedcaseHistoryItem) {
			updateCaseHistoryWithName(selectedcaseHistoryItem.getValue());
		}

		public void updateCaseHistoryWithName(String caseHistory) {
			logger.debug("Updating Case History to " + caseHistory);
			getSelectedTask().setCaseHistory(caseHistory);
			save("log.patient.task.change.caseHistory", getSelectedTask(), caseHistory);
		}

		/**
		 * Copies the diagnosis record to the diagnosis if no text was entered. If task
		 * was provided bevore, a dialog will be opened.
		 * 
		 * @param diagnosis
		 */
		public void copyHistologicalRecord(Diagnosis diagnosis) {
			// setting diagnosistext if no text is set
			if ((diagnosis.getParent().getText() == null || diagnosis.getParent().getText().isEmpty())
					&& diagnosis.getDiagnosisPrototype() != null) {
				logger.debug("No extended diagnosistext found, text copied");
				setSelectedTask(taskService.copyHistologicalRecord(diagnosis, true));
				globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);

				return;
			} else if (diagnosis.getDiagnosisPrototype() != null) {
				logger.debug("Extended diagnosistext found, showing confing dialog");
				MessageHandler.executeScript("clickButtonFromBean('dialogContent:stainingPhaseExitFromDialog')");
				dialogHandler.getCopyHistologicalRecordDialog().initAndPrepareBean(diagnosis);
			}
		}

		/**
		 * Changes the material of the sample.
		 * 
		 * @param sample
		 * @param materialPreset
		 */
		public void updateMaterialOfSample(Sample sample, MaterialPreset materialPreset) {
			logger.debug("Change maerial of sample with preset");
			setSelectedTask(sampleService.updateMaterialOfSample(sample, materialPreset));
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
		}

		/**
		 * Changes the material of the sample.
		 * 
		 * @param sample
		 * @param materialPreset
		 */
		public void updateMaterialOfSampleWithName(Sample sample, String materialPreset) {
			logger.debug("Change maerial of sample");
			setSelectedTask(sampleService.updateMaterialOfSampleWithoutPrototype(sample, materialPreset));
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
		}

		/**
		 * Updates a diagnosis with a preset
		 * 
		 * @param diagnosis
		 * @param preset
		 */
		public void updateDiagnosisPrototype(Diagnosis diagnosis, DiagnosisPreset preset) {
			logger.debug("Updating diagnosis with prototype");
			setSelectedTask(diagnosisService.updateDiagnosisWithPrototype(diagnosis.getTask(), diagnosis, preset));
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
		}

		/**
		 * Updates a diagnosis without a preset. (Removes the previously set preset)
		 */
		public void updateDiagnosisPrototype(Diagnosis diagnosis, String diagnosisAsText) {
			updateDiagnosisPrototype(diagnosis, diagnosisAsText, "", diagnosis.isMalign(), "");
		}

		/**
		 * Updates a diagnosis without a preset. (Removes the previously set preset)
		 */
		public void updateDiagnosisPrototype(Diagnosis diagnosis, String diagnosisAsText, String extendedDiagnosisText,
				boolean malign, String icd10) {
			logger.debug("Updating diagnosis to " + diagnosisAsText);
			setSelectedTask(diagnosisService.updateDiagnosisWithoutPrototype(diagnosis.getTask(), diagnosis,
					diagnosisAsText, extendedDiagnosisText, malign, icd10));
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
		}

		/**
		 * Saves the manually altered flag, if the sample/block/ or slide id was
		 * manually altered.
		 * 
		 * @param idManuallyAltered
		 */
		public void entityNameChange(StainingTableChooser<?> chooser) {
			// checking if something was altered, if not do nothing
			if (chooser != null && chooser.isIdChanged()) {
				logger.debug("Text changed and saved: " + chooser.getIDText());

				chooser.getEntity().setIdManuallyAltered(true);
				chooser.getEntity().updateAllNames(chooser.getEntity().getTask().isUseAutoNomenclature(), false);
				chooser.setIdChanged(false);

				save("log.patient.task.idManuallyAltered", chooser.getEntity().toString());
			}
		}

		/**
		 * Creates a block by using the gui
		 * 
		 * @param sample
		 */
		public void createNewBlock(Sample sample) {
			workPhase.updateStainingPhase(blockService.createBlockAndPersist(sample));
		}

		/**
		 * Sets a slide as stating status completed
		 * 
		 * @param slide
		 */
		public void completeSlide(Slide slide, boolean complete) {
			workPhase.updateStainingPhase(slideService.completedStainingAndPersist(slide, complete));
		}

		/**
		 * Updates the signatures role
		 * 
		 * @param physician
		 */
		public void changePhysiciansSignature(Signature signature) {
			signature.setRole(signature.getPhysician() != null ? signature.getPhysician().getClinicRole() : "");
		}

		public void beginDiagnosisAmendment(DiagnosisRevision diagnosisRevision) {
			// workaround for forcing a persist of the task, even if no changes have been
			// made
			getWorklistData().getWorklist().getSelectedTaskInfo().admendRevision(diagnosisRevision);
			getSelectedTask().getAudit().setUpdatedOn(System.currentTimeMillis());
			save("log.patient.task.diagnosisRevision.lock", getSelectedTask(), diagnosisRevision);
		}

		public void endDiagnosisAmendment(DiagnosisRevision diagnosisRevision) {
			// workaround for forcing a persist of the task, even if no changes have been
			// made
			getWorklistData().getWorklist().getSelectedTaskInfo().lockRevision(diagnosisRevision);

			getSelectedTask().getAudit().setUpdatedOn(System.currentTimeMillis());
			Task task = diagnosisService.generateDefaultDiagnosisReport(getSelectedTask(), diagnosisRevision);
			save(task, false, "log.patient.task.diagnosisRevision.lock", task, diagnosisRevision);
		}

		public void addPhysicianWithRole(Person person, ContactRole role) {
			try {

				associatedContactService.addAssociatedContactAndAddDefaultNotifications(getSelectedTask(),
						new AssociatedContact(getSelectedTask(), person, role));
				// increment counter
				associatedContactService.incrementContactPriorityCounter(person);
			} catch (IllegalArgumentException e) {
				// todo error message
				logger.debug("Not adding, double contact");
				MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.contact.duplicated");
			}

			reloadGuiData();
			globalEditViewHandler.generateViewData(TaskInitilize.RELOAD, TaskInitilize.GENERATE_TASK_STATUS);
		}

	}

	public static enum TaskInitilize {
		GENERATE_TASK_STATUS, GENERATE_MENU_MODEL, RELOAD_MENU_MODEL_FAVOURITE_LISTS, RELOAD;
	}

	@Configurable
	public class WorkPhase {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		public void updateStainingPhase(Task task) {
			// update staing phase
			if (workPhaseService.updateStainigPhase(task)) {
				getWorklistData().getWorklist().setSelectedTask(task);
				// open end staining phase dialog
				MessageHandler.executeScript("clickButtonFromBean('headerForm:stainingPhaseExit')");
			} else {
				workPhaseService.startStainingPhase(task);
				if (getWorklistData().isSelected(task)) {
					// reload task again, updateStainingPhase changes the task
					generateViewData(TaskInitilize.RELOAD, TaskInitilize.GENERATE_MENU_MODEL);
				}
			}
		}

		public void startStainingPhase(Task task) {
			workPhaseService.startStainingPhase(task);
		}

		public Task endStainingPhase(Task task, boolean endPhase, boolean removeFromList, boolean startDiagnosisPhase,
				boolean removeFromWorklist) {

			if (endPhase && removeFromList) {
				// ending staining pahse
				task = workPhaseService.endStainingPhase(task, removeFromList);

				MessageHandler.sendGrowlMessagesAsResource("growl.staining.endAll",
						startDiagnosisPhase ? "growl.staining.endAll.text.true" : "growl.staining.endAll.text.false");
			} else {
				if (removeFromList)
					task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.StainingList,
							PredefinedFavouriteList.ReStainingList);
			}

			if (startDiagnosisPhase) {
				logger.debug("Adding Task to diagnosis list");
				task = startDiagnosisPhase(task);
			}

			if (removeFromWorklist) {
				// only remove from worklist if patient has one active task
				if (task.getParent().getTasks().stream().filter(p -> !p.isFinalized()).count() > 1) {
					MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
				} else {
					worklistViewHandlerAction.removePatientFromCurrentWorklist(task.getPatient());
					// updates the view and selects the next active task
					worklistViewHandlerAction.onDeselectPatient();
				}
			}

			return task;
		}

		public void updateDiagnosisPhase(Task task) {

			for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
				if (revision.getCompletionDate() != 0) {
					startDiagnosisPhase(task);
					generateViewData(TaskInitilize.RELOAD, TaskInitilize.GENERATE_MENU_MODEL);
					break;
				}
			}
		}

		public Task startDiagnosisPhase(Task task) {
			return favouriteListService.addTaskToList(task, PredefinedFavouriteList.DiagnosisList);
		}

		public void endDiagnosisPhase(Task task, DiagnosisRevision diagnosisRevision, boolean endPhase,
				boolean removeFromList, boolean startNotificationPhase, boolean removeFromWorklist) {

			// end diagnosis phase
			if (endPhase && removeFromList) {
				task = workPhaseService.endDiagnosisPhase(task, removeFromList);

				MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.endAll",
						startNotificationPhase ? "growl.diagnosis.endAll.text.true"
								: "growl.diagnosis.endAll.text.false");
			} else {
				// otherwise approve diangosis
				task = diagnosisService.approveDiangosis(task, diagnosisRevision, startNotificationPhase);

				MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.approved",
						startNotificationPhase ? "growl.diagnosis.endAll.text.true"
								: "growl.diagnosis.endAll.text.false");

				if (removeFromWorklist)
					task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.DiagnosisList,
							PredefinedFavouriteList.ReDiagnosisList);
			}

			// adding to notification phase
			if (startNotificationPhase)
				task = startNotificationPhase(task);

			if (removeFromWorklist) {
				// only remove from worklist if patient has one active task
				if (task.getParent().getTasks().stream().filter(p -> !p.isFinalized()).count() > 1) {
					MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
				} else {
					worklistViewHandlerAction.removePatientFromCurrentWorklist(task.getPatient());
					// updates the view and selects the next active task
					worklistViewHandlerAction.onDeselectPatient();
				}
			}
		}

		public Task startNotificationPhase(Task task) {
			return favouriteListService.addTaskToList(task, PredefinedFavouriteList.NotificationList);
		}

	}

}
