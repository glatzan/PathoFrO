package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ConfirmEvent;
import com.patho.main.util.dialog.event.PatientDeleteEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

@Getter
@Setter
public class DeletePatientDialog extends AbstractDialog {

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
        SpringContextBridge.services().getPatientService().deleteOrArchive(patient);
        hideDialog(new PatientDeleteEvent(patient));
    }

    public void onConfirmDialogReturn(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof ConfirmEvent) {
            deleteAndHide();
        }
    }
}
