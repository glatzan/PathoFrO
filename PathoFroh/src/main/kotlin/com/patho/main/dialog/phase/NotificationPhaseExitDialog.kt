package com.patho.main.dialog.phase

import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.dialogReturn.ReloadTaskEvent
import com.patho.main.util.task.ArchiveTaskStatus
import com.patho.main.util.task.TaskNotFoundException
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
        private val taskRepository: TaskRepository) : AbstractPhaseExitDialog(Dialog.NOTIFICATION_PHASE_EXIT) {

    /**
     * True if info text for not removing the task form the notification list should be displayed
     */
    open var isRenderInfoTextFavouriteList: Boolean = false

    /**
     * True if info text for not completing the notification phase should be displayed
     */
    open var isRenderInfoTextExit: Boolean = false

    /**
     * True if info text for not archving the task should be displayed
     */
    open var isRenderInfoTaskArchive: Boolean = false

    /**
     * True if task should be archived (will trigger the archiveTask dialog)
     */
    open var archiveTask: Boolean = false

    override fun initBean(task: Task): Boolean {

        var oTask = task
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)
        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        oTask = optionalTask.get()
        val status = ArchiveTaskStatus(oTask)

        // remove from notification list in no notification for an other diagnosis should be performed
        removeFromFavouriteList = !oTask.diagnosisRevisions.any { it.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }
        // render info text while not removing from notification list
        isRenderInfoTextFavouriteList = !removeFromFavouriteList

        // only remove from notification list if no more notifications need do be done
        removeFromWorklist = removeFromFavouriteList

        // true if all notifications had been performed
        exitPhase = oTask.diagnosisRevisions.any { it.isNotificationNecessary }
        // info text only if no phase exit should be done
        isRenderInfoTextExit = !exitPhase

        // status for archiving task after phase has ended
        archiveTask = status.isArchiveAble

        isRenderInfoTextExit = !archiveTask

        return super.initBean(oTask)
    }

    /**
     * Hides the dialog an exits the notification phase
     */
    open fun hideAndExitPhase() {
//        if (endNotificationPhase && removeFromNotificationList) {
//			notificationService.endNotificationPhase(getTask());
//		} else {
//			if (removeFromNotificationList)
//				favouriteListService.removeTaskFromList(task.getId(), PredefinedFavouriteList.NotificationList);
//		}
//
//		if (removeFromWorklist) {
//			// only remove from worklist if patient has one active task
//			if (task.getPatient().getTasks().stream().filter(p -> !p.isFinalized()).count() > 1) {
//				mainHandlerAction.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
//			} else {
//				worklistViewHandler.removePatientFromWorklist(task.getPatient());
//			}
//		}
//
//		setExitSuccessful(true);
    }

    /**
     * Hides the dialog and fires a task reload event
     */
    override fun hideDialog() {
        super.hideDialog(ReloadTaskEvent())
    }
}