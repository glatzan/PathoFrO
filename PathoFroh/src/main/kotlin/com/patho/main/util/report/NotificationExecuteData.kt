package com.patho.main.util.report

import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.util.print.PrintPDFBearer

/**
 * Data for a normal notification execution
 */
open class NotificationExecuteData {

    /**
     * Contact
     */
    val contact: ReportIntent

    /**
     * Notification to perform
     */
    val notification: ReportIntentNotification

    /**
     * If true the notification should be performed
     */
    val use: Boolean

    /**
     * Address, copied form contact
     */
    val contactAddress: String

    /**
     * The individual pdf is saved here
     */
    var printPDFBearer: PrintPDFBearer?

    constructor(contact: ReportIntent, notification: ReportIntentNotification, use: Boolean = true, contactAddress: String = "", printPDFBearer: PrintPDFBearer? = null) {
        this.contact = contact
        this.notification = notification
        this.use = use
        this.contactAddress = contactAddress
        this.printPDFBearer = printPDFBearer
    }
}