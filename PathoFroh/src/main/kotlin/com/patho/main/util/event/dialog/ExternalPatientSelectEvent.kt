package com.patho.main.util.event.dialog

import com.patho.main.model.patient.Patient

/**
 * Select event for external patients
 */
class ExternalPatientSelectEvent(patient: Patient) : SelectEvent<Patient>(patient)