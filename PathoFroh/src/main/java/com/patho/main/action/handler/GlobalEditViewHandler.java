package com.patho.main.action.handler;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.handler.views.ReportView;
import com.patho.main.action.handler.views.TaskView;
import com.patho.main.action.views.DiagnosisView;
import com.patho.main.action.views.GenericViewData;
import com.patho.main.action.views.ReceiptLogView;
import com.patho.main.common.ContactRole;
import com.patho.main.common.View;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.*;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.person.Person;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.*;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.ui.menu.MenuGenerator;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.AbstractWorklistSearch;
import com.patho.main.util.worklist.search.WorklistSimpleSearch;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlPanelGroup;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class GlobalEditViewHandler extends AbstractHandler {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserHandlerAction userHandlerAction;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private WorklistViewHandler worklistViewHandler;

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
    private SlideService slideService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskRepository taskRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DiagnosisService diagnosisService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private WorklistHandler worklistHandler;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private WorkPhaseHandler workPhaseHandler;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ReportIntentService reportIntentService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private GenericViewData genericViewData;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DiagnosisView diagnosisView;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ReceiptLogView receiptLogView;

    /**
     * Navigation Data
     */
    private NavigationData navigationData = new NavigationData();

    /**
     * Data for Taskview
     */
    private TaskView taskView = new TaskView(this);

    /**
     * Data for the report view
     */
    private ReportView reportView = new ReportView(this);

    /**
     * Methodes for saving task data
     */
    private CurrentTaskFunctions ct = new CurrentTaskFunctions(this);

    /**
     * Search String for quick search
     */
    private String quickSearch;

    /**
     * Launch data for opening a patient add dialog from quick search
     */
    private PatientAddDialogLaunchData patientAddDialogLaunchData;

    /**
     * TODO: use
     */
    private boolean searchWorklist;

    /**
     * MenuModel for task editing
     */
    private MenuModel taskMenuModel;

    /**
     * H:pannelgrid for dynamic command buttons
     */
    private HtmlPanelGroup taskMenuCommandButtons;

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

        logger.debug("2. Setting navigation data");
        // setting start view
        getNavigationData().setCurrentView(userHandlerAction.getCurrentUser().getSettings().getStartView());
        // setting default subview
        getNavigationData().setLastDefaultView(userHandlerAction.getCurrentUser().getSettings().getDefaultView());

        logger.debug("3. Loading common data");
        genericViewData.loadView();

        logger.debug("4. Loading view data");
        navigationData.updateData();

        logger.debug("5. Setting worklist");
        worklistViewHandler.addWorklist(worklist, true);

        logger.debug("6. Init task data");

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
                        worklistHandler.getCurrent().reloadSelectedPatientAndTask();

                        reload = true;
                        break;
                    }
                }

                for (TaskInitilize taskInitilize : initilizes) {
                    // updating task status
                    if (taskInitilize == TaskInitilize.GENERATE_TASK_STATUS && !reload) {
                        worklistHandler.getCurrent().getSelectedTask().generateTaskStatus();
                        // updating menu model
                    } else if (taskInitilize == TaskInitilize.GENERATE_MENU_MODEL)
                        setTaskMenuModel(
                                (new MenuGenerator()).generateEditMenu(worklistHandler.getCurrent().getSelectedPatient(),
                                        worklistHandler.getCurrent().getSelectedTask(), taskMenuCommandButtons));
                }

                if (view == View.WORKLIST_RECEIPTLOG) {
                    // generating guilist for display
                    receiptLogView.loadView();
                }

                if (view == View.WORKLIST_DIAGNOSIS) {
                    diagnosisView.loadView();
                }

                genericViewData.loadView();

                break;
            case WORKLIST_PATIENT:
                for (TaskInitilize taskInitilize : initilizes) {
                    if (taskInitilize == TaskInitilize.GENERATE_MENU_MODEL)
                        setTaskMenuModel(
                                (new MenuGenerator()).generateEditMenu(worklistHandler.getCurrent().getSelectedPatient(),
                                        worklistHandler.getCurrent().getSelectedTask(), taskMenuCommandButtons));
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
                                (new MenuGenerator()).generateEditMenu(worklistHandler.getCurrent().getSelectedPatient(),
                                        worklistHandler.getCurrent().getSelectedTask(), taskMenuCommandButtons));
                }

                reportView.loadView();
                break;
            default:
                break;
        }
    }

    public void reloadAllData() {
        logger.debug("Force Reload of all data");
        genericViewData.loadView();
        navigationData.updateData();
        logger.debug("Reloading worklist");
        worklistHandler.reloadWorklist();
    }

    public void reloadTask() {

    }

    public void reloadPatient() {

    }

    public void reloadGuiData() {
        genericViewData.loadView();
    }

    @Transactional
    public void addTaskToFavouriteList(Task task, long favouriteListID) {
//		try {
//			favouriteListDAO.addReattachedTaskToList(task, favouriteListID);
//			FavouriteList list = favouriteListDAO.getFavouriteList(favouriteListID, false, false, false);
//			mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.added", "growl.favouriteList.added.text",
//					new Object[] { task.getTaskID(), list.getName() });
//
//			updateDataOfTask(true, true, false, false);
//		} catch (HistoDatabaseInconsistentVersionException e) {
//			worklistViewHandler.replacePatientInWorklist(task.getPatient(), true);
//		}
    }

    @Transactional
    public void removeTaskFromFavouriteList(Task task, Long favouriteListID) {
//		try {
//			favouriteListDAO.removeReattachedTaskFromList(task, favouriteListID);
//			FavouriteList list = favouriteListDAO.getFavouriteList(favouriteListID, false, false, false);
//			mainHandlerAction.sendGrowlMessagesAsResource("growl.favouriteList.removed",
//					"growl.favouriteList.removed.text", new Object[] { task.getTaskID(), list.getName() });
//			updateDataOfTask(true, true, false, false);
//		} catch (HistoDatabaseInconsistentVersionException e) {
//			worklistViewHandler.replacePatientInWorklist(task.getPatient(), true);
//		}
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
                    worklistViewHandler.addTaskToWorklist(task.get(), true);
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

                    worklistViewHandler.addPatientToWorkList(patient.get(), true, true);
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.piz",
                            "growl.search.patient.piz.text");

                    // if alternate mode the create Task dialog will be
                    // shown after the patient is added to the worklist
                    if (alternateMode) {
                        // starting task button from gui (return handler)
                        MessageHandler.executeScript("clickButtonFromBean('headerForm:newTaskBtn')");
                    }

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
                    worklistViewHandler.addTaskToWorklist(task.get(), true);
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
                // setting data for launching dialog via gui element (return handler
                setPatientAddDialogLaunchData(new PatientAddDialogLaunchData(arr[0], arr[1], "", null));
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')");
            } else if (quickSerach.matches("^(.+) (.+)$")) {
                logger.debug("Search for firstname, name");
                // name, surename; name surename
                String[] arr = quickSerach.split(" ");
                setPatientAddDialogLaunchData(new PatientAddDialogLaunchData(arr[1], arr[0], "", null));
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')");
            } else if (quickSerach.matches("^[\\p{Alpha}\\-]+")) {
                logger.debug("Search for name");
                setPatientAddDialogLaunchData(new PatientAddDialogLaunchData(quickSerach, "", "", null));
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')");
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
        private DialogHandler dialogHandler;

        private GlobalEditViewHandler globalEditViewHandler;

        public CurrentTaskFunctions(GlobalEditViewHandler globalEditViewHandler) {
            this.globalEditViewHandler = globalEditViewHandler;
        }

        private Worklist worklist() {
            return worklistHandler.getCurrent();
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
         */
        public Task save(String resourcesKey, Object... arr) {
            return save(false, resourcesKey, arr);
        }

        /**
         * Saves dynamically changed data of the views. Error-handling is done via
         * global error Handler.
         */
        public Task save(boolean reload, String resourcesKey, Object... arr) {
            return save(getSelectedTask(), reload, resourcesKey, arr);
        }

        /**
         * Saves dynamically changed data of the views. Error-handling is done via
         * global error Handler.
         *
         * @param arr
         */
        public Task save(Task task, boolean reload, String resourcesKey, Object... arr) {
            logger.debug("Saving task " + task.getTaskID());
            task = taskRepository.save(task, resourceBundle.get(resourcesKey, arr), task.getPatient());
            if (worklistHandler.isSelected(task)) {
                if (!reload) {
                    setSelectedTask(task);
                    globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
                } else {
                    worklistHandler.getCurrent().reloadSelectedPatientAndTask();
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
                setSelectedTask(diagnosisService.copyHistologicalRecord(diagnosis, true));
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
            updateDiagnosisPrototype(diagnosis, diagnosisAsText, "", diagnosis.getMalign(), "");
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
         */
        public void entityNameChange(StainingTableChooser<?> chooser) {
            // checking if something was altered, if not do nothing
            if (chooser != null && chooser.isIdChanged()) {
                logger.debug("Text changed and saved: " + chooser.getIDText());

                chooser.getEntity().setIdManuallyAltered(true);
                //TaskTreeTools.updateNamesInTree(chooser.getEntity(),chooser.getEntity().getTask(), false);
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
            workPhaseHandler.updateStainingPhase(blockService.createBlockAndPersist(sample));
        }

        /**
         * Sets a slide as stating status completed
         *
         * @param slide
         */
        public void completeSlide(Slide slide, boolean complete) {
            workPhaseHandler.updateStainingPhase(slideService.completedStainingAndPersist(slide, complete));
        }

        /**
         * Updates the signatures role
         */
        public void changePhysiciansSignature(Signature signature) {
            signature.setRole(signature.getPhysician() != null ? signature.getPhysician().getClinicRole() : "");
        }

        public void beginDiagnosisAmendment(DiagnosisRevision diagnosisRevision) {
            // workaround for forcing a persist of the task, even if no changes have been
            // made
            worklistHandler.getCurrent().getSelectedTaskInfo().admendRevision(diagnosisRevision);
            getSelectedTask().getAudit().setUpdatedOn(System.currentTimeMillis());
            save("log.patient.task.diagnosisRevision.lock", getSelectedTask(), diagnosisRevision);
        }

        public void endDiagnosisAmendment(DiagnosisRevision diagnosisRevision) {
            // workaround for forcing a persist of the task, even if no changes have been
            // made
            worklistHandler.getCurrent().getSelectedTaskInfo().lockRevision(diagnosisRevision);

            getSelectedTask().getAudit().setUpdatedOn(System.currentTimeMillis());
            Task task = diagnosisService.generateDefaultDiagnosisReport(getSelectedTask(), diagnosisRevision);
            save(task, false, "log.patient.task.diagnosisRevision.lock", task, diagnosisRevision);
        }

        public void addPhysicianWithRole(Person person, ContactRole role) {
            try {

                reportIntentService.addReportIntent(getSelectedTask(), new ReportIntent(getSelectedTask(), person, role), true, true);
                // increment counter
                SpringContextBridge.services().getPhysicianService().incrementPhysicianPriorityCounter(person);
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

    @AllArgsConstructor
    @Getter
    @Setter
    public static class PatientAddDialogLaunchData {
        private String name;
        private String surname;
        private String piz;
        private Date birthday;
    }

}
