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
@Configurable(preConstruction=true)
@Getter
@Setter
public class LoadedPDFContainer{

	@Autowired()
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	private PDFContainer pdfContainer;
	
	private String name;
	protected DocumentType type;
	
	private byte pdfData[];
	private byte thumbnailData[];

	public LoadedPDFContainer(DocumentType type, String name, byte[] pdfData, byte[] thumbnailData) {
		this.type = type;
		this.name = name;
		this.pdfData = pdfData;
		this.thumbnailData = thumbnailData;
	}

	public LoadedPDFContainer(PDFContainer pdfContainer) {
		this.pdfContainer = pdfContainer;
		System.out.println(pdfContainer.getPath() + "  " + mediaRepository);
		
		this.pdfData = mediaRepository.getBytes(pdfContainer.getPath());

		this.type = pdfContainer.getType();
		this.name = pdfContainer.getName();
		
		if (pdfContainer.getThumbnail() != null)
			this.thumbnailData = mediaRepository.getBytes(pdfContainer.getThumbnail());
	}
}
