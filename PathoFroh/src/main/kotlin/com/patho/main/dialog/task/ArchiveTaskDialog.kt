package com.patho.main.dialog.task

import com.patho.main.common.Dialog
import com.patho.main.config.settings.ServiceSettings
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.service.TaskService
import com.patho.main.util.task.AdvancedTaskStatus
import com.patho.main.util.task.ArchiveTaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ArchiveTaskDialog @Autowired constructor(
        private val taskService: TaskService,
        private val serviceSettings: ServiceSettings) : AbstractTaskDialog(Dialog.TASK_ARCHIVE) {


    @Autowired
    lateinit var serviceSettings: ServiceSettings

    open lateinit var status: ArchiveTaskStatus

    var taskStatus: AdvancedTaskStatus? = null

    var commentary: String = ""

    var removeFromWorklist: Boolean = true

    override fun initBean(task: Task): Boolean {
        super.initBean(task)
        println(serviceSettings)
        println("hallo")
        println(serviceSettings.taskArchiveRules.blockingFavouriteLists)

        this.taskStatus = AdvancedTaskStatus(task).generateStatus()

        task.taskStatus

        return true
    }

    fun hideAndArchive() {
        // taskService.archiveTask(task)

//		if (removeFromWorklist) {
//			// only remove from worklist if patient has one active task
//			if (task.getPatient().getTasks().stream().filter(p -> !p.isFinalized()).count() > 1) {
//				mainHandlerAction.sendGrowlMessagesAsResource("growl.error", "growl.error.worklist.remove.moreActive");
//			} else {
//				worklistViewHandler.removePatientFromWorklist(task.getPatient());
//			}
//		}
//
//		mainHandlerAction.sendGrowlMessagesAsResource("growl.task.archived", "growl.task.archived.text");
    }
}