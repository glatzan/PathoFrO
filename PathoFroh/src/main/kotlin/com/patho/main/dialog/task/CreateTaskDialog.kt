package com.patho.main.dialog.task

import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.*
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.MaterialPreset
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.MaterialPresetRepository
import com.patho.main.repository.jpa.PatientRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.*
import com.patho.main.service.PDFService.Companion.flatListOfDataLists
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.DocumentToken
import com.patho.main.ui.selectors.StainingPrototypeHolder
import com.patho.main.util.dialog.event.PDFSelectEvent
import com.patho.main.util.dialog.event.PatientReloadEvent
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.helper.HistoUtil
import com.patho.main.util.helper.TaskUtil
import com.patho.main.util.pdf.LazyPDFReturnHandler
import com.patho.main.util.pdf.PrintOrder
import com.patho.main.util.pdf.creator.PDFCreatorNonBlocking
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import javax.faces.application.FacesMessage
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.validator.ValidatorException


@Component()
open class CreateTaskDialog @Autowired constructor(
        private val materialPresetRepository: MaterialPresetRepository,
        private val transactionTemplate: TransactionTemplate,
        private val currentUserHandler: CurrentUserHandler,
        private val taskRepository: TaskRepository,
        private val taskService: TaskService,
        private val sampleService: SampleService,
        private val blockService: BlockService,
        private val slideService: SlideService,
        private val diagnosisService: DiagnosisService,
        private val reportIntentService: ReportIntentService,
        private val bioBankService: BioBankService,
        private val favouriteListService: FavouriteListService,
        private val pdfService: PDFService,
        private val patientRepository: PatientRepository,
        private val pathoConfig: PathoConfig,
        private val printDocumentRepository: PrintDocumentRepository) : AbstractDialog_(Dialog.TASK_CREATE) {

    lateinit var patient: Patient

    var materialList: List<MaterialPreset> = listOf()

    lateinit var taskData: TaskTemplate

    open fun initAndPrepareBean(patient: Patient): CreateTaskDialog {
        if (initBean(patient))
            prepareDialog()
        return this
    }

    open fun initBean(patient: Patient): Boolean {
        this.patient = patient

        // setting material list
        materialList = materialPresetRepository.findAllOrderByPriorityCountDesc(true, true)

        taskData = TaskTemplate(materialList)

        return super.initBean()
    }

    /**
     * Creates a new Task and a Biobank object.
     * TODO: Move to service
     */
    private fun createNewTask(): Task? {
        return transactionTemplate.execute {
            logger.debug("Creating new Task")
            var task = taskService.createTask(patient, taskData.taskID, true)
            task.receiptDate = taskData.receiptDate
            task.dueDate = taskData.dueDate
            task.dateOfSugery = taskData.receiptDate
            task.taskPriority = taskData.taskPriority
            task.useAutoNomenclature = taskData.isUseAutoNomenclature
            task = taskRepository.save(task, resourceBundle["log.patient.task.update", task], patient)

            for (sampleTempData in taskData.samples) {
                logger.debug("Creating sample ${sampleTempData.material}")
                sampleService.createSample(task, sampleTempData.materialPreset, sampleTempData.material,
                        false, true, true)
                // creating block manually
                val sample = HistoUtil.getLastElement(task.samples)
                blockService.createBlock(sample, false, true, false)
                val block = HistoUtil.getLastElement(sample.blocks)
                slideService.createSlidesXTimes(sampleTempData.stainings, block, "", "", false, true, false, false)
            }

            // creating standard diagnoses
            task = diagnosisService.createDiagnosisRevision(task, DiagnosisRevisionType.DIAGNOSIS)
            task = reportIntentService.addReportIntent(task, patient.person, ContactRole.PATIENT, false, false, true).first

            val bioBank = bioBankService.createBioBank(task)
            task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.StainingList.id)

            if (taskData.isExternalSamples) {
                task = favouriteListService.addTaskToList(task, taskData.externalSampleCommentary,
                        PredefinedFavouriteList.ReturnSampleList.id)
            }

            if (taskData.bioBank.isPDFSelected) {
                var p = patientRepository.findOptionalById(patient.id).get()
                p = pdfService.initializeDataListTree(p)
                bioBank.informedConsentType = taskData.bioBank.informedConsentType
                if (taskData.bioBank.container != null)
                    pdfService.movePDF(flatListOfDataLists(p)!!, bioBank, taskData.bioBank.container!!)
            }
            task
        }
    }

    open fun createAndHide() {
        val task = createNewTask()
        hideDialog(PatientReloadEvent(patient, task, true))
    }

    open fun createPrintAndHide() {
        val task = createNewTask()
        val currentPrinter = currentUserHandler.printer

        logger.debug("Stargin PDF Generation in new Thread")

        val printDocument = printDocumentRepository.findByID(pathoConfig.defaultDocuments.taskCreationDocument)

        if (!printDocument.isPresent) {
            logger.error("New Task: No TemplateUtil for printing UReport found")
            MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.error.noTemplate")
            return
        }

        printDocument.get().initialize(DocumentToken("task", task),
                DocumentToken("patient", patient))

        PDFCreatorNonBlocking().create(printDocument.get(), pathoConfig.fileSettings.workDirectory, false,
                LazyPDFReturnHandler() { container: PDFContainer, s: String ->
                    currentPrinter!!.print(PrintOrder(container, 1, printDocument.get()))
                })

        hideDialog(PatientReloadEvent(patient, task, true))
    }

    open fun validateTaskID(context: FacesContext, componentToValidate: UIComponent, value: Any?) {
        if (value == null || value.toString().length != 6) {
            throw ValidatorException(
                    FacesMessage(FacesMessage.SEVERITY_ERROR, "", resourceBundle["dialog.createTask.validation.error.numberNotMatching"]))
        } else if (!value.toString().matches(Regex("[0-9]{6}"))) {
            throw ValidatorException(
                    FacesMessage(FacesMessage.SEVERITY_ERROR, "", resourceBundle["dialog.createTask.validation.error.onlyNumbers"]))
        } else {
            try {
                services().taskRepository.findByTaskID(value.toString(), false, false, false, false, false)
                throw ValidatorException(
                        FacesMessage(FacesMessage.SEVERITY_ERROR, "", resourceBundle["dialog.createTask.validation.error.doubleNumber"]))
            } catch (e: TaskNotFoundException) {
            }
        }
    }

    class TaskTemplate(private var materialList: List<MaterialPreset>) {
        var taskID: String = SpringContextBridge.services().taskService.getNextTaskID()
        var receiptDate: LocalDate = LocalDate.now()
        var dueDate: LocalDate = LocalDate.now()
        var isUseAutoNomenclature: Boolean = true
        var taskPriority: TaskPriority = TaskPriority.NONE;

        var isExternalSamples = false
        var externalSampleCommentary: String = ""

        val samples: MutableList<SampleTempData> = mutableListOf()

        val bioBank: BioBankTempData = BioBankTempData()

        init {
            samples.add(SampleTempData(materialList.first()))
        }

        fun addSample() {
            samples.add(SampleTempData(materialList.first()))
            updateSampleNames()
        }

        fun removeSample() {
            if (samples.size > 1) samples.removeAt(samples.size - 1)
            updateSampleNames()
        }

        fun updateSampleNames() {
            var i = 1
            for (sampleTempData in samples) {
                sampleTempData.sampleID = TaskUtil.getSampleName(samples.size, i, isUseAutoNomenclature)
                i++
            }
        }

        class SampleTempData(var materialPreset: MaterialPreset?) {
            var sampleID: String = ""
            var material: String = ""
            var showStainings: Boolean = false
            val stainings: MutableList<StainingPrototypeHolder> = mutableListOf()

            fun onMaterialPresetChange() {
                material = materialPreset?.name ?: ""
                stainings.clear()

                materialPreset?.stainingPrototypes?.forEach {
                    stainings.add(StainingPrototypeHolder(it))
                }
            }
        }
    }

    class BioBankTempData {
        var container: PDFContainer? = null
        var informedConsentType: InformedConsentType = InformedConsentType.NONE

        val isPDFSelected
            get() = container != null

        fun onMediaSelectReturn(event: SelectEvent) {
            val obj = event.getObject()

            if (obj != null && obj is PDFSelectEvent) {
                container = (event.getObject() as PDFSelectEvent).obj
                informedConsentType = InformedConsentType.FULL
            } else {
                informedConsentType = InformedConsentType.NONE
            }
        }

    }
}