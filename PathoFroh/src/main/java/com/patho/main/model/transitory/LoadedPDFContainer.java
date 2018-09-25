package com.patho.main.model.transitory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.template.PrintDocument.DocumentType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
/**
 * Container which loads the pdf data in ram
 * 
 * @author andi
 *
 */
public class LoadedPDFContainer extends PDFContainer {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	private byte pdfData[];
	private byte thumbnailData[];

	public LoadedPDFContainer(DocumentType type, String name, byte[] pdfData, byte[] thumbnailData) {
		this.type = type;
		this.name = name;
		this.pdfData = pdfData;
		this.thumbnailData = thumbnailData;
	}

	public LoadedPDFContainer(PDFContainer pdfContainer) {
		this.id = pdfContainer.getId();
		this.type = pdfContainer.getType();
		this.name = pdfContainer.getName();
		this.audit = pdfContainer.getAudit();
		this.finalDocument = pdfContainer.isFinalDocument();
		this.commentary = pdfContainer.getCommentary();
		this.intern = pdfContainer.getIntern();
		this.path = pdfContainer.getPath();
		this.thumbnail = pdfContainer.getThumbnail();
		this.restricted = pdfContainer.isRestricted();

		this.pdfData = mediaRepository.getBytes(this.path);

		if (this.thumbnail != null)
			this.thumbnailData = mediaRepository.getBytes(this.thumbnail);
	}
}
