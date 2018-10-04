package com.patho.main.action.dialog.settings.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.UserService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class UserListDialog extends AbstractDialog {

	@Autowired
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	private List<HistoUser> users;

	private List<HistoGroup> groups;

	private DefaultTransformer<HistoGroup> groupTransformer;

	private boolean showArchived;

	private HistoUser selectedUser;

	private boolean selectMode;

	private boolean editMode;

	public void initAndPrepareBean() {
		if (initBean(false, false))
			prepareDialog();
	}

	public void initAndPrepareBean(boolean selectMode, boolean editMode) {
		if (initBean(selectMode, editMode))
			prepareDialog();
	}

	public boolean initBean(boolean selectMode, boolean editMode) {
		setShowArchived(false);
		setSelectMode(selectMode);
		setEditMode(editMode);

		setSelectedUser(null);

		updateData();

		super.initBean(task, Dialog.SETTINGS_USERS_LIST);
		return true;
	}

	public void updateData() {
		setUsers(userRepository.findAllIgnoreArchived(!showArchived));
		setGroups(groupRepository.findAll(true));
		setGroupTransformer(new DefaultTransformer<HistoGroup>(getGroups()));
	}

	public void selectUserAndHide() {
		super.hideDialog(getSelectedUser());
	}

	public void onChangeUserGroup(HistoUser user) {
		try {
			userService.updateGroupOfUser(user, user.getGroup());
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	public void archive(HistoUser user, boolean archive) {
		try {
			userService.archiveUser(user, archive);
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

}
