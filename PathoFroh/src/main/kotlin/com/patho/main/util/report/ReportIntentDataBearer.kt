package com.patho.main.util.report

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.util.print.PrintPDFBearer

/**
 * Class for performing a notification for a diangosis
 */
data class ReportIntentDataBearer(var task: Task, var diagnosisRevision: DiagnosisRevision) {

    var additionalReports: AdditionalReports = AdditionalReports()

    var mailReports: MailReports = MailReports()

    var faxReports: FaxReports = FaxReports()

    var letterReports: LetterReports = LetterReports()

    var phoneReports : PhoneReports = PhoneReports()

    /**
     * Additional reports with empty address
     */
    class AdditionalReports {
        /**
         * True if additional reports should be printed
         */
        var printAdditionalReports: Boolean = false

        /**
         * Number of the addition reports prints
         */
        var printAdditionalReportsCount: Int = 0

        /**
         * Template for printing additional reports, report has no address
         */
        var additionalReportTemplate: PrintDocument? = null

        /**
         * Bearer containing created pdf
         */
        var additionalReport: PrintPDFBearer? = null
    }

    /**
     * Class containing settings for sending the report via mail
     */
    class MailReports {
        /**
         * True if mail should be send
         */
        var applyReport: Boolean = false

        /**
         * True if the address should be used
         */
        var individualAddress: Boolean = false

        /**
         * Mail template
         */
        var mailTemplate: MailTemplate? = null

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
        var receivers: List<ReportIntentNotificationBearer> = listOf<ReportIntentNotificationBearer>()
    }

    /**
     * Class containing settings for sending the report via fax
     */
    class FaxReports {
        /**
         * True if fax should be send
         */
        var applyReport: Boolean = false

        /**
         * True if the address should be used
         */
        var individualAddress: Boolean = false

        /**
         * True if the fax should be send by the program
         */
        var sendFax: Boolean = false

        /**
         * True if the fax should be printed and send by the user
         */
        var printFax: Boolean = false

        /**
         * Template of the generic report which should be send via mail
         */
        var reportTemplate: PrintDocument? = null

        /**
         * Bearer for the generic report which should be send via mail
         */
        var report: PrintPDFBearer? = null

        /**
         * List of all fax receivers
         */
        var receivers: List<ReportIntentNotificationBearer> = listOf<ReportIntentNotificationBearer>()

    }

    /**
     * Class containing settings for sending the report via letter
     */
    class LetterReports {
        /**
         * True if the report should be send via letter
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
         * List of all letter receivers
         */
        var receivers: List<ReportIntentNotificationBearer> = listOf<ReportIntentNotificationBearer>()
    }

    /**
     * Class containing settings for announcing the report via phone
     */
    class PhoneReports {
        /**
         * True if the reports should be announced
         */
        var applyReport: Boolean = false

        /**
         * List of all letter receivers
         */
        var receivers: List<ReportIntentNotificationBearer> = listOf<ReportIntentNotificationBearer>()

    }
}