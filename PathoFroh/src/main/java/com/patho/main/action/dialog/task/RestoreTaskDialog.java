package com.patho.main.action.dialog.task;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.ListItem;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.service.TaskService;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.interfaces.ListItemsAutoCompete;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Getter
@Setter
public class RestoreTaskDialog extends AbstractDialog implements ListItemsAutoCompete {

    /**
     * Contains all available attachments
     */
    private List<ListItem> predefinedListItems;

    /**
     * commentary for restoring
     */
    private String commentary;

    /**
     * True if task was archived
     */
    private boolean archiveSuccessful;

    public RestoreTaskDialog initAndPrepareBean(Task task) {
        if (initBean(task))
            prepareDialog();

        return this;
    }

    public boolean initBean(Task task) {

        setPredefinedListItems(SpringContextBridge.services().getListItemRepository()
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.TASK_RESTORE, false));

        return super.initBean(task, Dialog.TASK_RESTORE);
    }

    public void hideAndRestore() {
        //taskService.restoreTask(getTask(), getCommentary());
        hideDialog(new TaskReloadEvent());
    }

    public boolean isSubmitable() {
        return getCommentary() != null && getCommentary().length() > 5;
    }
}
