package com.patho.main.action.dialog.settings.organization;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.person.Organization;
import com.patho.main.model.person.Person;
import com.patho.main.repository.OrganizationRepository;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.OrganizationSelectEvent;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Getter
@Setter
public class OrganizationListDialog extends AbstractDialog {

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

	public void update() {
		setSelectedOrganization(null);
		setOrganizations(SpringContextBridge.services().getOrganizationRepository().findAll(true));
	}

	/**
	 * Selects an hides the dialog
	 */
	public void selectAndHide() {
		hideDialog(new OrganizationSelectEvent(getSelectedOrganization()));
	}

	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() instanceof ReloadEvent) {
			update();
		}
	}
}