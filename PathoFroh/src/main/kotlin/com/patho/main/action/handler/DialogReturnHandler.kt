package com.patho.main.action.handler

import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.service.BlockService
import com.patho.main.service.SampleService
import com.patho.main.service.SlideService
import com.patho.main.util.dialogReturn.ReloadTaskEvent
import com.patho.main.util.event.dialog.PatientMergeEvent
import com.patho.main.util.event.dialog.PatientReloadEvent
import com.patho.main.util.event.dialog.StainingPhaseUpdateEvent
import com.patho.main.util.event.dialog.TaskEntityDeleteEvent
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
open class DialogReturnHandler @Autowired constructor(
        private val worklistHandler: WorklistHandler,
        private val workPhaseHandler: WorkPhaseHandler,
        private val slideService: SlideService,
        private val blockService: BlockService,
        private val sampleService: SampleService) : AbstractHandler() {
    override fun loadHandler() {
    }


    open fun onDefaultReturn(event: SelectEvent) {
        logger.debug("Default Return Handler")
        event ?: return;

        val obj = event.`object`

        when (obj) {
            // Patient reload event
            //TODO Test CreateTaskDialog
            //TODO Test SearchPatientDialog
            is PatientReloadEvent -> {
                logger.debug("Patient reload event")
                if (obj.task != null) {
                    worklistHandler.addTaskToWorklist(obj.task, obj.select)
                } else {
                    worklistHandler.addPatientToWorkList(obj.patient, obj.select)
                }
            }
            // Task reload event, optional replacing task
            //Todo Test CreateDiagnosisRevisionDialog
            is ReloadTaskEvent -> {
                logger.debug("Task reload event")
                if (obj is Task)
                    worklistHandler.replaceTaskInWorklist(obj)
                else
                    worklistHandler.replaceTaskInWorklist()
            }
        }
    }

    open fun onCreateTaskReturn(event: SelectEvent) {

    }

    /**
     * Return handler for the create Sample- and Slide-Dialog
     */
    open fun onStainingPhaseUpdateReturn(event: SelectEvent) {
        if (event.`object` is StainingPhaseUpdateEvent) {
            logger.debug("Staining phase update event")
            workPhaseHandler.updateStainingPhase((event.`object` as StainingPhaseUpdateEvent).task)
            return
        }
        onDefaultReturn(event)
    }

    /**
     * Return handler for patient merge dialog
     */
    open fun onPatientMergeReturn(event: SelectEvent) {
        val obj = event.`object`
        if (obj is PatientMergeEvent) {
            logger.debug("Patient merge event")
            if (obj.source.archived)
                worklistHandler.removePatientFromWorklist(obj.source)
            else
                worklistHandler.replacePatientInWorklist(obj.source)

            if (obj.target.archived)
                worklistHandler.removePatientFromWorklist(obj.target)
            else
                worklistHandler.replacePatientInWorklist(obj.target)
        }
        onDefaultReturn(event)
    }

    /**
     * Return handler for DeleteDialog
     */
    open fun onDeleteTaskEntityReturn(event: SelectEvent) {
        val obj = event.`object`
        if (obj is TaskEntityDeleteEvent) {
            logger.debug("Deleting task entity event")

            val task: Task? = when (obj.obj) {
                is Slide -> slideService.deleteSlideAndPersist(obj.obj)
                is Block -> blockService.deleteBlockAndPersist(obj.obj)
                is Sample -> sampleService.deleteSampleAndPersist(obj.obj)
                else -> null
            }

            // updating staining phase if task is not null
            if (task != null)
                onStainingPhaseUpdateReturn(SelectEvent(event.component, event.behavior, StainingPhaseUpdateEvent(task)))

            return
        }
        onDefaultReturn(event)
    }
}