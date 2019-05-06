package com.patho.main.util.status

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.service.impl.SpringContextBridge

/**
 */
open class ReportIntentStatusByDiagnosis(val task: Task) {

    val diagnosisBearer: List<DiagnosisBearer> = task.diagnosisRevisions.map { p -> DiagnosisBearer(task, p) }

//    val completed: Boolean = reportIntents.all { p -> p.completed }

    open class DiagnosisBearer(task: Task, val diagnosisRevision: DiagnosisRevision) {

        val reportIntents = mutableListOf<NotificationBearer>()

        val activeReportIntents : List<NotificationBearer>
        get() = reportIntents.filter { p -> p.isActive }

        init {
            for (contact in task.contacts) {
                val notificationArray = mutableListOf<TypedRecord>()
                for (notification in contact.notifications) {
                    val record = SpringContextBridge.services().reportIntentService.findDiagnosisHistoryRecordByDiagnosis(notification, diagnosisRevision)
                    if (record != null) {
                        notificationArray.add(TypedRecord(record, notification.notificationTyp, notification.active))
                    }
                }

                reportIntents.add(NotificationBearer(contact, notificationArray))
            }
         }

        open class TypedRecord(val diagnosisHistoryRecord: DiagnosisHistoryRecord, val notificationTyp: NotificationTyp, val active: Boolean)

        open class NotificationBearer(val reportIntent: ReportIntent, val records: List<TypedRecord>) {

            val isActive = reportIntent.active

            val mailRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.EMAIL }
            val isMailRecord = mailRecord != null
            val isMailActive = isActive && mailRecord?.active ?: false
            val mailPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(mailRecord?.diagnosisHistoryRecord
                    ?: DiagnosisHistoryRecord())

            val faxRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.FAX }
            val isFaxRecord = faxRecord != null
            val isFaxActive = isActive && faxRecord?.active ?: false
            val faxPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(faxRecord?.diagnosisHistoryRecord
                    ?: DiagnosisHistoryRecord())

            val letterRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.LETTER }
            val isLetterRecord = letterRecord != null
            val isLetterActive = isActive && letterRecord?.active ?: false
            val letterPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(letterRecord?.diagnosisHistoryRecord
                    ?: DiagnosisHistoryRecord())

            val phoneRecord = records.firstOrNull { p -> p.notificationTyp == NotificationTyp.PHONE }
            val isPhoneRecord = phoneRecord != null
            val isPhoneActive = isActive && phoneRecord?.active ?: false
            val phonePerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(phoneRecord?.diagnosisHistoryRecord
                    ?: DiagnosisHistoryRecord())
        }
    }

}