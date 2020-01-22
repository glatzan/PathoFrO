package com.patho.main.dialog.phase

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.DiagnosisRevisionNotFoundException
import com.patho.main.util.status.ExtendedNotificationStatus
import com.patho.main.util.status.IExtendedDatatableNotificationStatusByDiagnosis
import com.patho.main.util.ui.backend.CheckBoxStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for leaving the reportIntent phase
 */
@Component()
@Scope(value = "session")
open class DiagnosisPhaseExitDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val workPhaseHandler: WorkPhaseHandler,
        private val diagnosisService: DiagnosisService) : AbstractPhaseExitDialog(Dialog.DIAGNOSIS_PHASE_EXIT), IExtendedDatatableNotificationStatusByDiagnosis {

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
     * Checkbox backend value for completeAllDiagnoses
     */
    open lateinit var competeAllDiagnoses: CheckBoxStatus

    /**
     * Checkbox backend value for goToNotification
     */
    open lateinit var goToNotification: CheckBoxStatus

    /**
     * If true no single diagnosis can be selected.
     * This is in case completeAllDiagnoses is true
     */
    open val isDisableDiagnosisSelection
        get() = competeAllDiagnoses.value

    /**
     * If true the submit button is disabled. This is the case if no diagnosis is selected and the
     * completeAllDiagnoses checkbox is not set
     */
    open val isSubmitButtonDisabled
        get() = !(selectedDiagnosisRevisionStatus != null || competeAllDiagnoses.value)


    open fun initAndPrepareBean(task: Task, diagnosisRevision: DiagnosisRevision?): DiagnosisPhaseExitDialog {
        if (initBean(task, diagnosisRevision))
            prepareDialog()

        return this
    }

    /**
     * Initializes the bean and sets the first diagnosis that is not approved as the selected diagnosis
     */
    override fun initBean(task: Task): Boolean {
        return initBean(task, null)
    }

    /**
     * Initializes the dialog
     */
    open fun initBean(task: Task, diagnosisRevision: DiagnosisRevision?): Boolean {
        val oTask = taskRepository.findByID(task.id, true, true, false, true, true)

        diagnosisNotificationStatus = ExtendedNotificationStatus(oTask).diagnosisNotificationStatus

        // sets the first diagnosis that is not approved if  diagnosisRevision is null
        selectedDiagnosisRevisionStatus = if (diagnosisRevision != null)
            diagnosisNotificationStatus.diagnoses.firstOrNull { it.diagnosisRevision == diagnosisRevision }
                    ?: throw DiagnosisRevisionNotFoundException()
        else {
            diagnosisNotificationStatus.diagnoses.firstOrNull { it.diagnosisRevision.notificationStatus == NotificationStatus.NOT_APPROVED }
        }

        // complete checkbox
        competeAllDiagnoses = object : CheckBoxStatus() {
            override fun onClick() {
                if (value) {
                    // only set if at least one notification is necessary
                    goToNotification.set(diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.isNotificationNecessary } > 0, true, false, false)
                    removeFromWorklist.set(true, true, false, false)
                    exitPhase.set(true, true, false, false)
                    selectedDiagnosisRevisionStatus = null
                } else {
                    onDiagnosisSelection()
                }
            }
        }

        // go to notification checkbox
        goToNotification = object : CheckBoxStatus() {
            override fun onClick() {
                // set only if notification for the current diagnosis is necessary or if all should be approved, at least for one diagnosis a notification is pending
                goToNotification.isInfo = if (!value) {
                    ((selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationNecessary == true) || (competeAllDiagnoses.value && diagnosisNotificationStatus.diagnoses.any { it.diagnosisRevision.isNotificationNecessary }))
                } else
                    false
            }
        }

        // true if selectDiagnosisRevision == null
        competeAllDiagnoses.set(selectedDiagnosisRevisionStatus == null, true, false, false)

        // true if selectDiagnosisRevision is the last not approved diagnosis or all should be approved
        removeFromWorklist.set(competeAllDiagnoses.value || isCurrentDiagnosisNotApprovedAndLast, true, false, false)

        // true if notification should be performed for the selected diagnosis, or if all should be approved, at least for one diagnosis a notification is pending
        goToNotification.set((competeAllDiagnoses.value && isNotificationNecessary) || isCurrentDiagnosisNotificationNecessary, true, false, false)

        // true if selectDiagnosisRevision is the last not approved diagnosis or all should be approved
        exitPhase.set(competeAllDiagnoses.value || isCurrentDiagnosisNotApprovedAndLast, true, !(competeAllDiagnoses.value || isCurrentDiagnosisNotApprovedAndLast), !(competeAllDiagnoses.value || isCurrentDiagnosisNotApprovedAndLast))

        return super.initBean(oTask)
    }

    /**
     * On diagnosis change
     */
    override fun onDiagnosisSelection() {
        competeAllDiagnoses.set(false, true, false, false)
        if (selectedDiagnosisRevisionStatus != null) {
            removeFromWorklist.set(isCurrentDiagnosisNotApprovedAndLast, true, false, false)
            goToNotification.set(isCurrentDiagnosisNotificationNecessary, true, false, false)
            exitPhase.set(isCurrentDiagnosisNotApprovedAndLast, true, !isCurrentDiagnosisNotApprovedAndLast, !isCurrentDiagnosisNotApprovedAndLast)
        } else {
            removeFromWorklist.set(false, true, false, false)
            goToNotification.set(false, true, false, false)
            exitPhase.set(false, true, true, true)
        }
    }

    /**
     * Returns true if a notification for the current diagnosis is mandatory
     */
    private val isCurrentDiagnosisNotificationNecessary
        get() = selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotificationNecessary == true

    /**
     * Returns true if the selected diagnosis is the last one that is not approved
     */
    private val isCurrentDiagnosisNotApprovedAndLast
        get() = selectedDiagnosisRevisionStatus?.diagnosisRevision?.isNotApproved == true && diagnosisNotificationStatus.diagnoses.count { it.diagnosisRevision.isNotApproved } == 1


    /**
     * Returns true is any diagnosis is missing a notification
     */
    private val isNotificationNecessary
        get() = diagnosisNotificationStatus.diagnoses.any { it.diagnosisRevision.isNotificationNecessary }

    /**
     * Hides the dialog, and performs the phase exit action
     */
    open fun hideAndExitPhase() {
        var tmp = task

        if (competeAllDiagnoses.value)
            tmp = diagnosisService.approveAllDiagnoses(tmp, if (goToNotification.value) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
        else if (selectedDiagnosisRevisionStatus != null) {
            val diagnosis = selectedDiagnosisRevisionStatus?.diagnosisRevision ?: return

            val keepNotificationStatus = (diagnosis.notificationStatus == NotificationStatus.NOTIFICATION_PERFORMED || diagnosis.notificationStatus == NotificationStatus.NOTIFICATION_COMPLETED ) && !goToNotification.value

            // only show this growl if the notification was not performed already
            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.approved.headline",
                    if (keepNotificationStatus) "growl.diagnosis.approved.notificationAlreadyHappened" else if (goToNotification.value) "growl.diagnosis.approved.goToNotification" else "growl.diagnosis.approved.noNotification")
            // only change notification status if no notification was already performed
            tmp = diagnosisService.approveDiagnosis(tmp, diagnosis, if (keepNotificationStatus) diagnosis.notificationStatus else if (goToNotification.value) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
        }

        tmp = workPhaseHandler.endDiagnosisPhase(tmp, exitPhase.value, goToNotification.value, removeFromWorklist.value)

        hideDialog()
    }

    /**
     * Hides the dialog, fires a task reloadTaskEvent
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

}