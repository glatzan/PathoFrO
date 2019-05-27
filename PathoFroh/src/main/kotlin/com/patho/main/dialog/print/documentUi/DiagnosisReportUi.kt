package com.patho.main.dialog.print.documentUi

import com.patho.main.action.dialog.print.CustomAddressDialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.template.DocumentToken
import com.patho.main.template.print.DiagnosisReport
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.ui.selector.ReportIntentSelector
import org.primefaces.event.SelectEvent

class DiagnosisReportUi : AbstractTaskReportUi<DiagnosisReport, DiagnosisReportUi.DiagnosisSharedData> {

    constructor(diagnosisReport: DiagnosisReport) : this(diagnosisReport, DiagnosisSharedData())

    constructor(diagnosisReport: DiagnosisReport, diagnosisSharedData: DiagnosisSharedData) : super(diagnosisReport, diagnosisSharedData) {
        inputInclude = "include/diagnosisReport.xhtml"
    }

    override fun initialize(task: Task) {
        initialize(task, listOf())
    }

    fun initialize(task: Task, contactList: List<ReportIntentSelector>) {
        sharedData.initialize(task, contactList)
        super.initialize(task)
    }

    /**
     * Change Address dialog return event handler
     */
    fun onCustomAddressReturn(event: SelectEvent) {
        logger.debug("Returning from custom address dialog")
        val returnObject = event.`object`

        if (returnObject is CustomAddressDialog.CustomAddressReturn) {
            returnObject.contactSelector.manuallyAltered = true
            returnObject.contactSelector.customAddress = returnObject.customAddress
            logger.debug("Custom address set to ${returnObject.customAddress}")
        }
    }

    /**
     * Return default template configuration for printing
     */
    override fun getDefaultTemplateConfiguration(): TemplateConfiguration<DiagnosisReport> {
        printDocument.initialize(DocumentToken("patient", task.parent), DocumentToken("task", task),
                DocumentToken("diagnosisRevisions", listOf(sharedData.selectedDiagnosis)),
                DocumentToken("address", if (sharedData.isRenderSelectedContact) getAddressOfFirstSelectedContact() else ""))
        return TemplateConfiguration<DiagnosisReport>(printDocument)
    }

    /**
     * Sets the data for the next print
     */
    override fun getNextTemplateConfiguration(): TemplateConfiguration<DiagnosisReport>? {
        val address = contactListPointer?.customAddress ?: ""
        printDocument.initialize(DocumentToken("patient", task.parent), DocumentToken("task", task),
                DocumentToken("diagnosisRevisions", listOf(sharedData.selectedDiagnosis)),
                DocumentToken("address", address))

        return TemplateConfiguration<DiagnosisReport>(printDocument, contactListPointer?.contact, address, contactListPointer?.copies
                ?: 1
        )
    }

    /**
     *  Shared data of diagnosisReports
     */
    class DiagnosisSharedData : AbstractTaskReportUi.SharedContactData() {

        /**
         * List of all diagnoses
         */
        var diagnoses: Set<DiagnosisRevision> = setOf()
            set(value) {
                field = value
                diagnosesTransformer = DefaultTransformer(value)
            }

        /**
         * Transformer for diagnoses
         */
        var diagnosesTransformer: DefaultTransformer<DiagnosisRevision> = DefaultTransformer(diagnoses)

        /**
         * Selected reportIntent
         */
        var selectedDiagnosis: DiagnosisRevision? = null

        /**
         * Initializes the shareddata context.
         */
        override fun initialize(task: Task): Boolean {
            return initialize(task, task.contacts.map { ReportIntentSelector(it, false, false) }.toList())
        }

        /**
         * Initializes the shareddata context.
         */
        override fun initialize(task: Task, contactSelector: List<ReportIntentSelector>): Boolean {
            // return if already initialized
            if (super.initialize(task, contactList))
                return true

            diagnoses = task.diagnosisRevisions

            // getting last reportIntent
            selectedDiagnosis = diagnoses.firstOrNull()

            return false
        }
    }
}