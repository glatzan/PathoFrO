package com.patho.main.dialog.contact

import com.patho.main.action.dialog.notification.ContactSelectDialog
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.task.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.repository.TaskRepository
import com.patho.main.service.ReportIntentService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.dialogReturn.ReloadEvent
import com.patho.main.util.status.ReportIntentStatusByDiagnosis
import com.patho.main.util.task.TaskNotFoundException
import com.patho.main.util.ui.selector.UISelector
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ContactDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val reportIntentService: ReportIntentService) :
        AbstractTaskDialog() {

    open var showRole = arrayOf<ContactRole>()
    open var selectAbleRoles = arrayOf<ContactRole>()
    open var reportIntents = listOf<ReportIntentSelector>()

    override fun initBean(task: Task): Boolean {
        selectAbleRoles = ContactRole.values()
        showRole = ContactRole.values()
        reportIntents = task.contacts.map { p -> ReportIntentSelector(p, task) }
        return super.initBean(task, Dialog.CONTACTS)
    }

    override fun update(reload: Boolean) {
        super.update()

        if (reload) {
            var optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, false, false, false, true, true)
            if (!optionalTask.isPresent)
                throw TaskNotFoundException()
        }

        reportIntents = task.contacts.map { p -> ReportIntentSelector(p, task) }
    }

    open fun removeContact(reportIntent: ReportIntent) {
        reportIntentService.removeReportIntent(task, reportIntent)
        update(true)
    }

    open fun openNewContactDialog() {

    }

    /**
     *
     */
    override fun onSubDialogReturn(event: SelectEvent) {
        if (event.`object`?.javaClass is ContactSelectDialog.SelectPhysicianReturnEvent) {

            update(true)
            return;
        }
        super.onSubDialogReturn(event)
    }

    override fun hideDialog() {
        super.hideDialog(ReloadEvent())
    }

    /**
     * Class for selecting the report intent. This class also contains a report intent status and
     * a flag if the report intent is deletable.
     */
    class ReportIntentSelector(val reportIntent: ReportIntent, task: Task) : UISelector<ReportIntent>(reportIntent) {
        /**
         * Status sorted by diagnoses
         */
        val reportIntentStatus = ReportIntentStatusByDiagnosis.ReportIntentBearer(reportIntent, task)

        /**
         * True if the report intent can be deleted
         */
        var deletable: Boolean = !SpringContextBridge.services().reportIntentService.isHistoryPresent(reportIntent)

        /**
         * Toggle var for showing details
         */
        var showDetails = false

        /**
         * True is details ar present, for older task pre version 2.0 there aren't any details
         */
        val detailsPresent = reportIntentStatus.diagnosisBearers.isNotEmpty()

        /**
         * Status for the report intents sorted by type
         */
        val reportIntentByTyp = (reportIntent.notifications.map { p -> ReportIntentTypeStatus(p) }).sortedBy { p -> p.reportIntentNotification.notificationTyp }

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
    }

}