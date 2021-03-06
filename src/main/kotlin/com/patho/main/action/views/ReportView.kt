package com.patho.main.action.views

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.util.audit.Audit
import com.patho.main.service.PDFService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.task.DiagnosisReportUpdater
import com.patho.main.util.pdf.IPDFThumbnailListStreamContainer
import com.patho.main.util.pdf.LazyPDFReturnHandler
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
open class ReportView : AbstractTaskView(), IPDFThumbnailListStreamContainer {

    /**
     * The selected PDF container
     */
    override var displayPDF: PDFContainer? = null

    /**
     * List of all reportIntent revisions with correspondending reports
     */
    open var data: MutableList<DiagnosisReportReturnHandler> = mutableListOf()

    /**
     * The data which are displayed
     */
    open var selectedData: DiagnosisReportReturnHandler? = null

    /**
     * If true the update poll will be started
     */
    open var generatingPDFs: Boolean = false

    /**
     * If true the update poll will be stoped
     */
    open var generationCompleted: Boolean = false

    /**
     * True if selected pdf is currently generating
     */
    open var generatingSelectedPDF: Boolean = false

    /**
     * Returns a the thumbnail list for displaying the thumbnails
     */
    override val thumbnailList: List<PDFContainer>
        get() = data.map { p -> p.pdf ?: PDFContainer() }

    override fun loadView(task: Task) {
        super.loadView(task)

        logger.debug("Loading report view")

        data = mutableListOf()

        generatingPDFs = false
        generatingSelectedPDF = false
        generationCompleted = false

        reset()

        for ((i, revision) in task.diagnosisRevisions.withIndex()) {

            val c = PDFService.findDiagnosisReport(task, revision)

            if (c == null && revision.completed) {
                logger.debug("No report present, generating new report")
                val returnHandler = DiagnosisReportReturnHandler(revision, true)
                // TODO updating task globally?
                this.task = DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, revision, returnHandler)
                // serach for created pdfcontainer
                returnHandler.pdf = PDFService.findDiagnosisReport(task, revision)
                data.add(returnHandler)
                generatingPDFs = true
            } else if (revision.completed) {
                data.add(DiagnosisReportReturnHandler(revision, c, false))
            } else {
                val container = PDFContainer(PrintDocumentType.DIAGNOSIS_REPORT_NOT_APPROVED,
                        revision.name, SpringContextBridge.services().pathoConfig.pdfErrorFiles.reportNotApprovedPDF, SpringContextBridge.services().pathoConfig.pdfErrorFiles.reportNotApprovedIMG)

                container.audit = Audit()
                container.audit?.createdOn = revision.audit?.createdOn ?: System.currentTimeMillis()

                data.add(DiagnosisReportReturnHandler(revision, container, false))
            }

        }

        if (data.size > 0) {
            selectedData
            setSelectedPDF(data[0])
        }
    }

    /**
     * Is called via poll by the gui, checks if the selected pdf is generated or if it is ready
     */
    open fun updateData() {
        if (generatingSelectedPDF) {
            setSelectedPDF(selectedData)
        }

        val loading = data.any { p -> p.loading }

        generationCompleted = !loading
        generatingPDFs = loading
    }

    /**
     * Sets the selected PDF if it is available
     */
    open fun setSelectedPDF(data: DiagnosisReportReturnHandler?) {
        logger.debug("Setting pdf $data")
        if (data != null) {
            selectedData = data
            if (data.loading) {
                generatingSelectedPDF = true
                displayPDF = null
            } else {
                generatingSelectedPDF = false
                displayPDF = data.pdf
            }
        }
    }

    /**
     * Return-Handler for pdf generation
     */
    open class DiagnosisReportReturnHandler : LazyPDFReturnHandler {

        open var diagnosis: DiagnosisRevision

        open var pdf: PDFContainer?

        /**
         * True if the image is generated
         */
        open var loading: Boolean = false

        constructor(diagnosis: DiagnosisRevision, pdf: PDFContainer?, loading: Boolean) {
            this.diagnosis = diagnosis
            this.pdf = pdf
            this.loading = loading
        }

        constructor(diagnosis: DiagnosisRevision, loading: Boolean) : this(diagnosis, null, loading) {}

        override fun returnPDFContent(container: PDFContainer, uuid: String) {
            if (loading) {
                loading = false
            }
        }
    }
}