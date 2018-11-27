package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class MergePatientConfimDialog extends AbstractDialog {

	@Override
	public boolean initBean() {
		return super.initBean(Dialog.PATIENT_MERGE_CONFIRM);
	}

	public void confirmAndHide() {
		hideDialog(new MergePatientConfimDialogReturnEvent(true));
	}

	/**
	 * 
	 * @author dvk-glatza
	 *
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	public static class MergePatientConfimDialogReturnEvent implements DialogReturnEvent {
		private boolean mergeConfirmed;
	}
};
