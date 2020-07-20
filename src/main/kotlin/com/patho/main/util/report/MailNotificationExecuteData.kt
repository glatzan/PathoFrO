package com.patho.main.util.report

import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.template.MailTemplate
import com.patho.main.util.print.PrintPDFBearer

/**
 * NotificationExecuteData extension with mail template
 */
class MailNotificationExecuteData : NotificationExecuteData {

    /**
     * Mailtemplate
     */
    val mailTemplate: MailTemplate

    constructor(contact: ReportIntent, notification: ReportIntentNotification, mailTemplate: MailTemplate, use: Boolean = true, contactAddress: String = "", printPDFBearer: PrintPDFBearer? = null)
            : super(contact, notification, use, contactAddress, printPDFBearer) {
        this.mailTemplate = mailTemplate
    }
}