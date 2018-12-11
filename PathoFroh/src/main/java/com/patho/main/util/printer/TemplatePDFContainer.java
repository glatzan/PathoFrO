package com.patho.main.util.printer;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TemplatePDFContainer extends PDFContainer {

	@Delegate
	private PDFContainer pdfContainer;

	private PrintDocument printDocument;

	public TemplatePDFContainer(PDFContainer pdfContainer) {
		this.pdfContainer = pdfContainer;
	}
}
