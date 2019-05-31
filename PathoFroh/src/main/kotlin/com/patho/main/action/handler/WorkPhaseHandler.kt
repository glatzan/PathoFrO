package com.patho.main.action.handler

import com.patho.main.common.GuiCommands
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.service.DiagnosisService
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Controller


@Controller
@Scope("session")
class WorkPhaseHandler @Autowired constructor(
        private val workPhaseService: WorkPhaseService,
        private val diagnosisService: DiagnosisService,
        private val worklistHandler: WorklistHandler) : AbstractHandler() {

    override fun loadHandler() {
    }


    /**
     * Updates the stating phase. If staings are completed and the phase has not ended the exit phase dialog will be opened.
     * If slides have to be stained, the staining phase is started
     *
     * This methode returns the new task and true if the end phase dialog is shown, false if not
     */
    fun updateStainingPhase(task: Task, exitDialogCommand: String = GuiCommands.OPEN_STAINING_PHASE_EXIT_DIALOG): Pair<Task, Boolean> {
        // checks if stainigs are completed
        if (TaskStatus.checkIfStainingCompleted(task)) {
            if (!task.stainingCompleted) {
                MessageHandler.executeScript(exitDialogCommand)
            }
            return Pair(task, true)
        } else {
            return Pair(workPhaseService.startStainingPhase(task), false)
        }
    }

    fun startStainingPhase(task: Task): Task {
        return workPhaseService.startStainingPhase(task)
    }

    fun endStainingPhase(task: Task, endPhase: Boolean, startDiagnosisPhase: Boolean, removeFromWorklist: Boolean): Task {
        var task = task

        if (endPhase) {
            // ending staining pahse
            task = workPhaseService.endStainingPhase(task, true)

            MessageHandler.sendGrowlMessagesAsResource("growl.staining.endAll",
                    if (startDiagnosisPhase) "growl.staining.endAll.text.true" else "growl.staining.endAll.text.false")
        }

        if (startDiagnosisPhase) {
            logger.debug("Adding Task to reportIntent list")
            task = workPhaseService.startDiagnosisPhase(task)
        }

        if (removeFromWorklist)
            removeFromWorklist(task.patient!!, PredefinedFavouriteList.StainingList)

        return task
    }

    fun startDiagnosisPhase(task: Task) {
        workPhaseService.startDiagnosisPhase(task)
    }

    fun updateDiagnosisPhase(task: Task): Task {
        if (task.diagnosisRevisions.any { !it.completed })
            return startNotificationPhase(task)
        return task
    }

    fun endDiagnosisPhase(task: Task, endPhase: Boolean,
                          goToNotification: Boolean, removeFromCurrentWorklist: Boolean) {
        return endDiagnosisPhase(task, null, endPhase, goToNotification, removeFromCurrentWorklist)
    }

    fun endDiagnosisPhase(task: Task, diagnosisRevision: DiagnosisRevision?, endPhase: Boolean,
                          goToNotification: Boolean, removeFromCurrentWorklist: Boolean) {
        var task = task

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

        // adding to notification phase
        if (goToNotification)
            task = startNotificationPhase(task)

        if (removeFromCurrentWorklist)
            removeFromWorklist(task.patient!!, PredefinedFavouriteList.DiagnosisList)
    }

    fun startNotificationPhase(task: Task): Task {
        return workPhaseService.startNotificationPhase(task)
    }

    private fun removeFromWorklist(patient: Patient, vararg lists: PredefinedFavouriteList) {
        // only remove from worklist if patient has one active task
        if (patient.tasks.any { it.favouriteLists.any { p -> lists.any { p.id == it.id } } }) {
            MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive")
        } else {
            // remove from current worklist, view is updated
            worklistHandler.removePatientFromWorklist(patient)
        }
    }
}