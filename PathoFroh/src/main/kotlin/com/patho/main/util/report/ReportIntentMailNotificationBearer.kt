package com.patho.main.util.report

import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.template.MailTemplate

class ReportIntentMailNotificationBearer(
        contact: ReportIntent,
        notification: ReportIntentNotification,
        var mailTemplate: MailTemplate) : ReportIntentNotificationBearer(contact, notification) {
}