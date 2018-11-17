package com.patho.main.action.dialog.diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Setter
@Getter
public class EditDiagnosisRevisionsDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	private DiagnosisRevision revision;

	public EditDiagnosisRevisionsDialog initAndPrepareBean(Task task, DiagnosisRevision revision) {
		if (initBean(task, revision))
			prepareDialog();

		return this;
	}

	/**
	 * Initializes all field of the object
	 * 
	 * @param task
	 */
	public boolean initBean(Task task, DiagnosisRevision revision) {
		this.revision = revision;
		return super.initBean(task, Dialog.DIAGNOSIS_REVISION_EDIT);
	}

	public void saveAndHide() {
		taskRepository.save(task, resourceBundle.get("log.patient.task.diagnosisRevisions.update", revision));
		hideDialog(new ReloadTaskEvent());
	}
}
