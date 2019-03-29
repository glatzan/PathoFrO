package com.patho.main.dialog.task

import com.patho.main.action.dialog.AbstractDialog
import com.patho.main.model.patient.Task

abstract class AbstractTaskDialog() : AbstractDialog() {

    override fun initAndPrepareBean(task: Task): AbstractDialog {
        if (initBean(task))
            prepareDialog();

        return this;
    }

    abstract fun initBean(task: Task): Boolean;
}