package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.log.Log;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.LogRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@Getter
@Setter
public class PatientLogDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private LogRepository logRepository;

	private Patient patient;

	private List<Log> patientLog;

	public PatientLogDialog initAndPrepareBean(Patient patient) {
		if (initBean(patient))
			prepareDialog();
		return this;
	}

	/**
	 * Initializes the bean
	 * 
	 * @param patient
	 */
	public boolean initBean(Patient patient) {
		setPatient(patient);
		setPatientLog(logRepository.findAllByLogInfoPatientOrderByIdAsc(patient));
		return super.initBean(null, Dialog.PATIENT_LOG);
	}
}
