package com.patho.main.action.dialog.settings.listitem;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.ListItem;
import com.patho.main.repository.PersonRepository;
import com.patho.main.service.ListItemService;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Getter
@Setter
public class ListItemEditDialog extends AbstractDialog {

	private ListItem listItem;

	private boolean newListItem;

	public ListItemEditDialog initAndPrepareBean(ListItem.StaticList type) {
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
