package com.patho.main.action.dialog.settings.physician;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.AbstractTabDialog.AbstractTab;
import com.patho.main.action.dialog.settings.organization.OrganizationFunctions;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PhysicianSearchDialog extends AbstractTabDialog<PhysicianSearchDialog> implements OrganizationFunctions {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private LDAPRepository ldapRepository;

	private InternalPhysician internalPhysicianTab;

	private ExternalPhysician externalPhysicianTab;

	/**
	 * If true it is possible to create an external physician or patient
	 */
	private boolean externalMode;

	/**
	 * Transformer for selecting organization for external physicians
	 */
	private DefaultTransformer<Organization> organizationTransformer;

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
		 * Used for creating new or for editing existing physicians
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
			setName("dialog.physicianSearch.internal");
			setViewID("internalPhysicianSearch");
			setCenterInclude("include/clinicSearch.xhtml");
		}

		public boolean initTab() {
			setSelectedPhysician(null);
			setSearchString("");
			setPhysicianList(null);
			setAssociatedRoles(new ContactRole[] { ContactRole.NONE });
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

				setPhysicianList(ldapRepository.findAllByName(arr));

				setSelectedPhysician(null);
			}
		}
	}

	public class ExternalPhysician extends AbstractTab {
		public ExternalPhysician() {
			setTabName("ExternalPhysicianTab");
			setName("dialog.physicianSearch.extenal");
			setViewID("ecternalPhysicianSearch");
			setCenterInclude("include/externalPerson.xhtml");
		}
	}

	public void changeMode() {
		setAssociatedRoles(new ContactRole[] { ContactRole.NONE });

		if (searchView == SearchView.EXTERNAL) {
			setSelectedPhysician(new Physician(new Person(new Contact())));
			// person is not auto update able
			getSelectedPhysician().getPerson().setAutoUpdate(false);
			getSelectedPhysician().getPerson().setOrganizsations(new ArrayList<Organization>());

			setOrganizationTransformer(
					new DefaultTransformer<Organization>(getSelectedPhysician().getPerson().getOrganizsations()));
		} else
			setSelectedPhysician(null);
	}

	/**
	 * Sets the role Array and returns the physician
	 * 
	 * @return
	 */
	public Physician getPhysician() {
		if (getSelectedPhysician() == null)
			return null;

		getSelectedPhysician().setAssociatedRolesAsArray(getAssociatedRoles());
		return getSelectedPhysician();
	}

	/**
	 * returns the person of the physician
	 * 
	 * @return
	 */
	public Person getPerson() {
		return selectedPhysician != null ? selectedPhysician.getPerson() : null;
	}
}
