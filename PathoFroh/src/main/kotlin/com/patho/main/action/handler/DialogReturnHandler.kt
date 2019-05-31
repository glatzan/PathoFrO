package com.patho.main.action.handler

import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.service.BlockService
import com.patho.main.service.SampleService
import com.patho.main.service.SlideService
import com.patho.main.service.UserService
import com.patho.main.util.dialogReturn.ReloadTaskEvent
import com.patho.main.util.event.dialog.*
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
        private val sampleService: SampleService,
        private val userService: UserService,
        private val centralHandler: CentralHandler) : AbstractHandler() {
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

                if (obj.select) {
                    if (obj.task != null)
                        centralHandler.onSelectTaskAndPatient(obj.task)
                    else if (obj.patient != null)
                        centralHandler.onSelectPatient(obj.patient)
                    else
                        centralHandler.onSelectPatient(worklistHandler.current.selectedPatient)
                } else {
                    if (obj.task != null)
                        worklistHandler.replaceTaskInWorklist(obj.task)
                    else if (obj.patient != null) {
                        if (worklistHandler.isSelected(obj.patient.tasks))
                            worklistHandler.replaceTaskInWorklist()
                        else
                            worklistHandler.replacePatientInWorklist(obj.patient)
                    } else {
                        if (worklistHandler.selectedTask != null)
                            worklistHandler.replaceTaskInWorklist()
                        else
                            worklistHandler.replacePatientInWorklist()
                    }
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
            val t = (event.`object` as StainingPhaseUpdateEvent).task
            workPhaseHandler.updateStainingPhase(t)
            worklistHandler.replaceTaskInWorklist(t)
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
                is Slide -> slideService.deleteSlide(obj.obj,true)
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

    /**
     * Return handler for UserSettingsDialog
     */
    open fun onUserSettingsReturn(event: SelectEvent) {
        if (event.`object` is UserReloadEvent) {
            logger.debug("User settings return event")
            userService.reloadCurrentUser()
            return
        }
        onDefaultReturn(event)
    }


    open fun onSettingsReturn(event: SelectEvent) {
        if (event.`object` is SettingsReloadEvent) {
            logger.debug("Settings return event")
            centralHandler.reloadAllData()
            return
        }
        onDefaultReturn(event)
    }

    /**
     * Return event handler for selecting new worklists
     */
    open fun onWorklistSelectReturn(event: SelectEvent) {
        val obj = event.`object`
        if (obj is WorklistSelectEvent) {
            worklistHandler.addWorklist(obj.obj, true)
            return
        }
        onDefaultReturn(event)
    }

    /**
     * Event handler for adding a patient to the current worklist
     */
    open fun onSearchForPatientReturn(event: SelectEvent) {
        val obj = event.`object`
        if (obj is PatientSelectEvent) {
            logger.debug("Patient select event")

            if (obj.obj == null) {
                logger.debug("No Patient selected")
                return
            }

            worklistHandler.addPatientToWorkList(obj.obj, true, true)
            return
        }
        onDefaultReturn(event)
    }

    /**
     * Event handler for Patient delete dialog
     */
    open fun onPatientDeleteReturn(event: SelectEvent) {
        val obj = event.`object`
        if (obj is PatientDeleteEvent) {
            // TODO implement logic
            return
        }
        onDefaultReturn(event)
    }

    open fun onTaskPhaseChangeEvent(event: SelectEvent){
        val obj = event.`object`
        if (obj is RemovePatientFromWorklistEvent) {
           worklistHandler.removePatientFromWorklist(obj.obj)
            return
        }

        onDefaultReturn(event)
    }
}