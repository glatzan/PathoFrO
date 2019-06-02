package com.patho.main.dialog.phase

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.DiagnosisRevisionNotFoundException
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.status.diagnosis.DiagnosisBearer
import com.patho.main.util.status.diagnosis.IReportIntentStatusByDiagnosisViewData
import com.patho.main.util.status.diagnosis.ReportIntentStatusByDiagnosis
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
        private val workPhaseHandler: WorkPhaseHandler) : AbstractPhaseExitDialog(Dialog.DIAGNOSIS_PHASE_EXIT), IReportIntentStatusByDiagnosisViewData {

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

    open val isSelectDiagnosis
        get() = selectDiagnosisRevision == null && !competeAllDiagnoses

    /**
     * If true all diagnoses are completed
     */
    open var competeAllDiagnoses: Boolean = false

    /**
     * If true no single diagnosis can be selected.
     * This is in case completeAllDiagnoses is true
     */
    open var isDisableDiagnosisSelection = false

    /**
     * If true the notification phase will set on the given task
     */
    open var goToNotification: Boolean = false

    open var isRenderNotificationInfo = false

    open var isDisableExitDiagnosisPhase = false

    open var isRenderExitDiagnosisInfo = false

    open fun initAndPrepareBean(task: Task, diagnosisRevision: DiagnosisRevision?): DiagnosisPhaseExitDialog {
        if (initBean(task, diagnosisRevision))
            prepareDialog()

        return this
    }

    override fun initBean(task: Task): Boolean {
        return initBean(task, null)
    }

    open fun initBean(task: Task, diagnosisRevision: DiagnosisRevision?): Boolean {
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)

        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        val oTask = optionalTask.get()

        diagnosisRevisions = ReportIntentStatusByDiagnosis(oTask)

        selectDiagnosisRevision = if (diagnosisRevision != null)
            diagnosisRevisions.diagnosisBearer.firstOrNull { it.diagnosisRevision == diagnosisRevision }
                    ?: throw DiagnosisRevisionNotFoundException()
        else {
            diagnosisRevisions.diagnosisBearer.firstOrNull { it.diagnosisRevision.notificationStatus == NotificationStatus.NOT_APPROVED }
        }

        // set complete all to false
        competeAllDiagnoses = false
        // selection within the datatable will be disabled
        isDisableDiagnosisSelection = competeAllDiagnoses

        // setting go to notification phase
        updateGoToNotification()

        // sets the notification warning
        onNotificationChange()

        // sets the flag for worklist removal
        updateRemoveFromWorklist()

        // sets the exit phase flag
        updateExitPhase()

        updateDisableExitDiagnosis()

        return super.initBean(task)
    }


    private fun updateRemoveFromWorklist() {
        removeFromWorklist = competeAllDiagnoses || diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 1
    }

    private fun updateExitPhase() {
        exitPhase = competeAllDiagnoses ||
                (diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 1 && selectDiagnosisRevision?.diagnosisRevision?.isNotApproved == true)
    }

    /**
     * If a notification for the selected diagnosis should be performed, or if all diagnoses should be completed and at least one diagnosis needs a notification
     */
    private fun updateGoToNotification() {
        goToNotification = (selectDiagnosisRevision?.diagnosisRevision?.isNotificationNecessary == true) || (competeAllDiagnoses && diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotificationNecessary } > 0)
    }

    private fun updateDisableExitDiagnosis() {
        println(competeAllDiagnoses)
        println((selectDiagnosisRevision?.diagnosisRevision?.isNotApproved == true && diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 1))
        println((diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } > 1))
        isDisableExitDiagnosisPhase = !(competeAllDiagnoses ||
                (selectDiagnosisRevision?.diagnosisRevision?.isNotApproved == true && diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 1) ||
                (diagnosisRevisions.diagnosisBearer.count { it.diagnosisRevision.isNotApproved } == 0))
    }

    open fun onCompleteAllDiagnosesChange() {
        isDisableDiagnosisSelection = competeAllDiagnoses
        updateExitPhase()
        if (competeAllDiagnoses) {
            removeFromWorklist = true
            selectDiagnosisRevision = null
            updateGoToNotification()
        } else {
            removeFromWorklist = false
            goToNotification = false
        }

        updateDisableExitDiagnosis()
    }

    /**
     * Setting info for notification phase. Either if the selected diagnosis need a notification or all diagnoses should be completed and at least on diagnosis needs a notification
     */
    open fun onNotificationChange() {
        isRenderNotificationInfo = if (!goToNotification) {
            ((selectDiagnosisRevision?.diagnosisRevision?.isNotificationNecessary == true) ||
                    (competeAllDiagnoses && diagnosisRevisions.diagnosisBearer.any { it.diagnosisRevision.isNotificationNecessary }))
        } else
            false
    }

    override fun onDiagnosisSelection() {
        updateRemoveFromWorklist()
        updateExitPhase()
        updateGoToNotification()
        onNotificationChange()
        updateDisableExitDiagnosis()
    }

    open fun hideAndExitPhase() {
        workPhaseHandler.endDiagnosisPhase(task, selectDiagnosisRevision?.diagnosisRevision, exitPhase, goToNotification, removeFromWorklist)
        super.hideDialog(TaskReloadEvent())


        // all diagnoses wil be approved
        if (diagnosisRevision == null) {
            task = workPhaseService.endDiagnosisPhase(task, true,
                    if (goToNotification) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.endAll",
                    if (goToNotification) "growl.diagnosis.endAll.text.true" else "growl.diagnosis.endAll.text.false")
        } else {
            task = diagnosisService.approveDiangosis(task, diagnosisRevision,
                    if (goToNotification) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)

            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.approved",
                    if (goToNotification) "growl.diagnosis.endAll.text.true" else "growl.diagnosis.endAll.text.false")

            // only remove from list if no diagnosis is left
            if (endPhase) {
                if (task.diagnosisRevisions.all { !it.isNotificationNecessary }) {
                    task = workPhaseService.endDiagnosisPhase(task, true,
                            if (goToNotification) NotificationStatus.NOTIFICATION_PENDING else NotificationStatus.NO_NOTFICATION)
                } else {
                    MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.removeFromDiagnosisList.failed",
                            "growl.diagnosis.removeFromDiagnosisList.failed.text")
                }
            }
        }

//        if (allRevisions)
//            hideDialog(new DiagnosisPhaseExitEvent(task, null, removeFromDiagnosisList, removeFromWorklist, performNotification));
//        else if (selectedRevision != null)
//            hideDialog(new DiagnosisPhaseExitEvent(task, selectedRevision, removeFromDiagnosisList, removeFromWorklist, performNotification));
//        else {
//            //TODO Error
//            hideDialog();
//        }
    }

    /**
     * Hides the dialog, fires a task reloadTaskEvent
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

}