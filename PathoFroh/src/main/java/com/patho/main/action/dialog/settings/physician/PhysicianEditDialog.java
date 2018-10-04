package com.patho.main.action.dialog.settings.physician;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PhysicianEditDialog extends AbstractDialog implements OrganizationFunctions {

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

	private DefaultTransformer<Organization> organizationTransformer;

	public void initAndPrepareBean(Physician physician) {
		if (initBean(physician))
			prepareDialog();
	}

	public boolean initBean(Physician physician) {
		setPhysician(physician);
		setAllRoles(Arrays.asList(ContactRole.values()));

		// setting organization transformer for selecting default address
		setOrganizationTransformer(
				new DefaultTransformer<Organization>(getPhysician().getPerson().getOrganizsations()));

		super.initBean(task, Dialog.SETTINGS_PHYSICIAN_EDIT);

		return true;
	}

	/**
	 * Saves an edited physician to the database
	 * 
	 * @param physician
	 */
	public void save() {
		try {
			if (getPhysician().hasNoAssociateRole())
				getPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

			physicianRepository.save(getPhysician(), resourceBundle.get("log.settings.physician.physician.edit",
					getPhysician().getPerson().getFullName()));

		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Updates the data of the physician with data from the clinic backend
	 */
	public void updateDataFromLdap() {
		try {
			physicianService.updatePhysicianWithLdapData(getPhysician());
			updateOrganizationSelection();
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Getting person from physician
	 */
	public Person getPerson() {
		return getPhysician().getPerson();
	}
}
