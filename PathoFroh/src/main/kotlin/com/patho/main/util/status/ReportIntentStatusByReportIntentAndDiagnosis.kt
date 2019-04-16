package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.model.person.Contact
import com.patho.main.service.impl.SpringContextBridge

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

    open class ReportIntentBearer(val reportIntent: ReportIntent, val task: Task) {

        val diagnosisBearers = mutableListOf<DiagnosisBearer>()

        val person = reportIntent.person

        val role = reportIntent.role

        var address: Contact = if (person?.defaultAddress != null) person?.defaultAddress?.contact
                ?: person.contact else person?.contact ?: Contact()

        val completed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntent)

        init {
            reportIntent.notifications.forEach { p -> addNotification(p) }
            task.diagnosisRevisions.forEach { p -> addDiagnosisBearer(p.id) }
        }

        private fun addNotification(notification: ReportIntentNotification) {
            val type = notification.notificationTyp

            for (history in notification.history) {
                addType(history.diagnosisID, type
                        ?: NotificationTyp.NONE, history)
            }
        }

        /**
         * Adds a notification type record to the linked diagnosis bearer.
         */
        private fun addType(diagnosisID: Long, type: NotificationTyp, reportHistoryRecord: ReportHistoryRecord) {
            val reportIntentNotificationBearer = DiagnosisBearer.ReportIntentNotificationBearer(type, reportHistoryRecord)
            addDiagnosisBearer(diagnosisID).reportIntentNotificationBearers.add(reportIntentNotificationBearer)
        }

        /**
         * This method searches for a diagnosis bearer in the diagnosisBearer list. If found the bearer will be returned, if not a
         * new bearer will be created.
         */
        private fun addDiagnosisBearer(diagnosisID: Long): DiagnosisBearer {
            var diagnosisBearer = diagnosisBearers.firstOrNull { p -> p.diagnosisID == diagnosisID }

            if (diagnosisBearer == null) {
                diagnosisBearer = DiagnosisBearer(diagnosisID, task)
                diagnosisBearers.add(diagnosisBearer)
                print("New diagnosis $diagnosisID")
            }

            return diagnosisBearer
        }

        /**
         * Container for a diagnosis revision.
         * The revision will be searched via its id. If not found the revision was deleted. This state will be marked.
         */
        open class DiagnosisBearer(val diagnosisID: Long, task: Task) {

            val reportIntentNotificationBearers = mutableListOf<ReportIntentNotificationBearer>()

            val diagnosisRevision = (task.diagnosisRevisions.firstOrNull { p -> p.id == diagnosisID })

            val diagnosisName: String = diagnosisRevision?.name
                    ?: SpringContextBridge.services().resourceBundle.get("Deleted")

            open class ReportIntentNotificationBearer(var type: NotificationTyp, val reportHistoryRecord: ReportHistoryRecord) {
                val success = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportHistoryRecord)
                val successes = reportHistoryRecord.data.count { p -> !p.failed }
                val failedAttempts = reportHistoryRecord.data.count { p -> p.failed }
                val attempts = reportHistoryRecord.data.size
                val reportData = reportHistoryRecord.data.toList()

            }
        }
    }
}