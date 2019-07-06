package com.patho.main.util.search.settings

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import org.slf4j.LoggerFactory

abstract class SearchSettings {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    abstract fun getPatients() : List<Patient>

    abstract fun getTasks() : List<Task>
}