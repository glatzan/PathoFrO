package com.patho.main.action.dialog.settings.organization;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class OrganizationListDialog extends AbstractDialog<OrganizationListDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private OrganizationRepository organizationRepository;

	private List<Organization> organizations;

	private Person person;

	private boolean selectMode;

	private Organization selectedOrganization;

	public OrganizationListDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();
		return this;
	}

	public boolean initBean() {
		setSelectMode(false);
		updateOrganizationList();
		return super.initBean(null, Dialog.SETTINGS_ORGANIZATION_LIST);
	}

	public void selectMode(boolean selectMode) {
		setSelectMode(selectMode);
	}

	public void updateOrganizationList() {
		setSelectedOrganization(null);
		setOrganizations(organizationRepository.findAll(true));
	}

	public void removeOrganization(Person person, Organization organization) {
		person.getOrganizsations().remove(organization);
	}

	/**
	 * Selects an hides the dialog
	 */
	public void selectAndHide() {
		hideDialog(new OrganizationSelectReturnEvent(getSelectedOrganization()));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class OrganizationSelectReturnEvent implements DialogReturnEvent {
		private Organization organization;
	}
}