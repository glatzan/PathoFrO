package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.interfaces.PdfStreamProvider
import lombok.Synchronized
import org.primefaces.model.DefaultStreamedContent
import org.primefaces.model.StreamedContent
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.faces.context.FacesContext
import javax.faces.event.PhaseId

class LazyPDFGuiManager() : PdfStreamProvider, LazyPDFReturnHandler {

    val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * If true the component wil be rendered.
     */
    var renderComponent: Boolean = false

    /**
     * If true the poll element at the view page will start
     */
    var stopPoll = AtomicBoolean(true)

    /**
     * If true the poll element will start
     */
    var autoStartPoll = AtomicBoolean(false)

    /**
     * If true the pdf will be rendered
     */
    var renderPDF = AtomicBoolean(false)

    /**
     * pdf container
     */
    var pdfContainerToRender: PDFContainer? = null

    /**
     * Thread id of the last pdf generating thread
     */
    var currentTaskUuid: String? = null

    /**
     * Resets render state
     */
    fun reset() {
        renderPDF.set(false)
        stopPoll.set(true)
        autoStartPoll.set(false)
        currentTaskUuid = ""
    }

    /**
     * If the pdf was created manually is can be set using this method.
     */
    fun setManuallyCreatedPDF(container: PDFContainer) {
        setPDFContainerToRender(container)
        renderPDF.set(true)
        stopPoll.set(true)
        autoStartPoll.set(false)
    }

    /**
     * Starts rendering in other thread
     */
    fun startRendering(template: PrintDocument, outputPath: String) {
        startRendering(template, File(outputPath))
    }

    /**
     * Starts rendering in other thread
     */
    fun startRendering(template: PrintDocument, outputPath: File) {
        currentTaskUuid = PDFCreator().createPDFNonBlocking(template, outputPath, this)
        stopPoll.set(false)
        autoStartPoll.set(true)
    }

    /**
     * Return method for the pdf creating thread
     */
    @Synchronized
    override fun returnPDFContent(container: PDFContainer?, uuid: String) {
        if (currentTaskUuid == uuid) {

            if (container != null && SpringContextBridge.services().mediaRepository.isFile(container.path)) {
                logger.debug("Setting PDf for rendering. Path: {}", container)
                setPDFContainerToRender(container)
            } else {
                setPDFContainerToRender(
                        PDFContainer(PrintDocumentType.PRINT_DOCUMENT, "RenderError.pdf", PathoConfig.RENDER_ERROR_PDF, ""))
            }

            renderPDF.set(true)
            stopPoll.set(true)
            autoStartPoll.set(false)

        } else {
            logger.debug("More then one Thread! Old Thread")
        }
    }

    /**
     * Returns the pdf as stream
     */
    override fun getPdfContent(): StreamedContent {
        val context = FacesContext.getCurrentInstance()
        if (context.currentPhaseId === PhaseId.RENDER_RESPONSE || pdfContainerToRender == null) {
            // So, we're rendering the HTML. Return a stub StreamedContent so
            // that it will generate right URL.
            return DefaultStreamedContent()
        } else {

            val pdf: ByteArray

            if (SpringContextBridge.services().mediaRepository.isFile(pdfContainerToRender!!.path))
                pdf = SpringContextBridge.services().mediaRepository.getBytes(pdfContainerToRender!!.path)
            else
                pdf = SpringContextBridge.services().mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF)

            return DefaultStreamedContent(ByteArrayInputStream(pdf), "application/pdf",
                    pdfContainerToRender!!.name)
        }
    }


    @Synchronized
    override fun getPDFContainerToRender(): PDFContainer? {
        return pdfContainerToRender
    }

    @Synchronized
    fun setPDFContainerToRender(container: PDFContainer) {
        this.pdfContainerToRender = container
    }
}