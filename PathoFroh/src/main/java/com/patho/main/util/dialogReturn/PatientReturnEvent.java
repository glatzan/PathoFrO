package com.patho.main.util.dialogReturn;

import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientReturnEvent implements DialogReturnEvent {

	private Patient patien;
	private Task task;

	public PatientReturnEvent() {
	}

	public PatientReturnEvent(Patient patient) {
		this.patien = patient;
	}

	public PatientReturnEvent(Patient patient, Task task) {
		this(patient);
		this.task = task;
	}
}
