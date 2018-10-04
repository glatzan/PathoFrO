package com.patho.main.action.dialog.patient;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class DeleteTaskDialog extends AbstractDialog {

	public static final int maxRevisionToDelete = 1;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	private boolean deleteAble;

	public void initAndPrepareBean(Task task) {
		initBean(task);
		prepareDialog();
	}

	public void initBean(Task task) {
		super.initBean(task, Dialog.TASK_DELETE, false);
		setDeleteAble(!taskWasAltered());
	}

	public boolean taskWasAltered() {
		List<Task> revisions = taskDAO.getTasksRevisions(task.getId());

		log.debug(revisions.size() + " Revsions available");

		if (revisions.size() > maxRevisionToDelete) {
			return true;
		}

		return false;
	}

	public void deleteTask() {
		try {

			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					genericDAO.reattach(getTask());
					genericDAO.reattach(getTask().getPatient());
					favouriteListDAO.removeTaskFromAllLists(getTask());
					bioBankDAO.removeAssociatedBioBank(getTask());
					genericDAO.deletePatientData(task, "log.patient.task.remove", task.toString());
					task.getPatient().getTasks().remove(task);
				}
			});

			taskDAO.lock(getTask().getParent());

		} catch (Exception e) {
			onDatabaseVersionConflict();
		}
	}

	public void onDatabaseVersionConflict() {
		worklistViewHandlerAction.replacePatientInCurrentWorklist(getTask().getParent());
		super.onDatabaseVersionConflict();
	}

}
