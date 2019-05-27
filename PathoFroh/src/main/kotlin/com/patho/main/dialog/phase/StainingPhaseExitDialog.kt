package com.patho.main.dialog.phase

import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.SlideService
import com.patho.main.util.dialogReturn.ReloadTaskEvent
import com.patho.main.util.task.ArchiveTaskStatus
import com.patho.main.util.task.TaskNotFoundException
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
     * If true the checkbox for complete all stainings is rendered
     * Only rendered if not all stainings are completed.
     */
    open var isRenderCompleteStainings: Boolean = false

    /**
     * If true all stainigns are completed
     */
    open var competeStainings: Boolean = false

    /**
     * If true the task will be added to the reportIntent list
     */
    open var goToDiagnosis: Boolean = false

    /**
     * If true the exit staining phase checkbox is disabled
     */
    open var isDisableExitStainingPhase = false

    /**
     * If true the additional info for not adding the task to the reportIntent list is displayed
     * !goToDiagnosis and exitPhase
     */
    open var isRenderDiagnosisPhaseInfoText = false

    override fun initBean(task: Task): Boolean {
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)

       if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        val oTask = optionalTask.get()

        val status = ArchiveTaskStatus(oTask)

        // complete stainings if not all completed
        competeStainings = true
        // only render is not all staings are completed
        isRenderCompleteStainings = !status.stainingStatus.allSlidesCompleted

        // disable exit staining phase if not all stagings are completed
        isDisableExitStainingPhase = !competeStainings
        // only exit if all stainings are completed
        exitPhase = competeStainings

        // always go to notification phase
        goToDiagnosis = true
        // always remove from worklist
        removeFromWorklist = true

        // only render reportIntent info if exitPhase and not goToDiagnosis
        isRenderDiagnosisPhaseInfoText = exitPhase && !goToDiagnosis

        return super.initBean(oTask)
    }

    /**
     * Function called on checkbox complete stainings change
     */
    open fun onCompleteStainingsChange() {
        isDisableExitStainingPhase = !competeStainings
        exitPhase = competeStainings
        if (exitPhase)
            goToDiagnosis = true
        onPhaseExitChange()
    }

    /**
     * Function is called on checkbox end phase change
     */
    open fun onPhaseExitChange() {
        isRenderDiagnosisPhaseInfoText = exitPhase && !goToDiagnosis
    }

    /**
     * Hides the dialog and removes the task from the staining lsit
     */
    open fun hideAndExitPhase() {
        if (competeStainings)
            slideService.completedStaining(task, true)
        workPhaseHandler.endStainingPhase(task, exitPhase, exitPhase, goToDiagnosis, removeFromWorklist)
        super.hideDialog(ReloadTaskEvent())
    }

    /**
     * Hides the dialog, fires a task reloadTaskEvent
     */
    override fun hideDialog() {
        super.hideDialog(ReloadTaskEvent())
    }
}
