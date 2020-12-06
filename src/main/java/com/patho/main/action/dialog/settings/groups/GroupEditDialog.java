package com.patho.main.action.dialog.settings.groups;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoGroup.AuthRole;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import com.patho.main.util.search.settings.SimpleListSearchOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class GroupEditDialog extends AbstractTabDialog {

    private GeneralTab generalTab;
    private NameTab nameTab;
    private PermissionTab permissionTab;

    private boolean newGroup;

    private HistoGroup group;

    public GroupEditDialog() {
        setNameTab(new NameTab());
        setGeneralTab(new GeneralTab());
        setPermissionTab(new PermissionTab());

        tabs = new AbstractTab[]{nameTab, generalTab, permissionTab};
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
            setGroup(SpringContextBridge.services().getGroupRepository().findOptionalById(group.getId(), true).get());
            setNewGroup(false);
        } else {
            setGroup(group);
            setNewGroup(true);
        }

        return super.initBean(Dialog.SETTINGS_GROUP_EDIT);
    }

    public void saveAndHide() {

        // setting default view if not set
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

        // saving
        SpringContextBridge.services().getGroupService().addOrUpdate(getGroup());

        hideDialog(new ReloadEvent());
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
        private SimpleListSearchOption[] allWorklistOptions;

        public GeneralTab() {
            setTabName("GeneralTab");
            setName("dialog.groupEdit.tab.settings.headline");
            setViewID("generalTab");
            setCenterInclude("include/general.xhtml");
        }

        public boolean initTab() {
            setAllViews(new View[]{View.GUEST, View.INCLUDE_PAGE_TASKS, View.INCLUDE_PAGE_PATIENT, View.INCLUDE_PAGE_DIAGNOSIS,
                    View.INCLUDE_PAGE_RECEIPTLOG, View.INCLUDE_PAGE_REPORT});

            setAllWorklistOptions(
                    new SimpleListSearchOption[]{SimpleListSearchOption.DIAGNOSIS_LIST, SimpleListSearchOption.STAINING_LIST,
                            SimpleListSearchOption.NOTIFICATION_LIST, SimpleListSearchOption.EMPTY_LIST});

            return true;
        }

        public void voidAction() {
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
