package com.patho.main.service

import com.patho.main.action.UserHandlerAction
import com.patho.main.config.util.ResourceBundle
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.template.InitializeToken
import com.patho.main.template.PrintDocument
import com.patho.main.util.notification.NotificationFeedback
import com.patho.main.util.pdf.PDFCreationFailedException
import com.patho.main.util.pdf.PDFCreator
import com.patho.main.util.print.PrintPDFBearer
import com.patho.main.util.report.ReportIntentDataBearer
import com.patho.main.util.report.ReportIntentNotificationBearer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service()
open class ReportService @Autowired constructor(
        private val resourceBundle: ResourceBundle,
        private val userHandlerAction: UserHandlerAction) : AbstractService() {


    open fun executeReportNotification(bearer: ReportIntentDataBearer, feedback: NotificationFeedback) {
        feedback.setFeedback("report.feedback.generatingReport")

        // printing generic report
        if (bearer.additionalReports.printAdditionalReports)


        // sending the report via mail
            if (bearer.mailReports.applyReport) {
                for (container in bearer.mailReports.receivers) {
                    sendMail(container, bearer.mailReports, feedback)
                }
            }

    }

    private fun sendMail(container: ReportIntentNotificationBearer, bearer: ReportIntentDataBearer, feedback: NotificationFeedback): Boolean {
        feedback.setFeedback("report.feedback.mail.send", container.contactAddress)
        logger.debug("Sending mail to {}", container.contactAddress)

        // pdf was selected for the individual contact
        // adding pdf to generated pdf array
        if (container.printPDFBearer == null) {
            if (bearer.mailReports.reportTemplate != null) {
                if (!bearer.mailReports.individualAddress) {
                    // setting generic pdf
                    logger.debug("Using generic pdf as attachment")
                    var tmp = getGeneratedPDFForTemplate(bearer.mailReports.reportTemplate)

                    // checking if a generic pdf was found, if not setting the mail generic pdf
                    if (tmp == null) {
                        logger.debug("Generating mail generic pdf")
                        bearer.mailReports.report = getPrintPDFBearer(bearer.mailReports.reportTemplate as PrintDocument, "", bearer.diagnosisRevision)
                        tmp = bearer.mailReports.report as PrintPDFBearer
                    }
                    container.printPDFBearer = tmp
                } else {
                    // individual address
                    val reportAddressField = AssociatedContactService.generateAddress(container.getContact(),
                            container.getContact().person!!.defaultAddress)
                    logger.debug("Generating pdf for {} (individual address)", reportAddressField)
                    container.printPDFBearer = getPrintPDFBearer(bearer.mailReports.reportTemplate as PrintDocument, reportAddressField, bearer.diagnosisRevision)
                }
            } else {
                throw
            }
        }


    }


    /**
     * Prints additional reports
     */
    private fun printAdditionalReportPDF(bearer: ReportIntentDataBearer): Boolean {
        if (bearer.additionalReports.additionalReportTemplate != null) {
            if (bearer.additionalReports.additionalReport == null)
                bearer.additionalReports.additionalReport = getPrintPDFBearer(bearer.additionalReports.additionalReportTemplate as PrintDocument, "", bearer.diagnosisRevision)

            userHandlerAction.selectedPrinter.print(bearer.additionalReports.additionalReport, bearer.additionalReports.printAdditionalReportsCount)
            return true
        }
        return false
    }

    /**
     * Creates a pdf for a printDocument. This happens in the same thread.
     * If pdf creation is not possible an error is thrown
     */
    private fun getPrintPDFBearer(printDocument: PrintDocument, address: String, diagnosisRevision: DiagnosisRevision): PrintPDFBearer {
        printDocument.initilize(InitializeToken("task", diagnosisRevision.parent),
                InitializeToken("diagnosisRevisions", Arrays.asList<DiagnosisRevision>(diagnosisRevision)),
                InitializeToken("patient", diagnosisRevision.patient), InitializeToken("address", address),
                InitializeToken("subject", ""))

        try {
            return PrintPDFBearer(PDFCreator().createPDF(printDocument), printDocument)
        } catch (e: Exception) {
            throw PDFCreationFailedException()
        }
    }

    /**
     * This method look for an already generated pdf with the same printDocument
     */
    private fun getGeneratedPDFForTemplate(printDocument: PrintDocument?, bearer: ReportIntentDataBearer): PrintPDFBearer {
        if (bearer.additionalReports.additionalReportTemplate == printDocument && bearer.additionalReports.additionalReport != null)
            return bearer.additionalReports.additionalReport as PrintPDFBearer

        if (bearer.mailReports.reportTemplate == printDocument && bearer.mailReports.report != null)
            return bearer.mailReports.report as PrintPDFBearer

        if (bearer.faxReports.reportTemplate == printDocument && bearer.faxReports.report != null)
            return bearer.faxReports.report as PrintPDFBearer

        if (bearer.letterReports.reportTemplate == printDocument && bearer.letterReports.report != null)
            return bearer.letterReports.report as PrintPDFBearer

        return null
    }
}