package com.patho.main.ui.task;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoPermissions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable(preConstruction = true)
public class TaskInfo {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	private Task task;

	private HashSet<Long> admentRevisions = new HashSet<Long>();

	public TaskInfo(Task task) {
		logger.debug("New Task Info");
		this.task = task;
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

		// Blocked
		// TODO: Blocking

		return true;
	}

	public boolean isDiagnosisRevisionEditable(DiagnosisRevision diagnosisRevision) {
		if (!isTaksEditable())
			return false;

		if (diagnosisRevision.getCompletionDate() != 0 && !isDiagnosisRevisionInEditAmendmentMode(diagnosisRevision))
			return false;

		return true;
	}

	public boolean isDiagnosisRevisionInEditAmendmentMode(DiagnosisRevision diagnosisRevision) {
		return admentRevisions.contains(diagnosisRevision.getId());
	}

	public void admendRevision(DiagnosisRevision diagnosisRevision) {
		admentRevisions.add(diagnosisRevision.getId());
	}

	public void lockRevision(DiagnosisRevision diagnosisRevision) {
		admentRevisions.remove(diagnosisRevision.getId());
	}
}
