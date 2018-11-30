package com.patho.main.action.dialog.diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.service.DiagnosisService;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Setter
@Getter
public class DeleteDiagnosisRevisionDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	private DiagnosisRevision diagnosisRevision;

	public DeleteDiagnosisRevisionDialog initAndPrepareBean(DiagnosisRevision diagnosisRevision) {
		if (initBean(diagnosisRevision))
			prepareDialog();
		return this;
	}

	public boolean initBean(DiagnosisRevision diagnosisRevision) {
		super.initBean(diagnosisRevision.getTask(), Dialog.DIAGNOSIS_REVISION_DELETE);
		this.diagnosisRevision = diagnosisRevision;
		return true;
	}

	public void deleteAndHide() {
		try {
			diagnosisService.removeDiagnosisRevision(task, diagnosisRevision);
			MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.delete", "growl.diagnosis.delete.text");
		} catch (CustomUserNotificationExcepetion e) {
			MessageHandler.sendGrowlMessagesAsResource(e);
		}
		hideDialog(new ReloadTaskEvent());
	}
}
