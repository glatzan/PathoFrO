package com.patho.main.action.dialog.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.service.FavouriteListService;
import com.patho.main.service.SampleService;
import com.patho.main.service.TaskService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class NotificationPhaseExitDialog extends AbstractDialog {

	/**
	 * If true the task will be removed from the notification list
	 */
	private boolean removeFromNotificationList;

	/**
	 * If true the notification phase of the task will terminated
	 */
	private boolean endNotificationPhase;

	/**
	 * If true the task will be removed from worklist
	 */
	private boolean removeFromWorklist;

	/**
	 * True if the phase was successfully left
	 */
	private boolean exitSuccessful;

	/**
	 * Initializes the bean and shows the dialog
	 * 
	 */
	public NotificationPhaseExitDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
		return this;
	}

	/**
	 * Initializes all field of the object
	 * 
	 * @param task
	 */
	public boolean initBean(Task task) {

		removeFromNotificationList = true;
		removeFromWorklist = true;

		exitSuccessful = false;

//		endNotificationPhase = task.getDiagnosisRevisions().stream()
//				.allMatch(p -> p.getNotificationDate() != 0 && !p.isNotificationPending());

		return super.initBean(task, Dialog.NOTIFICATION_PHASE_EXIT);
	}

	/**
	 * Removes task from notification list and ends the notification phae. if set
	 * the task will be archived.
	 */
	public void exitPhase() {

//		if (endNotificationPhase && removeFromNotificationList) {
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

	public void hideDialog() {
		super.hideDialog(new Boolean(exitSuccessful));
	}
}
