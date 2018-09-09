package com.patho.main.action.dialog.settings.organization;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.OrganizationRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class OrganizationListDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private OrganizationRepository organizationRepository;

	private List<Organization> organizations;

	private Person person;

	private boolean selectMode;

	private Organization selectedOrganization;

	public void initAndPrepareBean() {
		initAndPrepareBean(false);
	}

	public void initAndPrepareBean(boolean selectMode) {
		initBean(selectMode);
		prepareDialog();
	}

	public void initBean(boolean selectMode) {
		super.initBean(null, Dialog.SETTINGS_ORGANIZATION_LIST);

		this.selectMode = selectMode;

		updateOrganizationList();
	}

	public void updateOrganizationList() {
		setSelectedOrganization(null);
		setOrganizations(organizationRepository.findAll(true));
	}

	public void selectOrganisation() {
		super.hideDialog(selectedOrganization);
	}

	public void removeOrganization(Person person, Organization organization) {
		person.getOrganizsations().remove(organization);
	}
}