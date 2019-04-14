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
import com.patho.main.util.notification.NotificationContainer
import org.springframework.beans.factory.annotation.Autowired
import java.util.ArrayList

class NotificationDialog @Autowired constructor(
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val mailRepository: MailRepository) : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

    val generalTab: GeneralTab = GeneralTab()

    init {
        tabs = arrayOf(generalTab)
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
        var useNotification: Boolean = false

        /**
         * List of templates to select from
         */
        var templates: List<PrintDocument> = listOf()
            set(value) {
                field = value
                templatesTransformer = DefaultTransformer(value)
            }

        /**
         * Transformer for gui
         */
        var templatesTransformer: DefaultTransformer<PrintDocument> = DefaultTransformer(templates)

        /**
         * The selected template
         */
        var selectedTemplate: PrintDocument? = null
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
        var individualAddresses: Boolean = false

        override fun updateData() {
            val newContainers = AssociatedContactService.getNotificationListForType(task,
                    notificationType, generalTab.isCompleteNotification())

            val foundContainer = ArrayList<NotificationContainer>()

            // adding new, saving found one
            for (notificationContainer in newContainers) {
                if (getContainer().stream().anyMatch({ p -> p == notificationContainer })) {
                    // TODO update old conatienr
                } else {
                    getContainer().add(notificationContainer)
                }
                foundContainer.add(notificationContainer)
            }

            // removing old container
            newContainers.removeAll(foundContainer)
            getContainer().removeAll(newContainers)

            getContainer().stream().sorted({ p1, p2 ->
                if (p1.isFaildPreviously() == p2.isFaildPreviously())
                    return@getContainer ().stream().sorted 0
                else if (p1.isFaildPreviously())
                return@getContainer ().stream().sorted 1
                else
                return@getContainer ().stream().sorted - 1
            })
        }
    }

    inner class GeneralTab : NotificationTab(
            "GeneralTab",
            "dialog.notification.tab.general",
            "generalTab",
            "include/general.xhtml") {

        private var diagnosisRevisions: List<DiagnosisRevision> = listOf()

        private var selectDiagnosisRevision: DiagnosisRevision? = null

        private var printCount: Int = 0

        /**
         * No diagnosis is selected, selection by user is required
         */
        private var selectDiagnosisManually: Boolean = false

        /**
         * Selected diagnosis is not approved
         */
        private var selectedDiagnosisNotApprovedy: Boolean = false

        /**
         * If true all contacts will be notified, even contacts that are not refreshed
         */
        private var completeNotification: Boolean = false

        override fun initTab(): Boolean {
            logger.debug("Initializing general data...")
            diagnosisRevisions = task.diagnosisRevisions.toList()
            selectDiagnosisRevision = diagnosisRevisions.firstOrNull { p -> p.notificationStatus == DiagnosisRevision.NotificationStatus.NOTIFICATION_PENDING }
            useNotification = true

            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultPrintDocument).orElse(null)
            printCount = 2

            return super.initTab()
        }

        override fun updateData() {}

    }

    inner class MailTab : ContactTab(
            "MailTab",
            "dialog.notification.tab.general",
            "generalTab",
            "include/general.xhtml",
            NotificationTyp.EMAIL) {

        /**
         * Template of the email which is send to the receivers
         */
        var mailTemplate: MailTemplate? = null

        override fun initTab(): Boolean {
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

            useNotification = container.size > 0

            return super.initTab()
        }
    }
}