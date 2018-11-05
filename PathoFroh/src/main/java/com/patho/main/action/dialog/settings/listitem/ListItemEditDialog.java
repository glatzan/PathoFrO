package com.patho.main.action.dialog.settings.listitem;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.common.Dialog;
import com.patho.main.model.Contact;
import com.patho.main.model.ListItem;
import com.patho.main.model.ListItem_;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.repository.PersonRepository;
import com.patho.main.service.ListItemService;
import com.patho.main.service.OrganizationService;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class ListItemEditDialog extends AbstractDialog<ListItemEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemService listItemService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PersonRepository personRepository;

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
		listItemService.addOrUpdate(getListItem());
	}

}
