package com.patho.main.action.dialog.settings.diagnosis;

import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog.SlideSelectResult;
import com.patho.main.action.dialog.slides.AddSlidesDialog.StainingPrototypeHolder;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.service.DiagnosisPresetService;
import com.patho.main.service.MaterialPresetService;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DiagnosisPresetEditDialog extends AbstractDialog<DiagnosisPresetEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisPresetService diagnosisPresetService;

	private boolean newDiagnosis;

	private DiagnosisPreset diagnosisPreset;

	private ContactRole[] allRoles;

	public DiagnosisPresetEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new DiagnosisPreset());
	}

	public DiagnosisPresetEditDialog initAndPrepareBean(DiagnosisPreset diagnosisPresets) {
		if (initBean(diagnosisPresets))
			prepareDialog();
		return this;
	}

	public boolean initBean(DiagnosisPreset diagnosisPresets) {
		setNewDiagnosis(diagnosisPresets.getId() == 0);
		setDiagnosisPreset(diagnosisPresets);

		setAllRoles(new ContactRole[] { ContactRole.FAMILY_PHYSICIAN, ContactRole.PATIENT, ContactRole.SURGEON,
				ContactRole.PRIVATE_PHYSICIAN, ContactRole.RELATIVES });

		return super.initBean(task, Dialog.SETTINGS_DIAGNOSIS_EDIT);
	}

	public void saveAndHide() {
		diagnosisPresetService.addOrUpdate(getDiagnosisPreset());
		hideDialog(new ReloadEvent());
	}

	/**
	 * On dialog return, reload data
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
		} else if (event.getObject() != null && event.getObject() instanceof SlideSelectResult) {
		}
	}
}
