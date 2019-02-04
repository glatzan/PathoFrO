package com.patho.main.dialog.task

import com.patho.main.action.dialog.AbstractDialog
import com.patho.main.model.patient.Task

public abstract class AbstractTaskDialog() : AbstractDialog() {

    override fun initAndPrepareBean(task : Task): AbstractDialog {
        if (iniBean(task))
            prepareDialog();

        return this;
    }

    abstract fun iniBean(task : Task): Boolean;
}