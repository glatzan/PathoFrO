package com.patho.main.util.search.settings

import com.patho.main.common.Eye
import com.patho.main.model.Physician
import com.patho.main.model.patient.Patient
import com.patho.main.model.person.Person
import net.sf.cglib.core.Local
import java.time.LocalDate

class ExtendedSearch : SearchSettings() {

    var patientName: String = ""
    var patientSurname: String = ""
    var patientBirthday: LocalDate? = null
    var patientGender: Person.Gender = Person.Gender.UNKNOWN


    var material: Array<String> = arrayOf<String>()
    var caseHistory: Array<String> = arrayOf<String>()

    var physician: Array<Physician> = arrayOf<Physician>()
    var signature: Array<Physician> = arrayOf<Physician>()

    var eye: Eye = Eye.UNKNOWN

    var ward: String = ""

    var malign: String = ""

    var diagnosis: Array<String> = arrayOf<String>()

    var diagnosisText: String = ""

    var from: LocalDate = LocalDate.now()
    var to: LocalDate = LocalDate.now()

    override fun getPatients(): List<Patient> {
        return listOf()
    }
}