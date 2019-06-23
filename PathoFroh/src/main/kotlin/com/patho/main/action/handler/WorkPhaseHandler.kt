package com.patho.main.action.handler

import com.patho.main.common.GuiCommands
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.service.DiagnosisService
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional


@Controller
@Scope("session")
open class WorkPhaseHandler @Autowired constructor(
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
    @Transactional
    open fun updateStainingPhase(task: Task, exitDialogCommand: String = GuiCommands.OPEN_STAINING_PHASE_EXIT_DIALOG): Pair<Task, Boolean> {
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

    /**
     * Starts the staining phase
     */
    @Transactional
    open fun startStainingPhase(task: Task): Task {
        return workPhaseService.startStainingPhase(task)
    }

    /**
     * Ends the stating phase
     */
    @Transactional
    open fun endStainingPhase(task: Task, endPhase: Boolean, startDiagnosisPhase: Boolean, removeFromWorklist: Boolean): Task {
        var task = task

        if (endPhase) {
            // ending staining pahse
            task = workPhaseService.endStainingPhase(task, true)

            MessageHandler.sendGrowlMessagesAsResource("growl.staining.endAll.headline",
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

    /**
     * Starts the diagnosis phase
     */
    @Transactional
    open fun startDiagnosisPhase(task: Task): Task {
        return workPhaseService.startDiagnosisPhase(task)
    }

    /**
     * Checks if any diagnosis is not completed, if so the diagnosis phase will be restarted.
     */
    @Transactional
    open fun updateDiagnosisPhase(task: Task): Task {
        if (task.diagnosisRevisions.any { !it.completed })
            return startDiagnosisPhase(task)
        return task
    }

    /**
     * Ends the diagnosis phase
     */
    @Transactional
    open fun endDiagnosisPhase(task: Task, endPhase: Boolean,
                               startNotificationPhase: Boolean, removeFromCurrentWorklist: Boolean): Task {
        var tmp = task

        if (endPhase) {
            tmp = workPhaseService.endDiagnosisPhase(tmp, startNotificationPhase)
            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.endAll.headline",
                    if (startNotificationPhase) "growl.diagnosis.endAll.goToNotification" else "growl.diagnosis.endAll.noNotification")
        }

        if (startNotificationPhase) {
            logger.debug("Adding task to notification list")
            tmp = startNotificationPhase(tmp)
        }

        if (removeFromCurrentWorklist)
            removeFromWorklist(tmp.patient!!, PredefinedFavouriteList.DiagnosisList)

        return tmp
    }

    /**
     * Starts the notification phase
     */
    @Transactional
    open fun startNotificationPhase(task: Task): Task {
        return workPhaseService.startNotificationPhase(task)
    }

    /**
     * End the notification pahse
     */
    @Transactional
    open fun endNotificationPhase(task: Task, endPhase: Boolean,
                                  removeFromCurrentWorklist: Boolean): Task {
        var tmp = task
        if (endPhase) {
            tmp = workPhaseService.endNotificationPhase(task, true)
            MessageHandler.sendGrowlMessagesAsResource("growl.notification.endPhase.headline", "growl.notification.endPhase.text")
        }

        if (removeFromCurrentWorklist)
            removeFromWorklist(tmp.patient!!, PredefinedFavouriteList.NotificationList)

        return tmp
    }

    /**
     * Removes a task from the current worklist if the task is not listed in the passed favourite lists
     */
    @Transactional
    protected open fun removeFromWorklist(patient: Patient, vararg lists: PredefinedFavouriteList) {
        // only remove from worklist if patient has one active task
        if (patient.tasks.any { it.favouriteLists.any { p -> lists.any { p.id == it.id } } }) {
            MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive")
        } else {
            // remove from current worklist, view is updated
            worklistHandler.removePatientFromWorklist(patient)
        }
    }
}