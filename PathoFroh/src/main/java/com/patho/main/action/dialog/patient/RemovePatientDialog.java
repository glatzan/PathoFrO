package com.patho.main.action.dialog.patient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.dao.LogDAO;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.PatientService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope(value = "session")
@Getter
@Setter
public class RemovePatientDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private LogDAO logDAO;

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
		try {
			logDAO.deletePatientLogs(patient);
			patientService.removePatient(patient);
		} catch (Exception e) {
			onDatabaseVersionConflict();
		}
	}
}
