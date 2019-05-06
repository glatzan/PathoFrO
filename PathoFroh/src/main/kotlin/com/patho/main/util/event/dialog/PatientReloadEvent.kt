package com.patho.main.util.event.dialog

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task

/**
 * Reload event for patient.
 */
class PatientReloadEvent(val patient: Patient, val task: Task? = null, val select: Boolean = false) : DialogReturnEvent()