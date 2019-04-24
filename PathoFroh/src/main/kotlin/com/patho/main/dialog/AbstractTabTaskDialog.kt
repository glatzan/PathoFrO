package com.patho.main.dialog

import com.patho.main.common.Dialog
import com.patho.main.model.patient.Task

abstract class AbstractTabTaskDialog(dialog: Dialog) : AbstractTabDialog_(dialog) {

    /**
     * Task
     */
    open lateinit var task: Task

    /**
     * Initializes and displays the dialog
     */
    open fun initAndPrepareBean(task: Task): AbstractDialog_ {
        if (initBean(task))
            prepareDialog()
        return this
    }

    open fun initBean(): Boolean {
        return initBean(task, forceInitialization = true)
    }

    open fun initBean(task: Task): Boolean {
        return initBean(task, forceInitialization = true)
    }

    open fun initBean(task: Task, forceInitialization: Boolean = true, tabName: String): Boolean {
        return initBean(forceInitialization, findTabByName(tabName))
    }

    open fun initBean(task: Task, forceInitialization: Boolean = true,
                      selectTab: AbstractTab? = null): Boolean {
        this.task = task
        return super.initBean(forceInitialization, selectTab)
    }
}