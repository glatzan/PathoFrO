package com.patho.main.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.patient.Patient;

public interface JSONPatientRepository {
	public Optional<Patient> findByPIZ(String piz);

	public List<Patient> findByNameAndSurNameAndBirthday(String name, String surname, Date birthday)
			throws ToManyEntriesException;

	public Optional<Patient> find(String url);

	public List<Patient> findAll(String url) throws ToManyEntriesException;
}
