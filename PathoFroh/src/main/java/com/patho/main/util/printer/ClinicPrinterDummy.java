package com.patho.main.util.printer;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.pdf.PrintOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ClinicPrinterDummy extends ClinicPrinter {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long serialVersionUID = 3965169880380400939L;

	public ClinicPrinterDummy() {
		this.name = "Drucker Auswählen";
		this.id = this.getName().hashCode();
	}

	@Override
	public boolean print(File file) {
		logger.debug("Dummy printer, printin...");
		return true;
	}

	@Override
	public boolean print(PDFContainer container) {
		return print(container, 1, null);
	}

	@Override
	public boolean print(PDFContainer container, int count) {
		return print(container, count, null);
	}

	@Override
	public boolean print(PDFContainer container, int count, String args) {
		logger.debug("Dummy printer, printin...");
		return true;
	}

	@Override
	public boolean printTestPage() {
		logger.debug("Dummy printer, printin...");
		return true;
	}

	@Override
	public boolean print(PDFContainer container, String args) {
		logger.debug("Dummy printer, printin...");
		return true;
	}

	@Override
	public boolean print(PDFContainer container, PrintDocument template) {
		logger.debug("Dummy printer, printin...");
		return true;
	}

	@Override
	public boolean print(PrintOrder printOrder) {
		logger.debug("Dummy printer, printin...");
		return true;
	}

}
