package com.patho.main.action.handler;

import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.views.*;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.*;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.*;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.worklist.Worklist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.application.FacesMessage;
import java.util.Date;
import java.util.Optional;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class GlobalEditViewHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserService userService;

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

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private CentralHandler centralHandler;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ReportView reportView;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskView taskView;


    /**
     * Methodes for saving task data
     */
    private CurrentTaskFunctions ct = new CurrentTaskFunctions(this);


    /**
     * DataTable selection to change a material via overlay panel
     */
    private MaterialPreset materialPresetToChange;


    public void reloadAllData() {
        logger.debug("Force Reload of all data");
        genericViewData.loadView();
        centralHandler.getNavigationData().updateData();
        logger.debug("Reloading worklist");
        worklistHandler.getCurrent().updateWorklist(true);
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
                    centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
                } else {
                    worklistHandler.getCurrent().reloadSelectedPatientAndTask();
                    centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
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
                centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);

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
            centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
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
            centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
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
            centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
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
            centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
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

    }

    public static enum TaskInitilize {
        GENERATE_TASK_STATUS, GENERATE_MENU_MODEL, RELOAD_MENU_MODEL_FAVOURITE_LISTS, RELOAD;
    }

}
