package com.patho.main.util.task

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.*
import com.patho.main.model.user.HistoPermissions
import com.patho.main.service.impl.SpringContextBridge
import org.slf4j.LoggerFactory

open class TaskStatus(task: Task) {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Task
     */
    var task: Task = task

    /**
     * Static status of favourite lists
     */
    var listStatus: ListStatus = ListStatus(task)

    /**
     * Returns true if task is finalized
     */
    var finalized: Boolean = false

    /**
     * True if task is finalizeable
     */
    var finalizeable: Boolean = false

    /**
     * True if task is editable
     */
    var editable: Boolean = false

    /**
     * True if an action is pedning
     */
    var actionPending: Boolean = false

    /**
     * True if task is active
     */
    var active: Boolean = false

    val isTaskEditable: Boolean
        get() {
            // task is editable
            // users and guest can't edit anything
            if (!SpringContextBridge.services().userService.userHasPermission(HistoPermissions.TASK_EDIT)) {
                return false
            }
            // finalized
            if (task.finalized) {
                return false;
            }

            // if (isDiagnosisCompleted(task) && isStainingCompleted(task))
            // return false;

            // Blocked
            // TODO: Blocking

            return true
        }

    open fun generateStatus(): TaskStatus {
        logger.debug("Generating simple Taskstatus for {}", task.taskID)

        listStatus = ListStatus(task)
        finalized = task.finalized
        actionPending = task.isActiveOrActionPending()
        active = task.active
        editable = isTaskEditable

        return this
    }

    public class ListStatus(task: Task) {
        val inListStaining: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.StainingList)
        val inListReStaining: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.ReStainingList)
        val inListStayInStaining: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.StayInStainingList)

        val inListDiagnosis: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.DiagnosisList)
        val inListReDiagnosis: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.ReDiagnosisList)
        val inListStayInDiagnosis: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.StayInDiagnosisList)

        val inListNotification: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.NotificationList)
        val inListStayInNotification: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.StayInNotificationList)

        val inListCouncil: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.Council)
        val inListCouncilRequest: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilRequest)
        val inListCouncilSendRequestMTA: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilSendRequestMTA)
        val inListCouncilSendRequestSecretary: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilSendRequestSecretary)
        val inListCouncilWaitingForReply: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilWaitingForReply)
        val inListCouncilReplyPresent: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilReplyPresent)
        val inListCouncilCompleted: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.CouncilCompleted)

        val inListScan: Boolean = TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.ScannList)
    }

    companion object {

        /**
         * Checks if the task contains a favourite list with the passed ids
         */
        @JvmStatic
        fun hasFavouriteLists(task: Task, vararg predefinedFavouriteLists: PredefinedFavouriteList): Boolean {
            return hasFavouriteLists(task, *predefinedFavouriteLists.map { p -> p.id }.toLongArray())
        }

        /**
         * Checks if the task contains a favourite list with the passed ids
         */
        @JvmStatic
        fun hasFavouriteLists(task: Task, vararg favouriteLists: FavouriteList): Boolean {
            return hasFavouriteLists(task, *favouriteLists.map { p -> p.id }.toLongArray())
        }

        /**
         * Checks if the task contains a favourite list with the passed ids
         */
        @JvmStatic
        fun hasFavouriteLists(task: Task, vararg idArr: Long): Boolean {
            return idArr.any { p -> task.favouriteLists?.any { f -> f.id == p } ?: false }
        }

        /**
         * Checks if stating is completed by stating flag.
         */
        @JvmStatic
        fun checkIfStainingCompleted(patient: Patient): Boolean {
            return patient.tasks.all { p -> checkIfStainingCompleted(p) }
        }

        /**
         * Checks if stating is completed by stating flag.
         */
        @JvmStatic
        fun checkIfStainingCompleted(task: Task): Boolean {
            return task.samples.all { p -> checkIfStainingCompleted(p) }
        }

        /**
         * Checks if stating is completed by stating flag.
         */
        @JvmStatic
        fun checkIfStainingCompleted(sample: Sample): Boolean {
            return sample.blocks.all { p -> checkIfStainingCompleted(p) }
        }

        /**
         * Checks if stating is completed by stating flag.
         */
        @JvmStatic
        fun checkIfStainingCompleted(block: Block): Boolean {
            return block.slides.all { p -> p.stainingCompleted }
        }

        /**
         * Checks if re stating flag is set
         */
        @JvmStatic
        fun checkIfReStainingFlag(patient: Patient): Boolean {
            return patient.tasks.all { p -> checkIfReStainingFlag(p) }
        }

        /**
         * Checks if re stating flag is set
         */
        @JvmStatic
        fun checkIfReStainingFlag(task: Task): Boolean {
            return task.samples.all { p -> checkIfReStainingFlag(p) }
        }

        /**
         * Checks if re stating flag is set
         */
        @JvmStatic
        fun checkIfReStainingFlag(sample: Sample): Boolean {
            return sample.blocks.all { p -> checkIfReStainingFlag(p) }
        }

        /**
         * Checks if re stating flag is set
         */
        @JvmStatic
        fun checkIfReStainingFlag(block: Block): Boolean {
            return block.slides.all { p -> p.reStaining }
        }

        /**
         * Checks if diagnoses are completed.
         */
        @JvmStatic
        fun checkIfDiagnosisCompleted(patient: Patient): Boolean {
            return patient.tasks.all { p -> checkIfDiagnosisCompleted(p) }
        }

        /**
         * Checks if diagnoses are completed.
         */
        @JvmStatic
        fun checkIfDiagnosisCompleted(task: Task): Boolean {
            return task.diagnosisRevisions?.all { p -> checkIfDiagnosisCompleted(p) } ?: true
        }

        /**
         * Checks if diagnoses are completed.
         */
        @JvmStatic
        fun checkIfDiagnosisCompleted(revision: DiagnosisRevision): Boolean {
            return revision.completed
        }

        /**
         * Checks if a notification status is pending
         */
        @JvmStatic
        fun checkIfNotificationIsCompleted(patient: Patient): Boolean {
            return patient.tasks.all { p -> checkIfNotificationIsCompleted(p) }
        }

        /**
         * Checks if a notification status is pending
         */
        @JvmStatic
        fun checkIfNotificationIsCompleted(task: Task): Boolean {
            return task.diagnosisRevisions?.all { p -> checkIfNotificationIsCompleted(p) } ?: true

        }

        /**
         * Checks if a notification status is pending
         */
        @JvmStatic
        fun checkIfNotificationIsCompleted(revision: DiagnosisRevision): Boolean {
            return revision.notificationStatus == DiagnosisRevision.NotificationStatus.NOTIFICATION_PENDING
        }

    }
}