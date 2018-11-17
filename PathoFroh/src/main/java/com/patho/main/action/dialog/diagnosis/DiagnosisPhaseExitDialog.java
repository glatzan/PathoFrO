package com.patho.main.action.dialog.diagnosis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
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
		if (initBean(task, (DiagnosisRevision) null))
			prepareDialog();
		return this;
	}

	public DiagnosisPhaseExitDialog initAndPrepareBean(Task task, DiagnosisRevision selectedRevision) {
		if (initBean(task, selectedRevision))
			prepareDialog();
		return this;
	}

	public boolean initBean(Task task, DiagnosisRevision selectedRevision) {

		data = new DiagnosisPhaseExitData();

		setDiagnosisRevisions(task.getDiagnosisRevisions());
		setDiagnosisRevisionTransformer(new DefaultTransformer<DiagnosisRevision>(getDiagnosisRevisions()));

		
		data.setSelectedRevision(selectedRevision);

		// if last diangosis in task
		boolean lastDiagnosis = HistoUtil.getLastElement(task.getDiagnosisRevisions()) == selectedRevision;

		data.setRemoveFromWorklist(lastDiagnosis);

		data.setEndDiangosisPhase(lastDiagnosis);
		data.setRemoveFromDiangosisList(lastDiagnosis);

		data.setGoToNotificationPhase(true);

		data.setTask(task);
		
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
		 * Diagnosis revision to notify about
		 */
		private DiagnosisRevision selectedRevision;
		
		/**
		 * If true the task will be removed from worklist
		 */
		private boolean removeFromWorklist;

		/**
		 * If true the task will be removed from diagnosis list
		 */
		private boolean removeFromDiangosisList;

		/**
		 * If true the diagnosis phase of the task will be finished
		 */
		private boolean endDiangosisPhase;

		/**
		 * If true the task will be shifted to the notification phase
		 */
		private boolean goToNotificationPhase;
	}
}
