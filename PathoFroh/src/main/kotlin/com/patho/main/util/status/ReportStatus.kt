package com.patho.main.util.status

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportHistoryJson
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Helper class for displaying all data
 */
open class ReportStatus(task: Task, reportIntent: ReportIntent) {

    /**
     * Reporttransmitter
     */
    val reportIntent: ReportIntent = reportIntent

    /**
     * List for all diagnoses
     */
    val diagnosisBearer: List<ReportDiagnosisBearer> = task.diagnosisRevisions.map { p -> ReportDiagnosisBearer(p) }

    /**
     * History for one diangosis
     */
    open inner class ReportDiagnosisBearer(diagnosis: DiagnosisRevision) {

        val diagnosis = diagnosis

        val historyRecord: ReportHistoryJson.HistoryRecord = SpringContextBridge.services().reportTransmitterService.getReportHistoryForDiagnosis(reportIntent, diagnosis)
                ?: ReportHistoryJson.HistoryRecord(diagnosis)

        val emailNotification = ReportBearer(reportIntent, ReportIntentNotification.NotificationTyp.EMAIL)
        val faxNotification = ReportBearer(reportIntent, ReportIntentNotification.NotificationTyp.FAX)
        val phoneNotification = ReportBearer(reportIntent, ReportIntentNotification.NotificationTyp.PHONE)
        val letterNotification = ReportBearer(reportIntent, ReportIntentNotification.NotificationTyp.LETTER)
        val printNotification = ReportBearer(reportIntent, ReportIntentNotification.NotificationTyp.PRINT)

        /**
         * Returns all notifications as a list
         */
        val notifications: List<ReportBearer>
            get() {
                val result: ArrayList<ReportBearer> = ArrayList<ReportBearer>(5)
                if (!emailNotification.empty) result.add(emailNotification)
                if (!faxNotification.empty) result.add(faxNotification)
                if (!phoneNotification.empty) result.add(phoneNotification)
                if (!letterNotification.empty) result.add(letterNotification)
                if (!printNotification.empty) result.add(printNotification)
                return result
            }

        /**
         * Bearer for a single notification type
         */
        open inner class ReportBearer(contact: ReportIntent, type: ReportIntentNotification.NotificationTyp) {

            val type: ReportIntentNotification.NotificationTyp = type

            val notifications = SpringContextBridge.services().reportTransmitterService.getReportDataForType(historyRecord, type)

            val totalAttempts: Int = notifications.count()
            val successfulAttempts: Int = notifications.count { p -> !p.failed }
            val failedAttempts: Int = notifications.count { p -> p.failed }

            val succeeded: Boolean = if (notifications.size > 0) !notifications.last().failed else false

            val empty = notifications.isEmpty()
        }

    }
}