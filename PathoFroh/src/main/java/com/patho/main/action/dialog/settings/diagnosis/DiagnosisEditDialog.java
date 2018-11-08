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
import com.patho.main.service.MaterialPresetService;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DiagnosisEditDialog extends AbstractDialog<DiagnosisEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetService materialPresetService;

	private boolean newDiagnosis;

	private DiagnosisPreset diagnosisPresets;

	private ContactRole[] allRoles;

	public DiagnosisEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new DiagnosisPreset());
	}

	public DiagnosisEditDialog initAndPrepareBean(DiagnosisPreset diagnosisPresets) {
		if (initBean(diagnosisPresets))
			prepareDialog();
		return this;
	}

	public boolean initBean(DiagnosisPreset diagnosisPresets) {
		setNewDiagnosis(diagnosisPresets.getId() == 0);
		setDiagnosisPresets(diagnosisPresets);

		setAllRoles(new ContactRole[] { ContactRole.FAMILY_PHYSICIAN, ContactRole.PATIENT, ContactRole.SURGEON,
				ContactRole.PRIVATE_PHYSICIAN, ContactRole.RELATIVES });
		
		return super.initBean(task, Dialog.SETTINGS_MATERIAL_EDIT);
	}

	public void saveAndHide() {
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
	
	public void saveDiagnosisPreset() {
		if (getSelectedDiagnosisPreset().getId() == 0) {

			// case new, save
			logger.debug("Creating new diagnosis " + getSelectedDiagnosisPreset().getCategory());

			getDiagnosisPresets().add(getSelectedDiagnosisPreset());

			diagnosisPresetRepository.save(getSelectedDiagnosisPreset(),
					resourceBundle.get("log.settings.diagnosis.new", getSelectedDiagnosisPreset().getCategory()));

			ListOrder.reOrderList(getDiagnosisPresets());

			diagnosisPresetRepository.saveAll(getDiagnosisPresets(),
					resourceBundle.get("log.settings.diagnosis.list.reoder"));

		} else {

			// case edit: update an save
			logger.debug("Updating diagnosis " + getSelectedDiagnosisPreset().getCategory());

			diagnosisPresetRepository.save(getSelectedDiagnosisPreset(), resourceBundle
					.get("log.settings.diagnosis.update", getSelectedDiagnosisPreset().getCategory()));
		}

		discardDiagnosisPreset();

	}
}
