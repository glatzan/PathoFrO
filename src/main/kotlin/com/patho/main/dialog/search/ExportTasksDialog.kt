package com.patho.main.dialog.search

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.UserService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.WorklistSelectEvent
import com.patho.main.util.search.settings.SearchSettings
import com.patho.main.util.task.FlatTaskData
import com.patho.main.util.ui.backend.CommandButtonStatus
import com.patho.main.util.worklist.WorklistFactroy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
@Scope(value = "session")
class ExportTasksDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val userService: UserService) : AbstractTaskDialog(Dialog.WORKLIST_EXPORT) {

    var tasks: List<Task> = listOf<Task>()

    var flatTasks: List<FlatTaskData> = listOf<FlatTaskData>()

    var selectedFlatTasks: MutableList<FlatTaskData> = mutableListOf<FlatTaskData>()

    var contentLoaded = false

    var columnModel: List<ColumnModel> = listOf()

    var selectedColumnModel: List<ColumnModel> = listOf()

    lateinit var columnModelTransformer: DefaultTransformer<ColumnModel>

    val exportButton: CommandButtonStatus = object : CommandButtonStatus() {
        override var isDisabled: Boolean
            get() = !contentLoaded || selectedFlatTasks.isEmpty()
            set(value) {}
    }

    val loadWorklistBtn: CommandButtonStatus = object : CommandButtonStatus() {
        override var isDisabled: Boolean
            get() = selectedFlatTasks.isEmpty()
            set(value) {}
    }

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
        contentLoaded = true

        columnModel = ColumnModel.factory(FlatTaskData::class.java.declaredFields.map { it.name })
        selectedColumnModel = columnModel.filter { it.selected }
        columnModelTransformer = DefaultTransformer<ColumnModel>(columnModel)

        return super.initBean()
    }

    override fun update() {
        logger.debug("Loading Data")
        tasks = search.getTasks()
        flatTasks = tasks.map { FlatTaskData(it) }
    }

    fun contentLoaded() {
        contentLoaded = true
    }

    fun selectAndHide() {
        super.hideDialog(WorklistSelectEvent(WorklistFactroy.defaultWorklist(search, false)))
    }

    class ColumnModel(propertyName: String, override var id: Long) : Serializable, ID {
        var header: String
        var property: String = propertyName
        var selected: Boolean

        init {
            println(propertyName)
            header = SpringContextBridge.services().resourceBundle["mics.flatTask.$propertyName"]
            if (header.startsWith("???"))
                header = propertyName
            selected = propertyName.startsWith("ed") || true
        }

        companion object {
            fun factory(propertyNames: List<String>): List<ColumnModel> {
                return propertyNames.filter { it[0] == 'e' }.mapIndexed { index, obj -> ColumnModel(obj, index.toLong()) }
            }
        }
    }
}