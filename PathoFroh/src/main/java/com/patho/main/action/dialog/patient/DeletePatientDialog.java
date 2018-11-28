package com.patho.main.action.dialog.patient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandler;
import com.patho.main.common.Dialog;
import com.patho.main.dao.LogDAO;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.PatientService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DeletePatientDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	private Patient patient;

	public DeletePatientDialog initAndPrepareBean(Patient patient) {
		if (initBean(patient))
			prepareDialog();
		return this;
	}

	public boolean initBean(Patient patient) {
		setPatient(patient);
		return super.initBean(Dialog.PATIENT_REMOVE);
	}

	public void deleteAndHide() {
		patientService.deleteOrArchive(patient);
		hideDialog(returnValue);
	}
}
