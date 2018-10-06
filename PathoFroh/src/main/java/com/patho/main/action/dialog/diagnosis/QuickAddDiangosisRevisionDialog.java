package com.patho.main.action.dialog.diagnosis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.diagnosis.CreateDiagnosisRevisionDialog.DiagnosisRevisionContainer;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.DiagnosisService;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Dialog for adding a diagnosis revision on creating a restaining
 * 
 * @author andi
 *
 */
@Configurable
@Setter
@Getter
public class QuickAddDiangosisRevisionDialog extends AbstractDialog<QuickAddDiangosisRevisionDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	private DiagnosisRevisionType type;

	private boolean renameOldDiagnoses;

	private List<DiagnosisRevisionContainer> revisionList;

	public QuickAddDiangosisRevisionDialog initAndPrepareBean(Task task, DiagnosisRevisionType type) {
		if (initBean(task, type))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task, DiagnosisRevisionType type) {
		task = taskRepository.findOptionalByIdAndInitialize(task.getId(), false, true, false, false, false).get();

		super.initBean(task, Dialog.DIAGNOSIS_REVISION_ADD);
		setType(type);
		setRenameOldDiagnoses(true);

		// generating new names, only applied if renameOldDiagnoses is true
		setRevisionList(DiagnosisRevisionContainer.factory(task,
				new ArrayList<DiagnosisRevision>(Arrays.asList(new DiagnosisRevision("", getType()))), true));

		return true;
	}

	public void createDiagnosisAndHide() {

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				if (renameOldDiagnoses)
					task = diagnosisService.renameDiagnosisRevisions(task, getRevisionList());

				task = diagnosisService.createDiagnosisRevision(getTask(), type);
			}
		});

		MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.create.rediagnosis");
		hideDialog(new ReloadTaskEvent());
	}
}
