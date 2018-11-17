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
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
	private GroupRepository groupRepository;

	private List<HistoGroup> groups;

	private boolean showArchived;

	private HistoGroup selectedGroup;

	public GroupListDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();
		return this;
	}

	public boolean initBean() {
		setShowArchived(false);
		setSelectedGroup(null);
		updateData();
		return super.initBean(task, Dialog.SETTINGS_GROUP_LIST);
	}

	public void updateData() {
		setGroups(groupRepository.findAllOrderByIdAsc(!showArchived));
	}

	public void selectAndHide() {
		super.hideDialog(new GroupSelectEvent(getSelectedGroup()));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class GroupSelectEvent implements DialogReturnEvent {
		private HistoGroup group;
	}

}
