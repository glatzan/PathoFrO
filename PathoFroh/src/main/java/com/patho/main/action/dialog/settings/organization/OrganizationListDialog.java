package com.patho.main.action.dialog.settings.organization;

import java.util.List;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

	public OrganizationListDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();
		return this;
	}

	public boolean initBean() {
		setSelectMode(false);
		update();
		return super.initBean(null, Dialog.SETTINGS_ORGANIZATION_LIST);
	}

	public void selectMode(boolean selectMode) {
		setSelectMode(selectMode);
	}

	private void update() {
		setSelectedOrganization(null);
		setOrganizations(organizationRepository.findAll(true));
	}

	/**
	 * Selects an hides the dialog
	 */
	public void selectAndHide() {
		hideDialog(new OrganizationSelectReturnEvent(getSelectedOrganization()));
	}

	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
			update();
		}
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class OrganizationSelectReturnEvent implements DialogReturnEvent {
		private Organization organization;
	}
}