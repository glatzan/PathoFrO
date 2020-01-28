package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import org.primefaces.model.StreamedContent

/**
 * Container Interface for streaming pdf content. The tooltip pdfContainer might
 * be the same as the displayPDF. It is used to provide a thumbnail.
 *
 */
interface IPDFThumbnailStreamContainer : IPDFStreamContainer {

    /**
     * Container for dynamic thumbnail display
     */
    var thumbnail: PDFContainer?

    override fun reset() {
        thumbnail = null
        super.reset()
    }

    fun resetPDF() {
        this.displayPDF = null
    }

    fun resetThumbnail() {
        this.thumbnail = null
    }

    override fun render(container: PDFContainer) {
        thumbnail = container
        super.render(container)
    }

    fun renderThumbnail(container: PDFContainer) {
        this.thumbnail = container
    }

    fun renderPDF(container: PDFContainer) {
        this.displayPDF = container
    }

    /**
     * Returns the thumbnail als stream
     */
    fun getThumbnailStream(): StreamedContent {
        val t = thumbnail
        val r = getStream(t?.thumbnail ?: "", t?.name ?: "", "image/png")
                ?: getStream(SpringContextBridge.services().pathoConfig.pdfErrorFiles.pdfNotFoundIMG, "", "image/png")
        return r as StreamedContent
    }

    /**
     * Workaround for p:media, does not find the getThumbnailStream in a component
     */
    fun thumbnailStream(): StreamedContent {
        return getThumbnailStream()
    }
}