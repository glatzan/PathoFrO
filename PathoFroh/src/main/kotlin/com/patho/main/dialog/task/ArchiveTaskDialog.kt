package com.patho.main.dialog.task

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.Dialog
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.ListItem
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.ListItemRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.TaskService
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.PatientReloadEvent
import com.patho.main.util.dialog.event.RemovePatientFromWorklistEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.status.ExtendedNotificationStatus
import com.patho.main.util.status.TotalTaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ArchiveTaskDialog @Autowired constructor(
        private val taskService: TaskService,
        private val listItemRepository: ListItemRepository,
        private val taskRepository: TaskRepository) : AbstractTaskDialog(Dialog.TASK_ARCHIVE) {

    /**
     * Status object of the task
     */
    open lateinit var taskStatus: TotalTaskStatus

    /**
     * Selected status for displaying infos
     */
    open var selectedReportIntentStatus: ExtendedNotificationStatus.ReportNotificationIntentStatus.ReportIntentStatus? = null

    /**
     * Contains a list of default reasons for filing a task
     */
    open lateinit var predefinedListItems: List<ListItem>

    /**
     * Transformer for list items, used by selectOneMenu
     */
    open lateinit var predefinedListItemTransformer: DefaultTransformer<ListItem>

    /**
     * Selected list item
     */
    open var selectedListItem: ListItem? = null

    /**
     * If true the archivation button will be rendered nevertheless archivation block favourite lists are present
     */
    open var forceFiling = false

    /**
     * Commentary for task archivation
     */
    open var commentary: String = ""

    /**
     * True if the patient containing the task should be removed from the current worklist
     */
    open var removeFromWorklist: Boolean = true

    /**
     * True if filing is possible, either task is completed or it is forced
     */
    open val isFilingPossible
        get() = forceFiling || taskStatus.isArchiveAble

    override fun initBean(task: Task): Boolean {
        val tmp = taskRepository.findByID(task, true, true, true, true, true)

        taskStatus = TotalTaskStatus(tmp)

        predefinedListItems = listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.TASK_ARCHIVE, false)

        predefinedListItemTransformer = DefaultTransformer(predefinedListItems)

        selectedReportIntentStatus = null

        forceFiling = false

        commentary = ""

        removeFromWorklist = !(task.patient?.tasks?.any { it.favouriteLists.any { it.id == PredefinedFavouriteList.NotificationList.id } }
                ?: true)

        return super.initBean(tmp)
    }

    /**
     * Called if a preset from the ListItems is selected. Copies the value auf the list Item into the
     * comment field
     */
    fun onSelectListItem() {
        if (selectedListItem != null)
            commentary = selectedListItem?.value ?: ""
    }

    /**
     * Archives the task and hides the dialog
     */
    fun archiveAndHide() {
        taskService.archiveTask(task, commentary)

        if (removeFromWorklist)
            super.hideDialog(RemovePatientFromWorklistEvent(task.patient!!))
        else
            super.hideDialog(PatientReloadEvent(task.patient!!))

        MessageHandler.sendGrowlMessagesAsResource("growl.task.archived.headline", "growl.task.archived.text")
    }
}