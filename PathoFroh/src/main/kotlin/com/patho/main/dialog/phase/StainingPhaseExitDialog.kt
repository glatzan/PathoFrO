package com.patho.main.dialog.phase

import com.patho.main.common.Dialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.dialogReturn.ReloadTaskEvent
import com.patho.main.util.task.ArchiveTaskStatus
import com.patho.main.util.task.TaskNotFoundException
import com.patho.main.util.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for leaving the staining phase
 */
@Component()
@Scope(value = "session")
open class StainingPhaseExitDialog @Autowired constructor(
        private val taskRepository: TaskRepository) : AbstractPhaseExitDialog(Dialog.STAINING_PHASE_EXIT) {

    open var isRenderCompleteStainings: Boolean = false
    open var competeStainings: Boolean = false

    open var goToDiagnosis: Boolean = false

    open var isDisableRemoveFavouriteList = false

    open fun initAndPrepareBean(task: Task, completeSlides: Boolean = true): StainingPhaseExitDialog {
        if (initBean(task, completeSlides))
            prepareDialog()
        return this
    }

    open fun initBean(task: Task, completeSlides: Boolean = true): Boolean {
        var oTask = task
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)
        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        oTask = optionalTask.get()

        val status = ArchiveTaskStatus(oTask)

        // complete stainings if not all completed
        competeStainings = true
        isRenderCompleteStainings = !status.stainingStatus.allSlidesCompleted

        exitPhase = completeSlides

        // always go to notification phase
        goToDiagnosis = true

        removeFromWorklist = true


        val stainingAlreadyCompleted = TaskStatus.checkIfStainingCompleted(oTask)

        removeFromFavouriteList = if (completeSlides) true else stainingAlreadyCompleted
        removeFromWorklist = if (completeSlides) true else stainingAlreadyCompleted
        exitPhase = if (completeSlides) true else stainingAlreadyCompleted

        return super.initBean(oTask)
    }

    override fun update() {
    }

    open fun hideAndExitPhase() {
//        super.hideDialog(StainingPhaseExitEvent(task, removeFromWorklist, removeFromStainingList, endStainingPhase, goToDiagnosisPhase))
    }

    override fun hideDialog() {
        super.hideDialog(ReloadTaskEvent())
    }
}

//if (TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.NotificationList))
//goToDiagnosisPhase = false;
//else
//goToDiagnosisPhase = true;
//
//// all slides will be marked as completed by endStainingphase methode
//if (autoCompleteStainings) {
//    setRemoveFromStainingList(true);
//    setRemoveFromWorklist(true);
//    setEndStainingPhase(true);
//} else {
//    boolean stainingCompleted = TaskStatus.checkIfStainingCompleted(task);
//
//    setRemoveFromStainingList(stainingCompleted);
//    setRemoveFromWorklist(stainingCompleted);
//    setEndStainingPhase(stainingCompleted);
//}