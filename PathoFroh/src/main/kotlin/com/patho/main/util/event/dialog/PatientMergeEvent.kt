package com.patho.main.util.event.dialog

import com.patho.main.model.patient.Patient

/**
 * Merge and reload event on patient merge
 */
class PatientMergeEvent(val source: Patient, val target: Patient) : ReloadEvent()