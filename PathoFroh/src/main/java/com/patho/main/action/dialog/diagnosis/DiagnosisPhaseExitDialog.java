package com.patho.main.action.dialog.diagnosis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.DiagnosisService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.helper.HistoUtil;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Setter
@Getter
public class DiagnosisPhaseExitDialog extends AbstractDialog {

	private DiagnosisPhaseExitData data;

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

		data = new DiagnosisPhaseExitData();

		data.setTask(task);

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
			data.setAllRevisions(true);
		}

		data.setSelectedRevision(selectedRevision);

		updateDate();

		return super.initBean(task, Dialog.DIAGNOSIS_PHASE_EXIT);
	}

	public void updateDate() {
		logger.debug("Updating data");

		int countDiagnosesToApprove = DiagnosisService.countRevisionToApprove(data.getTask());

		if (data.isAllRevisions()) {
			data.setNotification(countDiagnosesToApprove != 0);
			data.setRemoveFromDiangosisList(true);
			data.setRemoveFromWorklist(true);
			setRemoveFromDiagnosisListDisabled(false);
			setReApproveDiagnosis(false);
		} else {

			// last diagnoses
			if (data.selectedRevision.getCompletionDate() == 0 && countDiagnosesToApprove == 1) {
				// only notify if not a council diagnoses
				data.setNotification(data.selectedRevision.getType() != DiagnosisRevisionType.DIAGNOSIS_COUNCIL);
				data.setRemoveFromDiangosisList(true);
				data.setRemoveFromWorklist(true);
				setReApproveDiagnosis(false);
				setRemoveFromDiagnosisListDisabled(false);
				// there are other diagnoses to approve
			} else if (countDiagnosesToApprove > 1) {
				// only notify if not a council diagnoses
				data.setNotification(data.selectedRevision.getType() != DiagnosisRevisionType.DIAGNOSIS_COUNCIL);
				data.setRemoveFromDiangosisList(false);
				data.setRemoveFromWorklist(false);
				// reapprove if not approved jet
				setReApproveDiagnosis(data.selectedRevision.getCompletionDate() != 0);
				setRemoveFromDiagnosisListDisabled(true);
				// only in list with all diangoses approved
			} else if (countDiagnosesToApprove == 0) {
				data.setNotification(true);
				data.setRemoveFromDiangosisList(
						data.getTask().isListedInFavouriteList(PredefinedFavouriteList.DiagnosisList));
				data.setRemoveFromWorklist(true);
				data.setAllRevisions(false);
				setRemoveFromDiagnosisListDisabled(false);
				setReApproveDiagnosis(true);
			}

		}
	}

	public void exitPhaseAndClose() {
		hideDialog(data);
	}

	@Getter
	@Setter
	public static class DiagnosisPhaseExitData implements DialogReturnEvent {

		/**
		 * Current Task
		 */
		private Task task;

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
		private boolean removeFromDiangosisList;

		/**
		 * If true the task will be removed from worklist
		 */
		private boolean removeFromWorklist;

		/**
		 * true = Notifcation <br>
		 * false = No Notification
		 */
		private boolean notification;

	}
}
