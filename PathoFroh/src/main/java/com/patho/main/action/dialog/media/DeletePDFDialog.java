package com.patho.main.action.dialog.media;

import com.patho.main.util.dialog.event.ReloadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.service.PDFService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class DeletePDFDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	/**
	 * Parent of the pdf
	 */
	private DataList parent;

	/**
	 * PDF container
	 */
	private PDFContainer container;

	public DeletePDFDialog initAndPrepareBean(PDFContainer container, DataList parent) {
		if (initBean(container, parent))
			prepareDialog();
		return this;
	}

	public boolean initBean(PDFContainer container, DataList parent) {
		setContainer(container);
		setParent(parent);
		super.initBean(null, Dialog.PDF_DELETE);
		return true;
	}

	public void deleteAndHide() {
		logger.debug("Deleting container");
		MessageHandler.sendGrowlMessagesAsResource("log.pdf.delete", "log.pdf.delete.text",
				new Object[] { container.getName() });
		pdfService.deletePdf(parent, container);
		hideDialog();
	}

	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadEvent());
	}
}
