package com.patho.main.repository;

import com.patho.main.model.patient.Patient;
import com.patho.main.repository.service.PatientRepositoryCustom;

public interface PatientRepository extends BaseRepository<Patient, Long>, PatientRepositoryCustom {

}
