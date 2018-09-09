package com.patho.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.patho.main.model.patient.Patient;
import com.patho.main.repository.service.PatientRepositoryCustom;

public interface PatientRepository extends BaseRepository<Patient, Long>, PatientRepositoryCustom {

}
