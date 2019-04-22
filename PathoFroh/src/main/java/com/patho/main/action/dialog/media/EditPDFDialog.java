package com.patho.main.action.dialog.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.PDFRepository;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class EditPDFDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFRepository pdfRepository;

	/**
	 * PDF container
	 */
	private PDFContainer container;

	/**
	 * Type of uploaded file
	 */
	private DocumentType[] availableFileTypes;

	public EditPDFDialog initAndPrepareBean(PDFContainer container) {
		if (initBean(container))
			prepareDialog();
		return this;
	}

	public boolean initBean(PDFContainer container) {
		setContainer(container);
		setAvailableFileTypes(new DocumentType[] { DocumentType.BIOBANK_INFORMED_CONSENT, DocumentType.COUNCIL_REPLY,
				DocumentType.OTHER, DocumentType.U_REPORT });
		super.initBean(null, Dialog.PDF_EDIT);
		return true;
	}

	public void saveAndHide() {
		pdfRepository.save(container, resourceBundle.get("log.pdf.edit", container));
		hideDialog();
	}

	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadEvent());
	}

}
