package com.patho.main.action.dialog.settings.physician;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Contact;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.repository.LDAPRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PhysicianSearchDialog extends AbstractTabDialog<PhysicianSearchDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private LDAPRepository ldapRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianService physicianService;

	private InternalPhysician internalPhysicianTab;

	private ExternalPhysician externalPhysicianTab;

	/**
	 * If true it is possible to create an external physician or patient
	 */
	private boolean externalMode;

	public PhysicianSearchDialog() {
		setInternalPhysicianTab(new InternalPhysician());
		setExternalPhysicianTab(new ExternalPhysician());

		tabs = new AbstractTab[] { internalPhysicianTab, externalPhysicianTab };
	}

	public PhysicianSearchDialog initAndPrepareBean() {
		return initAndPrepareBean(false);
	}

	public PhysicianSearchDialog initAndPrepareBean(boolean externalMode) {
		if (initBean(externalMode))
			prepareDialog();

		return this;
	}

	public boolean initBean(boolean externalMode) {
		setExternalMode(externalMode);

		return super.initBean(task, Dialog.SETTINGS_PHYSICIAN_SEARCH);
	}

	@Getter
	@Setter
	public class InternalPhysician extends AbstractTab {
		/**
		 * List containing all physicians known in the histo database
		 */
		private List<Physician> physicianList;

		/**
		 * Used for selecting a physician
		 */
		private Physician selectedPhysician;

		/**
		 * String is used for searching for internal physicians
		 */
		private String searchString;

		/**
		 * Array of roles which the physician should be associated
		 */
		private ContactRole[] associatedRoles;

		/**
		 * all roles
		 */
		private List<ContactRole> allRoles;

		public InternalPhysician() {
			setTabName("InternalPhysicianTab");
			setName("dialog.physicianSearch.person.new.internal");
			setViewID("internalPhysicianSearch");
			setCenterInclude("include/clinicSearch.xhtml");
		}

		public boolean initTab() {
			setSelectedPhysician(null);
			setSearchString("");
			setPhysicianList(null);
			setAssociatedRoles(new ContactRole[] { ContactRole.OTHER_PHYSICIAN });
			setAllRoles(Arrays.asList(ContactRole.values()));
			return true;
		}

		/**
		 * Generates an ldap search filter (?(xxx)....) and offers the result list. The
		 * result list is a physician list with minimal details. Before adding an clinic
		 * physician a ldap fetch for more details has to be done
		 *
		 * @param name
		 */
		public void searchForPhysician() {
			if (searchString != null && searchString.length() > 3) {
				// removing multiple spaces an commas and replacing them with one
				// space,
				// splitting the whole thing into an array
				String[] arr = searchString.replaceAll("[ ,]+", " ").split(" ");
				logger.debug("Hallo " + searchString);
				setPhysicianList(ldapRepository.findAllByName(arr));

				int i = 0;
				for (Physician phys : getPhysicianList()) {
					phys.setId(i++);
				}

				setSelectedPhysician(null);
			}
		}

		public void selectAndHide() {
			save();
			hideDialog(new PhysicianReturnEvent(getSelectedPhysician()));
		}

		public void saveAndHide() {
			save();
			hideDialog();
		}

		private void save() {
			if (getSelectedPhysician() != null) {
				getSelectedPhysician()
						.setAssociatedRoles(new HashSet<ContactRole>(Arrays.asList(getAssociatedRoles())));

				if (getSelectedPhysician().getAssociatedRoles().size() == 0)
					getSelectedPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

				setSelectedPhysician(physicianService.addOrMergePhysician(getSelectedPhysician()));
			}
		}
	}

	@Getter
	@Setter
	public class ExternalPhysician extends AbstractTab {

		/**
		 * Used for creating new or for editing existing physicians
		 */
		private Physician selectedPhysician;

		/**
		 * Array of roles which the physician should be associated
		 */
		private ContactRole[] associatedRoles;

		/**
		 * all roles
		 */
		private List<ContactRole> allRoles;

		/**
		 * Transformer for selection an organization as contact address
		 */
		private DefaultTransformer<Organization> contactOrganizations;

		public ExternalPhysician() {
			setTabName("ExternalPhysicianTab");
			setName("dialog.physicianSearch.person.new.external");
			setViewID("ecternalPhysicianSearch");
			setCenterInclude("include/externalPerson.xhtml");
		}

		public boolean initTab() {
			setSelectedPhysician(new Physician(new Person(new Contact())));
			// person is not auto update able
			getSelectedPhysician().getPerson().setAutoUpdate(false);
			getSelectedPhysician().getPerson().setOrganizsations(new ArrayList<Organization>());

			setAssociatedRoles(new ContactRole[] { ContactRole.OTHER_PHYSICIAN });
			setAllRoles(Arrays.asList(ContactRole.values()));

			updateData();

			return true;
		}

		@Override
		public void updateData() {
			// setting organization for selecting an default notification
			setContactOrganizations(
					new DefaultTransformer<Organization>(getSelectedPhysician().getPerson().getOrganizsations()));

			// checking if organization exists within the organization array
			boolean organizationExsists = false;
			for (Organization organization : getSelectedPhysician().getPerson().getOrganizsations()) {
				if (getSelectedPhysician().getPerson().getDefaultAddress() != null
						&& getSelectedPhysician().getPerson().getDefaultAddress().equals(organization)) {
					organizationExsists = true;
					break;
				}
			}

			if (!organizationExsists)
				getSelectedPhysician().getPerson().setDefaultAddress(null);

			super.updateData();
		}

		public void selectAndHide() {
			save();
			hideDialog(new PhysicianReturnEvent(getSelectedPhysician()));
		}

		public void saveAndHide() {
			save();
			hideDialog();
		}

		private void save() {
			if (getSelectedPhysician() != null) {
				getSelectedPhysician()
						.setAssociatedRoles(new HashSet<ContactRole>(Arrays.asList(getAssociatedRoles())));

				if (getSelectedPhysician().hasNoAssociateRole())
					getSelectedPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

				setSelectedPhysician(physicianService.addOrMergePhysician(getSelectedPhysician()));
			}
		}

		/**
		 * Removes an organization from the user and updates the default address
		 * selection
		 * 
		 * @param organization
		 */
		public void removeFromOrganization(Organization organization) {
			getSelectedPhysician().getPerson().getOrganizsations().remove(organization);
			updateData();
		}

		public void onDefaultDialogReturn(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
				hideDialog();
			} else if (event.getObject() != null && event.getObject() instanceof OrganizationSelectReturnEvent) {
				getSelectedPhysician().getPerson().getOrganizsations()
						.add(((OrganizationSelectReturnEvent) event.getObject()).getOrganization());
			}
		}

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class PhysicianReturnEvent implements DialogReturnEvent {
		private Physician physician;
	}

}
