package com.patho.main.ui.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.patho.main.common.ContactRole;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.util.helper.StreamUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ContactSelector extends AbstractSelector implements ID {

	private AssociatedContact contact;

	private int copies;

	private List<OrganizationChooser> organizazionsChoosers;

	private String customAddress;

	private boolean emptyAddress;

	public ContactSelector(Task task, Person person, ContactRole role) {
		this(new AssociatedContact(task, person, role), false, false);
	}

	public ContactSelector(Task task, Person person, ContactRole role, boolean selected, boolean emptyAddress) {
		this(new AssociatedContact(task, person, role), selected, emptyAddress);
	}

	public ContactSelector(AssociatedContact associatedContact) {
		this(associatedContact, false, false);
	}

	public ContactSelector(AssociatedContact associatedContact, boolean selected, boolean emptyAddress) {
		this.contact = associatedContact;
		this.copies = 1;
		this.selected = selected;
		this.organizazionsChoosers = new ArrayList<OrganizationChooser>();

		if (associatedContact.getPerson().getOrganizsations() != null) {
			for (Organization organization : associatedContact.getPerson().getOrganizsations()) {
				this.organizazionsChoosers.add(new OrganizationChooser(this, organization,
						organization.equals(associatedContact.getPerson().getDefaultAddress())));
			}
		}

		this.emptyAddress = emptyAddress;

		if (emptyAddress) {
			setCustomAddress(" ");
		} else
			generateAddress();
	}

	public OrganizationChooser getSelectedOrganization() {
		try {
			return getOrganizazionsChoosers().stream().filter(p -> p.isSelected())
					.collect(StreamUtils.singletonCollector());
		} catch (IllegalStateException e) {
			return null;
		}
	}

	public void generateAddress() {
		generateAddress(false);
	}

	public void generateAddress(boolean overwrite) {
		Optional<String> address = Optional.ofNullable(getCustomAddress());

		if (address.isPresent() && !overwrite)
			return;

		Optional<Organization> selectedOrganization = Optional.ofNullable(getSelectedOrganization())
				.map(OrganizationChooser::getOrganization);

		setCustomAddress(AssociatedContact.generateAddress(getContact(), selectedOrganization.orElse(null)));

		log.trace("Custom Address is: " + getCustomAddress());
	}

	@Getter
	@Setter
	public class OrganizationChooser {

		private ContactSelector parent;
		private Organization organization;
		private boolean selected;

		public OrganizationChooser(ContactSelector parent, Organization organization, boolean selected) {
			this.parent = parent;
			this.organization = organization;
		}
	}

	public static List<ContactSelector> factory(Task task) {
		return task.getContacts().stream().map(p -> new ContactSelector(p, false, false)).collect(Collectors.toList());
	}

	@Override
	public long getId() {
		return getContact().getId();
	}
}
