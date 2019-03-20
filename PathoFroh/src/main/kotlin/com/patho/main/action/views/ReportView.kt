package com.patho.main.action.views

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.util.audit.Audit
import com.patho.main.service.PDFService
import com.patho.main.template.PrintDocument
import com.patho.main.ui.task.DiagnosisReportUpdater
import com.patho.main.util.helper.HistoUtil
import com.patho.main.util.pdf.LazyPDFReturnHandler
import com.patho.main.util.pdf.StreamedContentInterface
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
class ReportView : AbstractTaskView(), StreamedContentInterface {

    /**
     * List of all diagnosis revisions with correspondending reports
     */
    var data: MutableList<DiagnosisReportReturnHandler> = mutableListOf()

    /**
     * The data which are displayed
     */
    var selectedData: DiagnosisReportReturnHandler? = null

    /**
     * The selected PDF container
     */
    override var selectedContainer: PDFContainer? = null

    /**
     * If true the update poll will be started
     */
    var generatingPDFs: Boolean = false

    /**
     * If true the update poll will be stoped
     */
    var generationCompleted: Boolean = false

    /**
     * True if selected pdf is currently generating
     */
    var generatingSelectedPDF: Boolean = false

    /**
     * Returns a the thumbnail list for displaying the thumbnails
     */
    override val thumbnailList: List<PDFContainer>
        get() = data.map { p -> p.pdf ?: PDFContainer() }

    override fun loadView(task: Task) {
        super.loadView(task)

        data = mutableListOf()

        generatingPDFs = false
        selectedContainer = null
        generatingSelectedPDF = false
        generationCompleted = false

        for (i in 0 until task.diagnosisRevisions.size) {
            val revision = HistoUtil.getNElement<DiagnosisRevision>(task.diagnosisRevisions, i)

            val c = PDFService.findDiagnosisReport(task, revision!!)

            if (!c.isPresent && revision.completed) {
                logger.debug("No report present, generating new report")

                val returnHandler = DiagnosisReportReturnHandler(revision, true)
                // TODO updating task globally?
                this.task = DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, revision, returnHandler)
                // serach for created pdfcontainer
                returnHandler.pdf = PDFService.findDiagnosisReport(task, revision).get()
                data.add(returnHandler)
                generatingPDFs = true
            } else if (revision.completed) {
                data.add(DiagnosisReportReturnHandler(revision, c.get(), false))
            } else {
                val container = PDFContainer(PrintDocument.DocumentType.DIAGNOSIS_REPORT_NOT_APPROVED,
                        revision.name, PathoConfig.REPORT_NOT_APPROVED_PDF, PathoConfig.REPORT_NOT_APPROVED_IMG)

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
    fun updateData() {
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
    fun setSelectedPDF(data: DiagnosisReportReturnHandler?) {
        logger.debug("Setting pdf " + data!!)
        if (data != null) {
            selectedData = data
            if (data.loading) {
                generatingSelectedPDF = true
                selectedContainer = null
            } else {
                generatingSelectedPDF = false
                selectedContainer = data.pdf
            }
        }
    }

    /**
     * Return-Handler for pdf generation
     */
    class DiagnosisReportReturnHandler : LazyPDFReturnHandler {

        var diagnosis: DiagnosisRevision

        var pdf: PDFContainer?

        /**
         * True if the image is generated
         */
        var loading: Boolean = false

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