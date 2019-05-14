package com.patho.main.util.ui.patient.list

import com.patho.main.model.patient.Task
import com.patho.main.util.task.TaskStatus

class TaskEntity(var task : Task){

    var status = TaskStatus(task)

    public fun update (task: Task){
        this.task = task
        this.status = TaskStatus(task)
    }
}