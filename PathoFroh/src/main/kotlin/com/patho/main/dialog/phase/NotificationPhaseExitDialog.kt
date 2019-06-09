package com.patho.main.dialog.phase

import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.Dialog
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.dialog.event.NotificationPhaseExitEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.status.diagnosis.DiagnosisBearer
import com.patho.main.util.status.diagnosis.IReportIntentStatusByDiagnosisViewData
import com.patho.main.util.status.diagnosis.ReportIntentStatusByDiagnosis
import com.patho.main.util.ui.backend.CheckBoxStatus
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
        private val taskRepository: TaskRepository) : AbstractPhaseExitDialog(Dialog.NOTIFICATION_PHASE_EXIT), IReportIntentStatusByDiagnosisViewData {

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

    open lateinit var competeAllNotifications: CheckBoxStatus

    open lateinit var archiveTask: CheckBoxStatus

    override fun initBean(task: Task): Boolean {

        var oTask = task
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)
        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        oTask = optionalTask.get()

        diagnosisRevisions = ReportIntentStatusByDiagnosis(oTask)

        archiveTask = object : CheckBoxStatus() {
            override fun onClick() {
            }
        }

        competeAllNotifications = object : CheckBoxStatus(false, isNotificationNecessary, false, false) {
            override fun onClick() {
                this.isInfo = this.value
                if (this.value) {
                    exitPhase.set(true, true, false, false)
                    removeFromWorklist.set(true, true, false, false)
                } else {
                    exitPhase.set(!isNotificationPending, true, isNotificationPending, isNotificationPending)
                    removeFromWorklist.set(!isNotificationPending, true, false, false)
                }
            }
        }

        // setting exit phase
        exitPhase = object : CheckBoxStatus(!isNotificationPending, true, isNotificationPending, isNotificationPending) {
            override fun onClick() {
            }
        }

        removeFromWorklist.set(!isNotificationPending, true, false, false)

        archiveTask = object : CheckBoxStatus(isNotificationCompleted, true, !isNotificationCompleted, !isNotificationCompleted) {
            override fun onClick() {
            }
        }

        return super.initBean(oTask)
    }

    private val isNotificationPending: Boolean
        get() = diagnosisRevisions.diagnosisBearer.any { it.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }

    private val isNotificationNecessary: Boolean
        get() = diagnosisRevisions.diagnosisBearer.all { it.diagnosisRevision.isNotificationNecessary }

    private val isNotificationCompleted: Boolean
        get() = !isNotificationNecessary

    val disableOKButton
        get() = !(competeAllNotifications.value || exitPhase.value || removeFromWorklist.value)

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
        super.hideDialog(NotificationPhaseExitEvent())
    }

    /**
     * Hides the dialog and fires a task reload event
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }
}