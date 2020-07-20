package com.patho.main.dialog.task

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.repository.jpa.ListItemRepository
import com.patho.main.service.TaskService
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.TaskReloadEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for dearchving tasks
 */
@Component()
@Scope(value = "session")
open class DearchiveTaskDialog @Autowired constructor(
        private val listItemRepository: ListItemRepository,
        private val taskService: TaskService) : AbstractTaskDialog(Dialog.TASK_DEARCHIVE) {

    /**
     * Contains a list of default reasons for dearchving a task
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
     * Commentary for restoring
     */
    open var commentary: String = ""

    override fun initBean(task: Task): Boolean {
        predefinedListItems = listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.TASK_RESTORE, false)
        predefinedListItemTransformer = DefaultTransformer(predefinedListItems)
        commentary = ""
        return super.initBean(task)
    }

    /**
     * Called if a preset from the ListItems is selected. Copies the value auf the list Item into the
     * comment field
     */
    fun onSelectListItem() {
        if (selectedListItem != null)
            commentary = selectedListItem?.value ?: ""
    }

    fun dearchiveAndHide() {
        taskService.dearchiveTask(task, commentary);
        hideDialog(TaskReloadEvent())
    }
}