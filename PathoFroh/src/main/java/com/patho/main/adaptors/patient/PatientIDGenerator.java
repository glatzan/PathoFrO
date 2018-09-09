package com.patho.main.adaptors.patient;

import java.io.File;
import java.io.StringWriter;

import com.patho.main.model.Person.Gender;
import com.patho.main.model.patient.Patient;
import com.patho.main.util.helper.TimeUtil;

import lombok.Getter;
import lombok.Setter;

/*
 * -<PatientData>
 * 
 * <Surname>Hairer</Surname>
 * 
 * <Givenname>Martin</Givenname>
 * 
 * <Birthname>Hairer</Birthname>
 * 
 * <BirthDate>1975-11-14</BirthDate>
 * 
 * <PersonalTitle>Prof. Dr.</PersonalTitle>
 * 
 * <JobTitle>Mathematiker</JobTitle>
 * 
 * <CountryCodePostal>CH</CountryCodePostal>
 * 
 * <PLZ>1202</PLZ>
 * 
 * <City>Geneve</City>
 * 
 * <Street>Avenue de France 40</Street>
 * 
 * <TelephoneNumber>+49 761 270 41680</TelephoneNumber>
 * 
 * </PatientData>
 */
public class PatientIDGenerator {

	public long generatePatientID(Patient patient) {
		(new PatientData(patient)).getAsXML();
		return 0;
	}
}
