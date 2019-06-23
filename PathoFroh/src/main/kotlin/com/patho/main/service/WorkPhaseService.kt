package com.patho.main.service

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.exceptions.DiagnosisRevisionsNotCompletedException
import com.patho.main.util.exceptions.NotificationNotCompletedException
import com.patho.main.util.exceptions.StainingsNotCompletedException
import com.patho.main.util.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service()
open class WorkPhaseService @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val favouriteListService: FavouriteListService,
        private val slideService: SlideService,
        private val diagnosisService: DiagnosisService) : AbstractService() {

    /**
     * Start the staining phase, adds the task to the staining or restaining phase,
     * set staining completion date to 0
     */
    @Transactional
    open fun startStainingPhase(task: Task): Task {
        var tmp = task

        if (tmp.stainingCompleted) {
            tmp.stainingCompletionDate = null
            tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.staining.enter", tmp), tmp.patient)
        }

        if (!TaskStatus.hasFavouriteLists(tmp, PredefinedFavouriteList.StainingList,
                        PredefinedFavouriteList.ReStainingList)) {
            if (TaskStatus.checkIfReStainingFlag(tmp)) {
                tmp = favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.StainingList)
                tmp = favouriteListService.addTaskToList(tmp, PredefinedFavouriteList.ReStainingList)
            } else
                tmp = favouriteListService.addTaskToList(tmp, PredefinedFavouriteList.StainingList)
        }

        return tmp
    }

    /**
     * Ends the staining phase, removes the task from the staining lists, sets
     * staining completion date to current time
     */
    @Transactional
    open fun endStainingPhase(task: Task, removeFromList: Boolean): Task {
        var tmp = task

        // throws error if not all staings are completed
        if (!TaskStatus.checkIfStainingCompleted(task))
            throw  StainingsNotCompletedException()

        tmp.stainingCompletionDate = Instant.now()
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.staining.end", tmp), tmp.patient)

        if (removeFromList)
            tmp = favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.StainingList,
                    PredefinedFavouriteList.ReStainingList)

        return tmp
    }

    /**
     * Starts the reportIntent phase, sets the time of reportIntent completion to zero and
     * adds the task to the reportIntent list.
     */
    @Transactional
    open fun startDiagnosisPhase(task: Task): Task {
        var tmp = task
        tmp.diagnosisCompletionDate = null

        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.diagnosis.enter", tmp), tmp.patient)

        if (!TaskStatus.hasFavouriteLists(tmp, PredefinedFavouriteList.DiagnosisList,
                        PredefinedFavouriteList.ReDiagnosisList)) {
            tmp = favouriteListService.addTaskToList(tmp.id, PredefinedFavouriteList.DiagnosisList)
        }

        return tmp
    }

    /**
     * Ends the diagnosis phase, removes from diagnosis list and sets the diagnosis
     * time of completion. If not all diagnoses had been marked as completed a DiagnosisRevisionsNotCompletedException is
     * thrown
     */
    @Transactional
    open fun endDiagnosisPhase(task: Task, removeFromList: Boolean): Task {
        var tmp = task

        if (tmp.diagnosisRevisions.any { !it.completed })
            throw  DiagnosisRevisionsNotCompletedException()

        tmp.diagnosisCompletionDate = Instant.now()
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.diagnosis.end", tmp), tmp.patient)

        if (removeFromList)
            tmp = favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.DiagnosisList,
                    PredefinedFavouriteList.ReDiagnosisList)
        return tmp
    }

    /**
     * Starting notification phase and sets the notificationCompletionDate to null. The task is also added to the
     * notification list.
     */
    @Transactional
    open fun startNotificationPhase(task: Task): Task {
        var tmp = task
        tmp.notificationCompletionDate = null
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.notification.enter", tmp), tmp.patient)
        return favouriteListService.addTaskToList(tmp, PredefinedFavouriteList.NotificationList)
    }

    /**
     * Ends the notification phase. If not all notification are marked as completed, a NotificationNotCompletedException
     * is thrown.
     */
    @Transactional
    open fun endNotificationPhase(task: Task, removeFromList: Boolean): Task {
        var tmp = task

        if (tmp.diagnosisRevisions.any { !it.isNotified })
            throw NotificationNotCompletedException()

        tmp.notificationCompletionDate = Instant.now()
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.notification.end", tmp), tmp.patient)

        if (removeFromList)
            tmp = favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.NotificationList,
                    PredefinedFavouriteList.ReDiagnosisList)

        return tmp
    }
}