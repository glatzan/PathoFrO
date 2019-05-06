package com.patho.main.action.handler;

import java.util.List;

import com.patho.main.model.patient.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.DiagnosisService;
import com.patho.main.service.FavouriteListService;
import com.patho.main.service.WorkPhaseService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Controller
@Scope("session")
@Getter
@Setter
public class WorkPhaseHandler{

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorkPhaseService workPhaseService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistHandler worklistHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	public void updateStainingPhase(Task task) {
		// update staing phase
		if (workPhaseService.updateStainigPhase(task)) {
			worklistHandler.replaceTaskInWorklist(task);
			// open end staining phase dialog
			MessageHandler.executeScript("clickButtonFromBean('headerForm:stainingPhaseExit')");
		} else {
			task = workPhaseService.startStainingPhase(task);
			worklistHandler.replaceTaskInWorklist(task);
		}
	}

	public void startStainingPhase(Task task) {
		workPhaseService.startStainingPhase(task);
		worklistHandler.replaceTaskInWorklist();
	}

	public Task endStainingPhase(Task task, boolean endPhase, boolean removeFromList, boolean startDiagnosisPhase,
			boolean removeFromWorklist) {

		if (endPhase && removeFromList) {
			// ending staining pahse
			task = workPhaseService.endStainingPhase(task, removeFromList);

			MessageHandler.sendGrowlMessagesAsResource("growl.staining.endAll",
					startDiagnosisPhase ? "growl.staining.endAll.text.true" : "growl.staining.endAll.text.false");
		} else {
			if (removeFromList)
				task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.StainingList,
						PredefinedFavouriteList.ReStainingList);
		}

		if (startDiagnosisPhase) {
			logger.debug("Adding Task to diagnosis list");
			task = startDiagnosisPhase(task);
		}

		if (removeFromWorklist) {
			// only remove from worklist if patient has one active task
			if (task.getParent().getTasks().stream().filter(p -> !p.getFinalized()).count() > 1) {
				MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
			} else {
				// view is updated
				worklistHandler.removePatientFromWorklist(task.getPatient());
			}
		}

		return task;
	}

	public Task updateDiagnosisPhase(Task task) {
		for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
			if (!revision.getCompleted()) {
				return startDiagnosisPhase(task);
			}
		}
		return task;
	}

	public Task startDiagnosisPhase(Task task) {
		return favouriteListService.addTaskToList(task, PredefinedFavouriteList.DiagnosisList);
	}

	/**
	 * Ends the notification pahse, if no diagnosis revision is passed, all
	 * diangoses will be ended.
	 */
	public void endDiagnosisPhase(Task task, DiagnosisRevision diagnosisRevision, boolean removeFromDiagnosisList,
			boolean notification, boolean removeFromCurrentWorklist) {

		// all diagnoses wil be approved
		if (diagnosisRevision == null) {
			task = workPhaseService.endDiagnosisPhase(task, removeFromDiagnosisList,
					notification ? NotificationStatus.NOTIFICATION_PENDING : NotificationStatus.NO_NOTFICATION);
			MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.endAll",
					notification ? "growl.diagnosis.endAll.text.true" : "growl.diagnosis.endAll.text.false");
		} else {
			task = diagnosisService.approveDiangosis(task, diagnosisRevision,
					notification ? NotificationStatus.NOTIFICATION_PENDING : NotificationStatus.NO_NOTFICATION);

			MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.approved",
					notification ? "growl.diagnosis.endAll.text.true" : "growl.diagnosis.endAll.text.false");

			// only remove from list if no diagosis is left
			if (removeFromDiagnosisList) {
				if (DiagnosisService.countRevisionToApprove(task) == 0) {
					task = workPhaseService.endDiagnosisPhase(task, removeFromDiagnosisList,
							notification ? NotificationStatus.NOTIFICATION_PENDING : NotificationStatus.NO_NOTFICATION);
				} else {
					MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.removeFromDiagnosisList.failed",
							"growl.diagnosis.removeFromDiagnosisList.failed.text");
				}
			}
		}

		// adding to notification phase
		if (notification)
			task = startNotificationPhase(task);

		if (removeFromCurrentWorklist) {
			// only remove from worklist if patient has one active task
			if (task.getParent().getTasks().stream().filter(p -> !p.getFinalized()).count() > 1) {
				MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
			} else {
				// remove from current worklist, view is updated
				worklistHandler.removePatientFromWorklist(task.getPatient());
			}
		}
	}

	public Task startNotificationPhase(Task task) {
		return favouriteListService.addTaskToList(task, PredefinedFavouriteList.NotificationList);
	}

}
