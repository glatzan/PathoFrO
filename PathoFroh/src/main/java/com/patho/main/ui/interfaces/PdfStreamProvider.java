package com.patho.main.ui.interfaces;

import java.io.ByteArrayInputStream;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.patho.main.model.PDFContainer;

/**
 * Interface for handling pdf streams for primefaces
 * 
 * @author andi
 *
 */
public interface PdfStreamProvider {

	/**
	 * PDF to render
	 * 
	 * @return
	 */
	public PDFContainer getPDFContainerToRender();

	/**
	 * Content-Stream for rendering by gui
	 * 
	 * @return
	 */
	public default StreamedContent getPdfContent() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getPDFContainerToRender() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {
			return new DefaultStreamedContent(new ByteArrayInputStream(getPDFContainerToRender().getData()),
					"application/pdf", getPDFContainerToRender().getName());
		}
	}
}
