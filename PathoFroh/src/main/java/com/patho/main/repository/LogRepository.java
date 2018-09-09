package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.log.Log;
import com.patho.main.model.patient.Patient;

public interface LogRepository extends BaseRepository<Log, Long> {
	public List<Log> findAllByLogInfoPatientOrderByIdAsc(Patient patient);
}
