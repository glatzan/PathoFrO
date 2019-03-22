package com.patho.main.action.views

import com.patho.main.model.MaterialPreset
import com.patho.main.model.patient.Task


/**
 * Task associated view
 */
abstract class AbstractTaskView : AbstractView() {

    open lateinit var task: Task

    /**
     * Initializes the current view
     */
    override fun loadView() {}

    /**
     * Initializes the current view
     */
    open fun loadView(task: Task) {
        this.task = task
    }
}