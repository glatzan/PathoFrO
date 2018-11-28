package com.patho.main.action.dialog.patient;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.miscellaneous.ConfirmDialog.ConfirmEvent;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.PatientService;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DeletePatientDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientService patientService;

	private Patient patient;

	public DeletePatientDialog initAndPrepareBean(Patient patient) {
		if (initBean(patient))
			prepareDialog();
		return this;
	}

	public boolean initBean(Patient patient) {
		setPatient(patient);
		return super.initBean(Dialog.PATIENT_REMOVE);
	}

	public void deleteAndHide() {
		patientService.deleteOrArchive(patient);
		hideDialog(new PatientDeleteEvent(patient));
	}

	public void onConfirmDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ConfirmEvent) {
			deleteAndHide();
		}
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class PatientDeleteEvent implements DialogReturnEvent {
		private Patient patient;
	}
}
