package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import org.primefaces.model.StreamedContent
import javax.faces.context.FacesContext

interface IPDFThumbnailListStreamContainer : IPDFStreamContainer {

    /**
     * List of thumbnail to check against if id is provided
     */
    val thumbnailList: List<PDFContainer>

    fun resetPDF() {
        this.displayPDF = null
    }

    fun render(container: PDFContainer, list: MutableList<PDFContainer>) {
        super.render(container)
    }

    fun renderPDF(container: PDFContainer) {
        this.displayPDF = container
    }

    /**
     * Workaround for p:media, does not find the getThumbnailStream in a component
     */
    fun thumbnailStream(): StreamedContent {
        return getThumbnailStream()
    }

    /**
     * Returns a thumbnail stream with the given id.
     */
    fun getThumbnailStream(): StreamedContent {
        val id = FacesContext.getCurrentInstance().externalContext.requestParameterMap["id"]
                ?: displayPDF?.thumbnail

        val pdf = thumbnailList.firstOrNull { it.thumbnail == id }

        val r = getStream(pdf?.thumbnail ?: "", pdf?.name ?: "", "image/png")
                ?: getStream(SpringContextBridge.services().pathoConfig.pdfErrorFiles.pdfNotFoundIMG, "", "image/png")

        return r as StreamedContent
    }
}