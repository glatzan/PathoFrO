package com.patho.main.template.print;

import com.patho.main.template.PrintDocument;
import com.patho.main.template.print.ui.DiagnosisReportUi;

/**
 * patient, task, diagnosisRevisions, address, subject, date, latexTextConverter
 * 
 * new TextToLatexConverter()).convertToTex(toSendAddress)
 * 
 * @author andi
 *
 */
public class DiagnosisReport extends PrintDocument {

	public DiagnosisReport(PrintDocument printDocument) {
		super(printDocument);
	}

	public DiagnosisReportUi getDocumentUi() {
		return new DiagnosisReportUi(this);
	}
}
