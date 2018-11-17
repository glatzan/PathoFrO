package com.patho.main.action.dialog.diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.service.TaskService;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class CopyHistologicalRecordDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	private Diagnosis diagnosis;

	public CopyHistologicalRecordDialog initAndPrepareBean(Diagnosis diagnosis) {
		if (initBean(diagnosis))
			prepareDialog();

		return this;
	}

	public boolean initBean(Diagnosis diagnosis) {
		setDiagnosis(diagnosis);
		super.initBean(task, Dialog.DIAGNOSIS_RECORD_OVERWRITE);
		return true;
	}

	public void copyHistologicalRecord(boolean overwrite) {
		taskService.copyHistologicalRecord(getDiagnosis(), overwrite);
	}

	public void hideDialog() {
		super.hideDialog(new ReloadTaskEvent());
	}

}
