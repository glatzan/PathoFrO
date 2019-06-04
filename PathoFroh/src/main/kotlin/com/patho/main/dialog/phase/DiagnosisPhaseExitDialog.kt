package com.patho.main.dialog.phase

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.DiagnosisRevisionNotFoundException
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.status.diagnosis.DiagnosisBearer
import com.patho.main.util.status.diagnosis.IReportIntentStatusByDiagnosisViewData
import com.patho.main.util.status.diagnosis.ReportIntentStatusByDiagnosis
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
        private val diagosisService: DiagnosisService) : AbstractPhaseExitDialog(Dialog.DIAGNOSIS_PHASE_EXIT), IReportIntentStatusByDiagnosisViewData {

    /**
     * List of all reportIntent revisions with their status
     */
    override lateinit var diagnosisRevisions: ReportIntentStatusByDiagnosis

    /**
     * Selected reportIntent for that the notification will be performed
     */
    override var selectDiagnosisRevision: DiagnosisBearer? = null

    /**
     * Diagnosis bearer for displaying details in the datatable overlay panel
     */
    override var viewDiagnosisRevisionDetails: DiagnosisBearer? = null

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
        get() = !(selectDiagnosisRevision != null || competeAllDiagnoses.value)


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
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)

        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        val oTask = optionalTask.get()

        diagnosisRevisions = ReportIntentStatusByDiagnosis(oTask)

        // sets the first diagnosis that is not approved if  diagnosisRevision is null
        selectDiagnosisRevision = if (diagnosisRevision != null)
            diagnosisRevisions.diagnosisBearer.firstOrNull { it.diagnosisRevision == diagnosisRevision }
                    ?: throw DiagnosisRevisionNotFoundException()
        else {
            diagnosisRevisions.diagnosisBearer.firstOrNull { it.diagnosisRevision.notificationStatus == NotificationStatus.NOT_APPROVED }
        }

        // complete checkbox
        competeAllDiagnoses = object : CheckBoxStatus() {
            override fun onClick() {
                if (value) {
                    // only set if at least one notification is necessary
                    goToNotification.set(diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotificationNecessary } > 0, true, false, false)
                    removeFromWorklist.set(true, true, false, false)
                    exitPhase.set(true, true, false, false)
                    selectDiagnosisRevision = null
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
                    ((selectDiagnosisRevision?.diagnosisRevision?.isNotificationNecessary == true) || (competeAllDiagnoses.value && diagnosisRevisions.diagnosisBearer.any { it.diagnosisRevision.isNotificationNecessary }))
                } else
                    false
            }
        }

        removeFromWorklist = object : CheckBoxStatus() {
            override fun onClick() {
            }
        }

        exitPhase = object : CheckBoxStatus() {
            override fun onClick() {
            }
        }

        // true if selectDiagnosisRevision == null
        competeAllDiagnoses.set(selectDiagnosisRevision == null, true, false, false)

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
        if (selectDiagnosisRevision != null) {
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
        get() = selectDiagnosisRevision?.diagnosisRevision?.isNotificationNecessary == true

    /**
     * Returns true if the selected diagnosis is the last one that is not approved
     */
    private val isCurrentDiagnosisNotApprovedAndLast
        get() = selectDiagnosisRevision?.diagnosisRevision?.isNotApproved == true && diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 1


    /**
     * Returns true is any diagnosis is missing a notification
     */
    private val isNotificationNecessary
        get() = diagnosisRevisions.diagnosisBearer.any { it.diagnosisRevision.isNotificationNecessary }

    /**
     * Hides the dialog, and performs the phase exit action
     */
    open fun hideAndExitPhase() {
        var tmp = task

        if (competeAllDiagnoses.value)
            tmp = diagosisService.approveAllDiagnoses(tmp, if (goToNotification.value) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
        else if (selectDiagnosisRevision != null) {
            val diagnosis = selectDiagnosisRevision?.diagnosisRevision ?: return
            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.approved.headline",
                    if (goToNotification.value) "growl.diagnosis.approved.goToNotification" else "growl.diagnosis.approved.noNotification")
            tmp = diagosisService.approveDiagnosis(tmp, diagnosis, if (goToNotification.value) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
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