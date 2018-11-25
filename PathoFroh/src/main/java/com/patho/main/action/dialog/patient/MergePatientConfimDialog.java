package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;

public class MergePatientConfimDialog extends AbstractDialog {

	@Override
	public boolean initBean() {
		return super.initBean(Dialog.PATIENT_MERGE_CONFIRM);
	}

	public void confirmAndClose() {
		hideDialog(new Boolean(true));
	}
};
