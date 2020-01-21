package com.patho.main.action.dialog.diagnosis;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteDiagnosisRevisionDialog extends AbstractDialog {

    private DiagnosisRevision diagnosisRevision;

    public DeleteDiagnosisRevisionDialog initAndPrepareBean(DiagnosisRevision diagnosisRevision) {
        if (initBean(diagnosisRevision))
            prepareDialog();
        return this;
    }

    public boolean initBean(DiagnosisRevision diagnosisRevision) {
        super.initBean(diagnosisRevision.getTask(), Dialog.DIAGNOSIS_REVISION_DELETE);
        this.diagnosisRevision = diagnosisRevision;
        return true;
    }

    public void deleteAndHide() {
        try {
            SpringContextBridge.services().getDiagnosisService().removeDiagnosisRevision(task, diagnosisRevision);
            MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.delete", "growl.diagnosis.delete.text");
        } catch (CustomUserNotificationExcepetion e) {
            MessageHandler.sendGrowlMessagesAsResource(e);
        }
        hideDialog(new TaskReloadEvent());
    }
}
