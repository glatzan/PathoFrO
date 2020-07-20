package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.log.Log;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.impl.SpringContextBridge;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatientLogDialog extends AbstractDialog {

    private Patient patient;

    private List<Log> patientLog;

    public PatientLogDialog initAndPrepareBean(Patient patient) {
        if (initBean(patient))
            prepareDialog();
        return this;
    }

    /**
     * Initializes the bean
     *
     * @param patient
     */
    public boolean initBean(Patient patient) {
        setPatient(patient);
        setPatientLog(SpringContextBridge.services().getLogRepository().findAllByLogInfoPatientOrderByIdAsc(patient));
        return super.initBean(null, Dialog.PATIENT_LOG);
    }
}
