package com.patho.main.action.dialog.patient;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.PatientRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class EditPatientDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	private Patient patient;

	public void initAndPrepareBean(Patient patient) {
		initBean(patient);
		prepareDialog();
	}

	public void initBean(Patient patient) {
		setPatient(patient);

		super.initBean(null, Dialog.PATIENT_EDIT);

		setPatient(patient);
	}

	public void savePatientData() {
		patientRepository.save(getPatient(), resourceBundle.get("log.patient.edit"));
	}

	/**
	 * Is called from return of the merge dialog. If merging was successful the edit
	 * patient dialog will be closed with the event object
	 * 
	 * @param event
	 */
	public void onMergeReturn(SelectEvent event) {
		// TODO Handle event
	}
}
