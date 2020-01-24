package com.patho.main.dialog.media

import com.patho.main.action.dialog.DialogHandler
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.PDFContainer
import com.patho.main.model.interfaces.DataList
import com.patho.main.repository.jpa.BioBankRepository
import com.patho.main.repository.jpa.PatientRepository
import com.patho.main.service.PDFService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.util.dialog.event.ReloadEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for removing pdfs from a datalist
 */
@Component("pdfDeleteDialog")
@Scope(value = "session")
open class PDFDeleteDialog @Autowired constructor(
        private val patientRepository: PatientRepository,
        private val pdfService: PDFService,
        private val bioBankRepository: BioBankRepository,
        private val dialogHandler: DialogHandler) : AbstractDialog_(Dialog.PDF_DELETE) {

    /**
     * PDF
     */
    lateinit var container: PDFContainer

    /**
     * Parent of pdf
     */
    lateinit var parent: DataList

    open fun initAndPrepareBean(pdfContainer: PDFContainer, dataList : DataList): PDFDeleteDialog {
        if (initBean(pdfContainer,dataList))
            prepareDialog()
        return this
    }

    open fun initBean(pdfContainer: PDFContainer, dataList : DataList): Boolean {
        this.container = pdfContainer
        this.parent = dataList
        return super.initBean()
    }

    /**
     * Removes the pdf and closes the dialog
     */
    open fun deleteAndHide(){
        logger.debug("Deleting container")
        MessageHandler.sendGrowlMessagesAsResource("log.pdf.delete", "log.pdf.delete.text", container.name)
        services().pdfService.removeAndDeletePDF(parent, container)
        hideDialog(ReloadEvent())
    }

    override fun hideDialog() {
        super.hideDialog(ReloadEvent())
    }
}