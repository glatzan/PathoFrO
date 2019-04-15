package com.patho.main.util.status

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Listing for notification types
 */
class ReportIntentStatusByType(val task: Task, notificationTyp: NotificationTyp) {
    var reportIntent: List<ReportIntentNotification> = task.contacts.mapNotNull { p -> SpringContextBridge.services().reportIntentService.findReportIntentNotificationByType(p, notificationTyp) }
}