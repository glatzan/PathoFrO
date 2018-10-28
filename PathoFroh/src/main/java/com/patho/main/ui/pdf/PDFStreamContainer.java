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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PDFStreamContainer {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	protected PDFContainer displayPDF;

	protected PDFContainer tooltip;

	public void reset() {
		this.displayPDF = null;
		this.tooltip = null;
	}

	/**
	 * Returns the thumbnail als stream
	 * 
	 * @return
	 */
	public StreamedContent getThumbnail() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || tooltip == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {

			byte[] img;

			if (tooltip != null && mediaRepository.isFile(tooltip.getThumbnail()))
				img = mediaRepository.getBytes(tooltip.getThumbnail());
			else
				img = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_IMG);

			return new DefaultStreamedContent(new ByteArrayInputStream(img), "image/png");
		}
	}

	/**
	 * Returns the pdf als stream
	 * 
	 * @return
	 */
	public StreamedContent getPDF() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getDisplayPDF() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {
			byte[] file;

			if (getDisplayPDF() != null && mediaRepository.isFile(getDisplayPDF().getPath()))
				file = mediaRepository.getBytes(getDisplayPDF().getPath());
			else
				file = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

			return new DefaultStreamedContent(new ByteArrayInputStream(file), "application/pdf",
					getDisplayPDF().getName());
		}
	}

	public boolean renderPDF() {
		return getDisplayPDF() != null;
	}

	/**
	 * Opens a pdf in an new window
	 */
	public void openPDFinNewWindow() throws IOException {

		// Prepare.
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

		BufferedOutputStream output = null;

		try {
			byte[] file;

			if (getDisplayPDF() != null && mediaRepository.isFile(getDisplayPDF().getPath()))
				file = mediaRepository.getBytes(getDisplayPDF().getPath());
			else
				file = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

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