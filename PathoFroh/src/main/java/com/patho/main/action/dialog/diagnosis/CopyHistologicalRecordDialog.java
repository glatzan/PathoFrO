package com.patho.main.action.dialog.diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.TaskService;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value = "session")
@Getter
@Setter
public class CopyHistologicalRecordDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	private Diagnosis diagnosis;

	public void initAndPrepareBean(Diagnosis diagnosis) {
		if (initBean(diagnosis))
			prepareDialog();
	}

	public boolean initBean(Diagnosis diagnosis) {
		setDiagnosis(diagnosis);
		super.initBean(task, Dialog.DIAGNOSIS_RECORD_OVERWRITE);
		return true;
	}

	public void copyHistologicalRecord(boolean overwrite) {
		getDiagnosis().getTask().setVersion(getDiagnosis().getTask().getVersion() + 2);
		taskService.copyHistologicalRecord(getDiagnosis(), overwrite);
	}

	public void hideDialog(boolean reload) {
		super.hideDialog(reload ? new ReloadTaskEvent() : null);
	}

}
