package com.patho.main.util.search.settings

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.service.impl.SpringContextBridge

class StaticTaskListSearch : SearchSettings {

    private var taskIDs: List<Long> = listOf();

    constructor(tasks: List<Task>) {
        taskIDs = tasks.map { it.id }
    }

    override fun getTasks(): List<Task> {
        SpringContextBridge.services().taskRepository.findAllById(taskIDs)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPatients(): List<Patient> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}