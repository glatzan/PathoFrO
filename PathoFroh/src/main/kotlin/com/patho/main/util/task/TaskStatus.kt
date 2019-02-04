package com.patho.main.util.task

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.AssociatedContact
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.*
import com.patho.main.service.impl.SpringContextBridge

class TaskStatus(task: Task) {

    /**
     * Task
     */
    var task: Task = task

    /**
     * Static status of favourite lists
     */
    var listStatus: ListStatus = ListStatus(task)

    /**
     * True if Task.stainingCompleted is a valid timestamp
     */
    var stainingPhaseCompleted: Boolean = false

    /**
     * True if all slides are marked as completed
     */
    val stainingCompleted: Boolean
        get() = stainingsToComplete.isEmpty()

    /**
     * List of slides that are not completed yet
     */
    var stainingsToComplete: List<Slide> = ArrayList<Slide>()

    /**
     * True if Task.diagnosisPhaseCompleted is a valid timestamp
     */
    var diagnosisPhaseCompleted: Boolean = false

    /**
     * Checks if diagnoses are completed and signed or if the ignore flag is set
     */
    val diagnosesCompleted: Boolean
        get() = diagnosesStatus.any { p -> (p.completed && p.signature) || p.ignore }

    /**
     * List of diagnoses and their status
     */
    var diagnosesStatus: List<DiagnosisRevisionStatus> = ArrayList<DiagnosisRevisionStatus>()

    /**
     * True if task.notificationCompleted is a valid timestamp
     */
    var notificationPhaseCompleted: Boolean = false

    /**
     * Checks if a notification was performed for all diagnoses
     */
    val notificationCompleted: Boolean
        get() = notificationsOfDiagnoses.any { p -> p.completed || p.ignore }
    /**
     * Status of the single diagnoses
     */
    var notificationsOfDiagnoses: List<DiagnosisRevisionStatus> = ArrayList<DiagnosisRevisionStatus>()

    /**
     * Status of the contacts
     */
    var notificationsStatus: List<NotificationStatus> = ArrayList<NotificationStatus>()

    /**
     * Contains all favourite list of the task
     */
    var favouriteListOfTask: List<FavouriteList> = ArrayList<FavouriteList>()

    /**
     * Contains all favourite lists of a task which are blocking the archive process
     */
    var blockingFavouriteListsOfTask: List<FavouriteList> = ArrayList<FavouriteList>()

    /**
     * Returns true if task is finalized
     */
    var finalized: Boolean = false

    var finalizeable: Boolean = false

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
            return true
        }

    init {
        simpleStatus()
    }

    fun simpleStatus() {
        listStatus = ListStatus(task)
        finalized = task.isFinalized
        actionPending = task.isActiveOrActionPending
        active = task.isActive
        editable = isTaskEditable

        println(SpringContextBridge.services().userHandlerAction)
    }


    /**
     * Status for diagnoses
     */
    public class DiagnosisRevisionStatus(diagnosisRevision: DiagnosisRevision) {
        var completed: Boolean = diagnosisRevision.completionDate != 0L
        var signature: Boolean = diagnosisRevision.signatureOne != null || diagnosisRevision.signatureTwo != null
        var ignore: Boolean = false
    }

    public class DiagnosisNotificationStatus(diagnosisRevision: DiagnosisRevision) {
        val perfomed: Boolean = diagnosisRevision.notificationStatus == DiagnosisRevision.NotificationStatus.NOTIFICATION_COMPLETED;
        val igonre: Boolean = diagnosisRevision.notificationStatus == DiagnosisRevision.NotificationStatus.NO_NOTFICATION;
    }

    public class NotificationStatus(associatedContact: AssociatedContact) {
        var completed: Boolean = associatedContact.isNotificationPerformed
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
            return idArr.any { p -> task.favouriteLists.any { f -> f.id == p } }
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
            return block.slides.all { p -> p.isStainingCompleted }
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
            return block.slides.all { p -> p.isReStaining }
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
            return task.diagnosisRevisions.all { p -> checkIfDiagnosisCompleted(p) }
        }

        /**
         * Checks if diagnoses are completed.
         */
        @JvmStatic
        fun checkIfDiagnosisCompleted(revision: DiagnosisRevision): Boolean {
            return revision.completionDate != 0L
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
            return task.diagnosisRevisions.all { p -> checkIfNotificationIsCompleted(p) }

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