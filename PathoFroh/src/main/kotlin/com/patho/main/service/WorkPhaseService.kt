package com.patho.main.service

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.helper.HistoUtil
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
     * Updates the status of the stating phase, if the phase has ended, true will be
     * returned.
     */
    @Transactional
    open fun updateStainigPhase(task: Task): Boolean {
        // removing from staining list and showing the dialog for ending
        // staining phase
        return if (TaskStatus.checkIfStainingCompleted(task)) {
            logger.trace("Staining phase of task (" + task.taskID
                    + ") completed removing from staing list, adding to diagnosisList")
            // do nothin is done manual via dialog
            true
        } else {
            // reentering the staining phase, adding task to staining or
            // restaining list
            false
        }
    }

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
        slideService.completedStaining(tmp, true)
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

        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.reportIntent.enter", tmp), tmp.patient)

        if (!TaskStatus.hasFavouriteLists(tmp, PredefinedFavouriteList.DiagnosisList,
                        PredefinedFavouriteList.ReDiagnosisList)) {
            tmp = favouriteListService.addTaskToList(tmp.id, PredefinedFavouriteList.DiagnosisList)
        }

        return tmp
    }

    /**
     * Ends the reportIntent phase, removes from reportIntent list and sets the reportIntent
     * time of completion.
     */
    @Transactional
    open fun endDiagnosisPhase(task: Task, removeFromList: Boolean, status: NotificationStatus): Task {
        var tmp = task

        var change = false
        // setting reportIntent completion date if not set jet
        for (i in 0 until tmp.diagnosisRevisions.size) {
            val rev = HistoUtil.getNElement(tmp.diagnosisRevisions, i)
            if (!rev!!.completed) {
                tmp = diagnosisService.approveDiangosis(tmp, rev, status)
                change = true
            }
        }

        if (change || !tmp.diagnosisCompleted) {
            tmp.diagnosisCompletionDate = Instant.now()
            tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.reportIntent.end", tmp), tmp.patient)
        }

        if (removeFromList)
            tmp = favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.DiagnosisList,
                    PredefinedFavouriteList.ReDiagnosisList)
        return tmp
    }

    /**
     * Starting notification phase
     */
    @Transactional
    open fun startNotificationPhase(task: Task): Task {
        var tmp = task
        tmp.notificationCompletionDate = null
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.notification.enter", tmp), tmp.patient)
        return favouriteListService.addTaskToList(tmp, PredefinedFavouriteList.NotificationList)
    }

    /**
     * Ends the notification phase
     */
    @Transactional
    open fun endNotificationPhase(task: Task): Task {
        var tmp = task
        tmp.notificationCompletionDate = Instant.now()
        tmp = taskRepository.save(tmp, resourceBundle.get("log.phase.notification.end", tmp), tmp.patient)
        return favouriteListService.removeTaskFromList(tmp, PredefinedFavouriteList.NotificationList)
    }
}