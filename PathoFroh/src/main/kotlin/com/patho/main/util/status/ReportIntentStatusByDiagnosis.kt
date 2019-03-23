package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Klass listing notifications diagnosis focused
 * ReportIntent -> Diagnosis -> NotificationType -> History
 * Normal structure is: ReportIntent -> NotificationType -> Diagnosis -> History
 */
class ReportIntentStatusByDiagnosis(val task: Task) {

    open class ReportIntentBearer(val reportIntent: ReportIntent, val task: Task) {

        val diagnosisBearers = mutableListOf<DiagnosisBearer>()

        init {
            reportIntent.notifications.forEach { p -> addNotification(p) }
        }

        private fun addNotification(notification: ReportIntentNotification) {
            val type = notification.notificationTyp

            for (history in notification.history) {
                addType(history.diagnosisID, type
                        ?: ReportIntentNotification.NotificationTyp.NONE, history.data.toList())
            }
        }

        private fun addType(diagnosisID: Long, type: ReportIntentNotification.NotificationTyp, reportIntentNotifications: List<ReportHistoryRecord.ReportData>) {
            var diagnosisBearer = diagnosisBearers.firstOrNull { p -> p.diagnosisID == diagnosisID }

            if (diagnosisBearer == null) {
                diagnosisBearer = DiagnosisBearer(diagnosisID, task)
                diagnosisBearers.add(diagnosisBearer)
            }

            val reportIntentNotificationBearer = DiagnosisBearer.ReportIntentNotificationBearer(type, reportIntentNotifications)
            diagnosisBearer.reportIntentNotificationBearers.add(reportIntentNotificationBearer)

        }


        open class DiagnosisBearer(val diagnosisID: Long, task: Task) {

            val reportIntentNotificationBearers = mutableListOf<ReportIntentNotificationBearer>()

            val diagnosisName: String = (task.diagnosisRevisions.firstOrNull { p -> p.id == diagnosisID })?.name
                    ?: SpringContextBridge.services().resourceBundle.get("")

            open class ReportIntentNotificationBearer(var type: ReportIntentNotification.NotificationTyp, val reportData: List<ReportHistoryRecord.ReportData>) {

            }
        }
    }
}