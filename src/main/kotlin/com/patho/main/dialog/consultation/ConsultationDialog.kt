package com.patho.main.dialog.consultation

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.common.SortOrder
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.dialog.print.PrintDialog
import com.patho.main.dialog.print.documentUi.CouncilReportUi
import com.patho.main.model.PDFContainer
import com.patho.main.model.Physician
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.repository.jpa.CouncilRepository
import com.patho.main.repository.jpa.ListItemRepository
import com.patho.main.repository.jpa.PhysicianRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.CouncilService
import com.patho.main.service.PDFService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.ConfirmEvent
import com.patho.main.util.dialog.event.QuickDiagnosisAddEvent
import com.patho.main.util.dialog.event.ReloadEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.pdf.PDFThumbnailStreamContainerImpl
import org.primefaces.event.FileUploadEvent
import org.primefaces.event.SelectEvent
import org.primefaces.model.DefaultTreeNode
import org.primefaces.model.TreeNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.faces.application.FacesMessage


@Component()
@Scope(value = "session")
open class ConsultationDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val councilService: CouncilService,
        private val physicianRepository: PhysicianRepository,
        private val listItemRepository: ListItemRepository,
        private val councilRepository: CouncilRepository,
        private val pdfService: PDFService,
        private val printDialog: PrintDialog
) : AbstractTaskDialog(Dialog.CONSULTATION) {

    /**
     * Root node for Tree
     */
    lateinit var root: TreeNode

    /**
     * Selected Tree node
     */
    var selectedTreeNode: TreeNode? = null
        set(value) {
            field = value
            if (value != null)
                lastTeeNode = value
        }

    /**
     * Selected Tree node
     */
    var lastTeeNode: TreeNode? = null

    /**
     * Council tree nodes
     */
    var consultationTreeNodes: MutableList<TreeNode> = mutableListOf()

    /**
     * List of all councils of this tasks
     */
    private var consultationContainerList: MutableList<ConsultationContainer> = mutableListOf<ConsultationContainer>()

    /**
     * Selected consultation container for editing
     */
    var selectedConsultation: ConsultationContainer? = null

    /**
     * Object for PDF display
     */
    val stream = PDFThumbnailStreamContainerImpl()

    /**
     * List of physician to address for consultation
     */
    var consultantList: List<Physician> = listOf()

    /**
     * Transformer for consultantList
     */
    var consultantTransformer: DefaultTransformer<Physician> = DefaultTransformer(consultantList)

    /**
     * List of physicians to sign the request
     */
    var originatorList: List<Physician> = listOf()

    /**
     * Transformer for physicianSiangotureList
     */
    var originatorListTransformer: DefaultTransformer<Physician> = DefaultTransformer(originatorList)

    /**
     * Contains all available attachments
     */
    var attachmentList: List<ListItem> = listOf()

    /**
     * Returns a dynamic center include, depending on the type of the selected treeNode
     */
    val centerInclude: String
        get() {
            return when (lastTeeNode?.type) {
                "consultation_request" -> "_request.xhtml"
                "consultation_ship" -> "_ship.xhtml"
                "consultation_reply" -> "_reply.xhtml"
                "consultation_diagnosis" -> "_end.xhtml"
                "pdf_node" -> "_report.xhtml"
                else -> "_empty.xhtml"
            }
        }

    override fun initBean(task: Task): Boolean {
        logger.debug("Show Consultation Dialog")
        this.task = task
        selectedTreeNode = null
        lastTeeNode = null
        selectedConsultation = null

        stream.reset()

        update(true)

        updatePhysicianLists()

        attachmentList = listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.COUNCIL_ATTACHMENT, false)

        if (consultationContainerList.isNotEmpty())
            selectNode(consultationContainerList.last(), 0)

        editable = task.taskStatus.editable

        return true
    }

    override fun update(reload: Boolean) {
        if (reload) {
            task = taskRepository.findByID(task, true, true, true, true, true)

            val consultations: MutableList<Council> = task.councils.toMutableList()
            val foundConsultations: MutableList<Council> = mutableListOf()
            val oldConsultations: MutableList<ConsultationContainer> = mutableListOf()

            loop1@ for (container in consultationContainerList) {
                for (council in consultations) {
                    if (container.consultation == council) {
                        container.consultation = council
                        foundConsultations.add(council)
                        continue@loop1
                    }
                }
                oldConsultations.add(container)

            }
            // adding new councils
            consultations.removeAll(foundConsultations)

            for (council in consultations) {
                consultationContainerList.add(ConsultationContainer(council))
            }
            // removing old contaienr
            consultationContainerList.removeAll(oldConsultations)
            // sorting list
            consultationContainerList.sortBy { it.consultation.dateOfRequest }

        }

        task.generateTaskStatus()

        editable = task.taskStatus.editable

        val newTree = generateTree(consultationContainerList)
        root = newTree.first
        consultationTreeNodes.clear()
        consultationTreeNodes = newTree.second

        // replacing selected node
        if (selectedTreeNode != null) {
            logger.debug("Replacing selected node")
            for (node in consultationTreeNodes) {
                if (node.data == selectedTreeNode?.getData() && node.type == selectedTreeNode?.getType()) {
                    selectedTreeNode = node
                    logger.debug("Replacing selected node")
                    return
                }
            }
        }
    }

    /**
     * Generates an primefaces tree for displaying a list of consultations
     */
    private fun generateTree(consultationContainerList: List<ConsultationContainer>): Pair<TreeNode, MutableList<TreeNode>> {
        logger.debug("Generating new tree")
        val root: TreeNode = DefaultTreeNode("Root", null)
        val taskNode: TreeNode = DefaultTreeNode("task", task, root)
        val result = mutableListOf<TreeNode>()

        taskNode.isExpanded = true
        taskNode.isSelectable = false

        for (consultationContainer in consultationContainerList) {
            logger.debug("Creating tree for $consultationContainer")

            val consultationNode: TreeNode = DefaultTreeNode("consultation", consultationContainer, taskNode)
            consultationNode.isExpanded = true
            consultationNode.isSelectable = false
            result.add(consultationNode)

            val consultationRequestNode: TreeNode = DefaultTreeNode("consultation_request", consultationContainer, consultationNode)
            consultationRequestNode.isExpanded = true
            consultationRequestNode.isSelectable = true
            result.add(consultationRequestNode)

            val consultationshipNode: TreeNode = DefaultTreeNode("consultation_ship", consultationContainer, consultationNode)
            consultationshipNode.isExpanded = true
            consultationshipNode.isSelectable = true
            result.add(consultationshipNode)

            val consultationReturnNode: TreeNode = DefaultTreeNode("consultation_reply", consultationContainer, consultationNode)
            consultationReturnNode.isExpanded = true
            consultationReturnNode.isSelectable = true
            result.add(consultationReturnNode)

            val consultationDiagnosisNode: TreeNode = DefaultTreeNode("consultation_diagnosis", consultationContainer, consultationNode)
            consultationDiagnosisNode.isExpanded = true
            consultationDiagnosisNode.isSelectable = true
            result.add(consultationDiagnosisNode)

            val consultationDataNode: TreeNode = DefaultTreeNode("data_node", consultationContainer, consultationNode)
            consultationDataNode.isExpanded = true
            consultationDataNode.isSelectable = false
            result.add(consultationDataNode)

            for (pdf in consultationContainer.consultation.attachedPdfs) {
                val consultationFileNode: TreeNode = DefaultTreeNode("pdf_node", ConsultationPDFContainer(consultationContainer.consultation, pdf),
                        consultationDataNode)
                consultationFileNode.isExpanded = false
                consultationFileNode.isSelectable = true
                result.add(consultationFileNode)
            }
        }
        return Pair(root, result)
    }

    /**
     * Selects a sub node of a consultation
     */
    open fun selectNode(consultationContainer: ConsultationContainer, child: Int) {
        for (node in consultationTreeNodes) {
            if ((node.data as ConsultationContainer).consultation == consultationContainer.consultation && node.type == "consultation") {
                if (child < node.childCount) {
                    selectedConsultation = node.children[child].data as ConsultationContainer
                    selectedConsultation?.forceEditRequest = false
                    selectedTreeNode?.isSelected = false
                    selectedTreeNode = node.children[child]
                    selectedTreeNode?.isSelected = true
                }
                return
            }
        }
    }

    /**
     * Is fired on selecting a new Treenode
     */
    open fun onTreeNodeSelectionChange() {
        val t = selectedTreeNode?.data ?: return

        if (t is ConsultationContainer)
            this.selectedConsultation = t

        if (t is ConsultationPDFContainer) {
            logger.debug("Is pdf container, render pdf")
            stream.render(t.container)
        }
    }

    /**
     * Saves the current consultation
     */
    open fun saveSelectedConsultation() {
        logger.debug("Saving council data")
        val consultationContainer = selectedConsultation
        if (consultationContainer != null) {
            val c = saveConsultation(consultationContainer.consultation)
            consultationContainer.consultation = c
            task = c.task as Task
            editable = task.taskStatus.editable
        }
    }

    /**
     * Saves the given consultation
     */
    open fun saveConsultation(consultation: Council): Council {
        logger.debug("Saving council data")
        val c: Council = councilRepository.save(consultation,
                resourceBundle["log.patient.task.council.update", task, consultation.name],
                consultation.task?.patient as Patient)
        councilRepository.initializeTask(c)
        c.task?.generateTaskStatus()
        return c
    }

    open fun onEndRequestState(event: SelectEvent) {
        logger.debug("-------------" + event.`object`)
        val o = event.`object`
        if (o is QuickDiagnosisAddEvent) { // reloading
            logger.debug("Ending request phase")
            if (o.diagnosisCreated) update(true)

            val c = selectedConsultation?.consultation ?: return
            councilService.endCouncilRequest(c.task, c)
            update(true)
            // error handling e.g. version conflict
        } else if (event.`object` is ReloadEvent) {
            hideDialog(ReloadEvent())
        }
    }

    /**
     * Updates the name of the consultation if the consultant is changed.
     */
    open fun onConsultantOrDateChange() {
        val c = selectedConsultation?.consultation ?: return
        selectedConsultation?.consultation?.name = councilService.generateCouncilName(c)
        saveSelectedConsultation()
    }

    /**
     * Handles the material shipment state
     */
    open fun onShipSample() {
        var c = selectedConsultation?.consultation ?: return

        if (c.sampleShipped) {
            // setting date
            if (c.sampleShippedDate == null)
                c.sampleShippedDate = LocalDate.now()

            // setting no sample shipped if secreatary is selected
            if (c.sampleReturnedCommentary.isNullOrEmpty() && c.notificationMethod == Council.CouncilNotificationMethod.SECRETARY) {
                c.sampleShippedCommentary = resourceBundle["dialog.consultation.sampleShipped.option.noSample"]
            }

        }

        c = saveConsultation(c)

        if (c.sampleShipped)
            councilService.endSampleShiped(c.task, c)
        else
            councilService.beginSampleShiped(c.task, c)

        update(true)
    }

    /**
     * Sets the current date if the sample returned check box was selected
     */
    open fun onReturnSample() {
        val c = selectedConsultation?.consultation ?: return

        if (c.sampleReturned && c.sampleReturnedDate == null) {
            c.sampleReturnedDate = LocalDate.now()
        }
        saveSelectedConsultation()
    }

    /**
     * Handles the reply received event
     */
    open fun onReplyReceived() {
        var c = selectedConsultation?.consultation ?: return

        if (c.replyReceived && c.replyReceivedDate == null) {
            c.replyReceivedDate = LocalDate.now()
        }

        c = saveConsultation(c)

        if (c.replyReceived) {
            councilService.endReplyReceived(c.task, c)
        } else {
            councilService.beginReplyReceived(c.task, c)
        }

        update(true)
    }

    open fun onCouncilCompleted() {
        var c = selectedConsultation?.consultation ?: return
        councilService.endCouncil(c.task, c)
        MessageHandler.sendGrowlMessages("growl.council.completed.headline", "growl.council.completed.text", FacesMessage.SEVERITY_ERROR)
        update(true)
    }

    /**
     * Handles fileupload to consultaion
     */
    open fun handleFileUpload(event: FileUploadEvent) {
        val c = selectedConsultation?.consultation ?: return
        val file = event.file
        try {
            logger.debug("Uploadgin to Consultation: ${c.name} ")
            val res = pdfService.createPDFAndAddToDataList(c, file.contents, true, file.fileName, PrintDocumentType.COUNCIL_REPLY, "", "", c.fileRepositoryBase.path)
            MessageHandler.sendGrowlMessagesAsResource("growl.upload.success.headline", "growl.upload.success.text", c.name)
        } catch (e: IllegalAccessError) {
            MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed.headline", "growl.upload.failed.text", FacesMessage.SEVERITY_ERROR)
        }
        update(true)
    }

    /**
     * Renews the consultant and originator lists
     */
    open fun updatePhysicianLists() { // list of physicians which are the counselors

        consultantList = physicianRepository.findAllByRole(arrayOf(ContactRole.CASE_CONFERENCE),
                true, SortOrder.PRIORITY)
        consultantTransformer = DefaultTransformer(consultantList)

        originatorList = physicianRepository.findAllByRole(arrayOf(ContactRole.SIGNATURE),
                true, SortOrder.PRIORITY)

        originatorListTransformer = DefaultTransformer(originatorList)
    }

    /**
     * Creates a new consultation and saves it
     */
    open fun createConsultation() {
        logger.info("Adding new council")
        val c = councilService.createCouncil(task, true)
        update(true)
        selectNode(consultationContainerList.last(), 0)
    }

    open fun deleteConsultation() {
        this.councilService.deleteCouncil(this.task, this.selectedConsultation!!.consultation)
        if (consultationContainerList.isNotEmpty())
            selectNode(consultationContainerList.last(), 0)
        else {
            selectedTreeNode = null
            selectedConsultation = null
            lastTeeNode = null
        }

        update(true)

    }

    open fun onConfirmDialogReturn(event: SelectEvent) {
        if (event.getObject() != null && event.getObject() is ConfirmEvent && (event.getObject() as ConfirmEvent).obj) {
            deleteConsultation()
        }
    }

    /**
     * Opens the print dialog with an consultation request selected as template
     */
    open fun printCouncilReport() {
        val c = selectedConsultation ?: return
        val printDocuments = services().printDocumentRepository.findAllByTypes(PrintDocumentType.COUNCIL_REQUEST)
        // getting ui objects
        val printDocumentUIs = PrintDocumentRepository.factory(printDocuments)
        for (documentUi in printDocumentUIs) {
            (documentUi as CouncilReportUi).initialize(task, c.consultation)
            documentUi.sharedData.isRenderSelectedContact = true
            documentUi.sharedData.isUpdatePdfOnEverySettingChange = true
            documentUi.sharedData.isSingleSelect = true
        }
        printDialog.initAndPrepareBean(task, printDocumentUIs, printDocumentUIs[0])
    }

    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

    /**
     * Ui Wrapper for Consultation Class
     */
    open class ConsultationContainer(var consultation: Council) {
        /**
         * If true the request can be edited
         */
        var forceEditRequest = false
    }

    /**
     * UI Wrapper for PDFContainer class
     */
    class ConsultationPDFContainer(consultation: Council, var container: PDFContainer) : ConsultationContainer(consultation) {

    }
}
