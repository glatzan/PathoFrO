package com.patho.main.dialog.media

import com.patho.main.action.dialog.DialogHandler
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.PDFContainer
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.patient.Patient
import com.patho.main.repository.BioBankRepository
import com.patho.main.repository.PatientRepository
import com.patho.main.service.PDFService
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.dialog.event.PDFSelectEvent
import com.patho.main.util.dialog.event.PatientReloadEvent
import com.patho.main.util.dialog.event.ReloadEvent
import com.patho.main.util.exceptions.PatientNotFoundException
import com.patho.main.util.pdf.PDFThumbnailStreamContainerImpl
import org.primefaces.event.FileUploadEvent
import org.primefaces.event.SelectEvent
import org.primefaces.event.TreeDragDropEvent
import org.primefaces.model.DefaultTreeNode
import org.primefaces.model.TreeNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import javax.faces.application.FacesMessage

@Component("pdfOrganizerDialog")
@Scope(value = "session")
open class PDFOrganizerDialog @Autowired constructor(
        private val patientRepository: PatientRepository,
        private val pdfService: PDFService,
        private val bioBankRepository: BioBankRepository,
        private val dialogHandler: DialogHandler) : AbstractDialog_(Dialog.PDF_ORGANIZER) {

    open lateinit var patient: Patient

    open var stream: PDFThumbnailStreamContainerImpl = PDFThumbnailStreamContainerImpl()

    open var root: TreeNode = DefaultTreeNode("Root", null)

    open var selectedNode: TreeNode? = null

    private val dataLists: MutableList<DataList> = mutableListOf()

    open var enablePDFSelection: Boolean = false

    open var viewOnly: Boolean = false

    open lateinit var uploadTarget: DataList

    open lateinit var uploadDocumentType: PrintDocumentType

    open fun initAndPrepareBean(patient: Patient): PDFOrganizerDialog {
        if (initBean(patient))
            prepareDialog()
        return this
    }

    open fun initAndPrepareBean(patient: Patient, selectedContainer: PDFContainer? = null): PDFOrganizerDialog {
        if (initBean(patient, selectedContainer))
            prepareDialog()
        return this
    }

    fun initBean(patient: Patient, selectedContainer: PDFContainer? = null): Boolean {
        this.patient = patient

        stream.reset()

        this.selectedNode = null

        this.enablePDFSelection = false
        this.viewOnly = false

        uploadDocumentType = PrintDocumentType.OTHER
        uploadTarget = patient

        update(true)

        // selecting pdf if pdf is passed
        if (selectedContainer != null) {
            val node = findParentNodeOfContainer(selectedContainer)

            if (node != null) {
                node.isSelected = true
                this.selectedNode = node
                if (node.data is PDFContainer)
                    this.stream.displayPDF = node.data as PDFContainer
            }
        }

        return super.initBean()
    }

    /**
     * Updates the tree menu und reloads the patient's data
     */
    override fun update(reloadPatient: Boolean) {
        if (reloadPatient) {
            val optionalPatient = patientRepository.findOptionalById(patient.id, true)
            if (!optionalPatient.isPresent)
                throw PatientNotFoundException()
            patient = optionalPatient.get()
        }

        updateTree()

        // unloading pdf is it was selected
        if (stream.displayPDF != null && PDFService.getParentOfPDF(dataLists, stream.displayPDF) == null) {
            selectedNode = null
            stream.resetPDF()
        }
    }

    /**
     * Generates the menu tree
     */
    private fun updateTree() {

        dataLists.clear()

        root = DefaultTreeNode("Root", null)

        patient = pdfService.initializeDataListTree(patient)

        val patientNode = DefaultTreeNode("dropAble_Patient", patient, root)
        patientNode.isExpanded = true
        patientNode.isSelectable = false

        dataLists.add(patient)

        patient.attachedPdfs.forEach { DefaultTreeNode("pdf", it, patientNode) }

        patient.tasks.forEach {
            val taskNode = DefaultTreeNode("dropAble_Task", it, patientNode)
            taskNode.isExpanded = true
            taskNode.isSelectable = false
            dataLists.add(it)
            it.attachedPdfs.forEach { DefaultTreeNode("pdf", it, taskNode) }

            val b = bioBankRepository.findOptionalByTaskAndInitialize(it, true, true)
            if (b.isPresent) {
                dataLists.add(b.get())
                val bioBankNode = DefaultTreeNode("dropAble_Biobank", b.get(), taskNode)
                bioBankNode.isExpanded = true
                bioBankNode.isSelectable = false

                b.get().attachedPdfs.forEach { DefaultTreeNode("pdf", it, bioBankNode) }
            }
        }
    }

    private fun findParentNodeOfContainer(pdfContainer: PDFContainer, node: TreeNode = root): TreeNode? {
        if (node.data is PDFContainer && node.data == pdfContainer)
            return node

        for (childNode in node.children) {
            val tmp = findParentNodeOfContainer(pdfContainer, childNode)
            if (tmp != null)
                return tmp
        }

        return null
    }

    /**
     * Method is called an tree node select, copies the selected pdf container
     */
    fun displaySelectedContainer() {
        logger.debug("Display selected container")
        val tmp = selectedNode
        if (tmp != null && tmp.data != null)
            stream.displayPDF = tmp.data as PDFContainer
        else
            stream.displayPDF = null
    }

    /**
     * Event handler for moving pdfs
     */
    fun onDragDrop(event: TreeDragDropEvent) {
        val dragNode = event.dragNode
        val dropNode = event.dropNode

        // do not move if is not a pdf, parent is not dropable and pdf is not restricted
        if (dragNode.data !is PDFContainer || !dropNode.type.startsWith("dropAble")
                || (dragNode.data as PDFContainer).restricted) {
            logger.debug("Cannot move PDf undo change")
            update(true)
            return
        }

        val pdf = dragNode.data as PDFContainer
        val from = PDFService.getParentOfPDF(dataLists, pdf)
        val to = dropNode.data as DataList

        pdfService.movePdf(from, to, pdf)

        logger.debug("Moving PDF")

        update(true)

        MessageHandler.sendGrowlMessagesAsResource("growl.pdf.move.headline", "growl.pdf.move.text",
                arrayOf<Any>(pdf.name))
    }

    /**
     * Handles file media
     */
    fun handleFileUpload(event: FileUploadEvent) {
        val file = event.file
        try {
            logger.debug("Uploading to Patient: " + patient.id)

            val uploadList = PDFService.getDatalistFromDatalists(dataLists, uploadTarget)

            if (uploadList == null) {
                MessageHandler.sendGrowlMessagesAsResource("growl.media.noUpload.headline", "growl.media.noUpload.text", FacesMessage.SEVERITY_ERROR)
                return
            }

            val res = pdfService.createAndAttachPDF(uploadList,
                    PDFService.PDFInfo(file.fileName, uploadDocumentType), file.contents, true)

            MessageHandler.sendGrowlMessagesAsResource("growl.media.success.headline", "growl.media.success.text", FacesMessage.SEVERITY_INFO, { res.container.name })
        } catch (e: IllegalAccessError) {
            MessageHandler.sendGrowlMessagesAsResource("growl.media.failed.headline", "growl.media.failed.text", FacesMessage.SEVERITY_ERROR)
        } catch (e: FileNotFoundException) {
            MessageHandler.sendGrowlMessagesAsResource("growl.media.failed.headline", "growl.media.failed.text", FacesMessage.SEVERITY_ERROR)
        }

        update(true)
    }

    fun uploadTarget(uploadTarget: DataList): PDFOrganizerDialog {
        this.uploadTarget = uploadTarget
        return this
    }

    fun uploadDocumentType(uploadDocumentType: PrintDocumentType): PDFOrganizerDialog {
        this.uploadDocumentType = uploadDocumentType
        return this
    }

    /**
     * Show print success notification
     */
    fun displayPrintNotification(success: Boolean) {
        if (success) {
            MessageHandler.sendGrowlMessagesAsResource("growl.print.printing",
                    "growl.print.success.simple")
        } else {
            MessageHandler.sendGrowlMessagesAsResource("growl.print.error.headline",
                    "growl.print.error.printError", FacesMessage.SEVERITY_ERROR)
        }
    }

    /**
     * Deletes the pdf of the current tree node
     */
    fun deletePDFContainer() {
        val tmp = selectedNode
        if (tmp != null && !(tmp.data as PDFContainer).restricted)
            deletePDFContainer(tmp.data as PDFContainer)
        else
            MessageHandler.sendGrowlMessagesAsResource("growl.pdf.delete.forbidden.headline", "growl.pdf.delete.forbidden.text", FacesMessage.SEVERITY_ERROR)
    }

    /**
     * Deletes the passed pdf container
     */
    fun deletePDFContainer(container: PDFContainer) {
        logger.debug("Deleting container")
        dialogHandler.deletePDFDialog.initAndPrepareBean(container,
                PDFService.getParentOfPDF(dataLists, container))
    }

    /**
     * Opens the edit dialog for the current tree node
     */
    fun editPDFContainer() {
        val tmp = selectedNode
        if (tmp != null && !(tmp.data as PDFContainer).restricted)
            editPDFContainer(tmp.data as PDFContainer)
        else
            MessageHandler.sendGrowlMessagesAsResource("growl.pdf.edit.forbidden")
    }

    /**
     * Opens the edit dialog for the passed pdf
     */
    fun editPDFContainer(container: PDFContainer) {
        logger.debug("Editing container")
        dialogHandler.editPDFDialog.initAndPrepareBean(container)
    }

    /**
     * On dialog return, reload data (delete, edit and updload=
     */
    fun onDefaultDialogReturn(event: SelectEvent) {
        if (event.getObject() is ReloadEvent) {
            update(true)
        }
    }

    /**
     * Sets the view mode for this dialog
     */
    fun viewMode(): PDFOrganizerDialog {
        this.viewOnly = true
        return this
    }

    /**
     * Hides the dialog, returning a reload event
     */
    override fun hideDialog() {
        super.hideDialog(PatientReloadEvent(patient, null, false))
    }

    /**
     * Hies the dialog an returns the selected pdf as an event
     */
    fun selectAndHide() {
        val t = stream.displayPDF ?: return
        super.hideDialog(PDFSelectEvent(t))
    }

}