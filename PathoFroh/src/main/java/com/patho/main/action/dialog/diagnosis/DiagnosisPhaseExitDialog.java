package com.patho.main.action.dialog.diagnosis;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.DiagnosisService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.event.dialog.DiagnosisPhaseExitEvent;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.task.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Set;

@Configurable
@Setter
@Getter
public class DiagnosisPhaseExitDialog extends AbstractDialog {

    /**
     * List of diagnosis revisions of the task
     */
    private Set<DiagnosisRevision> diagnosisRevisions;

    /**
     * Transformer for diagnosis revisions
     */
    private DefaultTransformer<DiagnosisRevision> diagnosisRevisionTransformer;

    /**
     * If true it is a reapprovel of a diangoses
     */
    private boolean reApproveDiagnosis;

    /**
     * If true the task cant be removed from the diagnosisList
     */
    private boolean removeFromDiagnosisListDisabled;

    /**
     * True if all revisions should be completed
     */
    private boolean allRevisions;

    /**
     * Diagnosis revision to notify about
     */
    private DiagnosisRevision selectedRevision;

    /**
     * If true the task will be removed from diagnosis list
     */
    private boolean removeFromDiagnosisList;

    /**
     * If true the task will be removed from worklist
     */
    private boolean removeFromWorklist;

    /**
     * true = Notifcation <br>
     * false = No Notification
     */
    private boolean performNotification;

    public DiagnosisPhaseExitDialog initAndPrepareBean(Task task) {
        return initAndPrepareBean(task, null, true);
    }

    public DiagnosisPhaseExitDialog initAndPrepareBean(Task task, DiagnosisRevision selectedRevision) {
        return initAndPrepareBean(task, selectedRevision, false);
    }

    public DiagnosisPhaseExitDialog initAndPrepareBean(Task task, DiagnosisRevision selectedRevision,
                                                       boolean autoselect) {
        if (initBean(task, selectedRevision, autoselect))
            prepareDialog();
        return this;
    }

    public boolean initBean(Task task, DiagnosisRevision selectedRevision, boolean autoselect) {

        // shoud not happen
        if (task.getDiagnosisRevisions().size() == 0)
            return false;

        setDiagnosisRevisions(task.getDiagnosisRevisions());
        setDiagnosisRevisionTransformer(new DefaultTransformer<DiagnosisRevision>(getDiagnosisRevisions()));

        // searching for first diagnosis of that the notification was not performed jet
        if (autoselect) {
            selectedRevision = DiagnosisService.getNextRevisionToApprove(task);
        }

        // either no revision was passed or autoselect ding't find a diagnosis to
        // approve
        if (selectedRevision == null) {
            // sets the last element as select, but also sets all revisions to true
            selectedRevision = HistoUtil.getLastElement(task.getDiagnosisRevisions());
            setAllRevisions(true);
        }

        setSelectedRevision(selectedRevision);

        updateDate();

        return super.initBean(task, Dialog.DIAGNOSIS_PHASE_EXIT);
    }

    public void updateDate() {
        logger.debug("Updating data");

        int countDiagnosesToApprove = DiagnosisService.countRevisionToApprove(getTask());

        if (isAllRevisions()) {
            setPerformNotification(countDiagnosesToApprove != 0);
            setRemoveFromDiagnosisList(true);
            setRemoveFromWorklist(true);
            setRemoveFromDiagnosisListDisabled(false);
            setReApproveDiagnosis(false);
        } else {

            // last diagnoses
            if (!selectedRevision.getCompleted() && countDiagnosesToApprove == 1) {
                // only notify if not a council diagnoses
                setPerformNotification(selectedRevision.getType() != DiagnosisRevisionType.DIAGNOSIS_COUNCIL);
                setRemoveFromDiagnosisList(true);
                setRemoveFromWorklist(true);
                setReApproveDiagnosis(false);
                setRemoveFromDiagnosisListDisabled(false);
                // there are other diagnoses to approve
            } else if (countDiagnosesToApprove > 1) {
                // only notify if not a council diagnoses
                setPerformNotification(selectedRevision.getType() != DiagnosisRevisionType.DIAGNOSIS_COUNCIL);
                setRemoveFromDiagnosisList(false);
                setRemoveFromWorklist(false);
                // reapprove if not approved jet
                setReApproveDiagnosis(selectedRevision.getCompleted());
                setRemoveFromDiagnosisListDisabled(true);
                // only in list with all diangoses approved
            } else if (countDiagnosesToApprove == 0) {
                setPerformNotification(true);
                setRemoveFromDiagnosisList(TaskStatus.hasFavouriteLists(getTask(), PredefinedFavouriteList.DiagnosisList));
                setRemoveFromWorklist(true);
                setAllRevisions(false);
                setRemoveFromDiagnosisListDisabled(false);
                setReApproveDiagnosis(true);
            }

        }
    }

    public void exitPhaseAndClose() {
        if (allRevisions)
            hideDialog(new DiagnosisPhaseExitEvent(task, null, removeFromDiagnosisList, removeFromWorklist, performNotification));
        else if (selectedRevision != null)
            hideDialog(new DiagnosisPhaseExitEvent(task, selectedRevision, removeFromDiagnosisList, removeFromWorklist, performNotification));
        else {
            //TODO Error
            hideDialog();
        }

    }
}
