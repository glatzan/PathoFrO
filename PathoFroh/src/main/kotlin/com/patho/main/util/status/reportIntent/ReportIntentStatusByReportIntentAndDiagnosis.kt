package com.patho.main.util.status.reportIntent

import com.patho.main.model.patient.Task

/**
 * Class listing notifications reportIntent focused
 * ReportIntent -> Diagnosis -> NotificationType -> History
 * Normal structure is: ReportIntent -> NotificationType -> Diagnosis -> History
 *
 * This list will contain all diagnoses, even if no notification record was created for that reportIntent!
 */
class ReportIntentStatusByReportIntentAndDiagnosis(val task: Task) {

    val reportIntentBearer = task.contacts.map { p -> ReportIntentBearer(p, task) }

    val completed: Boolean = reportIntentBearer.all { p -> p.completed }

}