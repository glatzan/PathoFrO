package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.model.person.Contact
import com.patho.main.service.impl.SpringContextBridge

/**
 * Class for processing notification data based on the User's view
 */
open class ReportIntentStatusByUser(val task: Task) {

    val reportIntents: List<ReportIntentBearer> = task.contacts.map { p -> ReportIntentBearer(p, task) }

    val completed: Boolean = reportIntents.all { p -> p.completed }

    /**
     * Bearer for reprotIntest
     */
    open class ReportIntentBearer(val reportIntent: ReportIntent, val task: Task) {

        val person = reportIntent.person

        val role = reportIntent.role

        var address: Contact = if (person?.defaultAddress != null) person?.defaultAddress?.contact
                ?: person.contact else person?.contact ?: Contact()

        val reportIntentNotifications: List<ReportIntentNotificationBearer> = reportIntent.notifications.map { p -> ReportIntentNotificationBearer(p, task) }

        val completed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntent)

        open class ReportIntentNotificationBearer(val notification: ReportIntentNotification, val task: Task) {

            val completed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(notification)

            val history = notification.history.map { p -> DiagnosisBearer(p, task) }

            open class DiagnosisBearer(val history: ReportHistoryRecord, task: Task) {
                val diagnosisName: String = (task.diagnosisRevisions.firstOrNull { p -> p.id == history.diagnosisID })?.name
                        ?: SpringContextBridge.services().resourceBundle.get("")
            }

        }

    }

}