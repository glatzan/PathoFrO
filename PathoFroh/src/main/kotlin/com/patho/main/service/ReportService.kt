package com.patho.main.service

import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.template.DocumentToken
import com.patho.main.template.PrintDocument
import com.patho.main.util.exceptions.PDFCreationFailedException
import com.patho.main.util.exceptions.TemplateNotFoundException
import com.patho.main.util.pdf.PDFCreator
import com.patho.main.util.print.PrintPDFBearer
import com.patho.main.util.report.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@Service()
open class ReportService @Autowired constructor(
        private val currentUserHandler: CurrentUserHandler,
        private val mailService: MailService,
        private val reportIntentService: ReportIntentService,
        private val faxService: FaxService,
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val pdfService: PDFService,
        private val taskRepository: TaskRepository) : AbstractService() {


    open fun executeReportNotification(execute: ReportIntentExecuteData, feedback: NotificationFeedback): PDFContainer? {
        feedback.initializeFeedback(calculateSteps(execute))
        feedback.setFeedback("report.feedback.generatingReport")

        var success = true
        var containerList = HashMap<String, List<NotificationExecuteData>>()

        // printing generic report
        if (execute.additionalReports.applyReport) {
            printAdditionalReportPDF(execute)
            feedback.progress()
        }

        // sending the report via mail
        if (execute.mailReports.applyReport) {
            containerList["mailReports"] = execute.mailReports.receivers
            for (container in execute.mailReports.receivers) {
                success.and(sendMail(container as MailNotificationExecuteData, execute, feedback))
                feedback.progress()
            }
        }

        // sending the report via fax
        if (execute.faxReports.applyReport) {
            containerList.put("faxReports", execute.faxReports.receivers)
            for (container in execute.faxReports.receivers) {
                success.and(sendFax(container, execute, feedback))
                feedback.progress()
            }
        }

        // printing reports
        if (execute.letterReports.applyReport) {
            containerList.put("letterReports", execute.letterReports.receivers)
            for (container in execute.letterReports.receivers) {
                success.and(letterReports(container, execute, feedback))
                feedback.progress()
            }
        }

        // generating phone report
        if (execute.phoneReports.applyReport) {
            // TODO add reprot
            success.and(phoneReports(execute.phoneReports.receivers, execute, feedback))
        }

        taskRepository.save(execute.task, resourceBundle.get("special.pdfOrganizerDialog"), execute.task.parent)

        val result = generateSendReport(execute, containerList, success, feedback)
        feedback.progress()
        feedback.end(success)

        return result
    }

    /**
     * Prints additional reports
     */
    private fun printAdditionalReportPDF(execute: ReportIntentExecuteData): Boolean {
        if (execute.additionalReports.applyReport != null) {
            // creating generic report
            if (execute.additionalReports.report == null)
                execute.additionalReports.report = getPrintPDFBearer(execute.additionalReports.reportTemplate as PrintDocument, "", execute.diagnosisRevision)

            currentUserHandler.printer?.print(execute.additionalReports.report, execute.additionalReports.printAdditionalReportsCount)
            return true
        }
        return false
    }


//    execute: ReportIntentExecuteData, report: ReportIntentExecuteData.Reports, container: NotificationExecuteData, template: PrintDocument?, individualAddress: Boolean

    /**
     * Sends a report mail to the given person
     */
    private fun sendMail(container: MailNotificationExecuteData, execute: ReportIntentExecuteData, feedback: NotificationFeedback): Boolean {
        feedback.setFeedback("report.feedback.mail.sending", container.contactAddress.toString())
        logger.debug("Sending mail to {}", container.contactAddress)

        if (!generatePrintPDf(execute, execute.mailReports, container)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.mail.reportGenerationFailed"), save = true).first
            return false
        }

        // saving contact address for reusing it later
        container.notification.contactAddress = container.contactAddress

        // setting mail attachment
        container.mailTemplate.attachment = container.printPDFBearer?.pdfContainer

        // checking mail validity
        if (!ReportAddressValidator.approveMailAddress(container.contactAddress)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.mail.notValid", container.contactAddress), save = true).first
            return false
        }

        val success: Boolean = mailService.sendMail(container.contactAddress, container.mailTemplate)
        container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.mail.sendSuccessful") else resourceBundle.get("report.feedback.mail.sendFailed"), save = true).first
        return success
    }

    /**
     * Sends the report via fax
     */
    private fun sendFax(container: NotificationExecuteData, execute: ReportIntentExecuteData, feedback: NotificationFeedback): Boolean {

        // feedback message
        if (execute.faxReports.sendFax) {
            feedback.setFeedback("report.feedback.fax.sending", container.contactAddress.toString())
            logger.debug("Sending fax to {}", container.contactAddress)
        } else if (execute.faxReports.printFax) {
            feedback.setFeedback("report.feedback.fax.sending", container.contactAddress.toString())
            logger.debug("Print fax for person {}", container.notification.contact?.person?.getFullName())
        }

        if (!generatePrintPDf(execute, execute.faxReports, container)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.reportGenerationFailed"), save = true).first
            return false
        }

        // saving contact address for reusing it later
        container.notification.contactAddress = container.contactAddress

        // checking fax validity
        if (!ReportAddressValidator.approveFaxAddress(container.contactAddress)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.notValid", container.contactAddress), save = true).first
            return false
        }

        // TODO check for successful sending
        var success = true

        // printing
        if (execute.faxReports.printFax) {
            currentUserHandler.printer?.print(container.printPDFBearer, 1)
        }

        // sending fax
        if (execute.faxReports.sendFax) {
            faxService.sendFax(container.contactAddress, container.printPDFBearer?.pdfContainer)
        }

        container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.fax.sendSuccessful") else resourceBundle.get("report.feedback.fax.sendFailed"), save = true).first

        return success
    }

    /**
     * Prints the report for sending via mail
     */
    private fun letterReports(container: NotificationExecuteData, execute: ReportIntentExecuteData, feedback: NotificationFeedback): Boolean {
        feedback.setFeedback("report.feedback.print.printing".toString())
        logger.debug("Printing report for person {}", container.notification.contact?.person?.getFullName())

        if (!generatePrintPDf(execute, execute.letterReports, container)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.print.reportGenerationFailed"), save = true).first
            return false
        }


        // checking address validity
        if (!ReportAddressValidator.approveFaxAddress(container.contactAddress)) {
            container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = true, commentary = resourceBundle.get("report.feedback.fax.notValid", container.contactAddress), save = true).first
            return false
        }

        // TODO check for successful sending
        var success = true

        // printing
        currentUserHandler.printer?.print(container.printPDFBearer, 1)

        container.notification = reportIntentService.addHistoryEntry(execute.task, container.notification, execute.diagnosisRevision, failed = !success, commentary = if (success) resourceBundle.get("report.feedback.print.printSuccessful") else resourceBundle.get("report.feedback.print.printFailed"), save = true).first

        return success
    }

    /**
     * Generates a page with all phonenumbers for calling
     */
    private fun phoneReports(containerList: List<NotificationExecuteData>, execute: ReportIntentExecuteData, feedback: NotificationFeedback): Boolean {
        return true;
    }

    private fun generateSendReport(execute: ReportIntentExecuteData, reportIntentNotificationBearers: HashMap<String, List<NotificationExecuteData>>, success: Boolean, feedback: NotificationFeedback): PDFContainer? {
        val document = printDocumentRepository
                .findByID(pathoConfig.defaultDocuments.notificationSendReport)

        if (!document.isPresent) {
            logger.debug("Printdocument not found")
            throw TemplateNotFoundException()
        }

        var tmpMap = HashMap<String, Any?>()
        tmpMap.putAll(reportIntentNotificationBearers)
        tmpMap["task"] = execute.diagnosisRevision.task
        tmpMap["diagnosisRevisions"] = Arrays.asList<DiagnosisRevision>(execute.diagnosisRevision)
        tmpMap["applyMailReport"] = execute.mailReports.applyReport
        tmpMap["applyFaxReport"] = execute.faxReports.applyReport
        tmpMap["applyLetterReport"] = execute.letterReports.applyReport
        tmpMap["applyPhoneReport"] = execute.phoneReports.applyReport
        tmpMap["reportDate"] = Instant.now()
        tmpMap["notificationSuccessful"] = success

        document.get().initialize(tmpMap)

        try {
            feedback.setFeedback("report.feedback.report.feedback.generationSendReport")
            logger.debug("Creating send report")
            val pdfReturn = pdfService.createAndAttachPDF(execute.diagnosisRevision.task, document.get(), true)
            return pdfReturn.container
        } catch (e: FileNotFoundException) {
            logger.debug("Creating send report failed")
            return null
        }
    }

    /**
     * Generates the pdf
     */
    private fun generatePrintPDf(execute: ReportIntentExecuteData, report: ReportIntentExecuteData.Reports, container: NotificationExecuteData): Boolean {

        val template = report.reportTemplate

        if (container.printPDFBearer != null) {
            return true
        } else {
            if (template == null)
                return false

            if (report.individualAddress) {
                val reportAddressField = reportIntentService.generateAddress(container.contact)
                logger.debug("Generating pdf for $reportAddressField (individual address)")
                container.printPDFBearer = getPrintPDFBearer(template as PrintDocument, reportAddressField, execute.diagnosisRevision)
            } else {
                logger.debug("Using generic pdf as attachment")
                var tmp = getGenericReport(template, execute)

                // checking if a generic pdf was found, if not setting the mail generic pdf
                if (tmp == null) {
                    logger.debug("Generating generic pdf")
                    report.report = getPrintPDFBearer(template as PrintDocument, "", execute.diagnosisRevision)
                    tmp = report.report as PrintPDFBearer
                }

                container.printPDFBearer = tmp
            }

            return true
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
    private fun getGenericReport(printDocument: PrintDocument, execute: ReportIntentExecuteData): PrintPDFBearer? {
        logger.debug("Searching for $printDocument - print ${execute.additionalReports.reportTemplate} - mail ${execute.mailReports.reportTemplate} - fax ${execute.faxReports.reportTemplate} - letter ${execute.letterReports.reportTemplate}")
        if (execute.additionalReports.reportTemplate == printDocument && execute.additionalReports.report != null)
            return execute.additionalReports.report as PrintPDFBearer

        if (execute.mailReports.reportTemplate == printDocument && execute.mailReports.report != null)
            return execute.mailReports.report as PrintPDFBearer

        if (execute.faxReports.reportTemplate == printDocument && execute.faxReports.report != null)
            return execute.faxReports.report as PrintPDFBearer

        if (execute.letterReports.reportTemplate == printDocument && execute.letterReports.report != null)
            return execute.letterReports.report as PrintPDFBearer

        return null
    }

    private fun calculateSteps(execute: ReportIntentExecuteData): Int {
        var result = 1

        if (execute.additionalReports.applyReport) result += 1
        result += execute.mailReports.receivers.size
        result += execute.faxReports.receivers.size
        result += execute.letterReports.receivers.size
        if (execute.phoneReports.applyReport) result += 1

        return result
    }
}