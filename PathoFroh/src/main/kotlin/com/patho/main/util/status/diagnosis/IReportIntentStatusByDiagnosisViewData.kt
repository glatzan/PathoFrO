package com.patho.main.util.status.diagnosis

/**
 * ViewData for DatatableNotificationStatusByDiagnosis.xhtml
 */
interface IReportIntentStatusByDiagnosisViewData {
    /**
     * List of all reportIntent revisions with their status
     */
    open var diagnosisRevisions: ReportIntentStatusByDiagnosis

    /**
     * Selected reportIntent for that the notification will be performed
     */
    open var selectDiagnosisRevision: DiagnosisBearer?

    /**
     * Diagnosis bearer for displaying details in the datatable overlay panel
     */
    open var viewDiagnosisRevisionDetails: DiagnosisBearer?

    /**
     * Method is fired on diagnosis selection
     */
    fun onDiagnosisSelection() {
    }
}