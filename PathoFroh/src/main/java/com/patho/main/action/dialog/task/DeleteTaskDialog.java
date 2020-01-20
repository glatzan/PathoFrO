package com.patho.main.action.dialog.task;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionTemplate;

@Getter
@Setter
public class DeleteTaskDialog extends AbstractDialog {

	public static final int maxRevisionToDelete = 1;

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
