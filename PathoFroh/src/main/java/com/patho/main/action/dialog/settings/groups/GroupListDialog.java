package com.patho.main.action.dialog.settings.groups;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.repository.GroupRepository;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.GroupSelectEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Getter
@Setter
public class GroupListDialog extends AbstractDialog {

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
		setGroups(SpringContextBridge.services().getGroupRepository().findAllOrderByIdAsc(!showArchived));
	}

	public void selectAndHide() {
		super.hideDialog(new GroupSelectEvent(getSelectedGroup()));
	}

}
