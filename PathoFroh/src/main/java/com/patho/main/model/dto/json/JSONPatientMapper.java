package com.patho.main.model.dto.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.patho.main.model.Contact;
import com.patho.main.model.Person;
import com.patho.main.model.patient.Patient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONPatientMapper {

	private String titel;
	private String name;
	private String vorname;
	private String geburtsdatum;
	private String ort;
	private String land;
	private String plz;
	private String anschrift;
	private String tel;
	private String weiblich;
	private String piz;
	private String krankenkasse;

	private String error;

	public Patient getPatient() {
		return getPatient(new Patient(new Person(new Contact())));
	}

	public Patient getPatient(Patient patient) {

		patient.getPerson().setTitle(getTitel());
		patient.getPerson().setLastName(getName());
		patient.getPerson().setFirstName(getVorname());

		if (getGeburtsdatum() != null) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
			try {
				Date date = format.parse(getGeburtsdatum());
				patient.getPerson().setBirthday(date);
			} catch (ParseException e) {
				patient.getPerson().setBirthday(new Date());
			}
		} else {
			patient.getPerson().setBirthday(new Date());
		}

		patient.setInsurance(getKrankenkasse());
		patient.setPiz(getPiz());
		patient.getPerson().getContact().setTown(getOrt());
		patient.getPerson().getContact().setPostcode(getPlz());
		patient.getPerson().getContact().setStreet(getAnschrift());

		patient.getPerson().getContact().setEmail("");
		patient.getPerson().getContact().setPhone(getTel());

		// patient.getPerson().setGender(getWeiblich().equals("1") ?
		// Person.Gender.FEMALE : Person.Gender.MALE);

		return patient;
	}
}
