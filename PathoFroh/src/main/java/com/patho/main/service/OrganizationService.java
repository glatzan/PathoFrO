package com.patho.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.Contact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.repository.PersonRepository;

@Service
@Transactional
public class OrganizationService extends AbstractService {

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PersonRepository personRepository;

	/**
	 * Adds a new organization to the database
	 * 
	 * @param name
	 * @param contact
	 * @return
	 */
	public Organization addOrSaveOrganization(String name, Contact contact) {
		return addOrSaveOrganization(new Organization(name, contact));
	}

	/**
	 * Adds a new organization to the database
	 * 
	 * @param organization
	 * @return
	 */
	public Organization addOrSaveOrganization(Organization organization) {
		String log = resourceBundle.get(
				organization.getId() == 0 ? "log.organization.save" : "log.organization.created",
				organization.getName());
		return organizationRepository.save(organization, log);
	}

	/**
	 * Adds a person to an organization
	 * 
	 * @param organization
	 * @param person
	 */
	public void addPerson(Organization organization, Person person) {
		if (person.getOrganizsations() == null)
			person.setOrganizsations(new ArrayList<Organization>());

		if (!person.getOrganizsations().stream().anyMatch(p -> p.equals(organization))) {

			person.getOrganizsations().add(organization);

			// only save if person was saved before
			if (person.getId() != 0)
				personRepository.save(person,
						resourceBundle.get("log.organization.added", person.getFullName(), organization.getName()));

			logger.debug("Added Organization to Person");
		}
	}

	/**
	 * Removes a persons from an organization
	 * 
	 * @param organization
	 * @param person
	 * @return
	 */
	public boolean removePerson(Organization organization, Person person) {
		return removePerson(organization, person, true);
	}

	/**
	 * Removes a person from an organization, saving is optional
	 * 
	 * @param organization
	 * @param person
	 * @param save
	 * @return
	 */
	public boolean removePerson(Organization organization, Person person, boolean save) {
		logger.debug("Removing Organization from Patient");
		// removing organization form person, for the database this will remove the
		// inverted association as well
		boolean result = person.getOrganizsations().remove(organization);
		organization.getPersons().remove(person);

		if (result && save) {
			personRepository.save(person,
					resourceBundle.get("log.organization.remove", person.getFullName(), organization.getName()));
		}

		return result;
	}

	/**
	 * Checks the database if organizations exist. If organization is present it
	 * will be replaced in the list, otherwise it will be stored in the database.
	 * 
	 * @param organizations
	 */
	public void synchronizeOrganizations(List<Organization> organizations) {
		if (organizations == null)
			return;

		// saving new organizations
		for (int i = 0; i < organizations.size(); i++) {

			// do not reload loaded organizations
			if (organizations.get(i).getId() == 0) {
				Optional<Organization> databaseOrganization = organizationRepository
						.findOptionalByName(organizations.get(i).getName());
				if (!databaseOrganization.isPresent()) {
					logger.debug("Organization " + organizations.get(i).getName() + " not found, creating!");
					addOrSaveOrganization(organizations.get(i));
				} else {
					logger.debug("Organization " + organizations.get(i).getName() + " found, replacing in linst!");
					organizations.remove(i);
					organizations.add(i, databaseOrganization.get());
				}
			}
		}
	}
}