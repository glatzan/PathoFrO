package com.patho.main.util.status

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Helper class for displaying all data
 */
open class ReportIntentStatus(task: Task) {

    /**
     * List for all diagnoses
     */
    val diagnosisBearer: List<ReportDiagnosisBearer> = task.diagnosisRevisions.map { p -> ReportDiagnosisBearer(task, p) }

    /**
     * History for one diangosis
     */
    open class ReportDiagnosisBearer(task: Task, diagnosis: DiagnosisRevision) {

        val diagnosis = diagnosis

        val contacts: List<ContactBearer> = task.contacts.map { p -> ContactBearer(p, diagnosis) }

        open class ContactBearer(contact: ReportIntent, diagnosis: DiagnosisRevision) {

            val notifications: List<NotificationBearer> = contact.notifications.map { p -> NotificationBearer(p, diagnosis) }.filter { p ->  p.historyPresent }

            open class NotificationBearer(reportIntentNotification: ReportIntentNotification, diagnosis: DiagnosisRevision) {
                val reportIntentNotification = reportIntentNotification
                val type = reportIntentNotification.notificationTyp

                val historyForDiagnoses : List<DiagnosisHistoryRecord> = SpringContextBridge.services().reportIntentService.findCompletedDiagnosisHistoryRecordByDiagnosis(reportIntentNotification,diagnosis)
                val historyPresent = historyForDiagnoses.isNotEmpty()

            }
        }

    }
}