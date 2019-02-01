package com.patho.main.action.dialog.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class DeleteTaskDialog extends AbstractDialog {

	public static final int maxRevisionToDelete = 1;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandler worklistViewHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	private boolean deleteAble;

	public DeleteTaskDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
		return this;
	}

	public boolean initBean(Task task) {
		setDeleteAble(!taskWasAltered());
		return super.initBean(task, Dialog.TASK_DELETE, false);
	}

	public boolean taskWasAltered() {
//		List<Task> revisions = taskDAO.getTasksRevisions(task.getId());
//
//		log.debug(revisions.size() + " Revsions available");
//
//		if (revisions.size() > maxRevisionToDelete) {
//			return true;
//		}
//
		return false;
	}

	public void deleteTask() {
//		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//
//			public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//				genericDAO.reattach(getTask());
//				genericDAO.reattach(getTask().getPatient());
//				favouriteListDAO.removeTaskFromAllLists(getTask());
//				bioBankDAO.removeAssociatedBioBank(getTask());
//				genericDAO.deletePatientData(task, "log.patient.task.remove", task.toString());
//				task.getPatient().getTasks().remove(task);
//			}
//		});
//
//		taskDAO.lock(getTask().getParent());
	}

}
