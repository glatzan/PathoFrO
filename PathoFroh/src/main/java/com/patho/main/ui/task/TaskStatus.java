package com.patho.main.ui.task;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoPermissions;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Task status class, is used for gui request because the isInLists requests
 * will slow the gui down a lot.
 * 
 * @author andi
 *
 */
@Getter
@Setter
@Configurable(preConstruction = true)
public class TaskStatus {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	private Task task;

	private boolean stainingNeeded;
	private boolean reStainingNeeded;
	private boolean stayInStainingList;

	private boolean diagnosisNeeded;
	private boolean reDiagnosisNeeded;
	private boolean stayInDiagnosisList;

	private boolean notificationNeeded;
	private boolean stayInNotificationList;

	private boolean councilLendingMTA;
	private boolean councilLendingSecretary;
	private boolean councilPending;
	private boolean councilCompleted;

	private boolean scannList;

	private boolean finalized;

	private boolean finalizeable;

	private boolean editable;

	private boolean actionPending;

	private boolean active;

	public TaskStatus(Task task) {
		this.task = task;
		updateStatus();
	}

	public void updateStatus() {
		this.stainingNeeded = task.isListedInFavouriteList(PredefinedFavouriteList.StainingList);
		this.reStainingNeeded = task.isListedInFavouriteList(PredefinedFavouriteList.ReStainingList);
		this.stayInStainingList = task.isListedInFavouriteList(PredefinedFavouriteList.StayInStainingList);

		this.diagnosisNeeded = task.isListedInFavouriteList(PredefinedFavouriteList.DiagnosisList);
		this.reDiagnosisNeeded = task.isListedInFavouriteList(PredefinedFavouriteList.ReDiagnosisList);
		this.stayInDiagnosisList = task.isListedInFavouriteList(PredefinedFavouriteList.StayInDiagnosisList);

		this.notificationNeeded = task.isListedInFavouriteList(PredefinedFavouriteList.NotificationList);
		this.stayInNotificationList = task.isListedInFavouriteList(PredefinedFavouriteList.StayInNotificationList);

		this.councilLendingMTA = task.isListedInFavouriteList(PredefinedFavouriteList.CouncilLendingMTA);
		this.councilLendingSecretary = task.isListedInFavouriteList(PredefinedFavouriteList.CouncilLendingSecretary);
		this.councilPending = task.isListedInFavouriteList(PredefinedFavouriteList.CouncilPending);
		this.councilCompleted = task.isListedInFavouriteList(PredefinedFavouriteList.CouncilCompleted);

		this.scannList = task.isListedInFavouriteList(PredefinedFavouriteList.ScannList);

		this.finalized = task.isFinalized();

		this.editable = isTaksEditable();

		this.actionPending = task.isActiveOrActionPending();

		this.active = task.isActive();

		this.finalizeable = !(this.stainingNeeded || this.reStainingNeeded || this.stayInStainingList
				|| this.diagnosisNeeded || this.reDiagnosisNeeded || this.stayInDiagnosisList || this.notificationNeeded
				|| this.stayInNotificationList || task.isFinalized());

	}

	public boolean isStainingCompleted() {
		return !(this.stainingNeeded || this.reDiagnosisNeeded);
	}

	public boolean isTaksEditable() {
		// task is editable
		// users and guest can't edit anything
		if (!userHandlerAction.currentUserHasPermission(HistoPermissions.TASK_EDIT)) {
			return false;
		}

		// finalized
		if (task.isFinalized()) {
			return false;
		}

		// if (isDiagnosisCompleted(task) && isStainingCompleted(task))
		// return false;

		// Blocked
		// TODO: Blocking

		return true;
	}

	public static boolean checkIfStainingCompleted(Patient patient) {
		return patient.getTasks().stream().allMatch(p -> checkIfStainingCompleted(p));
	}

	public static boolean checkIfStainingCompleted(Task task) {
		return task.getSamples().stream().allMatch(p -> checkIfStainingCompleted(p));
	}

	public static boolean checkIfStainingCompleted(Sample sample) {
		return sample.getBlocks().stream().allMatch(p -> checkIfStainingCompleted(p));
	}

	public static boolean checkIfStainingCompleted(Block block) {
		return block.getSlides().stream().allMatch(p -> p.isStainingCompleted());
	}

	public static boolean checkIfReStainingFlag(Patient patient) {
		return patient.getTasks().stream().anyMatch(p -> checkIfReStainingFlag(p));
	}

	public static boolean checkIfReStainingFlag(Task task) {
		return task.getSamples().stream().anyMatch(p -> checkIfReStainingFlag(p));
	}

	public static boolean checkIfReStainingFlag(Sample sample) {
		return sample.getBlocks().stream().anyMatch(p -> checkIfReStainingFlag(p));
	}

	public static boolean checkIfReStainingFlag(Block block) {
		return block.getSlides().stream().anyMatch(p -> p.isReStaining());
	}

}
