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
	private OrganizationService organizationService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private OrganizationRepository organizationRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PersonRepository personRepository;

	private ListItem listItem;

	private boolean newListItem;

	public ListItemEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new ListItem());
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

	public static Specification<ListItem> isLongTermCustomer() {
		    return new Specification<ListItem> {
		    	 	
		      Predicate toPredicate(Root<ListItem> root, CriteriaQuery query, CriteriaBuilder cb) {
		        return null;
		      }
		   
		    };
	}

	private void save() {

		if (getEditListItem().getId() == 0) {

			getEditListItem().setListType(getSelectedStaticList());

			logger.debug("Creating new ListItem " + getEditListItem().getValue() + " for "
					+ getSelectedStaticList().toString());
			// case new, save
			getStaticListContent().add(getEditListItem());

			listItemRepository.save(getEditListItem(), resourceBundle.get("log.settings.staticList.new",
					getEditListItem().getValue(), getSelectedStaticList().toString()));

			ListOrder.reOrderList(getStaticListContent());

			listItemRepository.saveAll(getStaticListContent(),
					resourceBundle.get("log.settings.staticList.list.reoder", getSelectedStaticList()));

		} else {
			logger.debug("Updating ListItem " + getEditListItem().getValue());
			// case edit: update an save

			listItemRepository.save(getEditListItem(), resourceBundle.get("log.settings.staticList.update",
					getEditListItem().getValue(), getSelectedStaticList().toString()));
		}

		organization = organizationService.addOrSaveOrganization(organization);

		for (Person person : removeFromOrganization) {
			personRepository.save(person, resourceBundle.get("log.person.organization.remove", person, organization));
		}
	}

}
