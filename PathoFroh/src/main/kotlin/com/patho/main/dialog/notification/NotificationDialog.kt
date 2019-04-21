package com.patho.main.dialog.notification

import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractTabTaskDialog
import com.patho.main.dialog.print.PrintDialog
import com.patho.main.dialog.print.documentUi.AbstractDocumentUi
import com.patho.main.dialog.print.documentUi.DiagnosisReportUi
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.repository.MailRepository
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.template.DocumentToken
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.selectors.ContactSelector
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.print.LoadedPrintPDFBearer
import com.patho.main.util.status.ReportIntentStatusByDiagnosis
import com.patho.main.util.status.ReportIntentStatusNotification
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

@Component()
@Scope(value = "session")
class NotificationDialog @Autowired constructor(
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val mailRepository: MailRepository,
        private val printDialog: PrintDialog) : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

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
     * Sets the disable status of all tabs
     */
    private fun disableTabs(generalTab: Boolean, mailTab: Boolean, faxTab: Boolean, letterTab: Boolean, phoneTab: Boolean,
                            sendTab: Boolean) {
        this.generalTab.disabled = generalTab
        this.mailTab.disabled = mailTab
        this.faxTab.disabled = faxTab
        this.letterTab.disabled = letterTab
        this.phoneTab.disabled = phoneTab
        this.sendTab.disabled = sendTab
    }

    /**
     * Open the print dialog in select mode in order to select a report for the given contact
     */
    fun openSelectPDFDialog(task: Task, contact: ReportIntent) {

        val printDocuments = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

        //
        val selectors = ArrayList<ContactSelector>()
        selectors.add(ContactSelector(contact))
        selectors.add(ContactSelector(task,
                Person(resourceBundle.get("dialog.printDialog.individualAddress"), Contact()), ContactRole.NONE))
        selectors[0].isSelected = true

        // getting ui objects
        val printDocumentUIs = AbstractDocumentUi.factory(printDocuments)

        for (documentUi in printDocumentUIs) {
            (documentUi as DiagnosisReportUi).initialize(task, selectors)
            documentUi.sharedData.isRenderSelectedContact = true
            documentUi.sharedData.isUpdatePdfOnEverySettingChange = true
            documentUi.sharedData.isSingleSelect = true
        }

        printDialog.initAndPrepareBean(task, printDocumentUIs, printDocumentUIs[0])
                .selectMode(true)
    }

    /**
     * Default return function, handel the pdf select return
     */
    override fun onSubDialogReturn(event: SelectEvent) {
        val container = event.component.attributes["container"]
        val returnObject = event.`object`

        if (returnObject is LoadedPrintPDFBearer && container is ReportIntentStatusNotification.ReportIntentNotificationBearer) {
            logger.debug("Setting custom pdf for container $container.reportIntent.person?.getFullName()}")
            container.pdf = returnObject.pdfContainer
            return;
        }

        super.onSubDialogReturn(event)
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

        /**
         * Contais a list of contacts with the specified notification type
         */
        open lateinit var reportIntentStatus: ReportIntentStatusNotification

        override fun updateData() {
            reportIntentStatus.update(task)
        }

        /**
         * Initializes the useNotification value and calls the parent method
         */
        override fun initTab(force: Boolean): Boolean {
            useNotification = reportIntentStatus.size > 0
            return super.initTab(force)
        }
    }

    /**
     * General Tab
     */
    open inner class GeneralTab : NotificationTab(
            "GeneralTab",
            "dialog.notificationDialog.general.navigationText",
            "generalTab",
            "include/general.xhtml") {

        /**
         * List of all diagnosis revisions with their status
         */
        open var diagnosisRevisions: List<ReportIntentStatusByDiagnosis.DiagnosisBearer> = mutableListOf()

        /**
         * Selected diagnosis for that the notification will be performed
         */
        open var selectDiagnosisRevision: ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        /**
         * Diagnosis bearer for displaying details in the datatable overlay panel
         */
        open var viewDiagnosisRevisionDetails: ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        /**
         * Count of additional prints
         */
        open var printCount: Int = 0

        /**
         * Selected diagnosis is not approved
         */
        open var selectedDiagnosisNotApproved: Boolean = false

        /**
         * This tab should always be used
         */
        override var useNotification: Boolean = true

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing general data...")
            diagnosisRevisions = ReportIntentStatusByDiagnosis(task).diagnosisBearer
            selectDiagnosisRevision = diagnosisRevisions.firstOrNull { p -> p.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }

            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                    PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultPrintDocument).orElse(null)
            printCount = 2

            onDiagnosisSelect();

            return super.initTab(force)
        }

        override fun updateData() {}

        /**
         * Method is called on diagnosis change
         */
        open fun onDiagnosisSelect() {
            // sets a not approved warning
            selectedDiagnosisNotApproved = selectDiagnosisRevision?.diagnosisRevision?.notificationStatus != NotificationStatus.NOTIFICATION_PENDING

            // disable tabs if no diagnosis is selected.
            if (selectDiagnosisRevision == null)
                disableTabs(false, true, true, true, true, true)
            else
                disableTabs(false, false, false, false, false, false)
        }

    }

    open inner class MailTab : ContactTab(
            "MailTab",
            "dialog.notificationDialog.mail.navigationText",
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
            templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                    PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

            println(printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail))
            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)
            println(selectedTemplate)

            mailTemplate = mailRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)

            mailTemplate?.initialize(
                    DocumentToken("patient", task.patient),
                    DocumentToken("task", task),
                    DocumentToken("contact", null))

            reportIntentStatus = ReportIntentStatusNotification(task, notificationTyp)

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
            templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                    PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultFaxDocument).orElse(null)

            sendFax = true

            printFax = false

            reportIntentStatus = ReportIntentStatusNotification(task, notificationTyp)

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
            templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                    PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultLetterDocument).orElse(null)

            printLetter = true

            reportIntentStatus = ReportIntentStatusNotification(task, notificationTyp)

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

            reportIntentStatus = ReportIntentStatusNotification(task, notificationTyp)

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