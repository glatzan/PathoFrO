package com.patho.main.action.dialog.settings.physician;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PhysicianEditDialog extends AbstractDialog<PhysicianEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianService physicianService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	private Physician physician;

	private List<ContactRole> allRoles;

	private DefaultTransformer<Organization> contactOrganizations;

	public PhysicianEditDialog initAndPrepareBean(Physician physician) {
		if (initBean(physician))
			prepareDialog();
		return this;
	}

	public boolean initBean(Physician physician) {
		setPhysician(physician);
		setAllRoles(Arrays.asList(ContactRole.values()));

		super.initBean(task, Dialog.SETTINGS_PHYSICIAN_EDIT);

		update();

		return true;
	}

	public void update() {
		
	}
	
	public void update(boolean reloadPhysician) {
		
		if (reloadPhysician) {
			physicianRepository.findop
			Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(getTask().getId(), false, false, false,
					false, true);
			setTask(oTask.get());
		}
		
		
		// setting organization for selecting an default notification
		setContactOrganizations(new DefaultTransformer<Organization>(getPhysician().getPerson().getOrganizsations()));

		// checking if organization exists within the organization array
		boolean organizationExsists = false;
		for (Organization organization : getPhysician().getPerson().getOrganizsations()) {
			if (getPhysician().getPerson().getDefaultAddress() != null
					&& getPhysician().getPerson().getDefaultAddress().equals(organization)) {
				organizationExsists = true;
				break;
			}
		}

		if (!organizationExsists)
			getPhysician().getPerson().setDefaultAddress(null);
	}

	/**
	 * Saves an edited physician to the database
	 * 
	 * @param physician
	 */
	public void save() {
		if (getPhysician() != null) {
			if (getPhysician().hasNoAssociateRole())
				getPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

			setPhysician(physicianRepository.save(getPhysician(), resourceBundle
					.get("log.settings.physician.physician.edit", getPhysician().getPerson().getFullName())));
		}
	}

	/**
	 * Updates the data of the physician with data from the clinic backend
	 */
	public void updateDataFromLdap() {
		physicianService.updatePhysicianWithLdapData(getPhysician());
		update();
	}

	/**
	 * Removes an organization from the user and updates the default address
	 * selection
	 * 
	 * @param organization
	 */
	public void removeFromOrganization(Organization organization) {
		getPhysician().getPerson().getOrganizsations().remove(organization);
		update();
	}

	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
			hideDialog();
		} else if (event.getObject() != null && event.getObject() instanceof OrganizationSelectReturnEvent) {
			getPhysician().getPerson().getOrganizsations()
					.add(((OrganizationSelectReturnEvent) event.getObject()).getOrganization());
		}
	}
}
