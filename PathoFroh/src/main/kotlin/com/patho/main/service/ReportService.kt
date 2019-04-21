package com.patho.main.service

import com.patho.main.action.UserHandlerAction
import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.template.DocumentToken
import com.patho.main.template.InitializeToken
import com.patho.main.template.PrintDocument
import com.patho.main.util.notification.NotificationFeedback
import com.patho.main.util.pdf.PDFCreationFailedException
import com.patho.main.util.pdf.PDFCreator
import com.patho.main.util.pdf.TemplateNotFoundException
import com.patho.main.util.print.PrintPDFBearer
import com.patho.main.util.report.ReportAddressValidator
import com.patho.main.util.report.ReportIntentDataBearer
import com.patho.main.util.report.ReportIntentMailNotificationBearer
import com.patho.main.util.report.ReportIntentNotificationBearer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@Service()
open class ReportService @Autowired constructor(
        private val userHandlerAction: UserHandlerAction,
        private val mailService: MailService,
        private val reportIntentService: ReportIntentService,
        private val faxService: FaxService,
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val pdfService: PDFService) : AbstractService() {


    open fun executeReportNotification(bearer: ReportIntentDataBearer, feedback: NotificationFeedback) {
        feedback.setFeedback("report.feedback.generatingReport")

        var success = true
        var containerList = HashMap<String, List<ReportIntentNotificationBearer>>()

        // printing generic report
        if (bearer.additionalReports.printAdditionalReports)
            printAdditionalReportPDF(bearer)

        // sending the report via mail
        if (bearer.mailReports.applyReport) {
            containerList.put("mailReports", bearer.mailReports.receivers)
            for (container in bearer.mailReports.receivers) {
                success.and(sendMail(container, bearer, feedback))
                feedback.progressStep()
            }
        }

        // sending the report via fax
        if (bearer.faxReports.applyReport) {
            containerList.put("faxReports", bearer.faxReports.receivers)
            for (container in bearer.faxReports.receivers) {
                success.and(sendFax(container, bearer, feedback))
                feedback.progressStep()
            }
        }

        // printing reports
        if (bearer.letterReports.applyReport) {
            containerList.put("letterReports", bearer.letterReports.receivers)
            for (container in bearer.letterReports.receivers) {
                success.and(letterReports(container, bearer, feedback))
                feedback.progressStep()
            }
        }

        // generating phone report
        if (bearer.phoneReports.applyReport) {
            // TODO add reprot
            success.and(phoneReports(bearer.phoneReports.receivers, bearer, feedback))
        }

        generateSendReport(bearer, containerList, success)
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
     * Sends a report mail to the given person
     */
    private fun sendMail(container: ReportIntentMailNotificationBearer, bearer: ReportIntentDataBearer, feedback: NotificationFeedback): Boolean {
        feedback.setFeedback("report.feedback.mail.sending", container.contactAddress)
        logger.debug("Sending mail to {}", container.contactAddress)

        // pdf was selected for the individual contact
        // adding pdf to generated pdf array
        if (container.printPDFBearer == null) {
            if (bearer.mailReports.reportTemplate != null) {
                if (!bearer.mailReports.individualAddress) {
                    // setting generic pdf
                    logger.debug("Using generic pdf as attachment")
                    var tmp = getGeneratedPDFForTemplate(bearer.mailReports.reportTemplate, bearer)

                    // checking if a generic pdf was found, if not setting the mail generic pdf
                    if (tmp == null) {
                        logger.debug("Generating mail generic pdf")
                        bearer.mailReports.report = getPrintPDFBearer(bearer.mailReports.reportTemplate as PrintDocument, "", bearer.diagnosisRevision)
                        tmp = bearer.mailReports.report as PrintPDFBearer
                    }
                    container.printPDFBearer = tmp
                } else {
                    // individual address
                    val reportAddressField = reportIntentService.generateAddress(container.contact)
                    logger.debug("Generating pdf for {} (individual address)", reportAddressField)
                    container.printPDFBearer = getPrintPDFBearer(bearer.mailReports.reportTemplate as PrintDocument, reportAddressField, bearer.diagnosisRevision)
                }
            } else {
                reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.mail.reportGenerationFailed"))
                return false
            }
        }

        // saving contact address for reusing it later
        container.notification.contactAddress = container.contactAddress

        // setting mail attachment
        container.mailTemplate.attachment = container.printPDFBearer?.pdfContainer

        // checking mail validity
        if (ReportAddressValidator.approveMailAddress(container.contactAddress)) {
            reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.mail.notValid", container.contactAddress))
            return false
        }

        val success: Boolean = mailService.sendMail(container.contactAddress, container.mailTemplate)
        reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.mail.sendSuccessful") else resourceBundle.get("report.feedback.mail.sendFailed"))
        return success
    }

    /**
     * Sends the report via fax
     */
    private fun sendFax(container: ReportIntentNotificationBearer, bearer: ReportIntentDataBearer, feedback: NotificationFeedback): Boolean {

        // feedback message
        if (bearer.faxReports.sendFax) {
            feedback.setFeedback("report.feedback.fax.sending", container.contactAddress)
            logger.debug("Sending fax to {}", container.contactAddress)
        } else if (bearer.faxReports.printFax) {
            feedback.setFeedback("report.feedback.fax.sending", container.contactAddress)
            logger.debug("Print fax for person {}", container.notification.contact?.person?.getFullName())
        }

        // pdf was selected for the individual contact
        // adding pdf to generated pdf array
        if (container.printPDFBearer == null) {
            if (bearer.faxReports.reportTemplate != null) {
                if (!bearer.faxReports.individualAddress) {
                    // setting generic pdf
                    logger.debug("Using generic pdf as attachment")
                    var tmp = getGeneratedPDFForTemplate(bearer.faxReports.reportTemplate, bearer)

                    // checking if a generic pdf was found, if not setting the mail generic pdf
                    if (tmp == null) {
                        logger.debug("Generating mail generic pdf")
                        bearer.faxReports.report = getPrintPDFBearer(bearer.faxReports.reportTemplate as PrintDocument, "", bearer.diagnosisRevision)
                        tmp = bearer.faxReports.report as PrintPDFBearer
                    }
                    container.printPDFBearer = tmp
                } else {
                    // individual address
                    val reportAddressField = reportIntentService.generateAddress(container.contact)
                    logger.debug("Generating pdf for {} (individual address)", reportAddressField)
                    container.printPDFBearer = getPrintPDFBearer(bearer.faxReports.reportTemplate as PrintDocument, reportAddressField, bearer.diagnosisRevision)
                }
            } else {
                reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.reportGenerationFailed"))
                return false
            }
        }

        // saving contact address for reusing it later
        container.notification.contactAddress = container.contactAddress

        // checking fax validity
        if (ReportAddressValidator.approveFaxAddress(container.contactAddress)) {
            reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.notValid", container.contactAddress))
            return false
        }

        // TODO check for successful sending
        var success = true

        // printing
        if (bearer.faxReports.printFax) {
            userHandlerAction.selectedPrinter.print(container.printPDFBearer, 1)
        }

        // sending fax
        if (bearer.faxReports.sendFax) {
            faxService.sendFax(container.contactAddress, container.printPDFBearer?.pdfContainer)
        }

        reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.fax.sendSuccessful") else resourceBundle.get("report.feedback.fax.sendFailed"))

        return success
    }

    /**
     * Prints the report for sending via mail
     */
    private fun letterReports(container: ReportIntentNotificationBearer, bearer: ReportIntentDataBearer, feedback: NotificationFeedback): Boolean {
        feedback.setFeedback("report.feedback.print.printing")
        logger.debug("Printing report for person {}", container.notification.contact?.person?.getFullName())

        // pdf was selected for the individual contact
        // adding pdf to generated pdf array
        if (container.printPDFBearer == null) {
            if (bearer.letterReports.reportTemplate != null) {
                if (!bearer.letterReports.individualAddress) {
                    // setting generic pdf
                    logger.debug("Using generic pdf as attachment")
                    var tmp = getGeneratedPDFForTemplate(bearer.letterReports.reportTemplate, bearer)

                    // checking if a generic pdf was found, if not setting the mail generic pdf
                    if (tmp == null) {
                        logger.debug("Generating mail generic pdf")
                        bearer.letterReports.report = getPrintPDFBearer(bearer.letterReports.reportTemplate as PrintDocument, "", bearer.diagnosisRevision)
                        tmp = bearer.letterReports.report as PrintPDFBearer
                    }
                    container.printPDFBearer = tmp
                } else {
                    // individual address
                    val reportAddressField = reportIntentService.generateAddress(container.contact)
                    logger.debug("Generating pdf for {} (individual address)", reportAddressField)
                    container.printPDFBearer = getPrintPDFBearer(bearer.letterReports.reportTemplate as PrintDocument, reportAddressField, bearer.diagnosisRevision)
                }
            } else {
                reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.print.reportGenerationFailed"))
                return false
            }
        }


        // checking fax validity
        if (ReportAddressValidator.approveFaxAddress(container.contactAddress)) {
            reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.notValid", container.contactAddress))
            return false
        }

        // TODO check for successful sending
        var success = true

        // printing
        userHandlerAction.selectedPrinter.print(container.printPDFBearer, 1)

        reportIntentService.addNotificationHistoryData(container.notification, bearer.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.print.printSuccessful") else resourceBundle.get("report.feedback.print.printFailed"))

        return success
    }

    /**
     * Generates a page with all phonenumbers for calling
     */
    private fun phoneReports(containerList: List<ReportIntentNotificationBearer>, bearer: ReportIntentDataBearer, feedback: NotificationFeedback): Boolean {
        return true;
    }

    private fun generateSendReport(bearer: ReportIntentDataBearer, reportIntentNotificationBearers: HashMap<String, List<ReportIntentNotificationBearer>>, success: Boolean): PDFContainer? {
        val document = printDocumentRepository
                .findByID(pathoConfig.defaultDocuments.notificationSendReport)

        if (!document.isPresent) {
            logger.debug("Printdocument not found")
            throw TemplateNotFoundException()
        }

        var tmpMap = HashMap<String, Any?>()
        tmpMap.putAll(reportIntentNotificationBearers)
        tmpMap["task"] = bearer.diagnosisRevision.task
        tmpMap["diagnosisRevisions"] = Arrays.asList<DiagnosisRevision>(bearer.diagnosisRevision)
        tmpMap["applyMailReport"] = bearer.mailReports.applyReport
        tmpMap["applyFaxReport"] = bearer.faxReports.applyReport
        tmpMap["applyLetterReport"] = bearer.letterReports.applyReport
        tmpMap["applyPhoneReport"] = bearer.phoneReports.applyReport
        tmpMap["reportDate"] = Instant.now()
        tmpMap["notificationSuccessful"] = success

        document.get().initialize(tmpMap)

        try {
            logger.debug("Creating send report")
            val pdfReturn = pdfService.createAndAttachPDF(bearer.diagnosisRevision.task, document.get(), true)
            return pdfReturn.container
        } catch (e: FileNotFoundException) {
            logger.debug("Creating send report failed")
            return null
        }
    }


    /**
     * Creates a pdf for a printDocument. This happens in the same thread.
     * If pdf creation is not possible an error is thrown
     */
    private fun getPrintPDFBearer(printDocument: PrintDocument, address: String, diagnosisRevision: DiagnosisRevision): PrintPDFBearer {
        printDocument.initialize(DocumentToken("task", diagnosisRevision.parent),
                DocumentToken("diagnosisRevisions", Arrays.asList<DiagnosisRevision>(diagnosisRevision)),
                DocumentToken("patient", diagnosisRevision.patient), DocumentToken("address", address),
                DocumentToken("subject", ""))

        try {
            return PrintPDFBearer(PDFCreator().createPDF(printDocument), printDocument)
        } catch (e: Exception) {
            throw PDFCreationFailedException()
        }
    }

    /**
     * This method look for an already generated pdf with the same printDocument
     */
    private fun getGeneratedPDFForTemplate(printDocument: PrintDocument?, bearer: ReportIntentDataBearer): PrintPDFBearer? {
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