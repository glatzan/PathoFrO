package com.patho.main.util.event;

import com.patho.main.model.patient.Patient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatientMergeEvent extends HistoEvent {
	private Patient mergeFrom;
	private Patient mergeTo;
}
