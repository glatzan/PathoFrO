package com.patho.main.dialog.task

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.Dialog
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.TaskService
import com.patho.main.util.event.dialog.PatientReloadEvent
import com.patho.main.util.event.dialog.RemovePatientFromWorklistEvent
import com.patho.main.util.status.reportIntent.ReportIntentBearer
import com.patho.main.util.task.ArchiveTaskStatus
import com.patho.main.util.task.TaskNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ArchiveTaskDialog @Autowired constructor(
        private val taskService: TaskService,
        private val taskRepository: TaskRepository) : AbstractTaskDialog(Dialog.TASK_ARCHIVE) {

    open lateinit var status: ArchiveTaskStatus

    /**
     * Selected status for displaying infos
     */
    open var selectedReportIntentStatus: ReportIntentBearer? = null

    /**
     * If true the archivation button will be rendered nevertheless archivation block favourite lists are present
     */
    open var forceArchivation = false

    /**
     * Commentary for task archivation
     */
    open var commentary: String = ""

    /**
     * True if the patient containing the task should be removed from the current worklist
     */
    open var removeFromWorklist: Boolean = true

    /**
     * True if archivation is possible
     */
    open val isArchivationPossible
        get() = forceArchivation || status.isArchiveAble

    override fun initBean(task: Task): Boolean {
        val tmp = taskRepository.findOptionalByIdAndInitialize(task, true, true, true, true, true)

        if (!tmp.isPresent)
            throw TaskNotFoundException()

        status = ArchiveTaskStatus(tmp.get())

        selectedReportIntentStatus = null

        forceArchivation = false

        commentary = ""

        removeFromWorklist = !(task.patient?.tasks?.any { it.favouriteLists.any { it.id == PredefinedFavouriteList.NotificationList.id } }
                ?: true)

        return super.initBean(tmp.get())
    }

    /**
     * Archives the task and hides the dialog
     */
    fun archiveAndHide() {
        taskService.archiveTask(task)

        if (removeFromWorklist)
            super.hideDialog(RemovePatientFromWorklistEvent(task.patient!!))
        else
            super.hideDialog(PatientReloadEvent(task.patient!!))

        MessageHandler.sendGrowlMessages("growl.task.archived.headline", "growl.task.archived.text")
    }
}