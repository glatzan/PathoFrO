package com.patho.main.repository;

import com.patho.main.model.log.Log;
import com.patho.main.model.patient.Patient;

import java.util.List;

public interface LogRepository extends BaseRepository<Log, Long> {
	public List<Log> findAllByLogInfoPatientOrderByIdAsc(Patient patient);
}
