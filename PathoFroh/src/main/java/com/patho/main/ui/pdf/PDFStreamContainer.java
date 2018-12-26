package com.patho.main.ui.pdf;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;

/**
 * Container Interface for streaming pdf content. The tooltip pdfContainer might
 * be the same as the displayPDF. It is used to provide a thumbnail.
 * 
 * @author Andreas
 *
 */
public interface PDFStreamContainer {

	public void setDisplayPDF(PDFContainer container);

	public PDFContainer getDisplayPDF();

	public void setTooltip(PDFContainer container);

	public PDFContainer getTooltip();

	public MediaRepository getMediaRepository();

	/**
	 * Clears pdf and thumbnail
	 */
	public default void reset() {
		setDisplayPDF(null);
		setTooltip(null);
	}

	/**
	 * Sets the pdf as display an thumbnail
	 * 
	 * @param container
	 */
	public default void renderPDF(PDFContainer container) {
		setDisplayPDF(container);
		setTooltip(container);
	}

	/**
	 * Returns the thumbnail als stream
	 * 
	 * @return
	 */
	public default StreamedContent getThumbnail() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getTooltip() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {

			byte[] img;

			if (getTooltip() != null && getMediaRepository().isFile(getTooltip().getThumbnail()))
				img = getMediaRepository().getBytes(getTooltip().getThumbnail());
			else
				img = getMediaRepository().getBytes(PathoConfig.PDF_NOT_FOUND_IMG);

			return new DefaultStreamedContent(new ByteArrayInputStream(img), "image/png");
		}
	}

	/**
	 * Returns the pdf als stream
	 * 
	 * @return
	 */
	public default StreamedContent getPDF() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getDisplayPDF() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {
			byte[] file;

			if (getDisplayPDF() != null && getMediaRepository().isFile(getDisplayPDF().getPath()))
				file = getMediaRepository().getBytes(getDisplayPDF().getPath());
			else
				file = getMediaRepository().getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

			return new DefaultStreamedContent(new ByteArrayInputStream(file), "application/pdf",
					getDisplayPDF().getName());
		}
	}

	/**
	 * Returns true if pdf is set
	 * 
	 * @return
	 */
	public default boolean renderPDF() {
		return getDisplayPDF() != null;
	}

	/**
	 * Opens a pdf in an new window
	 */
	public default void openPDFinNewWindow() throws IOException {

		// Prepare.
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

		BufferedOutputStream output = null;

		try {
			byte[] file;

			if (getDisplayPDF() != null && getMediaRepository().isFile(getDisplayPDF().getPath()))
				file = getMediaRepository().getBytes(getDisplayPDF().getPath());
			else
				file = getMediaRepository().getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

			// Init servlet response.
			response.reset();
			response.setHeader("Content-Type", "application/pdf");
			response.setHeader("Content-Length", String.valueOf(file.length));
			response.setHeader("Content-Disposition", "inline; filename=\"asd\"");
			output = new BufferedOutputStream(response.getOutputStream());
			// Write file contents to response.
			output.write(file, 0, file.length);
			// Finalize task.
			output.flush();
		} finally {
			output.close();
		}

		// Inform JSF that it doesn't need to handle response.
		// This is very important, otherwise you will get the following exception in the
		// logs:
		// java.lang.IllegalStateException: Cannot forward after response has been
		// committed.
		facesContext.responseComplete();
	}
}
