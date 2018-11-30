package com.patho.main.action.dialog.settings.physician;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.ui.transformer.DefaultTransformer;

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

	/**
	 * All roles
	 */
	private List<ContactRole> allRoles;

	/**
	 * Organization transformer for selecting a default address
	 */
	private DefaultTransformer<Organization> organizationTransformer;

	public PhysicianEditDialog initAndPrepareBean(Physician physician) {
		if (initBean(physician))
			prepareDialog();
		return this;
	}

	public boolean initBean(Physician physician) {
		setPhysician(physician);
		setAllRoles(Arrays.asList(ContactRole.values()));
		update();
		return super.initBean(task, Dialog.SETTINGS_PHYSICIAN_EDIT);
	}

	public void update() {
		updateOrganizationSelection();
	}

	/**
	 * Saves an edited physician to the database
	 * 
	 * @param physician
	 */
	public void save() {
		if (getPhysician() != null) {
			physicianService.savePhysican(getPhysician());
		}
	}

	/**
	 * Updates the data of the physician with data from the clinic backend
	 */
	public void updateDataFromLdap() {
		try {
			Physician updatePhyscian = physicianRepository.findById(getPhysician().getId()).get();
			updatePhyscian.getPerson().setAutoUpdate(true);
			setPhysician(physicianService.updatePhysicianWithLdapData(updatePhyscian));
			update();
			MessageHandler.sendGrowlMessagesAsResource("growl.patient.updateLadp.success",
					"growl.patient.updateLadp.success.info");
		} catch (IllegalStateException e) {
			update();
			MessageHandler.sendGrowlMessagesAsResource("growl.patient.updateLadp.failed",
					"growl.patient.updateLadp.failed.info");
		}
	}

	@Override
	public Person getPerson() {
		return getPhysician().getPerson();
	}
}
