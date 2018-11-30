package com.patho.main.action.dialog.settings.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.UserRepository;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class UserListDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	private List<HistoUser> users;

	private boolean showArchived;

	private HistoUser selectedUser;

	public UserListDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();
		return this;
	}

	public boolean initBean() {
		setShowArchived(false);
		setSelectedUser(null);
		updateData();
		return super.initBean(task, Dialog.SETTINGS_USERS_LIST);
	}

	public void updateData() {
		setUsers(userRepository.findAllIgnoreArchived(!showArchived));
	}

	public void selectAndHide() {
		super.hideDialog(new HistoUserSelectEvent(getSelectedUser()));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class HistoUserSelectEvent implements DialogReturnEvent {
		private HistoUser user;
	}
}
