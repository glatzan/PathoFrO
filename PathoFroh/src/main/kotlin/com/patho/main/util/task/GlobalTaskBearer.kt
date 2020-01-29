package com.patho.main.util.task

import com.patho.main.model.patient.Task

/**
 * Object containing the current task
 */
// TODO Hold task in this object
class GlobalTaskBearer {

    lateinit var task: Task

    fun update(task: Task) {
        this.task = task
    }
}