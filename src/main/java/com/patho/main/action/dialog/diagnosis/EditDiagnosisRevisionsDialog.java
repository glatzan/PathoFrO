package com.patho.main.action.dialog.diagnosis;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EditDiagnosisRevisionsDialog extends AbstractDialog {

    private DiagnosisRevision revision;

    public EditDiagnosisRevisionsDialog initAndPrepareBean(Task task, DiagnosisRevision revision) {
        if (initBean(task, revision))
            prepareDialog();

        return this;
    }

    /**
     * Initializes all field of the object
     *
     * @param task
     */
    public boolean initBean(Task task, DiagnosisRevision revision) {
        this.revision = revision;
        return super.initBean(task, Dialog.DIAGNOSIS_REVISION_EDIT);
    }

    public void saveAndHide() {
        SpringContextBridge.services().getTaskRepository().save(task, resourceBundle.get("log.patient.task.diagnosisRevisions.update", revision));
        hideDialog(new TaskReloadEvent());
    }
}
