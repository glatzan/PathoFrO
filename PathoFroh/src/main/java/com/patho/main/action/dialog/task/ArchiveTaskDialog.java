package com.patho.main.action.dialog.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.service.TaskService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class ArchiveTaskDialog extends AbstractDialog {

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
	private TaskService taskService;

	/**
	 * If true the task will be removed from worklist
	 */
	private boolean removeFromWorklist;

	/**
	 * commentary for restoring
	 */
	private String commentary;

	/**
	 * True if task was archived
	 */
	private boolean archiveSuccessful;

	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	public boolean initBean(Task task) {
		this.removeFromWorklist = true;

		super.initBean(task, Dialog.TASK_ARCHIVE);

		return true;
	}

	public void archiveTask() {
		try {

			taskService.archiveTask(getTask());

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

			mainHandlerAction.sendGrowlMessagesAsResource("growl.task.archived", "growl.task.archived.text");
			setArchiveSuccessful(true);
		} catch (Exception e) {
			onDatabaseVersionConflict();
		}
	}

	public void hideDialog() {
		super.hideDialog(new Boolean(archiveSuccessful));
	}
}
