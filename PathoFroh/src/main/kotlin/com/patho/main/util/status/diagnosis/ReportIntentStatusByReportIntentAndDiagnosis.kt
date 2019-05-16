package com.patho.main.util.status.diagnosis

import com.patho.main.model.patient.Task

/**
 * Klass listing notifications diagnosis focused
 * ReportIntent -> Diagnosis -> NotificationType -> History
 * Normal structure is: ReportIntent -> NotificationType -> Diagnosis -> History
 *
 * This list will contain all diagnoses, even if no notification record was created for that diagnosis!
 */
class ReportIntentStatusByReportIntentAndDiagnosis(val task: Task) {

    val reportIntentBearer = task.contacts.map { p -> ReportIntentBearer(p, task) }

    val completed: Boolean = reportIntentBearer.all { p -> p.completed }

}