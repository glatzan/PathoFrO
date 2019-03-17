package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

open class ReportIntentStatusByUser(val task: Task) {

    val reportIntents: List<ReportIntentBearer> = task.contacts.map { p -> ReportIntentBearer(p) }

    val completed: Boolean = reportIntents.all { p -> p.completed }

    open class ReportIntentBearer(val reportIntent: ReportIntent) {

        val reportIntentNotifications: List<ReportIntentNotificationBearer> = reportIntent.notifications.map { p -> ReportIntentNotificationBearer(p) }

        val completed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntent)

        open class ReportIntentNotificationBearer(val reportIntentNotification: ReportIntentNotification) {

            val completed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntentNotification)

        }

    }

}