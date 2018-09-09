package com.patho.main.action.dialog.patient;

import java.util.List;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.log.Log;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.LogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PatientLogDialog extends AbstractDialog<PatientLogDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private LogRepository logRepository;

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
		setPatientLog(logRepository.findAllByLogInfoPatientOrderByIdAsc(patient));
		return super.initBean(null, Dialog.PATIENT_LOG);
	}
}
