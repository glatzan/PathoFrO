package com.patho.main.util.report

import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.util.print.PrintPDFBearer

class ReportIntentNotificationBearer(
        var contact: ReportIntent,
        var notification: ReportIntentNotification) {

    /**
     * If true the notification should be performed
     */
    var use: Boolean = false

    /**
     * Address, copied form contact
     */
    var contactAddress: String = ""

    /**
     * The individual pdf is saved here
     */
    var printPDFBearer: PrintPDFBearer? = null
}