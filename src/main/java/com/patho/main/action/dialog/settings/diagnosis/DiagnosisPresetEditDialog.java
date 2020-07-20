package com.patho.main.action.dialog.settings.diagnosis;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.preset.DiagnosisPreset;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import com.patho.main.util.dialog.event.SlideSelectEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

@Getter
@Setter
public class DiagnosisPresetEditDialog extends AbstractDialog {

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

        setAllRoles(new ContactRole[]{ContactRole.FAMILY_PHYSICIAN, ContactRole.PATIENT, ContactRole.SURGEON,
                ContactRole.PRIVATE_PHYSICIAN, ContactRole.RELATIVES});

        return super.initBean(task, Dialog.SETTINGS_DIAGNOSIS_EDIT);
    }

    public void saveAndHide() {
        SpringContextBridge.services().getDiagnosisPresetService().addOrUpdate(getDiagnosisPreset());
        hideDialog(new ReloadEvent());
    }

    /**
     * On dialog return, reload data
     *
     * @param event
     */
    public void onDefaultDialogReturn(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
        } else if (event.getObject() != null && event.getObject() instanceof SlideSelectEvent) {
        }
    }
}
