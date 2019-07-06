package com.patho.main.dialog.slides

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.common.Dialog
import com.patho.main.common.GuiCommands
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.ListItem
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.repository.ListItemRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.service.SlideService
import com.patho.main.ui.StainingTableChooser
import com.patho.main.util.dialog.event.StainingPhaseExitEvent
import com.patho.main.util.dialog.event.StainingPhaseUpdateEvent
import com.patho.main.util.dialog.event.TaskEntityDeleteEvent
import com.patho.main.util.dialog.event.TaskReloadEvent
import com.patho.main.util.task.TaskStatus
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component()
@Scope(value = "session")
open class SlideOverviewDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val listItemRepository: ListItemRepository,
        private val workPhaseHandler: WorkPhaseHandler,
        private val slideService: SlideService) : AbstractTaskDialog(Dialog.SLIDE_OVERVIEW) {


    /**
     * Task entity list in a flat form
     */
    open var flatTaskEntityList: MutableList<StainingTableChooser<out IdManuallyAltered>> = mutableListOf<StainingTableChooser<out IdManuallyAltered>>()

    /**
     * Contains all available case histories
     */
    open var slideCommentary: List<ListItem> = listOf<ListItem>()

    /**
     * Is used for selecting a chooser from the generated list.
     * It is used to edit the names of the entities by an overlaypannel
     */
    open var selectedStainingTableChooser: StainingTableChooser<out IdManuallyAltered>? = null

    /**
     * Initializes the dialog and reloads the task
     */
    override fun initBean(task: Task): Boolean {
        val result = super.initBean(task)
        slideCommentary =
                listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false);
        update(true)
        return result
    }

    /**
     * Updates task and the flat map, if reload is true the task will be fetched from the database
     */
    override fun update(reload: Boolean) {

        if (reload) {
            task = taskRepository.findByID(task.id, true, true, true, true,
                    true)
        }

        task.generateTaskStatus()
        flatTaskEntityList = StainingTableChooser.factory(task, false)
    }

    /**
     * Return handler for the create Sample- and Slide-Dialog
     */
    open fun onStainingPhaseUpdateReturn(event: SelectEvent) {
        if (event.`object` is StainingPhaseUpdateEvent) {
            logger.debug("Staining phase update event")
            // reloading task because slides might have been added
            update(true)
            // updating the stating phase, if stainig phase ends the staining pahse exit dialog is shown
            val tmp = workPhaseHandler.updateStainingPhase(task, GuiCommands.OPEN_STAINING_PHASE_EXIT_DIALOG_FROM_SLIDE_OVERVIEW_DIALOG)
            task = tmp.first

            // if true the end phase dialog is shown, if false reload the gui data and check if a restaining was added
            if (!tmp.second) {
                // updating without reloading the task
                update(false)

                // only show add diagnosis dialog if new restaining and only one diagnosis
                if (TaskStatus.checkIfReStainingFlag(task) && !TaskStatus.checkIfStainingCompleted(task)
                        && task.diagnosisRevisions.size == 1) {
                    logger.debug("Opening dialog for creating a reportIntent revision");
                    MessageHandler.executeScript(GuiCommands.OPEN_ADD_DIAGNOSIS_REVISION_DIALOG_FROM_SLIDE_OVERVIEW_DIALOG)
                }
            }
            return
        }
        onSubDialogReturn(event)
    }

    /**
     * Handler for staining phase return event
     */
    open fun onStainingPhaseExitReturn(event: SelectEvent) {
        if (event.`object` is StainingPhaseExitEvent) {
            // end staingphase confirmed, close dialog and forward
            logger.debug("Staining phase exit dialog return, forwarding to globalEditViewHandler")
            hideDialog(event.getObject())
            return
        }

        onSubDialogReturn(event)
    }

    /**
     * Return handler for entity deletion
     */
    open fun onTaskEntityDeleteReturn(event: SelectEvent) {
        if (event.`object` is TaskEntityDeleteEvent) {
            logger.debug("Deleting task entity object")
            val deleteEvent = event.getObject() as TaskEntityDeleteEvent

            val t = slideService.deleteSlide(deleteEvent.obj as Slide, true)

            t.samples.forEach { it.blocks.forEach { it.slides.forEach { println("$it ${it.completionDate}") } } }

            onStainingPhaseUpdateReturn(
                    SelectEvent(event.component, event.behavior, StainingPhaseUpdateEvent(task)))
            return
        }
        onSubDialogReturn(event)
    }

    /**
     * Subdialog return event
     */
    override fun onSubDialogReturn(event: SelectEvent) {
        if (event.`object` is TaskReloadEvent) {
            logger.debug("Reload task event, reloading")
            update(true)
        } else {
            // unknown event
            logger.debug("Unknow event closing dialog")
        }
    }

    /**
     * Hides the dialog an fires a task reload event
     */
    override fun hideDialog() {
        super.hideDialog(TaskReloadEvent())
    }

    /**
     * Saves dynamic name changes of task entities
     */
    fun save(resourcesKey: String, vararg arr: Any) {
        logger.debug("Saving task " + task.taskID)
        task = taskRepository.save(task, resourceBundle.get(resourcesKey, *arr))
        update(false)
    }
}