package com.patho.main.dialog.notification

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.common.GuiCommands
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractTabTaskDialog
import com.patho.main.dialog.print.PrintDialog
import com.patho.main.dialog.print.documentUi.DiagnosisReportUi
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.repository.miscellaneous.DocumentRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.ReportIntentService
import com.patho.main.template.DocumentToken
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.NotificationPerformedEvent
import com.patho.main.util.dialog.event.NotificationPhaseExitEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.print.PrintPDFBearer
import com.patho.main.util.report.MailNotificationExecuteData
import com.patho.main.util.report.NotificationExecuteData
import com.patho.main.util.report.ReportIntentExecuteData
import com.patho.main.util.report.ui.ReportIntentNotificationUIContainer
import com.patho.main.util.report.ui.ReportIntentUIContainer
import com.patho.main.util.status.ExtendedNotificationStatus
import com.patho.main.util.status.IExtendedDatatableNotificationStatusByDiagnosis
import com.patho.main.util.ui.backend.CheckBoxStatus
import com.patho.main.util.ui.backend.CommandButtonStatus
import com.patho.main.util.ui.selector.ReportIntentSelector
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
        private val documentRepository: DocumentRepository,
        private val printDialog: PrintDialog,
        private val taskRepository: TaskRepository,
        private val reportIntentService: ReportIntentService,
        private val performNotificationDialog: PerformNotificationDialog) : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

    val generalTab: GeneralTab = GeneralTab()
    val mailTab: MailTab = MailTab()
    val faxTab: FaxTab = FaxTab()
    val letterTab: LetterTab = LetterTab()
    val phoneTab: PhoneTab = PhoneTab()
    val sendTab: SendTab = SendTab()

    init {
        tabs = arrayOf(generalTab, mailTab, faxTab, letterTab, phoneTab, sendTab)
    }

    override fun initBean(task: Task): Boolean {
        logger.debug("Initializing notification dialog")
        return super.initBean(task, true, generalTab)
    }

    /**
     * Reloads task data and updates the tabs
     */
    override fun update() {
        update(true)
    }

    /**
     * Reloads the task if dialog is closed
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

    /**
     * Reloads task data and updates the tabs
     */
    override fun update(reload: Boolean) {
        logger.debug("Updating content, reload task $reload")
        if (reload) {
            task = taskRepository.findByID(task.id, false, true, false, true, true)
        }

        tabs.forEach { it.updateData() }
    }

    /**
     * Dis or enabled the given reportIntentNotification
     */
    open fun disableNotification(reportIntentNotification: ReportIntentNotification, active: Boolean) {
        reportIntentService.toggleReportIntentNotificationActiveStatus(task, reportIntentNotification, active)
        update(true)
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

        val printDocuments = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

        //
        val selectors = ArrayList<ReportIntentSelector>()
        selectors.add(ReportIntentSelector(contact))
        selectors.add(ReportIntentSelector(task,
                Person(resourceBundle.get("dialog.printDialog.individualAddress"), Contact()), ContactRole.NONE))

        selectors[0].selected = true

        // getting ui objects
        val printDocumentUIs = PrintDocumentRepository.factory(printDocuments)

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

        if (returnObject is PrintPDFBearer && container is ReportIntentNotificationUIContainer) {
            logger.debug("Setting custom pdf for container $container.reportIntent.person?.getFullName()}")
            container.printPDF = returnObject
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
                                         centerInclude: String) : AbstractTab(tabName, name, viewID, centerInclude), IContactReturnUpdate {

        /**
         * True if notification method should be used
         */
        open var useNotification: CheckBoxStatus = CheckBoxStatus()

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

        override fun onContactDialogReturn(event: SelectEvent) {
            update()
        }
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
        open lateinit var reportIntentStatus: ReportIntentUIContainer

        override fun updateData() {
            logger.debug("Updating $tabName")
            reportIntentStatus.update(task, generalTab.selectedDiagnosisRevisionStatus?.diagnosisRevision)

            // only updating notification status if no manually change had been made
            if (!useNotification.manuallyAltered)
                useNotification.value = reportIntentStatus.hasActiveNotifications
        }

        /**
         * Initializes the useNotification value and calls the parent method
         */
        override fun initTab(force: Boolean): Boolean {
            reportIntentStatus = ReportIntentUIContainer(task, notificationTyp, this, generalTab.selectedDiagnosisRevisionStatus?.diagnosisRevision)
            useNotification.value = reportIntentStatus.hasActiveNotifications
            return super.initTab(force)
        }

        /**
         * Updates the tab usages on dialog return
         */
        override fun onContactDialogReturn(event: SelectEvent) {
            logger.debug("Contact dialog return")
            super.onContactDialogReturn(event)
            useNotification.value = reportIntentStatus.hasActiveNotifications
        }

        /**
         * This function is triggered when the user changes the perform notification checkbox for a reportIntent
         */
        fun onPerformNotificationChange() {
            useNotification.value = reportIntentStatus.hasActiveNotifications
        }
    }

    /**
     * General Tab
     */
    open inner class GeneralTab : NotificationTab(
            "GeneralTab",
            "dialog.notificationDialog.general.navigationText",
            "generalTab",
            "_general.xhtml"), IExtendedDatatableNotificationStatusByDiagnosis {


        /**
         * List of all reportIntent revisions with their status
         */
        override lateinit var diagnosisNotificationStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus

        /**
         * Selected reportIntent for that the notification will be performed
         */
        override var selectedDiagnosisRevisionStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus.DiagnosisRevisionStatus? = null

        /**
         * Diagnosis bearer for displaying details in the datatable overlay panel
         */
        override var displayDiagnosisRevisionStatus: ExtendedNotificationStatus.DiagnosisNotificationStatus.DiagnosisRevisionStatus? = null

        /**
         * Count of additional prints
         */
        open var printCount: Int = 0

        /**
         * Selected reportIntent is not approved
         */
        open var selectedDiagnosisNotApproved: Boolean = false

        /**
         * True if a reportIntent is selected
         */
        open val diagnosisSelected
            get() = selectedDiagnosisRevisionStatus != null

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing general data...")
            // This tab should always be used
            useNotification.value = true

            diagnosisNotificationStatus = ExtendedNotificationStatus(task).diagnosisNotificationStatus
            selectedDiagnosisRevisionStatus = diagnosisNotificationStatus.diagnoses.firstOrNull { p -> p.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }
            displayDiagnosisRevisionStatus = null

            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                    PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultPrintDocument).orElse(null)
            printCount = 2

            onDiagnosisSelection();

            return super.initTab(force)
        }

        override fun updateData() {}

        /**
         * Method is called on reportIntent change
         */
        override fun onDiagnosisSelection() {
            // sets a not approved warning
            selectedDiagnosisNotApproved = selectedDiagnosisRevisionStatus?.diagnosisRevision?.notificationStatus != NotificationStatus.NOTIFICATION_PENDING

            // disable tabs if no reportIntent is selected.
            if (selectedDiagnosisRevisionStatus == null)
                disableTabs(false, true, true, true, true, true)
            else
                disableTabs(false, false, false, false, false, false)
        }

    }

    open inner class MailTab : ContactTab(
            "MailTab",
            "dialog.notificationDialog.mail.navigationText",
            "mailTab",
            "_mail.xhtml",
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

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmailDocument).orElse(null)

            mailTemplate = documentRepository.findByID<MailTemplate>(pathoConfig.defaultDocuments.notificationDefaultEmail)

            mailTemplate?.initialize(
                    DocumentToken("patient", task.patient),
                    DocumentToken("task", task),
                    DocumentToken("contact", null))

            val result = super.initTab(force)
            updateData()
            return result
        }
    }

    open inner class FaxTab : ContactTab(
            "FaxTab",
            "dialog.notificationDialog.fax.navigationText",
            "faxTab",
            "_fax.xhtml",
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

            val result = super.initTab(force)
            updateData()
            return result
        }
    }

    open inner class LetterTab : ContactTab(
            "LetterTab",
            "dialog.notificationDialog.letter.navigationText",
            "letterTab",
            "_letter.xhtml",
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

            val result = super.initTab(force)
            updateData()
            return result
        }
    }

    open inner class PhoneTab : ContactTab(
            "PhoneTab",
            "dialog.notificationDialog.phone.navigationText",
            "phoneTab",
            "_phone.xhtml",
            NotificationTyp.PHONE) {

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing phone data...")

            val result = super.initTab(force)
            updateData()
            return result
        }
    }

    open inner class SendTab : NotificationTab(
            "SendTab",
            "dialog.notification.tab.send",
            "sendTab",
            "_send.xhtml") {

        /**
         * Only active notifications
         */
        open var activeNotifications: MutableList<ReportIntentSummaryEntry> = mutableListOf()


        open lateinit var performButton: CommandButtonStatus

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing letter data...")

            performButton = object : CommandButtonStatus() {
                override fun onClick() {
                    this.isDisabled = true
                    disableTabs(true, true, true, true, true, true)
                    performNotifications()
                }
            }

            updateData()

            return super.initTab(force)
        }

        override fun updateData() {
            activeNotifications.clear()
            if (mailTab.useNotification.value) activeNotifications.addAll(mailTab.reportIntentStatus.activeNotifications.map { ReportIntentSummaryEntry(NotificationTyp.EMAIL, it) })
            if (faxTab.useNotification.value) activeNotifications.addAll(faxTab.reportIntentStatus.activeNotifications.map { ReportIntentSummaryEntry(NotificationTyp.FAX, it) })
            if (letterTab.useNotification.value) activeNotifications.addAll(letterTab.reportIntentStatus.activeNotifications.map { ReportIntentSummaryEntry(NotificationTyp.LETTER, it) })
            if (phoneTab.useNotification.value) activeNotifications.addAll(phoneTab.reportIntentStatus.activeNotifications.map { ReportIntentSummaryEntry(NotificationTyp.PHONE, it) })

            performButton.set(true, true, activeNotifications.isEmpty())
        }

        inner class ReportIntentSummaryEntry(val notificationTyp: NotificationTyp,
                                             var notificationBearer: ReportIntentNotificationUIContainer)

        /**
         * Starting perform notification dialog
         */
        open fun performNotifications() {
            val diagnosis = generalTab.selectedDiagnosisRevisionStatus?.diagnosisRevision
            if (diagnosis != null) {

                var report = ReportIntentExecuteData(task, diagnosis)
                // general
                report.additionalReports.applyReport = generalTab.useNotification.value
                report.additionalReports.printAdditionalReportsCount = generalTab.printCount
                report.additionalReports.reportTemplate = generalTab.selectedTemplate

                // mail
                report.mailReports.applyReport = mailTab.useNotification.value
                report.mailReports.individualAddress = mailTab.individualAddresses
                report.mailReports.mailTemplate = mailTab.mailTemplate
                report.mailReports.reportTemplate = mailTab.selectedTemplate

                if (mailTab.useNotification.value) {
                    val mailTemplate = mailTab.mailTemplate
                    if (mailTemplate == null) {
                        MessageHandler.sendGrowlMessagesAsResource("growl.headline.error", "growl.notification.noMailTemplateSelected")
                        return;
                    } else {
                        report.mailReports.receivers = mailTab.reportIntentStatus.activeNotifications.map { MailNotificationExecuteData(it.reportIntent, it.reportIntentNotification, mailTemplate, true, it.contactAddress, it.printPDF) }
                    }
                }

                // fax
                report.faxReports.applyReport = faxTab.useNotification.value
                report.faxReports.individualAddress = faxTab.individualAddresses
                report.faxReports.sendFax = faxTab.sendFax
                report.faxReports.printFax = faxTab.printFax
                report.faxReports.reportTemplate = faxTab.selectedTemplate

                if (faxTab.useNotification.value)
                    report.faxReports.receivers = faxTab.reportIntentStatus.activeNotifications.map { NotificationExecuteData(it.reportIntent, it.reportIntentNotification, true, it.contactAddress, it.printPDF) }

                // letter
                report.letterReports.applyReport = letterTab.useNotification.value
                report.letterReports.individualAddress = letterTab.individualAddresses
                report.letterReports.reportTemplate = letterTab.selectedTemplate

                if (letterTab.useNotification.value)
                    report.letterReports.receivers = letterTab.reportIntentStatus.activeNotifications.map { NotificationExecuteData(it.reportIntent, it.reportIntentNotification, true, it.contactAddress, it.printPDF) }

                // phone
                report.phoneReports.applyReport = phoneTab.useNotification.value
                if (phoneTab.useNotification.value)
                    report.phoneReports.receivers = phoneTab.reportIntentStatus.activeNotifications.map { NotificationExecuteData(it.reportIntent, it.reportIntentNotification, true, it.contactAddress, it.printPDF) }

                performNotificationDialog.initAndPrepareBean(task, report).startNotification(true)

            } else {
                MessageHandler.sendGrowlMessagesAsResource("growl.headline.error", "growl.notification.notDiagnosisSelected")
            }
        }

        /**
         * Default return function for sub dialogs
         */
        open fun onSubDialogReturn(event: SelectEvent) {
            println(event)
            val obj = event.getObject()
            // return event of send dialog
            if (obj is NotificationPerformedEvent) {
                // end phase, show end phase dialog
                if (obj.endPhase) {
                    MessageHandler.executeScript(GuiCommands.OPEN_END_STAINING_PHASE_FROM_NOTIFICATION_DIALOG)
                } else {
                    MessageHandler.sendGrowlMessagesAsResource("growl.notification.endWithoutPhaseEnd.headline", "growl.notification.endWithoutPhaseEnd.text")
                    hideDialog(TaskReloadEvent())
                }
            } else if (obj is NotificationPhaseExitEvent) {
                if (obj.startTaskArchival)
                    MessageHandler.executeScript(GuiCommands.OPEN_ARCHIVE_TASK_DIALOG_FROM_NOTIFICATION_DIALOG)
                else
                    hideDialog(TaskReloadEvent())
            } else
                hideDialog(event.`object` ?: TaskReloadEvent())
        }
    }

}