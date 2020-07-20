package com.patho.main.dialog.phase

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.dialog.event.NotificationPhaseExitEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.DiagnosisRevisionNotFoundException
import com.patho.main.util.status.ExtendedNotificationStatus
import com.patho.main.util.status.IExtendedDatatableNotificationStatusByDiagnosis
import com.patho.main.util.ui.backend.CheckBoxStatus
import com.patho.main.util.ui.backend.CommandButtonStatus
import com.patho.main.util.ui.backend.LabelStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for exiting the notification phase
 */
@Component()
@Scope(value = "session")
open class NotificationPhaseExitDialog @Autowired constructor(
        private val phaseService: WorkPhaseService,
        private val worklistHandler: WorklistHandler,
        private val taskRepository: TaskRepository,
        private val diagnosisService: DiagnosisService,
        private val workPhaseHandler: WorkPhaseHandler) : AbstractPhaseExitDialog(Dialog.NOTIFICATION_PHASE_EXIT), IExtendedDatatableNotificationStatusByDiagnosis {

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

    open lateinit var competeAllNotifications: CheckBoxStatus

    open val isDisableDiagnosisSelection
        get() = competeAllNotifications.value

    open lateinit var archiveTask: CheckBoxStatus

    /**
     * Status for label "select diagnosis text", if diagnosis is null and complete All notifications is not set
     */
    open var selectDiagnosisLabel = object : LabelStatus() {
        override var isRendered: Boolean
            get() = selectedDiagnosisRevisionStatus == null && !competeAllNotifications.value
            @Suppress("UNUSED_PARAMETER")
            set(value) {
            }
    }

    /**
     * Status for warning label "notification for diagnosis was not performed"
     */
    open var notificationNotPerformed = object : LabelStatus() {
        override var isRendered: Boolean
            get() = selectedDiagnosisRevisionStatus != null && selectedDiagnosisRevisionStatus?.diagnosisRevision?.notificationStatus != NotificationStatus.NOTIFICATION_PERFORMED && !competeAllNotifications.value
            @Suppress("UNUSED_PARAMETER")
            set(value) {
            }
    }

    /**
     * Status for submit button, only enable if a diagnosis is selected or complete all notifications is set
     */
    open var submitButton = object : CommandButtonStatus() {
        override var isDisabled: Boolean
            get() = selectedDiagnosisRevisionStatus == null && !competeAllNotifications.value
            @Suppress("UNUSED_PARAMETER")
            set(value) {
            }
    }

    open fun initAndPrepareBean(task: Task, diagnosisRevision: DiagnosisRevision?): NotificationPhaseExitDialog {
        if (initBean(task, diagnosisRevision))
            prepareDialog()

        return this
    }

    override fun initBean(task: Task): Boolean {
        return initBean(task, null)
    }

    fun initBean(task: Task, diagnosisRevision: DiagnosisRevision?): Boolean {
        val oTask = taskRepository.findByID(task.id, true, true, false, true, true)

        diagnosisNotificationStatus = ExtendedNotificationStatus(oTask).diagnosisNotificationStatus

        // sets the first diagnosis that is not approved if  diagnosisRevision is null
        selectedDiagnosisRevisionStatus = if (diagnosisRevision != null)
            diagnosisNotificationStatus.diagnoses.firstOrNull { it.diagnosisRevision == diagnosisRevision }
                    ?: throw DiagnosisRevisionNotFoundException()
        else {
            diagnosisNotificationStatus.diagnoses.firstOrNull { it.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PERFORMED }
        }

        competeAllNotifications = object : CheckBoxStatus(false, isNotificationNecessary, false, false) {
            override fun onClick() {
                this.isInfo = this.value
                if (this.value) {
                    exitPhase.set(true, true, false, false)
                    removeFromWorklist.set(true, true, false, false)
                    archiveTask.set(true, true, false, false)
                } else {
                    onDiagnosisSelection()
                }
            }
        }

        // setting exit phase
        exitPhase = object : CheckBoxStatus(!isMoreThenOneNotificationCurrentlyNecessary, true, isMoreThenOneNotificationCurrentlyNecessary, isMoreThenOneNotificationCurrentlyNecessary) {
            override fun onClick() {
            }
        }

        removeFromWorklist.set(!isMoreThenOneNotificationPerformedOrPendingException, true, false, false)

        archiveTask = object : CheckBoxStatus(!isMoreThenOneNotificationOverallNecessary, true, isMoreThenOneNotificationOverallNecessary, isMoreThenOneNotificationOverallNecessary) {
            override fun onClick() {
            }
        }

        return super.initBean(oTask)
    }

    private val isMoreThenOneNotificationCurrentlyNecessary
        get() = countNotificationCurrentlyNecessary(selectedDiagnosisRevisionStatus?.diagnosisRevision) > 0

    private fun countNotificationCurrentlyNecessary(current: DiagnosisRevision?): Int {
        return if (current == null) diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }
        else
            diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING && it.diagnosisRevision.id != current.id }
    }

    private val isMoreThenOneNotificationOverallNecessary
        get() = (selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationNecessary == true && countNotificationOverallNecessary > 1) || (selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationNecessary == false && countNotificationOverallNecessary > 0)

    private val countNotificationOverallNecessary
        get() = diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.isNotificationNecessary }

    private val isNotificationCompleted: Boolean
        get() = diagnosisNotificationStatus.diagnoses.all { !it.diagnosisRevision.isNotificationNecessary }

    private val isMoreThenOneNotificationPerformedOrPendingException
        get() = (selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationPerformedOrPending == true && countNotificationsPerformedOrPending > 1) || (selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationPerformedOrPending == false && countNotificationsPerformedOrPending > 0)

    private val countNotificationsPerformedOrPending
        get() = diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.isNotificationPerformedOrPending }

    private val isNotificationNecessary: Boolean
        get() = diagnosisNotificationStatus.diagnoses.any { it.diagnosisRevision.isNotificationNecessary }

    override fun onDiagnosisSelection() {
        competeAllNotifications.set(false, true, false, false)
        if (selectedDiagnosisRevisionStatus != null) {
            competeAllNotifications.value = false
            exitPhase.set(!isMoreThenOneNotificationCurrentlyNecessary, true, isMoreThenOneNotificationCurrentlyNecessary, isMoreThenOneNotificationCurrentlyNecessary)
            removeFromWorklist.set(!isMoreThenOneNotificationPerformedOrPendingException, true, false, false)
            archiveTask.set(!isMoreThenOneNotificationOverallNecessary, true, isMoreThenOneNotificationOverallNecessary, isMoreThenOneNotificationOverallNecessary)
        } else {
            exitPhase.set(!isMoreThenOneNotificationCurrentlyNecessary, true, isMoreThenOneNotificationCurrentlyNecessary, isMoreThenOneNotificationCurrentlyNecessary)
            removeFromWorklist.set(!isMoreThenOneNotificationPerformedOrPendingException, true, false, false)
            archiveTask.set(isNotificationCompleted, true, !isNotificationCompleted, !isNotificationCompleted)
        }
    }

    /**
     * Hides the dialog an exits the notification phase
     */
    open fun hideAndExitPhase() {
        var tmp = task

        if (competeAllNotifications.value) {
            tmp = diagnosisService.completeAllNotifications(tmp)
        } else if (selectedDiagnosisRevisionStatus != null) {
            val diagnosis = selectedDiagnosisRevisionStatus?.diagnosisRevision ?: return
            MessageHandler.sendGrowlMessagesAsResource("growl.notification.notificationCompleted.headline", "growl.notification.notificationCompleted.text", diagnosis)
            tmp = diagnosisService.completeNotification(tmp, diagnosis)
        }

        tmp = workPhaseHandler.endNotificationPhase(tmp, exitPhase.value, removeFromWorklist.value)

        super.hideDialog(NotificationPhaseExitEvent(archiveTask.value))
    }

    /**
     * Hides the dialog and fires a task reload event
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }
}