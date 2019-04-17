package com.patho.main.util.status

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.service.impl.SpringContextBridge

/**
 */
open class ReportIntentStatusByDiagnosis(val task: Task) {

    val diagnosisBearer: List<DiagnosisBearer> = task.diagnosisRevisions.map { p -> DiagnosisBearer(task, p) }

//    val completed: Boolean = reportIntents.all { p -> p.completed }

    open class DiagnosisBearer(task: Task, val diagnosisRevision: DiagnosisRevision) {

        val reportIntents = mutableListOf<NotificationBearer>()

        init {
            for (contact in task.contacts) {
                val notificationArray = mutableListOf<TypedRecord>()
                for (notification in contact.notifications) {
                    val record = SpringContextBridge.services().reportIntentService.findReportHistoryRecordByDiagnosis(notification, diagnosisRevision)
                    if (record != null) {
                        notificationArray.add(TypedRecord(record, notification.notificationTyp))
                    }
                }

                reportIntents.add(NotificationBearer(contact, notificationArray))
            }
        }

        open class TypedRecord(val reportHistoryRecord: ReportHistoryRecord, val notificationTyp: NotificationTyp)

        open class NotificationBearer(val reportIntent: ReportIntent, val records: List<TypedRecord>) {

            val mailRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.EMAIL }
            val isMailRecord
                get() = mailRecord != null
            val mailPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(mailRecord?.reportHistoryRecord
                    ?: ReportHistoryRecord())

            val faxRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.FAX }
            val isFaxRecord
                get() = faxRecord != null
            val faxPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(faxRecord?.reportHistoryRecord
                    ?: ReportHistoryRecord())

            val letterRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.LETTER }
            val isLetterRecord
                get() = letterRecord != null
            val letterPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(letterRecord?.reportHistoryRecord
                    ?: ReportHistoryRecord())

            val phoneRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.EMAIL }
            val isPhoneRecord
                get() = phoneRecord != null
            val phonePerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(phoneRecord?.reportHistoryRecord
                    ?: ReportHistoryRecord())
        }
    }

}