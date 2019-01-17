package com.patho.main.action.dialog.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.ListItem;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.service.TaskService;
import com.patho.main.ui.interfaces.ListItemsAutoCompete;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class RestoreTaskDialog extends AbstractDialog implements ListItemsAutoCompete {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;


	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

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

		setPredefinedListItems(listItemRepository
				.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.TASK_RESTORE, false));

		return super.initBean(task, Dialog.TASK_RESTORE);
	}

	public void hideAndRestore() {
		taskService.restoreTask(getTask(), getCommentary());
		hideDialog(new ReloadTaskEvent());
	}

	public boolean isSubmitable() {
		return getCommentary() != null && getCommentary().length() > 5;
	}
}
