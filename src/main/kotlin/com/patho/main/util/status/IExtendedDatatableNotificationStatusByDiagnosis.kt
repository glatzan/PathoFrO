package com.patho.main.util.status


interface IExtendedDatatableNotificationStatusByDiagnosis {

    var diagnosisNotificationStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus

    var selectedDiagnosisRevisionStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus.DiagnosisRevisionStatus?

    var displayDiagnosisRevisionStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus.DiagnosisRevisionStatus?

    /**
     * Method is fired on diagnosis selection
     */
    fun onDiagnosisSelection()
}