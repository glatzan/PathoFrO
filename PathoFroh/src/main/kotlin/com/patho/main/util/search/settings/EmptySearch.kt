package com.patho.main.util.search.settings

import com.patho.main.model.patient.Patient

class EmptySearch : SearchSettings() {
    override fun getPatients(): List<Patient> {
        return listOf()
    }
}