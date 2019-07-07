package com.patho.main.dialog.search

import com.patho.main.common.Dialog
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.UserService
import com.patho.main.util.WorklistFactroy
import com.patho.main.util.dialog.event.WorklistSelectEvent
import com.patho.main.util.search.settings.SearchSettings
import com.patho.main.util.task.ArchiveTaskStatus
import com.patho.main.util.task.FlatTaskData
import com.patho.main.util.worklist.Worklist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
class ExportTasksDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val userService: UserService) : AbstractTaskDialog(Dialog.WORKLIST_EXPORT) {

    var tasks: List<Task> = listOf<Task>()

    var flatTasks: List<FlatTaskData> = listOf<FlatTaskData>()

    var selectedFlatTasks: MutableList<FlatTaskData> = mutableListOf<FlatTaskData>()

    var runDeferredLoad = true

    lateinit var search: SearchSettings

    fun initAndPrepareBean(search: SearchSettings) {
        if (initBean(search))
            prepareDialog()
    }

    fun initBean(search: SearchSettings): Boolean {
        this.search = search
        tasks = listOf()
        flatTasks = listOf()
        selectedFlatTasks.clear()
        runDeferredLoad = true
        return super.initBean()
    }

    override fun update() {
        logger.debug("Loading Data")
        tasks = search.getTasks()
        flatTasks = tasks.map { FlatTaskData(it) }
        runDeferredLoad = false
        print("end")
    }

    fun selectAndHide() {
        super.hideDialog(WorklistSelectEvent(WorklistFactroy.defaultWorklist(search, false)))
    }

}