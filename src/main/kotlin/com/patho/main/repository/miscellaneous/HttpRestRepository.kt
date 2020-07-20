package com.patho.main.repository.miscellaneous

import com.patho.main.config.excepion.ToManyEntriesException
import com.patho.main.model.patient.Patient
import java.time.LocalDate
import java.util.*

/**
 * Rest Repository for request to foreign rest services
 */
interface HttpRestRepository {

    /**
     * Creates a patient in the pdv backend and returns a valid piz if successful.
     */
    fun createPatientInPDV(patient: Patient): String?

    /**
     * Finds a patient via piz
     */
    fun findPatientByPIZ(piz: String): Optional<Patient>

    /**
     * Search for patient via name, surname and birthday
     */
    @Throws(ToManyEntriesException::class)
    fun findPatientByNameAndSurNameAndBirthday(name: String, surname: String, birthday: LocalDate?): List<Patient>

    /**
     * Sends a rest request, awaits an json file matching the JSONPatientMapper object as return of that request
     */
    fun findPatient(url: String): Optional<Patient>

    /**
     * Sends a rest request, awaits an json file matching a list of JSONPatientMapper objects as return of that request
     */
    @Throws(ToManyEntriesException::class)
    fun findPatientAll(url: String): List<Patient>
}