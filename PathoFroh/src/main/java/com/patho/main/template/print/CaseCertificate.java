package com.patho.main.template.print;


import com.patho.main.template.PrintDocument;
import com.patho.main.template.print.ui.CaseCertificateUi;

/**
 * patient, task, date
 * @author andi
 *
 */
public class CaseCertificate extends PrintDocument {

	public CaseCertificate(PrintDocument printDocument) {
		super(printDocument);
	}
	
	public CaseCertificateUi getDocumentUi() {
		return new CaseCertificateUi(this);
	}
}
