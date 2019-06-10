package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import lombok.Synchronized
import org.primefaces.model.DefaultStreamedContent
import org.primefaces.model.StreamedContent
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.faces.context.FacesContext
import javax.faces.event.PhaseId

/**
 * Lazy implementation for the PDFStreamContainer
 */
class LazyPDFGuiManager() : IPDFStreamContainer, LazyPDFReturnHandler {

    val logger = LoggerFactory.getLogger(this.javaClass)

    private val lock = ReentrantLock()
    /**
     * PDF container
     */
    override var displayPDF: PDFContainer? = null

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
    var sRenderPDF = AtomicBoolean(false)

    /**
     * Thread id of the last pdf generating thread
     */
    var currentTaskUuid: String? = null

    /**
     * Resets render state
     */
    override fun reset() {
        sRenderPDF.set(false)
        stopPoll.set(true)
        autoStartPoll.set(false)
        currentTaskUuid = ""
    }

    /**
     * If the pdf was created manually is can be set using this method.
     */
    fun setManuallyCreatedPDF(container: PDFContainer) {
        render(container)
        sRenderPDF.set(true)
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
                render(container)
            } else {
                render(
                        PDFContainer(PrintDocumentType.PRINT_DOCUMENT, "RenderError.pdf", PathoConfig.RENDER_ERROR_PDF, ""))
            }

            sRenderPDF.set(true)
            stopPoll.set(true)
            autoStartPoll.set(false)

        } else {
            logger.debug("More then one Thread! Old Thread")
        }
    }
}