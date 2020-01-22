package com.patho.main.dialog.phase

import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.SlideService
import com.patho.main.util.dialog.event.StainingPhaseExitEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.status.TotalTaskStatus
import com.patho.main.util.ui.backend.CheckBoxStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for leaving the staining phase
 */
@Component()
@Scope(value = "session")
open class StainingPhaseExitDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val workPhaseHandler: WorkPhaseHandler,
        private val slideService: SlideService) : AbstractPhaseExitDialog(Dialog.STAINING_PHASE_EXIT) {

    /**
     * Checkbox backend for completeStainings
     */
    open lateinit var completeStainings: CheckBoxStatus

    /**
     * Checkbox backend for goToDiagnosis
     */
    open lateinit var goToDiagnosis: CheckBoxStatus

    /**
     * Initializes the dialog
     */
    override fun initBean(task: Task): Boolean {
        val oTask = taskRepository.findByID(task.id, true, true, false, true, true)

        val status = TotalTaskStatus(oTask)

        // complete checkbox
        completeStainings = object : CheckBoxStatus() {
            override fun onClick() {
                exitPhase.set(completeStainings.value, true, !completeStainings.value, false)
                goToDiagnosis.set(true, true, false, false)
                removeFromWorklist.set(completeStainings.value, true, false, false)
            }
        }

        // diagnosis checkbox
        goToDiagnosis = object : CheckBoxStatus() {
            override fun onClick() {
                isInfo = exitPhase.value && !goToDiagnosis.value
            }
        }

        // complete stainings if not all completed
        completeStainings.set(true, !status.staining.slidesMarkedAsCompleted, false, false)

        // remove from list
        removeFromWorklist.set(true, true, false, false)

        // goto diagnosis pahse
        goToDiagnosis.set(true, true, false, false)

        // exit phase
        exitPhase.set(completeStainings.value, true, !completeStainings.value, false)

        return super.initBean(oTask)
    }

    /**
     * Hides the dialog and removes the task from the staining lsit
     */
    open fun hideAndExitPhase() {
        var task = if (completeStainings.value) slideService.completeStaining(task, true, true) else task
        task = workPhaseHandler.endStainingPhase(task, exitPhase.value, goToDiagnosis.value, removeFromWorklist.value)
        super.hideDialog(StainingPhaseExitEvent())
    }

    /**
     * Hides the dialog, fires a task reloadTaskEvent
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }
}
