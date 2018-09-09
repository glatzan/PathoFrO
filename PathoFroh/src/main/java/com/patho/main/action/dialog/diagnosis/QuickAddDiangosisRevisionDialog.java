package com.patho.main.action.dialog.diagnosis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.service.DiagnosisService;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Dialog for adding a diagnosis revision on creating a restaining
 * 
 * @author andi
 *
 */
@Configurable
@Setter
@Getter
public class QuickAddDiangosisRevisionDialog extends AbstractDialog<QuickAddDiangosisRevisionDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	public QuickAddDiangosisRevisionDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.DIAGNOSIS_REVISION_ADD);
		return true;
	}

	public void createDiagnosisAndHide() {
		diagnosisService.createDiagnosisRevision(getTask(), DiagnosisRevisionType.DIAGNOSIS_REVISION);
		MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.create.rediagnosis");
		hideDialog(new ReloadTaskEvent());
	}
}
