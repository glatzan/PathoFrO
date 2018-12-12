package com.patho.main.util.printer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.template.PrintDocument.DocumentType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Container which loads the pdf data in ram
 * 
 * @author andi
 *
 */
@Configurable(preConstruction = true)
@Getter
@Setter
public class LoadedPDFContainer extends TemplatePDFContainer {

	@Autowired()
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	private byte pdfData[];
	private byte thumbnailData[];

	public LoadedPDFContainer(DocumentType type, String name, byte[] pdfData, byte[] thumbnailData) {
		super(new PDFContainer());
		getPdfContainer().setName(name);
		this.pdfData = pdfData;
		this.thumbnailData = thumbnailData;
	}

	public LoadedPDFContainer(PDFContainer pdfContainer) {
		setPdfContainer(pdfContainer);

		this.pdfData = mediaRepository.getBytes(pdfContainer.getPath());

		if (pdfContainer.getThumbnail() != null)
			this.thumbnailData = mediaRepository.getBytes(pdfContainer.getThumbnail());
	}
}
