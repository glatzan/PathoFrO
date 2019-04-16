package com.patho.main.dialog.notification

import com.patho.main.common.Dialog
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractTabTaskDialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.repository.MailRepository
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.template.InitializeToken
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.status.ReportIntentStatusByDiagnosis
import com.patho.main.util.status.ReportIntentStatusByType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
class NotificationDialog @Autowired constructor(
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val mailRepository: MailRepository) : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

    val generalTab: GeneralTab = GeneralTab()
    val mailTab: MailTab = MailTab()
    val faxTab: FaxTab = FaxTab()
    val letterTab: LetterTab = LetterTab()
    val phoneTab: PhoneTab = PhoneTab()
    val sendTab: SendTab = SendTab()

    init {
        tabs = arrayOf(generalTab, mailTab, faxTab, letterTab, phoneTab, sendTab)
    }

    fun initBean(task: Task): Boolean {
        return super.initBean(task, true, generalTab)
    }

    /**
     * Class for notification tabs
     */
    abstract inner class NotificationTab(tabName: String,
                                         name: String,
                                         viewID: String,
                                         centerInclude: String) : AbstractTab(tabName, name, viewID, centerInclude) {

        /**
         * True if notification method should be used
         */
        open var useNotification: Boolean = false

        /**
         * List of templates to select from
         */
        open var templates: List<PrintDocument> = listOf()
            set(value) {
                field = value
                templatesTransformer = DefaultTransformer(value)
            }

        /**
         * Transformer for gui
         */
        open var templatesTransformer: DefaultTransformer<PrintDocument> = DefaultTransformer(templates)

        /**
         * The selected template
         */
        open var selectedTemplate: PrintDocument? = null
    }

    /**
     *
     */
    abstract inner class ContactTab(tabName: String,
                                    name: String,
                                    viewID: String,
                                    centerInclude: String,
                                    val notificationTyp: NotificationTyp) : NotificationTab(tabName, name, viewID, centerInclude) {
        /**
         * If true individual addresses will be used in the reports
         */
        open var individualAddresses: Boolean = false

        open var reportIntentStatus: List<ReportIntentStatusByType> = listOf()

        override fun updateData() {
//            val newContainers = AssociatedContactService.getNotificationListForType(task,
//                    notificationType, generalTab.isCompleteNotification())
//
//            val foundContainer = ArrayList<NotificationContainer>()
//
//            // adding new, saving found one
//            for (notificationContainer in newContainers) {
//                if (getContainer().stream().anyMatch({ p -> p == notificationContainer })) {
//                    // TODO update old conatienr
//                } else {
//                    getContainer().add(notificationContainer)
//                }
//                foundContainer.add(notificationContainer)
//            }
//
//            // removing old container
//            newContainers.removeAll(foundContainer)
//            getContainer().removeAll(newContainers)
//
//            getContainer().stream().sorted({ p1, p2 ->
//                if (p1.isFaildPreviously() == p2.isFaildPreviously())
//                    return@getContainer ().stream().sorted 0
//                else if (p1.isFaildPreviously())
//                return@getContainer ().stream().sorted 1
//                else
//                return@getContainer ().stream().sorted - 1
//            })
        }
    }

    open inner class GeneralTab : NotificationTab(
            "GeneralTab",
            "dialog.notification.tab.general",
            "generalTab",
            "include/general.xhtml") {

        open var diagnosisRevisions: List<ReportIntentStatusByDiagnosis.DiagnosisBearer> = mutableListOf()

        open var selectDiagnosisRevision: ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        open var viewDiagnosisRevisionDetails : ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        open var printCount: Int = 0

        /**
         * No diagnosis is selected, selection by user is required
         */
        open var selectDiagnosisManually: Boolean = false

        /**
         * Selected diagnosis is not approved
         */
        open var selectedDiagnosisNotApprovedy: Boolean = false

        /**
         * If true all contacts will be notified, even contacts that are not refreshed
         */
        open var completeNotification: Boolean = false

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing general data...")
            diagnosisRevisions = ReportIntentStatusByDiagnosis(task).diagnosisBearer
            selectDiagnosisRevision = diagnosisRevisions.firstOrNull { p -> p.diagnosisRevision.notificationStatus == DiagnosisRevision.NotificationStatus.NOTIFICATION_PENDING }
            useNotification = true

            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultPrintDocument).orElse(null)
            printCount = 2

            return super.initTab(force)
        }

        override fun updateData() {}

    }

    open inner class MailTab : ContactTab(
            "MailTab",
            "dialog.notification.tab.mail",
            "mailTab",
            "include/mail.xhtml",
            NotificationTyp.EMAIL) {

        /**
         * Template of the email which is send to the receivers
         */
        open var mailTemplate: MailTemplate? = null

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing mail data...")
            individualAddresses = false
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)

            mailTemplate = mailRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)

            mailTemplate?.initilize(
                    InitializeToken("patient", task.patient),
                    InitializeToken("task", task),
                    InitializeToken("contact", null))

            updateData()

//            useNotification = container.size > 0

            return super.initTab(force)
        }
    }

    open inner class FaxTab : ContactTab(
            "FaxTab",
            "dialog.notification.tab.fax",
            "faxTab",
            "include/fax.xhtml",
            NotificationTyp.FAX) {

        /**
         * Sending faxes via pathofroh
         */
        open var sendFax = true

        /**
         * Printin faxes
         */
        open var printFax = false

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing fax data...")

            individualAddresses = true
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultFaxDocument).orElse(null)

            sendFax = true

            printFax = false

            updateData()

//            useNotification = container.size > 0

            return super.initTab(force)
        }
    }

    open inner class LetterTab : ContactTab(
            "LetterTab",
            "dialog.notification.tab.letter",
            "letterTab",
            "include/letter.xhtml",
            NotificationTyp.LETTER) {

        /**
         * If true the letters will be pritned
         */
        open var printLetter = true

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing letter data...")

            individualAddresses = true
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultLetterDocument).orElse(null)

            printLetter = true

            updateData()

//            useNotification = container.size > 0

            return super.initTab(force)
        }
    }

    open inner class PhoneTab : ContactTab(
            "PhoneTab",
            "dialog.notification.tab.phone",
            "phoneTab",
            "include/phone.xhtml",
            NotificationTyp.PHONE) {

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing phone data...")

            updateData()

//            useNotification = container.size > 0

            return super.initTab(force)
        }
    }

    open inner class SendTab : NotificationTab(
            "SendTab",
            "dialog.notification.tab.send",
            "sendTab",
            "include/send.xhtml") {

    }


}