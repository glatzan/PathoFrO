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

	private Patient patient;

	public void initAndPrepareBean(Patient patient) {
		initBean(patient);
		prepareDialog();
	}

	public void initBean(Patient patient) {
		setPatient(patient);

		super.initBean(null, Dialog.PATIENT_REMOVE);

		setPatient(patient);
	}

	public void removePatient() {
		logDAO.deletePatientLogs(patient);
		patientService.removePatient(patient);
	}
}
