package com.patho.main.action.dialog.patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.primefaces.event.SelectEvent;
import org.primefaces.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.patient.ConfirmExternalPatientDataDialog.ConfirmExternalPatientReturnEvent;
import com.patho.main.common.Dialog;
import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.service.PatientService;
import com.patho.main.ui.ListChooser;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.exception.CustomNullPatientExcepetion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class SearchPatientDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	private ClinicSearchTab clinicSearchTab;

	private ExternalPatientTab externalPatientTab;

	private boolean showExternPatientTab;

	private boolean persistPatient;

	public SearchPatientDialog() {
		setClinicSearchTab(new ClinicSearchTab());
		setExternalPatientTab(new ExternalPatientTab());
	}

	public SearchPatientDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();
		return this;
	}

	public boolean initBean() {
		tabs = new AbstractTab[] { clinicSearchTab };
		this.showExternPatientTab = false;
		this.persistPatient = false;
		return super.initBean(null, Dialog.PATIENT_ADD);
	}

	public SearchPatientDialog externalMode() {
		return externalMode(false);
	}

	public SearchPatientDialog externalMode(boolean forceExternalMode) {
		// only enable if forced or user has the permission to add external patients
		if (forceExternalMode || userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN)) {
			appendTab(externalPatientTab);
			externalPatientTab.initTab();
		}
		return this;
	}

	public SearchPatientDialog persistPatient() {
		this.persistPatient = true;
		return this;
	}

	public SearchPatientDialog inititalValues(String name, String surname, String piz, Date date) {
		clinicSearchTab.setPatientName(name);
		clinicSearchTab.setPatientSurname(surname);
		clinicSearchTab.setPatientPiz(piz);
		clinicSearchTab.setPatientBirthday(date);
		clinicSearchTab.searchForClinicPatienes();

		return this;
	}

	@Getter
	@Setter
	public class ClinicSearchTab extends AbstractTab {

		/**
		 * Patient to search for, piz
		 */
		private String patientPiz;

		/**
		 * Patient to search for, name
		 */
		private String patientName;

		/**
		 * Patient to search for, surname
		 */
		private String patientSurname;

		/**
		 * Patient to search for, birthday
		 */
		private Date patientBirthday;

		/**
		 * True if to many matches have been found in the clinic database, an so the
		 * clinic database did not return any data
		 */
		private boolean toManyMatchesInClinicDatabase;

		/**
		 * List of all found Patients of the patientSearchRequest, PatientList is used
		 * instead of Patient because primefaces needs a unique row collum.
		 */
		private List<ListChooser<Patient>> patientList;

		/**
		 * Selectes PatientList item
		 */
		private ListChooser<Patient> selectedPatientListItem;

		/**
		 * If the user has not the permission to search the pdv only the local database
		 * will be searched for.
		 */
		private boolean searchLocalDatabaseOnly;

		public ClinicSearchTab() {
			setTabName("ClinicSearchTab");
			setName("dialog.searchPatient.search");
			setViewID("clinicSearch");
			setCenterInclude("include/clinicSearch.xhtml");
		}

		public boolean initTab() {
			return initTab("", "", "", null);
		}

		public boolean initTab(String name, String surename, String piz, Date date) {
			setPatientBirthday(date);
			setPatientName(name);
			setPatientPiz(surename);
			setPatientSurname(piz);
			setSelectedPatientListItem(null);
			setPatientList(null);
			setToManyMatchesInClinicDatabase(false);
			setSearchLocalDatabaseOnly(
					!userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC));
			return true;
		}

		public void updateData() {
		}

		/**
		 * Search for pizes or given namen, firstname and birthday. Prefers pizes if not
		 * null. Considers search only in local database if the user has not the
		 * matching rights to add new clinic patients to the local database
		 */
		public void searchForClinicPatienes() {
			logger.debug("Searching for patients");
			try {
				setToManyMatchesInClinicDatabase(false);
				List<Patient> resultArr = new ArrayList<Patient>();

				if (getPatientPiz() != null && !getPatientPiz().isEmpty()) {
					if (getPatientPiz().matches("^[0-9]{8}$")) { // if full piz
						Optional<Patient> oPatient = patientService.findPatientByPizInDatabaseAndPDV(getPatientPiz(),
								isSearchLocalDatabaseOnly());
						if (oPatient.isPresent())
							resultArr.add(oPatient.get());

					} else if (getPatientPiz().replaceAll("_", "").matches("^[0-9]{6,8}$")) {
						// 6to 7 digits of piz
						// isSearchLocalDatabaseOnly() can be ignored because this function is only
						// supported by local database
						resultArr.addAll(patientService
								.findAllPatientsByPizInDatabaseAndPDV(getPatientPiz().replaceAll("_", "")));
					}

				} else if ((getPatientName() != null && !getPatientName().isEmpty())
						|| (getPatientSurname() != null && !getPatientSurname().isEmpty())
						|| getPatientBirthday() != null) {

					AtomicBoolean toManyEntries = new AtomicBoolean(false);

					resultArr.addAll(patientService.findAllPatientsByNameSurnameBirthdayInDatabaseAndPDV(
							getPatientName(), getPatientSurname(), getPatientBirthday(), isSearchLocalDatabaseOnly(),
							toManyEntries));

					setToManyMatchesInClinicDatabase(toManyEntries.get());
				}

				setPatientList(ListChooser.getListAsIDList(resultArr));
				setSelectedPatientListItem(null);

			} catch (JSONException | ToManyEntriesException | CustomNullPatientExcepetion e) {
				setToManyMatchesInClinicDatabase(true);
			}
		}

		/**
		 * Returns an dialog close event containing the selected patient an closes the
		 * dialog
		 */
		public void hideDialogAndSelectItem() {
			SearchPatientDialog.this.hideDialog(new PatientReturnEvent(
					persistPatient ? patientService.addPatient(getSelectedPatientListItem().getListItem(), false)
							: getSelectedPatientListItem().getListItem()));
		}

		/**
		 * Searches for the given strings and adds the patient to the worklist if one
		 * patient was found. (this method reacts to return clicks)
		 * 
		 * @param addToWorklist
		 */
		public void onQuickSubmit() {
			logger.debug("Quicksubmit, search for result and adding result to worklist if unique result");
			searchForClinicPatienes();

			// only adding if exactly one result was found
			if (getPatientList() != null && getPatientList().size() == 1) {
				logger.debug("One result found, adding to database");
				setSelectedPatientListItem(getPatientList().get(0));
				hideDialogAndSelectItem();
			} else {
				logger.debug("No result found, or result not unique, not firing quick submit");
			}
		}
	}

	@Getter
	@Setter
	public class ExternalPatientTab extends AbstractTab {

		/**
		 * Patient for creating external Patient
		 */
		private Patient patient;

		public ExternalPatientTab() {
			setTabName("ExternalPatientTab");
			setName("dialog.searchPatient.add");
			setViewID("externalPatient");
			setCenterInclude("include/externalPatient.xhtml");
		}

		public boolean initTab() {
			setDisabled(!userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN));
			setPatient(new Patient(new Person(new Contact())));
			getPatient().getPerson().setGender(null);
			return true;
		}

		public void updateData() {
		}

		/**
		 * Closes the dialog in order to add the patient
		 * 
		 * @param event
		 */
		public void onConfirmExternalPatientDialog(SelectEvent event) {
			if (event.getObject() != null && event.getObject() instanceof ConfirmExternalPatientReturnEvent) {
				SearchPatientDialog.this.hideDialog(new PatientReturnEvent(persistPatient
						? patientService
								.addPatient(((ConfirmExternalPatientReturnEvent) event.getObject()).getPatient(), false)
						: ((ConfirmExternalPatientReturnEvent) event.getObject()).getPatient()));
			}
		}
	}
}
