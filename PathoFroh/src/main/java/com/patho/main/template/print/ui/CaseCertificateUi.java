package com.patho.main.template.print.ui;

import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument.InitializeToken;
import com.patho.main.template.print.CaseCertificate;

public class CaseCertificateUi extends AbstractDocumentUi<CaseCertificate> {

	private boolean printed;

	public CaseCertificateUi(CaseCertificate documentTemplate) {
		super(documentTemplate);
	}

	public void initialize(Task task) {
		super.initialize(task);
	}

	public void beginNextTemplateIteration() {
		printed = false;
	}

	public boolean hasNextTemplateConfiguration() {
		return !printed;
	}

	/**
	 * Return default template configuration for printing
	 */
	public TemplateConfiguration<CaseCertificate> getDefaultTemplateConfiguration() {
		printDocument.initilize(new InitializeToken("task", task), new InitializeToken("patient", task.getParent()));
		return new TemplateConfiguration<CaseCertificate>(printDocument);
	}

	/**
	 * Sets the data for the next print
	 */
	public TemplateConfiguration<CaseCertificate> getNextTemplateConfiguration() {
		printed = true;
		return getDefaultTemplateConfiguration();
	}
}
