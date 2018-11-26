package com.patho.main.action.handler;

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;


import com.patho.main.action.UserHandlerAction;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.ui.LazyPDFGuiManager;
import com.patho.main.ui.transformer.DefaultTransformer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Scope("session")
@Getter
@Setter
@Slf4j
public class ReportViewHandlerAction {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;
	
	private Task task;

	/**
	 * Manager for rendering the pdf lazy style
	 */
	private LazyPDFGuiManager guiManager = new LazyPDFGuiManager();

	/**
	 * List of all diagnoses
	 */
	private Set<DiagnosisRevision> diagnoses;

	/**
	 * Transformer for diagnoses
	 */
	private DefaultTransformer<DiagnosisRevision> diagnosesTransformer;

	/**
	 * Selected diangosis
	 */
	private DiagnosisRevision selectedDiagnosis;

	public void prepareForTask(Task task) {
		log.debug("Initilize ReportViewHandlerAction for task");
		
		this.task = task;
		
		if (task.getDiagnosisCompletionDate() == 0 && !userHandlerAction.currentUserHasPermission(HistoPermissions.USER_ALWAYS_SHOW_IN_WORKLIST_REPORT)) {
			guiManager.reset();
			guiManager.setRenderComponent(false);
		} else {

			setDiagnoses(task.getDiagnosisRevisions());
			setDiagnosesTransformer(new DefaultTransformer<>(getDiagnoses()));

			// getting last diagnosis
			setSelectedDiagnosis(getDiagnoses().get(getDiagnoses().size() - 1));

			onChangeDiagnosis();
			
			// PDFContainer c = PDFUtil.getLastPDFofType(task,
			// DocumentType.DIAGNOSIS_REPORT_COMPLETED);
			//
			// if (c == null) {
			// // no document found, rendering diagnosis report in background thread
			//
			// } else {
			// guiManager.setManuallyCreatedPDF(c);
			// }
		}

	}

	public void onChangeDiagnosis() {

		guiManager.reset();
		
		guiManager.setRenderComponent(true);

		DiagnosisReport report = DocumentTemplate
				.getTemplateByID(globalSettings.getDefaultDocuments().getDiagnosisReportForUsers());

		if (report != null) {

			report.initData(task, Arrays.asList(selectedDiagnosis), "");

			guiManager.startRendering(report);
		}
	}
}
