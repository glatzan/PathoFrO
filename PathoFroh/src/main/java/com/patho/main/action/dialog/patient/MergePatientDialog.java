package com.patho.main.action.dialog.patient;

import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.SelectEvent;
import org.primefaces.json.JSONException;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.settings.physician.PhysicianSearchDialog.PhysicianReturnEvent;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.config.excepion.ToManyEntriesException;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PatientRepository;
import com.patho.main.service.PatientService;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.event.PatientMergeEvent;
import com.patho.main.util.exception.CustomNullPatientExcepetion;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class MergePatientDialog extends AbstractDialog {

	public static final int MERGE_PIZ = 0;
	public static final int MERGE_PATIENT = 1;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ConfirmExternalPatientDataDialog confirmPatientData;

	/**
	 * Patient source for merging
	 */
	private Patient patient;

	/**
	 * Selected tasks for merging
	 */
	private List<Task> tasksTomerge;

	/**
	 * True if an external patient should be deleted
	 */
	private boolean deletePatient;

	/**
	 * True if not all tasks of the source are selected
	 */
	private boolean deletePatientDisabled;

	/**
	 * Merge Option,
	 */
	private MergeOption mergeOption;

	/**
	 * Piz for search a patient for merge target
	 */
	private String piz;

	/**
	 * Patient as merge target
	 */
	private Patient patientToMerge;

	/**
	 * This can be set to true if piz search was performed and no patient was found.
	 */
	private boolean renderErrorPatientNotFound;

	private Patient source;

	private Patient target;

	private DualListModel<Task> taskLists = new DualListModel<Task>();

	private boolean taskPickerDisabled;

	private boolean sourceDeleteable;

	private boolean targetDeleteable;

	private boolean deleteSource;

	private boolean deleteTarget;

	public MergePatientDialog initAndPrepareBean() {
		return initAndPrepareBean((Patient) null);
	}

	public MergePatientDialog initAndPrepareBean(Patient source) {
		return initAndPrepareBean(source, null);
	}

	public MergePatientDialog initAndPrepareBean(Patient source, Patient target) {
		if (initBean(source, target))
			prepareDialog();
		return this;
	}

	public boolean initBean(Patient source, Patient target) {
		setSource(source);
		setTarget(target);

		taskLists.setSource(source == null ? new ArrayList<Task>() : new ArrayList<Task>(source.getTasks()));
		taskLists.setTarget(target == null ? new ArrayList<Task>() : new ArrayList<Task>(target.getTasks()));

		return super.initBean(null, Dialog.PATIENT_MERGE);
	}

	public void onReturnSelectSource(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof PatientReturnEvent) {
			Patient patient = ((PatientReturnEvent) event.getObject()).getPatien();
			// Reloading patient with tasks
			setSource(patient.getId() != 0 ? patientRepository.findOptionalById(patient.getId(), true, false).get()
					: patient);
			taskLists.setSource(source == null ? new ArrayList<Task>() : new ArrayList<Task>(source.getTasks()));

			updateOnChange();
		}
	}

	public void onReturnSelectTarget(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof PatientReturnEvent) {
			Patient patient = ((PatientReturnEvent) event.getObject()).getPatien();
			// Reloading patient with tasks
			setTarget(patient.getId() != 0 ? patientRepository.findOptionalById(patient.getId(), true, false).get()
					: patient);
			taskLists.setTarget(target == null ? new ArrayList<Task>() : new ArrayList<Task>(target.getTasks()));

			updateOnChange();
		}
	}

	public void updateOnChange() {
		setTaskPickerDisabled(source == null || target == null);
		setSourceDeleteable(source != null && source.getId() != 0 && taskLists.getSource().size() == 0);
		setTargetDeleteable(target != null && target.getId() != 0 && taskLists.getTarget().size() == 0);
	}

	/**
	 * Method is called when the merge option (piz vs patient) is changed.
	 */
	public void onChangeMergeOption() {
		renderErrorPatientNotFound = false;
		patientToMerge = null;
		onChangeTasksToMergeSelection();

		if (mergeOption == MergeOption.PIZ)
			onSelectPatientViaPiz();
	}

	/**
	 * Searches for a patient via piz if the piz is 8 digits long.
	 */
	public void onSelectPatientViaPiz() {
		// only search if 8 dignis are present
		if (HistoUtil.isNotNullOrEmpty(piz) && piz.matches("^\\d{8}$")) {
			try {
				patientToMerge = patientService.findPatientByPiz(piz, false);
			} catch (HistoDatabaseInconsistentVersionException | JSONException | ToManyEntriesException
					| CustomNullPatientExcepetion e) {
			} finally {
				if (patientToMerge == null)
					renderErrorPatientNotFound = true;
				else
					renderErrorPatientNotFound = false;
			}
		}
	}

	/**
	 * If merging was confirmed this method will merge the two patients
	 * 
	 * @param event
	 */
	public void onMergePatient(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof Boolean
				&& ((Boolean) event.getObject()).booleanValue()) {
			if (patient != null && patientToMerge != null) {

				if (patientToMerge.getId() == 0)
					patientService.addPatient(patientToMerge, false);

				patientService.mergePatient(patient, patientToMerge, tasksTomerge);

				if (deletePatient && patient.getTasks().isEmpty())
					patientService.archivePatient(patient);

				mainHandlerAction.sendGrowlMessagesAsResource("growl.success", "growl.patient.merge.success");
				hideDialog(new PatientMergeEvent(patient, patientToMerge));
			}
		}
	}

	/**
	 * Is called on return of the patient select dialog, if a patient is return it
	 * will set as the patientToMerge
	 * 
	 * @param event
	 */
	public void onSelectPatient(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof Patient) {
			patientToMerge = (Patient) event.getObject();
		}
	}

	/**
	 * Is called if the task selection of the source task was altered. Deletion is
	 * only possible if all tasks are selected.
	 */
	public void onChangeTasksToMergeSelection() {
		deletePatientDisabled = patient.getTasks().size() != tasksTomerge.size();

		if (deletePatientDisabled)
			deletePatient = false;
	}

	/**
	 * Option for determine the merge target.
	 * 
	 * @author andi
	 *
	 */
	public enum MergeOption {
		PIZ, PATIENT;
	}
}