package com.patho.main.template.print.ui.document.report;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.patho.main.model.patient.notification.AssociatedContact;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.selectors.ContactSelector.OrganizationChooser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class AbsctractContactUi<T extends PrintDocument, S extends AbsctractContactUi.SharedContactData>
		extends AbstractDocumentUi<T, S> {

	/**
	 * Pointer for printing all selected contacts
	 */
	protected ContactSelector contactListPointer;

	public AbsctractContactUi(T documentTemplate, S sharedData) {
		super(documentTemplate, sharedData);
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
			if (sharedData.isSingleSelect()) {
				sharedData.getContactList().stream().forEach(p -> {
					if (p != container) {
						p.setSelected(false);
						p.getOrganizazionsChoosers().forEach(s -> s.setSelected(false));
					}
				});
			}
		}

		container.generateAddress(false);

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

			// if single select mode remove other selections
			if (sharedData.isSingleSelect()) {
				sharedData.getContactList().stream().forEach(p -> {
					if (p != chooser.getParent()) {
						p.setSelected(false);
						p.getOrganizazionsChoosers().forEach(s -> s.setSelected(false));
					}
				});
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
			return sharedData.contactList.stream().filter(p -> p.isSelected()).findFirst().get().getCustomAddress();
		} catch (NoSuchElementException e) {
			return "";
		}
	}

	/**
	 * Returns the first selected contact
	 */
	public AssociatedContact getFirstSelectedContact() {
		Optional<ContactSelector> res = getSharedData().getContactList().stream().filter(p -> p.isSelected())
				.findFirst();
		if (res.isPresent()) {
			return res.get().getContact();
		}
		return null;
	}

	/**
	 * Resets the template pointer
	 */
	public void beginNextTemplateIteration() {
		contactListPointer = null;
	}

	/**
	 * Checks if an other template configuration is present
	 */
	public boolean hasNextTemplateConfiguration() {
		boolean searchForNextContact = false;

		for (ContactSelector contactSelector : sharedData.contactList) {
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

	/**
	 * Data which can be share between Ui Objects.
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	public static class SharedContactData extends AbstractDocumentUi.SharedData {
		/**
		 * List with all associated contacts
		 */
		protected List<ContactSelector> contactList;

		/**
		 * List if true single select mode of contacts is enabled
		 */
		protected boolean singleSelect;

		/**
		 * If true the pdf will be updated on every settings change
		 */
		protected boolean updatePdfOnEverySettingChange = false;

		/**
		 * If true the first selected contact will be rendered
		 */
		protected boolean renderSelectedContact = false;

		/**
		 * Initializes the shareddata context.
		 * 
		 * @param task
		 * @param contactList
		 */
		public void initializ(Task task, List<ContactSelector> contactList) {
			if (isInitialized())
				return;
		}
	}

}
