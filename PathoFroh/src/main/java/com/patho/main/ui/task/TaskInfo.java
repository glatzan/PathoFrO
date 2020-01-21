package com.patho.main.ui.task;

import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.service.impl.SpringContextBridge;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

@Getter
@Setter
public class TaskInfo {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Task task;

    private HashSet<Long> admentRevisions = new HashSet<Long>();

    public TaskInfo(Task task) {
        logger.debug("New Task Info");
        this.task = task;
    }

    public boolean isTaksEditable() {
        // task is editable
        // users and guest can'special.pdfOrganizerDialog edit anything
        if (!SpringContextBridge.services().getUserService().userHasPermission(HistoPermissions.TASK_EDIT)) {
            return false;
        }

        // finalized
        if (task.getFinalized()) {
            return false;
        }

        // Blocked
        // TODO: Blocking

        return true;
    }

    public boolean isDiagnosisRevisionEditable(DiagnosisRevision diagnosisRevision) {
        if (!isTaksEditable())
            return false;

        if (diagnosisRevision.getCompleted() && !isDiagnosisRevisionInEditAmendmentMode(diagnosisRevision))
            return false;

        return true;
    }

    public boolean isDiagnosisRevisionInEditAmendmentMode(DiagnosisRevision diagnosisRevision) {
        return admentRevisions.contains(diagnosisRevision.getId());
    }

    public void admendRevision(DiagnosisRevision diagnosisRevision) {
        admentRevisions.add(diagnosisRevision.getId());
    }

    public void lockRevision(DiagnosisRevision diagnosisRevision) {
        admentRevisions.remove(diagnosisRevision.getId());
    }
}
