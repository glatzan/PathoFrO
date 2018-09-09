package com.patho.main.template.print;

import com.patho.main.template.PrintDocument;
import com.patho.main.template.print.ui.CouncilReportUi;

/**
 * patient, task, council, address, date
 */
public class CouncilReport extends PrintDocument {

	public CouncilReport(PrintDocument printDocument) {
		super(printDocument);
	}

	public CouncilReportUi getDocumentUi() {
		return new CouncilReportUi(this);
	}
}
