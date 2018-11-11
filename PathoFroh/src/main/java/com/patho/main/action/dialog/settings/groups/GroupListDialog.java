package com.patho.main.action.dialog.settings.groups;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.repository.GroupRepository;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class GroupListDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandlerAction dialogHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	private List<HistoGroup> groups;

	private boolean showArchived;

	private HistoGroup selectedGroup;

	private boolean selectMode;

	private boolean editMode;

	public void initAndPrepareBean() {
		if (initBean(false, true))
			prepareDialog();
	}

	public void initAndPrepareBean(boolean selectMode, boolean editMode) {
		if (initBean(selectMode, editMode))
			prepareDialog();
	}

	public boolean initBean(boolean selectMode, boolean editMode) {
		updateData();

		setShowArchived(false);

		setSelectMode(selectMode);
		setEditMode(editMode);

		setSelectedGroup(null);

		super.initBean(task, Dialog.SETTINGS_GROUP_LIST);
		return true;
	}

	public void updateData() {
		setGroups(groupRepository.findAll(!showArchived));
	}

	public void onRowDblSelect() {
		if (isSelectMode()) {
			log.debug("Select Mode: hiding dialog");
			selectGroupAndHide();
		} else {
			log.debug("Edit Mode: showing edit dialog");
			if (userHandlerAction.currentUserHasPermission(HistoPermissions.PROGRAM_SETTINGS_GROUP)
					&& selectedGroup != null)
				dialogHandlerAction.getGroupEditDialog().initAndPrepareBean(selectedGroup);
		}
	}

	public void selectGroupAndHide() {
		super.hideDialog(getSelectedGroup());
	}

	public void archive(HistoGroup group, boolean archive) {
		try {
			group.setArchived(archive);
			groupRepository.save(group, resourceBundle
					.get(archive ? "log.settings.group.archived" : "log.settings.group.dearchived", group));
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

}
