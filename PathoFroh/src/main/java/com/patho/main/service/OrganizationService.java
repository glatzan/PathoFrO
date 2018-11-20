package com.patho.main.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.Contact;
import com.patho.main.model.DiagnosisPreset;
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
	public Organization addOrUpdate(String name, Contact contact) {
		return addOrUpdate(new Organization(name, contact));
	}

	/**
	 * Adds a new organization to the database
	 * 
	 * @param organization
	 * @return
	 */
	public Organization addOrUpdate(Organization organization) {
		String log = resourceBundle.get(organization.getId() == 0 ? "log.organization.new" : "log.organization.update",
				organization.getName());
		return organizationRepository.save(organization, log);
	}

	/**
	 * Tries to delete the organization, if not possible the organization will be
	 * archived
	 * 
	 * @param p
	 */
	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(Organization organization) {
		try {
			organizationRepository.delete(organization,
					resourceBundle.get("log.organization.deleted", organization.getName()));
			return true;
		} catch (Exception e) {
			archive(organization, true);
			return false;
		}
	}

	/**
	 * Archived or dearchives a organization
	 * 
	 * @param p
	 * @param archive
	 * @return
	 */
	public Organization archive(Organization organization, boolean archive) {
		organization.setArchived(archive);
		return organizationRepository.save(organization, resourceBundle
				.get(archive ? "log.organization.archived" : "log.organization.dearchived", organization.getName()));
	}

	/**
	 * Adds a person to an organization
	 * 
	 * @param organization
	 * @param person
	 */
	public void addPerson(Organization organization, Person person) {
		if (person.getOrganizsations() == null)
			person.setOrganizsations(new HashSet<Organization>());

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
	public List<Organization> synchronizeOrganizations(Set<Organization> organizations) {
		return synchronizeOrganizations(new ArrayList<Organization>(organizations));
	}

	/**
	 * Checks the database if organizations exist. If organization is present it
	 * will be replaced in the list, otherwise it will be stored in the database.
	 * 
	 * @param organizations
	 */
	public List<Organization> synchronizeOrganizations(List<Organization> organizations) {
		if (organizations == null)
			return null;

		List<Organization> result = new ArrayList<Organization>(organizations.size());

		// saving new organizations
		for (int i = 0; i < organizations.size(); i++) {

			// do not reload loaded organizations
			if (organizations.get(i).getId() == 0) {
				Optional<Organization> databaseOrganization = organizationRepository
						.findOptionalByName(organizations.get(i).getName());
				if (!databaseOrganization.isPresent()) {
					logger.debug("Organization " + organizations.get(i).getName() + " not found, creating!");
					result.add(addOrUpdate(organizations.get(i)));
				} else {
					logger.debug("Organization " + organizations.get(i).getName() + " found, replacing in linst!");
					result.add(databaseOrganization.get());
				}
			}
		}

		return result;
	}
}