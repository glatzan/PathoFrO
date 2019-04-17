package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Listing for notification types
 */
class ReportIntentStatusByType(val task: Task, notificationTyp: NotificationTyp) {

    var reportIntents: List<ReportIntentBearer> = task.contacts.filter { p -> p.active }.mapNotNull { p -> SpringContextBridge.services().reportIntentService.findReportIntentNotificationByType(p, notificationTyp) }.map { p -> ReportIntentBearer(p) }

    class ReportIntentBearer(reportIntentNotification: ReportIntentNotification) {

    }
}