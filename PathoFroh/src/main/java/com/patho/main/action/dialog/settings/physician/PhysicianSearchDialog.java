package com.patho.main.action.dialog.settings.physician;

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
import com.patho.main.service.PhysicianService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@Configurable
@Getter
@Setter
public class PhysicianSearchDialog extends AbstractTabDialog {

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

	/**
	 * If true the physician will not be saved, but returned as a transient entity
	 */
	private boolean selectMode;

	public PhysicianSearchDialog() {
		setInternalPhysicianTab(new InternalPhysician());
		setExternalPhysicianTab(new ExternalPhysician());

		tabs = new AbstractTab[] { internalPhysicianTab, externalPhysicianTab };
	}

	public PhysicianSearchDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();

		return this;
	}

	public boolean initBean() {
		return super.initBean(task, Dialog.SETTINGS_PHYSICIAN_SEARCH);
	}

	public PhysicianSearchDialog externalMode() {
		setExternalMode(true);
		externalPhysicianTab.setDisabled(true);
		return this;
	}

	public PhysicianSearchDialog selectMode() {
		setSelectMode(true);
		return this;
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
			hideDialog(new PhysicianReturnEvent(false, getSelectedPhysician()));
		}

		public void saveAndHide() {
			save();
			hideDialog(new ReloadEvent());
		}

		private void save() {
			if (getSelectedPhysician() != null) {
				getSelectedPhysician().setId(0);
				getSelectedPhysician()
						.setAssociatedRoles(new HashSet<ContactRole>(Arrays.asList(getAssociatedRoles())));
				setSelectedPhysician(physicianService.savePhysican(getSelectedPhysician()));
			}
		}
	}

	@Getter
	@Setter
	public class ExternalPhysician extends AbstractTab implements OrganizationFunctions {

		/**
		 * Used for creating new or for editing existing physicians
		 */
		private Physician selectedPhysician;

		/**
		 * all roles
		 */
		private List<ContactRole> allRoles;

		/**
		 * Transformer for selection an organization as contact address
		 */
		private DefaultTransformer<Organization> organizationTransformer;

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
			getSelectedPhysician().getPerson().setOrganizsations(new HashSet<Organization>());
			getSelectedPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN);

			setAllRoles(Arrays.asList(ContactRole.values()));

			updateData();

			return true;
		}

		@Override
		public void updateData() {
			updateOrganizationSelection();
		}

		public void selectAndHide() {
			save();
			hideDialog(new PhysicianReturnEvent(true, getSelectedPhysician()));
		}

		public void saveAndHide() {
			save();
			hideDialog(new ReloadEvent());
		}

		private void save() {
			setSelectedPhysician(physicianService.addOrMergePhysician(getSelectedPhysician()));
		}

		@Override
		public Person getPerson() {
			return getSelectedPhysician().getPerson();
		}

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class PhysicianReturnEvent implements DialogReturnEvent {
		private boolean extern;
		private Physician physician;
	}

}
