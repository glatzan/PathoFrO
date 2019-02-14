package com.patho.main.action.dialog.task;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.media.PDFOrganizer.PDFSelectEvent;
import com.patho.main.action.dialog.task.CreateTaskDialog.TaskTempData.SampleTempData;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.*;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.*;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.ui.selectors.StainingPrototypeHolder;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.exception.CustomNotUniqueReqest;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TaskUtil;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.pdf.PrintOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Configurable
@Getter
@Setter
public class CreateTaskDialog extends AbstractDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserHandlerAction userHandlerAction;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskService taskService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskRepository taskRepository;

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
    private SlideService slideService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DiagnosisService diagnosisService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AssociatedContactService associatedContactService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BioBankService bioBankService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListService favouriteListService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MaterialPresetRepository materialPresetRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PatientRepository patientRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PDFService pdfService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PathoConfig pathoConfig;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PrintDocumentRepository printDocumentRepository;

    private Patient patient;

    private List<MaterialPreset> materialList;

    private TaskTempData taskData;

    /**
     * Initializes the bean and shows the createTaskDialog
     *
     * @param patient
     */
    public CreateTaskDialog initAndPrepareBean(Patient patient) {
        if (initBean(patient))
            prepareDialog();

        return this;
    }

    /**
     * Initializes the bean
     *
     * @param patient
     */
    public boolean initBean(Patient patient) {
        super.initBean(null, Dialog.TASK_CREATE, false);

        setPatient(patient);

        // setting material list
        setMaterialList(materialPresetRepository.findAllIgnoreArchivedOrderByPriorityCountDesc(true, true));

        setTaskData(new TaskTempData());

        return true;
    }

    /**
     * Creates a new Task object and calls createBiobak at the end.
     *
     * @throws CustomNotUniqueReqest
     */
    public Task createNewTask() {
        return transactionTemplate.execute(new TransactionCallback<Task>() {

            public Task doInTransaction(TransactionStatus transactionStatus) {

                logger.debug("Creating new Task");

                Task task = taskService.createTask(getPatient(), getTaskData().getTaskID(), true);
                task.setReceiptDate(getTaskData().getReceiptDate());
                task.setDueDate(getTaskData().getDueDate());
                task.setDateOfSugery(getTaskData().getReceiptDate());
                task.setTaskPriority(getTaskData().getTaskPriority());
                task.setUseAutoNomenclature(getTaskData().isUseAutoNomenclature());

                task = taskRepository.save(task, resourceBundle.get("log.patient.task.update", task), patient);

                for (SampleTempData sampleTempData : getTaskData().getSamples()) {
                    logger.debug("Creating sample {}", sampleTempData.getMaterial());
                    sampleService.createSample(task, sampleTempData.getMaterialPreset(), sampleTempData.getMaterial(),
                            false, true, true);

                    // creating block manually
                    Sample sample = HistoUtil.getLastElement(task.getSamples());
                    blockService.createBlock(sample, false, true, false);

                    Block block = HistoUtil.getLastElement(sample.getBlocks());

                    slideService.createSlidesXTimes(sampleTempData.getStainings(), block);
                }

                // creating standard diagnoses
                task = diagnosisService.createDiagnosisRevision(task, DiagnosisRevisionType.DIAGNOSIS);

                task = associatedContactService
                        .addAssociatedContact(task, getPatient().getPerson(), ContactRole.PATIENT).getTask();

                BioBank bioBank = bioBankService.createBioBank(task);

                task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.StainingList.getId());

                if (getTaskData().isExternalSamples()) {
                    task = favouriteListService.addTaskToList(task, getTaskData().getExnternalSampleCommentary(),
                            PredefinedFavouriteList.ReturnSampleList.getId());
                }

                if (getTaskData().getBioBank().isPDFSelected()) {
                    Patient p = patientRepository.findOptionalById(getPatient().getId()).get();

                    p = pdfService.initializeDataListTree(p);

                    bioBank.setInformedConsentType(getTaskData().getBioBank().getInformedConsentType());

                    pdfService.movePdf(pdfService.getDataListsOfPatient(p), bioBank,
                            getTaskData().getBioBank().getContainer());

                }

                return task;
            }
        });
    }

    public void createAndHide() {
        Task task = createNewTask();
        hideDialog(new PatientReturnEvent(getPatient(), task));
    }

    public void createPrintAndHide() {
        task = createNewTask();

        taskExecutor.execute(new Thread() {
            public void run() {
                logger.debug("Stargin PDF Generation in new Thread");

                Optional<PrintDocument> printDocument = printDocumentRepository
                        .findByID(pathoConfig.getDefaultDocuments().getTaskCreationDocument());

                if (!printDocument.isPresent()) {
                    logger.error("New Task: No TemplateUtil for printing UReport found");
                    MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.noTemplate");
                    return;
                }

                printDocument.get().initilize(new InitializeToken("task", task),
                        new InitializeToken("patient", patient));

                PDFContainer container = null;
                try {
                    container = new PDFCreator().createPDF(printDocument.get());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.failed.creatingPDF");
                    return;
                }

                userHandlerAction.getSelectedPrinter().print(new PrintOrder(container, 1, printDocument.get()));

                logger.debug("PDF Generation completed, thread ended");
            }
        });

        hideDialog(new PatientReturnEvent(getPatient(), task));
    }

    public void validateTaskID(FacesContext context, UIComponent componentToValidate, Object value)
            throws ValidatorException {

        if (value == null || value.toString().length() != 6) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer muss sechs Zahlen enthalten."));
        } else if (!value.toString().matches("[0-9]{6}")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer darf nur Zahlen enthalten"));
        } else if (taskRepository.findOptionalByTaskId(value.toString()).isPresent()) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer bereits vorhanden"));
        }
    }

    @Getter
    @Setter
    public class TaskTempData {
        private String taskID;
        private LocalDate receiptDate;
        private LocalDate dueDate;
        private boolean useAutoNomenclature;
        private TaskPriority taskPriority;

        private boolean externalSamples;
        private String exnternalSampleCommentary;

        private List<SampleTempData> samples;

        private BioBankTempData bioBank;

        public TaskTempData() {
            this.taskID = taskService.getNextTaskID();
            this.receiptDate = LocalDate.now();
            this.dueDate = LocalDate.now();
            this.useAutoNomenclature = true;
            this.taskPriority = TaskPriority.NONE;

            this.samples = new ArrayList<SampleTempData>();
            this.samples.add(new SampleTempData(materialList.get(0)));

            this.bioBank = new BioBankTempData();
        }

        public void addSample() {
            samples.add(new SampleTempData(materialList.get(0)));
            updateSampleNames();
        }

        public void removeSample() {
            if (samples.size() > 1)
                samples.remove(samples.size() - 1);

            updateSampleNames();
        }

        public void updateSampleNames() {
            int i = 1;
            for (SampleTempData sampleTempData : samples) {
                sampleTempData.setSampleID(TaskUtil.getSampleName(samples.size(), i, useAutoNomenclature));
                i++;
            }
        }

        @Getter
        @Setter
        public class SampleTempData {

            private MaterialPreset materialPreset;
            private String material;
            private String sampleID;

            private List<StainingPrototypeHolder> stainings = new ArrayList<StainingPrototypeHolder>();
            private boolean showStainings;

            public SampleTempData(MaterialPreset materialPreset) {
                this.materialPreset = materialPreset;
                this.sampleID = "";
                this.showStainings = false;

                onMaterialPresetChange();
            }

            public void onMaterialPresetChange() {
                setMaterial(getMaterialPreset() != null ? getMaterialPreset().getName() : "");

                stainings.clear();

                for (StainingPrototype prototype : getMaterialPreset().getStainingPrototypes()) {
                    System.out.println("adding " + prototype.getName());
                    stainings.add(new StainingPrototypeHolder(prototype));
                }

            }

        }

        @Getter
        @Setter
        public class BioBankTempData {
            private PDFContainer container;
            private InformedConsentType informedConsentType;

            public BioBankTempData() {
                this.container = null;
                this.informedConsentType = InformedConsentType.NONE;
            }

            public boolean isPDFSelected() {
                return container != null;
            }

            public void onMediaSelectReturn(SelectEvent event) {
                if (event.getObject() != null && event.getObject() instanceof PDFSelectEvent) {
                    setContainer(((PDFSelectEvent) event.getObject()).getContainer());
                    setInformedConsentType(InformedConsentType.FULL);
                } else {
                    setInformedConsentType(InformedConsentType.NONE);
                }
            }
        }
    }
}
