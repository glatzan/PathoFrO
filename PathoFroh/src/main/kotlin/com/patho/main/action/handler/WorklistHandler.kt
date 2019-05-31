package com.patho.main.action.handler

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.repository.PatientRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.service.UserService
import com.patho.main.util.worklist.Worklist
import com.patho.main.util.worklist.search.AbstractWorklistSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Controller

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class WorklistHandler @Autowired @Lazy constructor(
        private val centralHandler: CentralHandler,
        private val userService: UserService,
        private val taskRepository: TaskRepository,
        private val patientRepository: PatientRepository) : AbstractHandler() {

    companion object {
        const val DEFAULT_WORKLIST_NAME = "Default"
    }

    /**
     * Containing all worklists
     */
    open var worklists: MutableList<Worklist> = mutableListOf()

    /**
     * Current worklist
     */
    open var current: Worklist = Worklist(DEFAULT_WORKLIST_NAME, AbstractWorklistSearch())

    /**
     * Returns the current patient
     */
    open val selectedPatient: Patient?
        get() = current.selectedPatient

    /**
     * Returns the current task
     */
    open val selectedTask: Task?
        get() = current.selectedTask


    override fun loadHandler() {
    }

    /**
     * Returns true if the given patient is selected
     */
    open fun isSelected(patient: Patient): Boolean {
        return current.isSelected(patient)
    }

    /**
     * Returns true if the given task is selected
     */
    open fun isSelected(task: Task): Boolean {
        return current.isSelected(task)
    }

    /**
     * Returns true if one of given task is selected
     */
    open fun isSelected(tasks: Set<Task>): Boolean {
        return current.isSelected(tasks)
    }

    /**
     * Returns true if the worklist is the active worklist
     */
    open fun isSelected(worklist: Worklist): Boolean {
        return current == worklist
    }

    /**
     * Adds a worklist with the default user settings
     */
    open fun addWorklist(worklistSearch: AbstractWorklistSearch, name: String, selected: Boolean = true) {
        addWorklist(Worklist(name, worklistSearch,
                userService.currentUser.settings.worklistHideNoneActiveTasks,
                userService.currentUser.settings.worklistSortOrder,
                userService.currentUser.settings.worklistAutoUpdate), selected)
    }

    /**
     * Adds the given worklist to the worklist array
     */
    open fun addWorklist(worklist: Worklist, selected: Boolean = true) {

        // removing worklist if worklist with the same name is present
        val toRemove = worklists.singleOrNull { p -> p.name == worklist.name }
        if (toRemove != null) worklists.remove(toRemove)

        worklists.add(worklist)

        if (selected) {
            current = worklist
            current.updateWorklist()
            centralHandler.goToNavigation()
        }

    }

    /**
     * Removes a worklist an updates the view if the worklist is the current
     * worklist
     */
    open fun removeWorklist(worklist: Worklist) {
        worklists.remove(worklist)
        if (isSelected(worklist)) {
            current = Worklist(DEFAULT_WORKLIST_NAME, AbstractWorklistSearch())
            centralHandler.goToNavigation()
        }
    }

    /**
     * Clears a worklist an updates the view if the worklist was the current worklist
     */
    open fun clearWorklist(worklist: Worklist) {
        // if a patient was selected and the worklist is the current worklist update
        // view
        if (worklist.clear() && isSelected(worklist))
            centralHandler.goToNavigation()
    }


    // TODO implement
    open fun autoReloadWorklist() {
        if (current.isAutoUpdate) {
            logger.debug("Auto updating worklist")
            current.updateWorklist(false)
        }
    }

    open fun addTaskToWorklist(id: Long): Task? {
        return addTaskToWorklist(id, false)
    }

    open fun addTaskToWorklist(id: Long, changeView: Boolean): Task? {
        val oTask = taskRepository.findOptionalByIdAndInitialize(id, false, true, true, true, true)
        return if (oTask.isPresent()) addTaskToWorklist(oTask.get(), changeView) else null
    }

    /**
     * Adds a task to the worklist. If changeView is true or the user setting addTaskWithSingleClick is set
     * the task will be set as the selected task
     */
    open fun addTaskToWorklist(task: Task, changeView: Boolean): Task {
        val changeViewOrClick = changeView or userService.currentUser.settings.addTaskWithSingleClick
        task.active = true

        // selecting task if patient is in worklist, or if usersettings force it
        if (current.contains(task.patient) || changeViewOrClick) {
            logger.debug("Showning task " + task.taskID)
            // reloading task and patient from database

            // // only selecting task if patient is already selected
            if (current.isSelected(task.patient) || changeViewOrClick)
                centralHandler.onSelectTaskAndPatient(task, false)
            else
                current.add(task.patient, true)

            return task
        } else {
            logger.debug("Adding task " + task.taskID + " to worklist")
            current.add(task.patient, true)
        }

        return task
    }

    /**
     * Replaces the current task in the current worklist
     */
    open fun replaceTaskInWorklist() {
        replaceTaskInWorklist(current.selectedTask, true)
    }

    /**
     * Replaces the task in the current worklist, if the task is the selected task
     */
    open fun replaceTaskInWorklist(task: Task) {
        replaceTaskInWorklist(task, true)
    }

    /**
     * Replaces the task in the current worklist, if the task is the selected task
     */
    open fun replaceTaskInWorklist(task: Task, reload: Boolean, reloadStaticData : Boolean = true) {
        var reloadedTask = task

        if (reload) {
            val oTask = taskRepository.findOptionalByIdAndInitialize(task.id, false, true, true, true, true)
            reloadedTask = if (oTask.isPresent) oTask.get() else return
        }

        logger.debug("Replacing task due to external changes!")

        current.add(reloadedTask)

        if (current.isSelected(reloadedTask)) {
            // generating task data, taskstatus is generated previously
            centralHandler.loadViews(*listOfNotNull(CentralHandler.Load.MENU_MODEL, if(reloadStaticData) null else CentralHandler.Load.NO_GENERIC_DATA).toTypedArray())
        }

    }

    /**
     * Adds a patient to the worklist. If the patient was already added it is check if the patient
     * should be selected. If so, the patient will be selected. The patient isn't
     * added twice. The patient will not be reloaded.
     */
    open fun addPatientToWorkList(patient: Patient, changeView: Boolean) {
        addPatientToWorkList(patient, changeView, false)
    }

    /**
     * Adds a patient to the worklist. If the patient was already added it is check if the patient
     * should be selected. If so, the patient will be selected. The patient isn't
     * added twice.
     */
    open fun addPatientToWorkList(patient: Patient, changeView: Boolean, reload: Boolean) {
        // change view to patient
        if (changeView)
            centralHandler.onSelectPatient(patient, reload)
        else
            replacePatientInWorklist(patient, reload)
    }

    /**
     * Removes a patient from the worklist.
     */
    open fun removePatientFromWorklist(patient: Patient) {
        logger.debug("Removing Patient from Worklist: #{patient.person.getFullName()} ")

        // if patient is the selected patient the view has to be updated
        if (current.isSelected(patient)) {
            current.remove(patient)
            centralHandler.goToNavigation()
        } else
            current.remove(patient)
    }


    /**
     * Replaces the given patient within the worklist
     */
    open fun replacePatientInWorklist() {
        replacePatientInWorklist(current.selectedPatient, true)
    }

    /**
     * Replaces the given patient within the worklist
     */
    open fun replacePatientInWorklist(patient: Patient) {
        replacePatientInWorklist(patient, true)
    }

    /**
     * Replaces the given patient within the worklist
     */
    open fun replacePatientInWorklist(patient: Patient, reload: Boolean) {
        var reloadedPatient = patient

        if (reload) {
            val oPatient = patientRepository.findOptionalById(patient.id)
            reloadedPatient = if (oPatient.isPresent) oPatient.get() else return
        }

        logger.debug("Replacing patient due to external changes!")

        current.add(reloadedPatient)

        if (current.isSelected(reloadedPatient)) {
            // generating task data, taskstatus is generated previously
            centralHandler.loadViews(CentralHandler.Load.MENU_MODEL)
        }
    }

    /**
     * Selects the next task in List
     */
    open fun selectNextTask() {
        if (!current.isEmpty) {
            if (current.selectedPatient != null) {

                val indexOfTask = current.selectedPatient.getActiveTasks(current.isShowActiveTasksExplicit)
                        .indexOf(current.selectedTask)

                // next task is within the same patient
                if (indexOfTask - 1 >= 0) {
                    centralHandler.onSelectTaskAndPatient(current.selectedPatient
                            .getActiveTasks(current.isShowActiveTasksExplicit)[indexOfTask - 1])
                    return
                }

                val indexOfPatient = current.items
                        .indexOf(current.selectedPatient)

                if (indexOfPatient == -1)
                    return

                if (indexOfPatient - 1 >= 0) {
                    val newPatient = current.items[indexOfPatient - 1]

                    if (newPatient.hasActiveTasks(current.isShowActiveTasksExplicit)) {
                        centralHandler.onSelectTaskAndPatient(
                                newPatient.getActiveTasks(current.isShowActiveTasksExplicit)[newPatient
                                        .getActiveTasks(current.isShowActiveTasksExplicit).size - 1])
                    } else {
                        centralHandler.onSelectPatient(newPatient)
                    }
                }
            } else {
                val newPatient = current.items[current.items.size - 1]

                if (newPatient.hasActiveTasks(current.isShowActiveTasksExplicit)) {
                    centralHandler.onSelectTaskAndPatient(
                            newPatient.getActiveTasks(current.isShowActiveTasksExplicit)[newPatient
                                    .getActiveTasks(current.isShowActiveTasksExplicit)
                                    .size - 1])
                } else {
                    centralHandler.onSelectPatient(newPatient)
                }
            }
        }
    }

    open fun selectPreviousTask() {
        if (!current.isEmpty) {
            if (current.selectedPatient != null) {

                val indexOfTask = current.selectedPatient
                        .getActiveTasks(current.isShowActiveTasksExplicit)
                        .indexOf(current.selectedTask)

                // next task is within the same patient
                if (indexOfTask + 1 < current.selectedPatient
                                .getActiveTasks(current.isShowActiveTasksExplicit).size) {
                    centralHandler.onSelectTaskAndPatient(current.selectedPatient
                            .getActiveTasks(current.isShowActiveTasksExplicit)[indexOfTask + 1])
                    return
                }

                val indexOfPatient = current.items
                        .indexOf(current.selectedPatient)

                if (indexOfPatient == -1)
                    return

                if (indexOfPatient + 1 < current.items.size) {
                    val newPatient = current.items[indexOfPatient + 1]

                    if (newPatient.hasActiveTasks(current.isShowActiveTasksExplicit)) {
                        centralHandler.onSelectTaskAndPatient(newPatient
                                .getActiveTasks(current.isShowActiveTasksExplicit)[0])
                    } else {
                        centralHandler.onSelectPatient(newPatient)
                    }
                }
            } else {
                val newPatient = current.items[0]

                if (newPatient.hasActiveTasks(current.isShowActiveTasksExplicit)) {
                    centralHandler.onSelectTaskAndPatient(
                            newPatient.getActiveTasks(current.isShowActiveTasksExplicit)[0])
                } else {
                    centralHandler.onSelectPatient(newPatient)
                }
            }
        }
    }

}