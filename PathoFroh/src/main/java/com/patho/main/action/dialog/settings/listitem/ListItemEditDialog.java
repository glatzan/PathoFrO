package com.patho.main.action.dialog.settings.listitem;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.system.ListItem;
import com.patho.main.model.system.ListItemType;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListItemEditDialog extends AbstractDialog {

    private ListItem listItem;

    private boolean newListItem;

    public ListItemEditDialog initAndPrepareBean(ListItemType type) {
        return initAndPrepareBean(new ListItem(type));
    }

    public ListItemEditDialog initAndPrepareBean(ListItem listItem) {
        if (initBean(listItem))
            prepareDialog();
        return this;
    }

    public boolean initBean(ListItem listItem) {

        setListItem(listItem);
        setNewListItem(listItem.getId() == 0);

        return super.initBean(task, Dialog.SETTINGS_LISTITEM_EDIT);
    }

    public void saveAndHide() {
        save();
        hideDialog(new ReloadEvent());
    }

    private void save() {
        SpringContextBridge.services().getListItemService().addOrUpdate(getListItem());
    }

}
