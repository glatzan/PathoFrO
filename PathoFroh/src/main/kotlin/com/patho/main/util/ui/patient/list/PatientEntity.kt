package com.patho.main.util.ui.patient.list

import com.patho.main.model.patient.Patient

class PatientEntity (var patient: Patient) {

    var tasks : MutableSet<TaskEntity> = patient.tasks.map { TaskEntity(it) }.toMutableSet()

    public fun update(patient: Patient){

    }
}