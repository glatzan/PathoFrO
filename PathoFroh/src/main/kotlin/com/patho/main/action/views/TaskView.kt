package com.patho.main.action.views

import com.patho.main.model.dto.TaskOverview
import com.patho.main.model.patient.Task
import com.patho.main.model.user.HistoUser
import com.patho.main.repository.jpa.TaskOverviewRepository
import com.patho.main.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Scope
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
@Scope(value = "session")
@ConfigurationProperties(prefix = "patho.common.taskview", ignoreInvalidFields = true, ignoreUnknownFields = true)
open class TaskView @Autowired constructor(
        private val taskOverviewRepository: TaskOverviewRepository,
        private val userService: UserService) : AbstractView() {

    /**
     * Lists of task to display
     */
    open var taskList: List<TaskOverview> = listOf<TaskOverview>()

    /**
     * Task to select from list
     */
    open var selectedTask: TaskOverview? = null

    /**
     * Task per Page, Initialized by spring
     */
    open var tasksPerPage: Int = 0

    /**
     * Options to choose the taksperPage, Initialized by spring
     */
    open var tasksPerPageOptions: IntArray = IntArray(0)

    /**
     * List of available pages
     */
    open var pages: List<Int> = listOf<Int>()

    /**
     * Page of list
     */
    open var page = 0

    /**
     * Loads view data
     */
    override fun loadView() {
        onChangeSelectionCriteria()
    }

    /**
     * Reloads view data on criteria change
     */
    open fun onChangeSelectionCriteria() {
        if (tasksPerPage == 0) {
            logger.error("Configuration error, default task per page is not configured, setting 50")
            tasksPerPage = 50
        }

        // updating page count
        val maxPages = taskOverviewRepository.count()

        if (maxPages != 0L) {
            val pagesCount = Math.ceil(maxPages.toDouble() / tasksPerPage).toInt()
            logger.debug("Found max {}, Page per page {}, Count of pages {}", maxPages, tasksPerPage, pagesCount)

            pages = List<Int>(pagesCount) { i -> i + 1 }

            if (page > pages.size - 1) {
                page = pages.get(pages.size - 1)
            }

            logger.debug("Reloading task lists")

            taskList = taskOverviewRepository.findAllWithAssociatedPerson(
                    userService.currentUser.physician.person.id,
                    PageRequest.of(page, tasksPerPage))

        } else {
            pages = ArrayList<Int>(0)
            taskList = listOf<TaskOverview>()
        }
    }

    open fun addUserToNotification(task: Task, histoUser: HistoUser) {
        // AssociatedContact associatedContact = contactDAO.addAssociatedContact(task,
        // histoUser.getPhysician().getPerson(), ContactRole.CLINIC_PHYSICIAN);
        //
        // contactDAO.addNotificationType(task, associatedContact,
        // AssociatedContactNotification.NotificationTyp.EMAIL);
    }

    open fun removeUserFromNotification(task: Task, histoUser: HistoUser) {
        // if (task.getContacts() != null) {
        // try {
        // AssociatedContact associatedContact = task.getContacts().stream()
        // .filter(p -> p.getPerson().equals(histoUser.getPhysician().getPerson()))
        // .collect(StreamUtils.singletonCollector());
        //
        // contactDAO.removeAssociatedContact(task, associatedContact);
        // } catch (IllegalStateException e) {
        // log.debug("No matching contact found!");
        // // do nothing
        // }
        // }
    }
}