package com.patho.main.action.dialog.settings.organization;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.Contact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.repository.PersonRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.OrganizationService;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class OrganizationEditDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private OrganizationService organizationService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PersonRepository personRepository;
	
	private Organization organization;

	private boolean newOrganization;

	/**
	 * Stores persons to remove from organization when saving
	 */
	private List<Person> removeFromOrganization;

	public void initAndPrepareBean() {
		if (initBean(new Organization(new Contact())))
			prepareDialog();
	}

	public void initAndPrepareBean(Organization organization) {
		if (initBean(organization))
			prepareDialog();
	}

	public boolean initBean(Organization organization) {

		setOrganization(organization);

		setNewOrganization(organization.getId() == 0);

		setRemoveFromOrganization(new ArrayList<Person>());

		super.initBean(task, Dialog.SETTINGS_ORGANIZATION_EDIT);

		return true;
	}

	/**
	 * Saves an edited physician to the database
	 * 
	 * @param physician
	 */
	public void save() {
		try {
			organizationService.addOrganization(organization);

			for (Person person : removeFromOrganization) {
				personRepository.save(person, resourceBundle.get("log.person.organization.remove", person, organization));
			}

		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Removes a organization temporary from the person and adds it to an array in
	 * order to save the change when the user clicks on the save button.
	 * 
	 * @param person
	 * @param organization
	 */
	public void removePersonFromOrganization(Person person, Organization organization) {
		organizationService.removePerson(organization, person, false);
		getRemoveFromOrganization().add(person);
	}
}
