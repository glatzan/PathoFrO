package com.patho.main.util.status.reportIntent

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.model.person.Contact
import com.patho.main.service.impl.SpringContextBridge

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
     * Adds a notification type record to the linked reportIntent bearer.
     */
    private fun addType(diagnosisID: Long, type: NotificationTyp, diagnosisHistoryRecord: DiagnosisHistoryRecord) {
        val reportIntentNotificationBearer = DiagnosisBearer.ReportIntentNotificationBearer(type, diagnosisHistoryRecord)
        addDiagnosisBearer(diagnosisID).reportIntentNotificationBearers.add(reportIntentNotificationBearer)
    }

    /**
     * This method searches for a reportIntent bearer in the diagnosisBearer list. If found the bearer will be returned, if not a
     * new bearer will be created.
     */
    private fun addDiagnosisBearer(diagnosisID: Long): DiagnosisBearer {
        var diagnosisBearer = diagnosisBearers.firstOrNull { p -> p.diagnosisID == diagnosisID }

        if (diagnosisBearer == null) {
            diagnosisBearer = DiagnosisBearer(diagnosisID, task)
            diagnosisBearers.add(diagnosisBearer)
            print("New reportIntent $diagnosisID")
        }

        return diagnosisBearer
    }

    /**
     * Container for a reportIntent revision.
     * The revision will be searched via its id. If not found the revision was deleted. This state will be marked.
     */
    open class DiagnosisBearer(val diagnosisID: Long, task: Task) {

        val reportIntentNotificationBearers = mutableListOf<ReportIntentNotificationBearer>()

        val diagnosisRevision = (task.diagnosisRevisions.firstOrNull { p -> p.id == diagnosisID })

        val diagnosisName: String = diagnosisRevision?.name
                ?: SpringContextBridge.services().resourceBundle.get("Deleted")

        open class ReportIntentNotificationBearer(var type: NotificationTyp, val diagnosisHistoryRecord: DiagnosisHistoryRecord) {
            val success = SpringContextBridge.services().reportIntentService.isNotificationPerformed(diagnosisHistoryRecord)
            val successes = diagnosisHistoryRecord.data.count { p -> !p.failed }
            val failedAttempts = diagnosisHistoryRecord.data.count { p -> p.failed }
            val attempts = diagnosisHistoryRecord.data.size
            val reportData = diagnosisHistoryRecord.data.toList()

        }
    }
}