package com.patho.main.dialog.contact

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.Physician
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.repository.AssociatedContactRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.service.PhysicianService
import com.patho.main.service.ReportIntentService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.dialog.event.PhysicianSelectEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.DuplicatedReportIntentException
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.status.reportIntent.ReportIntentBearer
import com.patho.main.util.ui.selector.UISelector
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ContactDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val reportIntentService: ReportIntentService,
        private val physicianService: PhysicianService,
        private val contactAddDialog: ContactAddDialog,
        private val associatedContactRepository: AssociatedContactRepository) :
        AbstractTaskDialog(Dialog.CONTACTS) {

    open var showRole = arrayOf<ContactRole>()
    open var selectAbleRoles = arrayOf<ContactRole>()
    open var reportIntents = listOf<ReportIntentSelector>()

    open var selectedReportIntent: ReportIntentSelector? = null

    open var selectableRoles = ContactRole.values()

    override fun initBean(task: Task): Boolean {
        super.initBean(task)
        selectAbleRoles = ContactRole.values()
        showRole = ContactRole.values()
        update(true)
        return true
    }

    override fun update(reload: Boolean) {

        if (reload) {
            var optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, false, false, false, true, true)
            if (!optionalTask.isPresent)
                throw TaskNotFoundException()

            task = optionalTask.get()
        }

        reportIntents = task.contacts.mapIndexed { index, p -> ReportIntentSelector(p, task, index.toLong()) }
    }

    open fun removeContact(reportIntent: ReportIntent) {
        reportIntentService.removeReportIntent(task, reportIntent)
        update(true)
        MessageHandler.sendGrowlMessagesAsResource("growl.contact.removed.headline", "growl.contact.removed.success", reportIntent.person?.getFullName())

    }

    open fun openNewContactDialog() {
        contactAddDialog.initAndPrepareBean(task, manuallySelectRole = true, addContactAsRole = ContactRole.OTHER_PHYSICIAN)
    }

    open fun onRoleChange() {
        val reportIntent = selectedReportIntent?.reportIntent
        if (reportIntent != null) {
            associatedContactRepository.save(reportIntent,
                    resourceBundle.get("log.contact.roleChange", reportIntent.task, reportIntent.person?.getFullName(), reportIntent.role),
                    reportIntent.task?.patient)
            update(true)
        }
    }

    open fun addReportIntentNotification(reportIntent: ReportIntent?, notificationTyp: NotificationTyp) {

        val task = reportIntent?.task

        if (reportIntent != null && task != null) {
            reportIntent.active = true
            reportIntentService.addReportIntentNotification(task, reportIntent, notificationTyp, save = true)
            update(true)
        }
    }

    open fun removeReportIntentNotification(reportIntent: ReportIntent?, reportIntentNotification: ReportIntentNotification?) {

        val task = reportIntent?.task

        if (reportIntent != null && task != null && reportIntentNotification != null) {
            reportIntentService.removeReportIntentNotification(task, reportIntent, reportIntentNotification)
            update(true)
        }
    }

    open fun toggleReportIntentNotificationActiveStatus(reportIntent: ReportIntent?, reportIntentNotification: ReportIntentNotification?, enabled: Boolean) {
        val task = reportIntent?.task

        if (reportIntent != null && task != null && reportIntentNotification != null) {
            reportIntentService.toggleReportIntentNotificationActiveStatus(task, reportIntent, reportIntentNotification, enabled)
            update(true)
        }
    }

    /**
     *
     */
    override fun onSubDialogReturn(event: SelectEvent) {
        val tmp = event.`object`
        if (tmp is PhysicianSelectEvent) {
            logger.debug("Return from contactAddDialog, reloading!")
            addPhysician(tmp.obj, tmp.role)
            update(true)
            return;
        }

        super.onSubDialogReturn(event)
    }

    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

    private fun addPhysician(physician: Physician, role: ContactRole) {
        try {
            // adding physician
            reportIntentService.addReportIntent(task, physician.person, role, true, true, true)
            // increment counter
            physicianService.incrementPhysicianPriorityCounter(physician.person)
        } catch (e: DuplicatedReportIntentException) {
            logger.debug("Duplicated ReportIntent")
            MessageHandler.sendGrowlMessagesAsResource(e)
        }
    }

    /**
     * Class for selecting the report intent. This class also contains a report intent status and
     * a flag if the report intent is deletable.
     */
    class ReportIntentSelector(val reportIntent: ReportIntent, task: Task, override var id: Long) : UISelector<ReportIntent>(reportIntent) {
        /**
         * Status sorted by diagnoses
         */
        val reportIntentStatus = ReportIntentBearer(reportIntent, task)

        /**
         * True if the report intent can be deleted
         */
        var deletable: Boolean = !SpringContextBridge.services().reportIntentService.isHistoryPresent(reportIntent)

        /**
         * Toggle var for showing details
         */
        var showDetails = false

        /**
         * True is details ar present, for older task pre version 2.0 there aren'special.pdfOrganizerDialog any details
         */
        val detailsPresent = reportIntentStatus.diagnosisBearers.isNotEmpty()

        /**
         * Status for the report intents sorted by type
         */
        val reportIntentByTyp = (reportIntent.notifications.map { p -> ReportIntentTypeStatus(p) }).sortedBy { p -> p.reportIntentNotification.notificationTyp }

        /**
         * Status for  buttons in overlay panel
         */
        val buttonStatus = listOf<ReportIntentGuiButtonStatus>(
                ReportIntentGuiButtonStatus(reportIntentByTyp.firstOrNull { p -> p.reportIntentNotification.notificationTyp == NotificationTyp.EMAIL }, NotificationTyp.EMAIL),
                ReportIntentGuiButtonStatus(reportIntentByTyp.firstOrNull { p -> p.reportIntentNotification.notificationTyp == NotificationTyp.LETTER }, NotificationTyp.LETTER),
                ReportIntentGuiButtonStatus(reportIntentByTyp.firstOrNull { p -> p.reportIntentNotification.notificationTyp == NotificationTyp.FAX }, NotificationTyp.FAX),
                ReportIntentGuiButtonStatus(reportIntentByTyp.firstOrNull { p -> p.reportIntentNotification.notificationTyp == NotificationTyp.PHONE }, NotificationTyp.PHONE))

        /**
         * Toggles visibility of the details
         */
        fun toggleDetails() {
            showDetails = !showDetails
        }

        /**
         * Class for a reportIntentNotification an its status
         */
        class ReportIntentTypeStatus(val reportIntentNotification: ReportIntentNotification) {
            /**
             * If true a report notification was performed
             */
            val isHistory = SpringContextBridge.services().reportIntentService.isHistoryPresent(reportIntentNotification)

            /**
             * Status if a notification was performed
             */
            val reportStatus = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntentNotification)
        }

        /**
         * Class for rendering editing options in the gui for an notification type
         */
        class ReportIntentGuiButtonStatus(val reportIntentTypeStatus: ReportIntentTypeStatus?, val notificationTyp: NotificationTyp) {
            private val isPresent = reportIntentTypeStatus != null
            private val isHistory = reportIntentTypeStatus?.isHistory ?: false

            val reportIntentNotification = reportIntentTypeStatus?.reportIntentNotification

            val isActive = reportIntentTypeStatus?.reportIntentNotification?.active ?: false

            /**
             * True if add button should be rendered
             */
            val renderAddButton
                get() = !isPresent

            /**
             * True if remove button should be rendered
             */
            val renderRemoveButton
                get() = isPresent && !isHistory

            /**
             * True if enable button should be rendered
             */
            val renderEnableButton
                get() = isPresent && !isActive && isHistory

            /**
             * True if disable button should be rendered
             */
            val renderDisableButton
                get() = isPresent && isActive && isHistory

        }
    }

}