package com.patho.main.dialog

import com.patho.main.common.Dialog
import com.patho.main.model.patient.Task

/**
 * Task dialog
 */
abstract class AbstractTaskDialog(dialog: Dialog) : AbstractDialog_(dialog) {

    /**
     * Task
     */
    protected lateinit var task: Task

    /**
     * Initializes and displays the dialog
     */
    open fun initAndPrepareBean(task: Task): AbstractDialog_ {
        if (initBean(task))
            prepareDialog()

        return this
    }

    open fun initBean(task: Task): Boolean {
        this.task = task
        return true
    }
}