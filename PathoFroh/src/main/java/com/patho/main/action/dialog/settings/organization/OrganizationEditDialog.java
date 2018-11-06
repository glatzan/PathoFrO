package com.patho.main.action.dialog.settings.organization;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.common.Dialog;
import com.patho.main.model.Contact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.repository.PersonRepository;
import com.patho.main.service.OrganizationService;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class OrganizationEditDialog extends AbstractDialog<OrganizationEditDialog> {

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

	private Organization organization;

	private boolean newOrganization;

	/**
	 * Stores persons to remove from organization when saving
	 */
	private List<Person> removeFromOrganization;

	public OrganizationEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new Organization(new Contact()));
	}

	public OrganizationEditDialog initAndPrepareBean(Organization organization) {
		if (initBean(organization))
			prepareDialog();
		return this;
	}

	public boolean initBean(Organization organization) {

		if (organization.getId() == 0) {
			setOrganization(organization);
			setNewOrganization(true);
		} else {
			setOrganization(organizationRepository.findOptionalByID(organization.getId(), true).get());
			setNewOrganization(false);
		}

		setRemoveFromOrganization(new ArrayList<Person>());

		return super.initBean(task, Dialog.SETTINGS_ORGANIZATION_EDIT);
	}

	public void selectAndHide() {
		save();
		hideDialog(new OrganizationSelectReturnEvent(organization));
	}

	public void saveAndHide() {
		save();
		hideDialog(new ReloadEvent());
	}

	/**
	 * Saves an edited physician to the database
	 * 
	 * @param physician
	 */
	private void save() {
		organization = organizationService.addOrSaveOrganization(organization);

		for (Person person : removeFromOrganization) {
			personRepository.save(person, resourceBundle.get("log.person.organization.remove", person, organization));
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
