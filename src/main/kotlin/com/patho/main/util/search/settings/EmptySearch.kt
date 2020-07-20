package com.patho.main.util.search.settings

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task

class EmptySearch : SearchSettings() {
    override fun getTasks(): List<Task> {
        return listOf()
    }

    override fun getPatients(): List<Patient> {
        return listOf()
    }
}