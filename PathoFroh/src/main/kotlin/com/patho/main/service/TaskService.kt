package com.patho.main.service

import com.patho.main.config.settings.ServiceSettings
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.repository.DiagnosisRevisionRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.util.helper.HistoUtil
import com.patho.main.util.helper.TimeUtil
import com.patho.main.util.task.TaskArchiveStatus
import com.patho.main.util.task.TaskStatus
import com.patho.main.util.task.TaskTreeTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service()
open class TaskService @Autowired constructor(
        private val diagnosisRevisionRepository: DiagnosisRevisionRepository,
        private val taskRepository: TaskRepository,
        private val serviceSettings: ServiceSettings) : AbstractService() {


    /**
     * Creates a new task and returns it
     */
    @Transactional
    open fun createTask(patient: Patient, taskID: String, save: Boolean): Task {
        var task = Task(patient)
        task.caseHistory = ""
        task.ward = ""
        task.insurance = patient.insurance

        if (HistoUtil.isNotNullOrEmpty(taskID)) {
            if (isTaskIDAvailable(taskID))
                task.taskID = taskID
            else
                throw IllegalArgumentException("Task ID taken")
        } else
            task.taskID = getNextTaskID()

        return if (save) taskRepository.save(task, resourceBundle.get("log.patient.task.new", task), patient) else task
    }

    /**
     * Sets the taskID of a task if it was changed. IMPORTANT: Validation of task id has to be done
     * manually
     */
    @Transactional
    open fun changeTaskID(task: Task, newTaskID: String): Task {
        if (newTaskID != task.id.toString()) {
            val oldId = task.taskID
            task.taskID = newTaskID
            return taskRepository.save(task, resourceBundle.get("log.task.changeID", oldId, newTaskID))
        }
        return task;
    }

    /**
     * Checks if taskID is a valid taskID, also checks if already present in database
     */
    @Transactional
    open fun validateTaskID(taskID: String): TaskIDValidity {
        if (HistoUtil.isNullOrEmpty(taskID) || taskID.length != 6) {
            return TaskIDValidity.SHORT
        } else if (!taskID.matches("[0-9]{6}".toRegex())) {
            return TaskIDValidity.ONLY_NUMBERS
        } else if (taskRepository.findOptionalByTaskId(taskID).isPresent) {
            return TaskIDValidity.ALREADY_PRESENT
        }

        return TaskIDValidity.VALID
    }

    /**
     * Checks if taskID is used, if not true is returned
     */
    @Transactional
    open fun isTaskIDAvailable(taskID: String): Boolean {
        return !taskRepository.findOptionalByTaskId(taskID).isPresent();
    }

    /**
     * Returns the next id for a task, if the year changes a new task id will be
     * generated
     */
    @Transactional
    open fun getNextTaskID(): String {
        // generating new task id
        val task = taskRepository.findOptinalByLastID(Calendar.getInstance(), false, false, false, false,
                false)

        // task is within the current year
        if (task.isPresent) {
            // getting counter
            val count = task.get().taskID.substring(2, 6)
            // increment counter
            val counterAsInt = Integer.valueOf(count) + 1
            return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(counterAsInt, 4, '0')
        } else {
            // first task ever, or first task of year , year + 0001
            return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(1, 4, '0')
        }
    }

    /**
     * Renames task entities, if ignoreManuallyChangedEntities is false, manual
     * changes will not be renamed.
     */
    @Transactional
    open fun updateNamesOfTaskEntities(task: Task, ignoreManuallyChangedEntities: Boolean, save: Boolean): Task {
        logger.debug("Updating names, ignore manually altered: {}", ignoreManuallyChangedEntities)

        for (sample in task.samples) {
            TaskTreeTools.updateNamesInTree(sample, sample.task?.useAutoNomenclature
                    ?: false, ignoreManuallyChangedEntities)
        }

        return if (save)
            taskRepository.save(task, resourceBundle.get("log.task.nameUpdate", task))
        else
            task
    }

    /**
     * Checks if task can be archived
     */
    @Transactional
    open fun getTaskArchiveStatus(task: Task): TaskArchiveStatus {
        var taskArchiveStatus = TaskArchiveStatus(task);

        taskArchiveStatus.stainingPhaseCompleted = task.stainingCompleted
        taskArchiveStatus.stainingCompleted = TaskStatus.checkIfStainingCompleted(task)


        taskArchiveStatus.diagnosisPhaseCompleted = TaskStatus.checkIfDiagnosisCompleted(task)
        taskArchiveStatus.notificationPhaseCompleted = TaskStatus.checkIfNotificationIsCompleted(task)
        // searching for blocking lists
//        taskArchiveStatus.blockingFavouriteLists =
//                task.favouriteLists?.filter { p -> serviceSettings.taskArchiveRules.blockingFavouriteLists.stream().anyMatch { c -> c.id == p.id } }) ?

        return taskArchiveStatus
    }

    /**
     * Enum for retuning the taskID validity status
     */
    public enum class TaskIDValidity {
        VALID, SHORT, ONLY_NUMBERS, ALREADY_PRESENT;
    }
}