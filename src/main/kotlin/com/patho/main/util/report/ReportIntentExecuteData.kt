package com.patho.main.util.report

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.util.print.PrintPDFBearer

/**
 * Class for performing a notification for a diangosis
 */
data class ReportIntentExecuteData(var task: Task, var diagnosisRevision: DiagnosisRevision) {

    var additionalReports: AdditionalReports = AdditionalReports()

    var mailReports: MailReports = MailReports()

    var faxReports: FaxReports = FaxReports()

    var letterReports: Reports = Reports()

    var phoneReports: Reports = Reports()


    open class Reports {
        /**
         * True if mail should be send
         */
        var applyReport: Boolean = false

        /**
         * True if the address should be used
         */
        var individualAddress: Boolean = false

        /**
         * Template of the generic report which should be send via mail
         */
        var reportTemplate: PrintDocument? = null

        /**
         * Bearer for the generic report which should be send via mail
         */
        var report: PrintPDFBearer? = null

        /**
         * List of mail receivers
         */
        var receivers: List<out NotificationExecuteData> = listOf<NotificationExecuteData>()
    }


    /**
     * Additional reports with empty address
     */
    class AdditionalReports : Reports() {

        /**
         * Number of the addition reports prints
         */
        var printAdditionalReportsCount: Int = 0
    }

    /**
     * Class containing settings for sending the report via mail
     */
    class MailReports : Reports() {

        /**
         * Mail template
         */
        var mailTemplate: MailTemplate? = null

    }

    /**
     * Class containing settings for sending the report via fax
     */
    class FaxReports : Reports() {
        /**
         * True if the fax should be send by the program
         */
        var sendFax: Boolean = false

        /**
         * True if the fax should be printed and send by the user
         */
        var printFax: Boolean = false

    }

}