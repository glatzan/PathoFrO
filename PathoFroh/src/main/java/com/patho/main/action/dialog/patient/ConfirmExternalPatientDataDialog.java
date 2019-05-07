package com.patho.main.action.dialog.patient;

import com.patho.main.util.event.dialog.PatientSelectEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value="session")
@Getter
@Setter
public class ConfirmExternalPatientDataDialog extends AbstractDialog {

	private Patient patient;

	private boolean confirmed;

	public void initAndPrepareBean(Patient patient) {
		initBean(patient);
		prepareDialog();
	}

	public void initBean(Patient patient) {
		super.initBean(null, Dialog.PATIENT_DATA_CONFIRM, false);
		setPatient(patient);
		setConfirmed(false);
	}

	public void hideDialogAndSelectItem() {
		super.hideDialog(new PatientSelectEvent(getPatient()));
	}
	
}
