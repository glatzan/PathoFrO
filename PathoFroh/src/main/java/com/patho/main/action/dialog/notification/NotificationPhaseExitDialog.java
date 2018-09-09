package com.patho.main.action.dialog.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.service.FavouriteListService;
import com.patho.main.service.NotificationService;
import com.patho.main.service.SampleService;
import com.patho.main.service.TaskService;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class NotificationPhaseExitDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalEditViewHandler globalEditViewHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SampleService sampleService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private NotificationService notificationService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

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
	 * @param patient
	 */
	public void initAndPrepareBean(Task task) {
		initBean(task);
		prepareDialog();
	}

	/**
	 * Initializes all field of the object
	 * 
	 * @param task
	 */
	public void initBean(Task task) {

		removeFromNotificationList = true;
		removeFromWorklist = true;

		exitSuccessful = false;

		endNotificationPhase = task.getDiagnosisRevisions().stream()
				.allMatch(p -> p.getNotificationDate() != 0 && !p.isNotificationPending());

		super.initBean(task, Dialog.NOTIFICATION_PHASE_EXIT);
	}

	/**
	 * Removes task from notification list and ends the notification phae. if set
	 * the task will be archived.
	 */
	public void exitPhase() {
		try {

			if (endNotificationPhase && removeFromNotificationList) {
				notificationService.endNotificationPhase(getTask());
			} else {
				if (removeFromNotificationList)
					favouriteListService.removeTaskFromList(task.getId(), PredefinedFavouriteList.NotificationList);
			}

			if (removeFromWorklist) {
				// only remove from worklist if patient has one active task
				if (task.getPatient().getTasks().stream().filter(p -> !p.isFinalized()).count() > 1) {
					mainHandlerAction.sendGrowlMessagesAsResource("growl.error",
							"growl.error.worklist.remove.moreActive");
				} else {
					worklistViewHandlerAction.removePatientFromCurrentWorklist(task.getPatient());
					worklistViewHandlerAction.onDeselectPatient(true);
				}
			}

			setExitSuccessful(true);
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	public void hideDialog() {
		super.hideDialog(new Boolean(exitSuccessful));
	}
}
