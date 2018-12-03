package com.patho.main.action.dialog.diagnosis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
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
		
		setDiagnosisRevisions(task.getDiagnosisRevisions());
		setDiagnosisRevisionTransformer(new DefaultTransformer<DiagnosisRevision>(getDiagnosisRevisions()));

		// searching for first diagnosis of that the notification was not performed jet
		if (autoselect) {
			for (DiagnosisRevision diagnosisRevision : diagnosisRevisions) {
				if (diagnosisRevision.getCompletionDate() == 0) {
					selectedRevision = diagnosisRevision;
					break;
				}
			}
		}

		// if diagnosis was found set variables for approving diagnosis
		if (selectedRevision != null) {
			data.setSelectedRevision(selectedRevision);
			data.setAllRevisions(false);

			// no notification after council diangoses
			if (selectedRevision.getType() == DiagnosisRevisionType.DIAGNOSIS_COUNCIL)
				data.setNotification(false);
			else
				data.setNotification(true);

			// if last diangosis in task
			boolean lastDiagnosis = HistoUtil.getLastElement(task.getDiagnosisRevisions()) == selectedRevision;
			data.setRemoveFromWorklist(lastDiagnosis);
			data.setEndDiangosisPhase(lastDiagnosis);
			data.setRemoveFromDiangosisList(lastDiagnosis);
			
		} else {
			data.setSelectedRevision(null);
			
			// if autosect didn't find a diagnosis this task is still in the diagnosis list
			// with no active diagnosis
			if (autoselect) {
				data.setAllRevisions(true);
				data.setEndDiangosisPhase(true);
				data.setRemoveFromDiangosisList(true);
				data.setNotification(false);
				data.setRemoveFromWorklist(true);
			} else {
				// selected revision was null, so aprove all diagnoses
				data.setAllRevisions(true);
				data.setNotification(true);
				data.setEndDiangosisPhase(true);
				data.setRemoveFromDiangosisList(true);
				data.setRemoveFromWorklist(true);
			}
		}

		return super.initBean(task, Dialog.DIAGNOSIS_PHASE_EXIT);
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
		 * If true the diagnosis phase of the task will be finished
		 */
		private boolean endDiangosisPhase;

		/**
		 * true = Notifcation <br>
		 * false = No Notification
		 */
		private boolean notification;

	}
}
