package com.patho.main.action.dialog.settings.users;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoUser;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.HistoUserSelectEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserListDialog extends AbstractDialog {

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
        setUsers(SpringContextBridge.services().getUserRepository().findAllIgnoreArchived(!showArchived));
    }

    public void selectAndHide() {
        super.hideDialog(new HistoUserSelectEvent(getSelectedUser()));
    }
}
