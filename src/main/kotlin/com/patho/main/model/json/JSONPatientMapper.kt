package com.patho.main.model.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.patho.main.model.patient.Patient
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@JsonIgnoreProperties(ignoreUnknown = true)
class JSONPatientMapper {
    var titel: String? = null
    var name: String? = null
    var vorname: String? = null
    var geburtsdatum: String? = null
    var ort: String? = null
    var land: String? = null
    var plz: String? = null
    var anschrift: String? = null
    var tel: String? = null
    var weiblich: String? = null
    var piz: String? = null
    var krankenkasse: String? = null

    var error: String? = null

    fun getPatient(): Patient {
        return getPatient(Patient(Person(Contact())))
    }

    fun getPatient(patient: Patient): Patient {

        patient.person.title = titel ?: ""
        patient.person.lastName = name ?: ""
        patient.person.firstName = vorname ?: ""

        if (geburtsdatum != null) {
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            try {
                val date = LocalDate.parse(geburtsdatum, format)
                patient.person.birthday = date
            } catch (e: DateTimeParseException) {
                patient.person.birthday = LocalDate.now()
            }

        } else {
            patient.person.birthday = LocalDate.now()
        }

        patient.insurance = krankenkasse ?: ""
        patient.piz = piz ?: ""
        patient.person.contact.town = ort ?: ""
        patient.person.contact.postcode = plz ?: ""
        patient.person.contact.street = anschrift ?: ""

        patient.person.contact.email = ""
        patient.person.contact.phone = tel ?: ""

        // patient.getPerson().setGender(getWeiblich().equals("1") ?
        // Person.Gender.FEMALE : Person.Gender.MALE);

        return patient
    }
}
