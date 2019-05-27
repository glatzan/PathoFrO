package com.patho.main.util.status.diagnosis

import com.patho.main.model.patient.Task

/**
 * Class listing notifications reportIntent focused
 * DiagnosisBearer -> NotificationBearer (mail, fax, phone, letter)
 * Normal structure is: ReportIntent -> NotificationType -> Diagnosis -> History
 *
 * This list will contain all diagnoses, even if no notification record was created for that reportIntent!
 */
open class ReportIntentStatusByDiagnosis(val task: Task) {

    val diagnosisBearer: List<DiagnosisBearer> = task.diagnosisRevisions.map { p -> DiagnosisBearer(task, p) }

//    val completed: Boolean = diagnosisBearer.all { p -> p.reportIntents.c }

}