package com.patho.main.repository.jpa;

import com.patho.main.model.patient.Patient;
import com.patho.main.repository.jpa.custom.PatientRepositoryCustom;

public interface PatientRepository extends BaseRepository<Patient, Long>, PatientRepositoryCustom {

}
