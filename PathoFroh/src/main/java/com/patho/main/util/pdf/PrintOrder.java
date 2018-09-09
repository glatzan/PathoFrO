package com.patho.main.util.pdf;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;

import lombok.Getter;
import lombok.Setter;

/**
 * Is returned by template in order to print.
 * 
 * @author andi
 *
 */
@Getter
@Setter
public class PrintOrder {

	private PDFContainer pdfContainer;
	private int copies;
	private boolean duplex;
	private boolean printEvenPageCount;
	private String args;

	public PrintOrder(PDFContainer container) {
		this(container, null);
	}

	public PrintOrder(PDFContainer container, PrintDocument documentTemplate) {
		this(container, documentTemplate, false);
	}

	public PrintOrder(PDFContainer container, PrintDocument documentTemplate, boolean duplex) {
		this.pdfContainer = container;
		if (documentTemplate != null) {
			this.duplex = documentTemplate.isDuplexPrinting() || duplex;
			this.args = documentTemplate.getAttributes();
			this.copies = documentTemplate.getCopies();
		}
	}

	public PrintOrder(PDFContainer container, int copies, boolean duplex, String args) {
		this.pdfContainer = container;
		this.duplex = duplex;
		this.args = args;
		this.copies = copies;
	}
}
