package com.patho.main.action.dialog.miscellaneous;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.patient.DeletePatientDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class ConfirmDialog extends AbstractDialog {

	private String headline;
	private String text;

	public ConfirmDialog initAndPrepareBean(String healine, String text) {
		if (initBean(healine, text))
			prepareDialog();
		return this;
	}

	public boolean initBean(String healine, String text) {
		setHeadline(headline);
		setText(text);
		return super.initBean(Dialog.PATIENT_REMOVE);
	}
	
	public void confirmAndHide() {
		
	}
	
}
