package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import org.primefaces.model.DefaultStreamedContent
import org.primefaces.model.StreamedContent
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.faces.context.FacesContext
import javax.faces.event.PhaseId
import javax.servlet.http.HttpServletResponse

/**
 * Container Interface for streaming pdf content.
 */
interface IPDFStreamContainer {

    /**
     * PDF Container
     */
    var displayPDF: PDFContainer?

    /**
     * Resets the pdf container
     */
    fun reset() {
        displayPDF = null
    }

    /**
     * Sets the pdf container
     */
    fun render(container: PDFContainer) {
        this.displayPDF = container
    }

    /**
     * Returns true if the pdf container is set
     */
    val isRenderPDF: Boolean
        get() = displayPDF != null

    /**
     * Returns the pdf als stream
     */
    fun pdfStream(): StreamedContent {
        return getPDFStream()
    }

    /**
     * Returns the pdf als stream
     */
    fun getPDFStream(): StreamedContent {
        val t = displayPDF
        val r = getStream(t?.path ?: "", t?.name ?: "", "application/pdf")
                ?: getStream(PathoConfig.PDF_NOT_FOUND_PDF, "", "application/pdf")

        return r as StreamedContent
    }

    /**
     * Returns a pdf stream
     */
    fun getStream(path: String, name: String, contentType: String): StreamedContent? {
        val context = FacesContext.getCurrentInstance()
        return if (context.currentPhaseId === PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so
            // that it will generate right URL.
            DefaultStreamedContent()
        } else {
            val file = if (SpringContextBridge.services().mediaRepository.isFile(path))
                SpringContextBridge.services().mediaRepository.getBytes(path)
            else
                return null
            DefaultStreamedContent(ByteArrayInputStream(file), contentType, name)
        }
    }

    /**
     * Opens a pdf in an new window
     */
    @Throws(IOException::class)
    fun openPDFinNewWindow() {

        // Prepare.
        val facesContext = FacesContext.getCurrentInstance()
        val externalContext = facesContext.externalContext
        val response = externalContext.response as HttpServletResponse

        var output: BufferedOutputStream? = null

        try {
            val pdf = displayPDF
            val file = if (pdf?.path != null && SpringContextBridge.services().mediaRepository.isFile(pdf.path))
                SpringContextBridge.services().mediaRepository.getBytes(pdf.path)
            else
                SpringContextBridge.services().mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF)

            // Init servlet response.
            response.reset()
            response.setHeader("Content-Type", "application/pdf")
            response.setHeader("Content-Length", file.size.toString())
            response.setHeader("Content-Disposition", "inline; filename=\"asd\"")
            output = BufferedOutputStream(response.outputStream)
            // Write file contents to response.
            output.write(file, 0, file.size)
            // Finalize task.
            output.flush()
        } finally {
            output!!.close()
        }

        // Inform JSF that it doesn'special.pdfOrganizerDialog need to handle response.
        // This is very important, otherwise you will get the following exception in the
        // logs:
        // java.lang.IllegalStateException: Cannot forward after response has been
        // committed.
        facesContext.responseComplete()
    }
}