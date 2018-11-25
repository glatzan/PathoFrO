package com.patho.main.action.handler;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.util.event.PatientMergeEvent;

@Controller
@Scope("session")
public class PatientViewHandlerAction {

	@Autowired
	private WorklistViewHandlerAction worklistViewHandlerAction;

	public void showTaskMediaDialog() {
//		// init dialog for patient and task
//		dialogHandlerAction.getMediaDialog().initBean(globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient(),
//				new DataList[] { globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask() }, false);
//
//		// enabeling upload to task
//		dialogHandlerAction.getMediaDialog().enableUpload(new DataList[] { globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask() },
//				new DocumentType[] { DocumentType.U_REPORT, DocumentType.COUNCIL_REPLY,
//						DocumentType.BIOBANK_INFORMED_CONSENT, DocumentType.OTHER });
//
//		// setting info text
//		dialogHandlerAction.getMediaDialog().setActionDescription(resourceBundle.get("dialog.pdfOrganizer.headline.info.task",
//				globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask().getTaskID()));
//
//		// show dialog
//		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

	/**
	 * Is called on return of the patient data edit dialog, if a merge event had
	 * happened the worklist is updated.
	 * 
	 * @param event
	 */
	public void onEditPatientDataReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof PatientMergeEvent) {
			PatientMergeEvent p = (PatientMergeEvent) event.getObject();
			
			// if merge source was archived, remove it from worklist
			if(p.getMergeFrom().isArchived())
				worklistViewHandlerAction.removePatientFromCurrentWorklist(p.getMergeFrom());
			else
				worklistViewHandlerAction.replacePatientInCurrentWorklist(p.getMergeFrom());
			
			worklistViewHandlerAction.replacePatientInCurrentWorklist(p.getMergeTo());
		}
	}
}
