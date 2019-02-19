package com.patho.main.ui.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.patho.main.model.patient.notification.ReportTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.util.helper.StreamUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactSelector extends AbstractSelector implements ID {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ReportTransmitter contact;

	private int copies;

	private List<OrganizationChooser> organizazionsChoosers;

	private String customAddress;

	private boolean emptyAddress;
	
	private boolean manuallyAltered;

	public ContactSelector(Task task, Person person, ContactRole role) {
		this(new ReportTransmitter(task, person, role), false, false);
	}

	public ContactSelector(Task task, Person person, ContactRole role, boolean selected, boolean emptyAddress) {
		this(new ReportTransmitter(task, person, role), selected, emptyAddress);
	}

	public ContactSelector(ReportTransmitter reportTransmitter) {
		this(reportTransmitter, false, false);
	}

	public ContactSelector(ReportTransmitter reportTransmitter, boolean selected, boolean emptyAddress) {
		this.contact = reportTransmitter;
		this.copies = 1;
		this.selected = selected;
		this.organizazionsChoosers = new ArrayList<OrganizationChooser>();

		if (reportTransmitter.getPerson().getOrganizsations() != null) {
			for (Organization organization : reportTransmitter.getPerson().getOrganizsations()) {
				this.organizazionsChoosers.add(new OrganizationChooser(this, organization,
						organization.equals(reportTransmitter.getPerson().getDefaultAddress())));
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

		setCustomAddress(AssociatedContactService.generateAddress(getContact(), selectedOrganization.orElse(null)));

		logger.trace("Custom Address is: " + getCustomAddress());
	}

	@Override
	public void setId(long l) {

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
