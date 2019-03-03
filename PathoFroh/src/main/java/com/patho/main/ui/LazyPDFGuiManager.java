package com.patho.main.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import com.patho.main.config.PathoConfig;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.ui.interfaces.PdfStreamProvider;
import com.patho.main.util.pdf.LazyPDFReturnHandler;
import com.patho.main.util.pdf.PDFCreator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

@Getter
@Setter
@Configurable
/**
 * Class for lazy media creation, used with dynamicMedia component
 * 
 * @author andi
 *
 */
public class LazyPDFGuiManager implements PdfStreamProvider, LazyPDFReturnHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	/**
	 * If true the component wil be rendered.
	 */
	private boolean renderComponent;

	/**
	 * If true the poll element at the view page will start
	 */
	private AtomicBoolean stopPoll = new AtomicBoolean(true);

	/**
	 * If true the poll element will start
	 */
	private AtomicBoolean autoStartPoll = new AtomicBoolean(false);

	/**
	 * If true the pdf will be rendered
	 */
	private AtomicBoolean renderPDF = new AtomicBoolean(false);

	/**
	 * pdf container
	 */
	private PDFContainer PDFContainerToRender;

	/**
	 * Thread id of the last pdf generating thread
	 */
	private String currentTaskUuid;

	/**
	 * Resets render state
	 */
	public void reset() {
		getRenderPDF().set(false);
		getStopPoll().set(true);
		getAutoStartPoll().set(false);
		setCurrentTaskUuid("");
	}

	/**
	 * If the pdf was created manually is can be set using this method.
	 * 
	 * @param container
	 */
	public void setManuallyCreatedPDF(PDFContainer container) {
		setPDFContainerToRender(container);
		getRenderPDF().set(true);
		getStopPoll().set(true);
		getAutoStartPoll().set(false);
	}

	/**
	 * Starts rendering in other thread
	 * 
	 * @param template
	 */
	public void startRendering(PrintDocument template, String outputPath) {
		startRendering(template, new File(outputPath));
	}

	public void startRendering(PrintDocument template, File outputPath) {
		currentTaskUuid = new PDFCreator().createPDFNonBlocking(template, outputPath, this);
		getStopPoll().set(false);
		getAutoStartPoll().set(true);
	}

	/**
	 * Return method for thread
	 */
	@Override
	@Synchronized
	public void returnPDFContent(PDFContainer container, String uuid) {
		if (getCurrentTaskUuid().equals(uuid)) {

			if (container != null && mediaRepository.isFile(container.getPath())) {
				logger.debug("Setting PDf for rendering. Path: {}", container);
				setPDFContainerToRender(container);
			} else {
				setPDFContainerToRender(
						new PDFContainer(DocumentType.PRINT_DOCUMENT, "RenderError.pdf", PathoConfig.Companion.getRENDER_ERROR_PDF()));
			}

			getRenderPDF().set(true);
			getStopPoll().set(true);
			getAutoStartPoll().set(false);

		} else {
			logger.debug("More then one Thread! Old Thread");
		}
	}

	/**
	 * Returns the pdf as stream
	 */
	public StreamedContent getPdfContent() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getPDFContainerToRender() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {

			byte[] pdf;

			if (mediaRepository.isFile(getPDFContainerToRender().getPath()))
				pdf = mediaRepository.getBytes(getPDFContainerToRender().getPath());
			else
				pdf = mediaRepository.getBytes(PathoConfig.Companion.getPDF_NOT_FOUND_PDF());

			return new DefaultStreamedContent(new ByteArrayInputStream(pdf), "application/pdf",
					getPDFContainerToRender().getName());
		}
	}

	@Synchronized
	public PDFContainer getPDFContainerToRender() {
		return PDFContainerToRender;
	}

	@Synchronized
	public void setPDFContainerToRender(PDFContainer container) {
		this.PDFContainerToRender = container;
	}
}
