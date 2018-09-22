package com.patho.main.action.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.action.dialog.diagnosis.CopyHistologicalRecordDialog;
import com.patho.main.model.ListItem;
import com.patho.main.model.Signature;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.SampleService;
import com.patho.main.service.TaskService;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.TimeUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Scope("session")
@Getter
@Setter
@Slf4j
public class DiagnosisViewHandlerAction {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CopyHistologicalRecordDialog copyHistologicalRecordDialog;

	public void prepareForTask(Task task) {
		log.debug("Initilize DiagnosisViewHandlerAction for task");

		for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
			if (revision.getCompletionDate() == 0) {
				revision.setSignatureDate(TimeUtil.setDayBeginning(System.currentTimeMillis()));

				if(revision.getSignatureOne() == null)
					revision.setSignatureOne(new Signature());
				
				if(revision.getSignatureTwo() == null)
					revision.setSignatureTwo(new Signature());
				
				if (revision.getSignatureOne().getPhysician() == null
						|| revision.getSignatureTwo().getPhysician() == null) {
					// TODO set if physician to the left, if consultant to the right
				}
			}

		}
	}



	

}
