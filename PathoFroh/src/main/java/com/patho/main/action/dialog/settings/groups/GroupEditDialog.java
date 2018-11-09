package com.patho.main.action.dialog.settings.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoGroup.AuthRole;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.UserService;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class GroupEditDialog extends AbstractTabDialog<GroupEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	private GeneralTab generalTab;
	private NameTab nameTab;
	private PermissionTab permissionTab;

	private boolean newGroup;

	private HistoGroup group;

	public GroupEditDialog() {
		setNameTab(new NameTab());
		setGeneralTab(new GeneralTab());
		setPermissionTab(new PermissionTab());

		tabs = new AbstractTab[] { nameTab, generalTab, permissionTab };
	}

	public GroupEditDialog initAndPrepareBean() {
		HistoGroup group = new HistoGroup(new HistoSettings());
		group.getSettings().setAvailableViews(new ArrayList<View>());
		group.setAuthRole(AuthRole.ROLE_NONEAUTH);
		group.setPermissions(new HashSet<HistoPermissions>());

		return initAndPrepareBean(group);
	}

	public GroupEditDialog initAndPrepareBean(HistoGroup group) {
		if (initBean(group, true))
			prepareDialog();

		return this;
	}

	public boolean initBean(HistoGroup group, boolean initialize) {

		if (group.getId() != 0) {
			groupRepository.fi
			setGroup(groupDao.find(group.getId(), true));
			setNewGroup(false);
		} else {
			setGroup(group);
			setNewGroup(true);
		}

		// if new group
	

		return super.initBean(Dialog.SETTINGS_GROUP_EDIT);
	}

	public void saveGroup() {

		if (getGroup().getSettings().getDefaultView() == null) {
			if (getGroup().getSettings().getAvailableViews() == null
					&& getGroup().getSettings().getAvailableViews().size() > 0)
				getGroup().getSettings().setDefaultView(getGroup().getSettings().getAvailableViewsAsArray()[0]);
		}

		// settings permissions
		getGroup().getPermissions().clear();

		// adding/ readding permissions
		permissionTab.permissions.forEach((p, v) -> {
			if (v.isValue()) {
				getGroup().getPermissions().add(v.getPermission());
			}
		});

		try {
			if (getGroup().getId() == 0) {
				groupRepository.save(getGroup(), resourceBundle.get("log.settings.group.new", getGroup()));
			} else {
				groupRepository.save(getGroup(), resourceBundle.get("log.settings.group.edit", getGroup()));

				// updating user settings
				List<HistoUser> usersOfGroup = userRepository.findByGroupId(getGroup().getId());

				for (HistoUser histoUser : usersOfGroup) {
					userService.updateGroupOfUser(histoUser, getGroup());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			onDatabaseVersionConflict();
		}

	}

	@Getter
	@Setter
	public class NameTab extends AbstractTab {

		public NameTab() {
			setTabName("NameTab");
			setName("dialog.groupEdit.tab.group.headline");
			setViewID("nameTab");
			setCenterInclude("include/name.xhtml");
		}

	}

	@Getter
	@Setter
	public class GeneralTab extends AbstractTab {

		private View[] allViews;
		private SimpleSearchOption[] allWorklistOptions;

		public GeneralTab() {
			setTabName("GeneralTab");
			setName("dialog.groupEdit.tab.settings.headline");
			setViewID("generalTab");
			setCenterInclude("include/general.xhtml");
		}

		public boolean initTab() {
			setAllViews(new View[] { View.GUEST, View.WORKLIST_TASKS, View.WORKLIST_PATIENT, View.WORKLIST_DIAGNOSIS,
					View.WORKLIST_RECEIPTLOG, View.WORKLIST_REPORT });

			setAllWorklistOptions(
					new SimpleSearchOption[] { SimpleSearchOption.DIAGNOSIS_LIST, SimpleSearchOption.STAINING_LIST,
							SimpleSearchOption.NOTIFICATION_LIST, SimpleSearchOption.EMPTY_LIST });

			return true;
		}
	}

	@Getter
	@Setter
	public class PermissionTab extends AbstractTab {

		@Setter(AccessLevel.NONE)
		private Map<String, PermissionHolder> permissions;

		public PermissionTab() {
			setTabName("PermissionTab");
			setName("dialog.groupEdit.tab.rights.headline");
			setViewID("permissionTab");
			setCenterInclude("include/permissions.xhtml");
		}

		public boolean initTab() {
			setPermissions(group.getPermissions());
			return true;
		}

		public void onChangePermission(PermissionHolder holder) {
			permissions.forEach((k, v) -> {
				if (v.getPermission().getParent() == holder.getPermission()) {
					v.setValue(holder.isValue());
				}
			});
		}

		public void setPermissions(Set<HistoPermissions> groupPermissions) {
			permissions = new HashMap<String, PermissionHolder>();

			Set<HistoPermissions> groupPermissionsCopy = new HashSet<HistoPermissions>(groupPermissions);
			HistoPermissions[] permissionArr = HistoPermissions.values();

			for (int i = 0; i < permissionArr.length; i++) {
				PermissionHolder permissionIsSet = new PermissionHolder(false, permissionArr[i]);
				for (HistoPermissions histoPermission : groupPermissionsCopy) {
					if (permissionArr[i] == histoPermission) {
						permissionIsSet.setValue(true);
						break;
					}
				}

				permissions.put(permissionArr[i].name(), permissionIsSet);
			}
		}
	}

	@Getter
	@Setter
	public class PermissionHolder {

		private boolean value;
		private HistoPermissions permission;

		public PermissionHolder(boolean value, HistoPermissions permission) {
			this.value = value;
			this.permission = permission;
		}
	}
}
