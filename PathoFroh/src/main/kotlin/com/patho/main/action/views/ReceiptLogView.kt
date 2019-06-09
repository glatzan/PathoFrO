package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.model.MaterialPreset
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.BlockService
import com.patho.main.service.PrintExecutorService
import com.patho.main.service.SampleService
import com.patho.main.service.SlideService
import com.patho.main.ui.StainingTableChooser
import com.patho.main.util.print.UnknownPrintingException
import com.patho.main.util.status.reportIntent.ReportIntentBearer
import com.patho.main.util.status.reportIntent.ReportIntentStatusByReportIntentAndDiagnosis
import com.patho.main.util.task.TaskTreeTools
import freemarker.template.TemplateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
open class ReceiptLogView @Autowired constructor(
        private val slideService: SlideService,
        private val workPhaseHandler: WorkPhaseHandler,
        private val taskRepository: TaskRepository,
        private val blockService: BlockService,
        private val worklistHandler: WorklistHandler,
        private val printExecutorService: PrintExecutorService,
        private val sampleService: SampleService) : AbstractEditTaskView() {

    /**
     * Currently selected task entity in table form, transient, used for gui
     */
    open var rows = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()

    /**
     * Is used for selecting a chooser from the generated list (generated by task).
     * It is used to edit the names of the entities by an overlaypannel
     */
    open var selectedRow: TaskEntityRow<IdManuallyAltered>? = null

    /**
     * This variable is used to save the selected action, which should be executed
     * upon all selected slides
     */
    open var actionOnMany: StainingListAction = StainingListAction.NONE

    /**
     * Status for notification list
     */
    open var reportIntentStatus: ReportIntentStatusByReportIntentAndDiagnosis = ReportIntentStatusByReportIntentAndDiagnosis(Task())

    /**
     * Selectedd status for displaying infos
     */
    open var selectedReportIntentStatus: ReportIntentBearer? = null

    /**
     * Loads all task data
     */
    override fun loadView(task: Task) {
        logger.debug("Loading receipt log view")
        super.loadView(task)
        actionOnMany = StainingListAction.NONE
        rows = TaskEntityRow.factory(task, false)

        reportIntentStatus = ReportIntentStatusByReportIntentAndDiagnosis(task)
        selectedReportIntentStatus = null
    }

    /**
     * Sets the element an all children as selected
     */
    open fun selectChildren(entity: TaskEntityRow<out IdManuallyAltered>, selected: Boolean) {
        entity.selected = selected
        entity.children.forEach { p -> selectChildren(p, selected) }
    }

    /**
     * Sets all elemtns of the list to selectedF
     */
    open fun selectListChildren(lists: List<TaskEntityRow<out IdManuallyAltered>>, selected: Boolean) {
        lists.forEach { p -> selectChildren(p, selected) }
    }

    /**
     * Toggles the selection of an entity
     */
    open fun toggleSelectoin(entity: TaskEntityRow<out IdManuallyAltered>) {
        selectChildren(entity, !entity.selected)
    }

    /**
     * Runs the selected action on the selected slides
     */
    open fun performActionOnSelectedSlides(action: StainingListAction) {
        // actions ca only be performed on slides
        val slideRows = rows.filter { p -> p.selected && p.isStainingType }

        if (slideRows.isEmpty()) run {
            logger.debug("Nothing selected, do not perform any action")
            return
        }

        when (action) {
            // set slided to performed
            StainingListAction.PERFORMED, StainingListAction.NOT_PERFORMED -> {
                logger.debug("Setting staining status of selected slides")
                slideRows.forEach { p -> slideService.completeStaining(p.entity as Slide, action == StainingListAction.PERFORMED, false) }
                var ntask = taskRepository.save(task, resourceBundle.get("log.task.slide.completedStack", task), task.patient)
                ntask = workPhaseHandler.updateStainingPhase(ntask).first
                worklistHandler.replaceTaskInWorklist(ntask)
            }
            // archive slides
            StainingListAction.ARCHIVE -> {
                //TODO implement this feature
                println("TODO implement")
            }
            StainingListAction.PRINT -> {
                slideRows.filter { p -> p.selected && p.isStainingType }
            }

            else -> {

            }
        }
    }

    /**
     * Creates a block and updtes the staining phase
     */
    open fun createNewBlock(sample: Sample) {
        var task = blockService.createBlock(sample, true, true, true)
        task = workPhaseHandler.updateStainingPhase(task).first
        worklistHandler.replaceTaskInWorklist(task)
    }

    /**
     * Completes a slide and updates the stating phase
     */
    open fun completeSlide(slide: Slide, complete: Boolean) {
        var task = slideService.completeStaining(slide, complete)
        task = workPhaseHandler.updateStainingPhase(task).first
        worklistHandler.replaceTaskInWorklist(task)
    }

    open fun printAllLabels() {
        printLabels(*rows.filter { p -> p.isStainingType }.map { p -> p.entity as Slide }.toTypedArray())
    }

    /**
     * Printing labels
     */
    open fun printLabels(vararg slides: Slide) {
        if (slides.isEmpty()) {
            MessageHandler.sendGrowlErrorAsResource("growl.print.error.headline", "growl.print.error.noTemplate")
            return
        }
        try {
            printExecutorService.printLabel(*slides)
            MessageHandler.sendGrowlMessagesAsResource("growl.print.printing", "growl.print.slide.print")
        } catch (e: UnknownPrintingException) {
            MessageHandler.sendGrowlErrorAsResource("growl.print.error.headline", "growl.print.error.printError")
            return
        } catch (e: TemplateNotFoundException) {
            MessageHandler.sendGrowlErrorAsResource("growl.print.error.headline", "growl.print.error.noTemplate")
            return
        }
    }

    /**
     * Entity row for displaying task slides as a flat list
     */
    open class TaskEntityRow<T : IdManuallyAltered>(var entity: T, val even: Boolean = false) {
        open var selected: Boolean = false

        open var idChanged: Boolean = false

        /**
         * Children of this enitity
         */
        open val children = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()


        open val isSampleType
            get() = entity is Sample

        open val isBlockType
            get() = entity is Block

        open val isStainingType
            get() = entity is Slide

        open fun setIDText(text: String?) {
            when (entity) {
                is Sample -> {
                    if (text != (entity as Sample).sampleID) idChanged = true
                    (entity as Sample).sampleID = text ?: ""
                }
                is Block -> {
                    if (text != (entity as Block).blockID) idChanged = true
                    (entity as Block).blockID = text ?: ""
                }
                is Slide -> {
                    if (text != (entity as Slide).slideID) idChanged = true
                    (entity as Slide).slideID = text ?: ""
                }
            }
        }

        open fun getIDText(): String {
            return when (entity) {
                is Sample -> (entity as Sample).sampleID
                is Block -> (entity as Block).blockID
                is Slide -> (entity as Slide).slideID
                else -> ""
            }
        }

        companion object {
            /**
             * Creates linear list of all slides of the given task. The StainingTableChosser
             * is used as holder class in order to offer an option to select the slides by
             * clicking on a backend. Archived elements will not be shown if showArchived
             * is false.
             *
             * @param showArchived
             */
            @JvmStatic
            fun factory(task: Task, showArchived: Boolean): MutableList<TaskEntityRow<out IdManuallyAltered>> {

                val result = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()

                var even = false

                for (sample in task.samples) {
                    // skips archived tasks

                    val sampleChooser = TaskEntityRow(sample, even)
                    result.add(sampleChooser)

                    for (block in sample.blocks) {
                        // skips archived blocks

                        val blockChooser = TaskEntityRow(block, even)
                        result.add(blockChooser)
                        sampleChooser.children.add(blockChooser)

                        for (staining in block.slides) {
                            // skips archived sliedes

                            val stainingChooser = TaskEntityRow(staining, even)
                            result.add(stainingChooser)
                            blockChooser.children.add(stainingChooser)
                        }
                    }

                    even = !even

                }
                return result
            }
        }
    }

    /**
     * Saves the manually altered flag, if the sample/block/ or slide id was
     * manually altered.
     */
    fun entityNameChange(chooser: TaskEntityRow<out IdManuallyAltered>?, resourcesKey: String, vararg arr: Any) {
        // checking if something was altered, if not do nothing
        if (chooser != null && chooser.idChanged) {
            logger.debug("Text changed and saved: " + chooser.getIDText())
            chooser.idChanged = false
            chooser.entity.idManuallyAltered = true
            TaskTreeTools.updateNamesInTree(chooser.entity);
            val t = chooser.entity.task ?: return
            save(t, resourcesKey, *arr)
        }
    }

    /**
     * Action commands for selecting action on many items
     */
    enum class StainingListAction {
        NONE, PERFORMED, NOT_PERFORMED, PRINT, ARCHIVE
    }
}