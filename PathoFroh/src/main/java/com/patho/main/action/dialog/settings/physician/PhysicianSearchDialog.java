package com.patho.main.action.dialog.settings.physician;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Organization;
import com.patho.main.model.person.Person;
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
	}

	public PhysicianSearchDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();

		return this;
	}

	public boolean initBean() {
		tabs = new AbstractTab[] { internalPhysicianTab };
		return super.initBean(task, Dialog.SETTINGS_PHYSICIAN_SEARCH);
	}

	public PhysicianSearchDialog externalMode() {
		setExternalMode(true);
		appendTab(externalPhysicianTab);
		externalPhysicianTab.initTab();
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

				AtomicInteger i = new AtomicInteger(0);
				setPhysicianList(ldapRepository.findAllByName(arr).stream()
						.map(p -> new PhysicianContainer(i.getAndIncrement(), p)).collect(Collectors.toList()));

				setSelectedPhysician(null);
			}
		}

		public void selectAndHide() {
			logger.debug("Select and Hide");
			hideDialog(new PhysicianReturnEvent(false, ((PhysicianContainer) getSelectedPhysician()).getPhysician()));
		}

		public void saveAndHide() {
			logger.debug("Save and hide");
			save();
			hideDialog(new ReloadEvent());
		}

		private void save() {
			if (getSelectedPhysician() != null) {
				Physician phys = ((PhysicianContainer) getSelectedPhysician()).getPhysician();
				phys.setAssociatedRoles(new HashSet<ContactRole>(Arrays.asList(getAssociatedRoles())));
				((PhysicianContainer) getSelectedPhysician()).setPhysician(physicianService.addOrMergePhysician(phys));
			}
		}

		/**
		 * Container for providing an artifical id
		 * 
		 * @author andi
		 *
		 */
		@Getter
		@Setter
		@AllArgsConstructor
		public class PhysicianContainer extends Physician {
			private int listID;
			@Delegate
			private Physician physician;
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
