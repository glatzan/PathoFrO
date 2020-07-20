package com.patho.main.model.interfaces

import com.patho.main.model.patient.Patient

interface PatientAccessible {
    val patient: Patient?
}