package com.patho.main.template.print.ui;

import java.util.List;
import java.util.NoSuchElementException;

import com.patho.main.action.dialog.print.PrintDialog;
import com.patho.main.model.AssociatedContact;
import com.patho.main.template.PrintDocument;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.selectors.ContactSelector.OrganizationChooser;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class AbsctractContactUi<T extends PrintDocument> extends AbstractDocumentUi<T> {

	/**
	 * List with all associated contacts
	 */
	protected List<ContactSelector> contactList;

	/**
	 * List if true single select mode of contacts is enabled
	 */
	protected boolean singleSelect;

	/**
	 * Pointer for printing all selected contacts
	 */
	protected ContactSelector contactListPointer;

	public AbsctractContactUi(T documentTemplate) {
		super(documentTemplate);
	}

	/**
	 * Updates the pdf content if a associatedContact was chosen for the first time
	 */
	public void onChooseContact(ContactSelector container) {
		if (!container.isSelected())
			container.getOrganizazionsChoosers().forEach(p -> p.setSelected(false));
		else {

			// setting default address to true
			if (container.getContact().getPerson().getDefaultAddress() != null) {
				for (OrganizationChooser organizationChooser : container.getOrganizazionsChoosers()) {
					if (organizationChooser.getOrganization()
							.equals(container.getContact().getPerson().getDefaultAddress())) {
						organizationChooser.setSelected(true);
						break;
					}
				}
			}

			// if single select mode remove other selections
			if (isSingleSelect()) {
				getContactList().stream().forEach(p -> {
					if (p != container) {
						p.setSelected(false);
						p.getOrganizazionsChoosers().forEach(s -> s.setSelected(false));
					}
				});
			}
		}

		container.generateAddress(true);

	}

	/**
	 * Updates the person if a organization was selected or deselected
	 * 
	 * @param chooser
	 */
	public void onChooseOrganizationOfContact(ContactSelector.OrganizationChooser chooser) {
		if (chooser.isSelected()) {
			// only one organization can be selected, removing other
			// organizations
			// from selection
			if (chooser.getParent().isSelected()) {
				for (ContactSelector.OrganizationChooser organizationChooser : chooser.getParent()
						.getOrganizazionsChoosers()) {
					if (organizationChooser != chooser) {
						organizationChooser.setSelected(false);
					}
				}
			} else {
				// setting parent as selected
				chooser.getParent().setSelected(true);
			}
		}

		chooser.getParent().generateAddress(true);
	}

	/**
	 * Gets the address of the first selected contact
	 * 
	 * @return
	 */
	public String getAddressOfFirstSelectedContact() {
		try {
			return contactList.stream().filter(p -> p.isSelected()).findFirst().get().getCustomAddress();
		} catch (NoSuchElementException e) {
			return "";
		}
	}

	public void beginNextTemplateIteration() {
		contactListPointer = null;
	}

	public boolean hasNextTemplateConfiguration() {
		boolean searchForNextContact = false;

		for (ContactSelector contactSelector : contactList) {
			if (contactSelector.isSelected()) {
				// first contact pointer
				if (contactListPointer == null) {
					contactListPointer = contactSelector;
					return true;
				} else if (searchForNextContact) {
					contactListPointer = contactSelector;
					return true;
				} else if (contactListPointer == contactSelector) {
					searchForNextContact = true;
					continue;
				}
			}
		}

		return false;

	}

}
