package com.patho.main.action.handler.views;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.model.dto.TaskOverview;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.TaskOverviewRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Task view Data
 */
@Getter
@Setter
@Configurable
@ConfigurationProperties(prefix = "patho.common.taskview", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class TaskView extends AbstractTaskView {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskOverviewRepository taskOverviewRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserHandlerAction userHandlerAction;

    public TaskView(GlobalEditViewHandler globalEditViewHandler) {
        super(globalEditViewHandler);
    }

    /**
     * Lists of task to display
     */
    private List<TaskOverview> taskList;

    /**
     * Task to select from list
     */
    private TaskOverview selectedTask;

    /**
     * Task per Page, Initialized by spring
     */
    private int tasksPerPage;

    /**
     * Options to choose the taksperPage, Initialized by spring
     */
    private int[] tasksPerPageOptions;

    /**
     * List of available pages
     */
    private List<Integer> pages;

    /**
     * Page of list
     */
    private int page = 0;

    public void onChangeSelectionCriteria() {

        if (tasksPerPage == 0) {
            logger.error("Configuration error, default task per page is not configured, setting 50");
            tasksPerPage = 50;
        }

        // updating page count
        long maxPages = taskOverviewRepository.count();

        if (maxPages != 0) {
            int pagesCount = (int) Math.ceil((double) maxPages / tasksPerPage);
            logger.debug("Found max {}, Page per page {}, Count of pages {}", maxPages, tasksPerPage, pagesCount);

            setPages(new ArrayList<Integer>(pagesCount));

            for (int i = 0; i < pagesCount - 1; i++) {
                getPages().add(i + 1);
            }

            if (page > getPages().size() - 1) {
                page = getPages().get(getPages().size() - 1);
            }

            logger.debug("Reloading task lists");

            List<TaskOverview> p = taskOverviewRepository.findAllWithAssociatedPerson(
                    userHandlerAction.getCurrentUser().getPhysician().getPerson().getId(),
                    PageRequest.of(getPage(), getTasksPerPage()));

            setTaskList(p);
        } else {
            setPages(new ArrayList<Integer>(0));
            setTaskList(new ArrayList<TaskOverview>());
        }


    }

    public void addUserToNotification(Task task, HistoUser histoUser) {
        // AssociatedContact associatedContact = contactDAO.addAssociatedContact(task,
        // histoUser.getPhysician().getPerson(), ContactRole.CLINIC_PHYSICIAN);
        //
        // contactDAO.addNotificationType(task, associatedContact,
        // AssociatedContactNotification.NotificationTyp.EMAIL);
    }

    public void removeUserFromNotification(Task task, HistoUser histoUser) {
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

    @Override
    public void loadView() {
        onChangeSelectionCriteria();
    }
}