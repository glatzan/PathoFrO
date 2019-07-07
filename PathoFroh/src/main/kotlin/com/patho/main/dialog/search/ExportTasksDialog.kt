package com.patho.main.dialog.search

import com.patho.main.common.Dialog
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.search.settings.SearchSettings
import com.patho.main.util.task.ArchiveTaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
class ExportTasksDialog @Autowired constructor(
        private val taskRepository: TaskRepository) : AbstractTaskDialog(Dialog.WORKLIST_EXPORT) {

    var tasks: List<Task> = listOf<Task>()

    var selectedTasks: MutableList<Task> = mutableListOf<Task>()

    var runDeferredLoad = true

    lateinit var search: SearchSettings

    fun initAndPrepareBean(search: SearchSettings) {
        if (initBean(search))
            prepareDialog()
    }

    fun initBean(search: SearchSettings): Boolean {
        this.search = search
        runDeferredLoad = true
        return super.initBean()
    }

    override fun update() {
        logger.debug("Loading Data")
        tasks = search.getTasks()
        runDeferredLoad = false
    }
}