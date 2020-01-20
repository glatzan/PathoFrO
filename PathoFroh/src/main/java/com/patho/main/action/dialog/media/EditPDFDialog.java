package com.patho.main.action.dialog.media;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.PDFRepository;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Getter
@Setter
public class EditPDFDialog extends AbstractDialog {

	/**
	 * PDF container
	 */
	private PDFContainer container;

	/**
	 * Type of uploaded file
	 */
	private PrintDocumentType[] availableFileTypes;

	public EditPDFDialog initAndPrepareBean(PDFContainer container) {
		if (initBean(container))
			prepareDialog();
		return this;
	}

	public boolean initBean(PDFContainer container) {
		setContainer(container);
		setAvailableFileTypes(new PrintDocumentType[] { PrintDocumentType.BIOBANK_INFORMED_CONSENT, PrintDocumentType.COUNCIL_REPLY,
				PrintDocumentType.OTHER, PrintDocumentType.U_REPORT });
		super.initBean(null, Dialog.PDF_EDIT);
		return true;
	}

	public void saveAndHide() {
		SpringContextBridge.services().getPdfRepository().save(container, resourceBundle.get("log.pdf.edit", container));
		hideDialog();
	}

	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadEvent());
	}

}
