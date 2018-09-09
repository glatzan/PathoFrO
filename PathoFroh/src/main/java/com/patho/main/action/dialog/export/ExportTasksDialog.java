package com.patho.main.action.dialog.export;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value = "session")
@Setter
@Getter
public class ExportTasksDialog extends AbstractDialog {
	
	private List<Task> tasks;
	
	private List<Task> tasksToExport;
	
	public void initAndPrepareBean(List<Task> tasks) {
		if (initBean(tasks))
			prepareDialog();
	}

	public boolean initBean(List<Task> tasks) {
		super.initBean(null, Dialog.WORKLIST_EXPORT);
		
		this.tasks = tasks;
		this.tasksToExport = tasks;

		return true;
	}

}
