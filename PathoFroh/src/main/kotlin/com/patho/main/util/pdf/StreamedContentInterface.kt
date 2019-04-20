package com.patho.main.util.pdf

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import org.primefaces.model.DefaultStreamedContent
import org.primefaces.model.StreamedContent
import java.io.ByteArrayInputStream
import javax.faces.context.FacesContext
import javax.faces.event.PhaseId

interface StreamedContentInterface {

    /**
     * Selected pdf to display
     */
    var selectedContainer: PDFContainer?

    /**
     * List of thumbnail to check against if id is provided
     */
    val thumbnailList: List<PDFContainer>
        get() = listOf<PDFContainer>()

    /**
     * Returns the selected pdf container
     */
    fun getPDF(): StreamedContent {
        if (FacesContext.getCurrentInstance().currentPhaseId === PhaseId.RENDER_RESPONSE || selectedContainer == null) {
            // So, we're rendering the HTML. Return a stub StreamedContent so
            // that it will generate right URL.
            return DefaultStreamedContent()
        } else {

            val img: ByteArray

            if (SpringContextBridge.services().mediaRepository.isFile(selectedContainer?.path))
                img = SpringContextBridge.services().mediaRepository.getBytes(selectedContainer?.path)
            else
                img = SpringContextBridge.services().mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF)

            return DefaultStreamedContent(ByteArrayInputStream(img), "application/pdf",
                    selectedContainer?.name)
        }
    }

    /**
     * Returns a thumbnail. Either if id is present, the thumbnail list will be check, otherwise
     * the thumbnail of the selected container will be returned
     *
     * @return
     */
    fun getThumbnail(): StreamedContent {
        if (FacesContext.getCurrentInstance().currentPhaseId === PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will
            // generate right URL.
            return DefaultStreamedContent()
        } else {

            // So, browser is requesting the image. Return a real StreamedContent with the
            // image bytes.
            val id = FacesContext.getCurrentInstance().externalContext.requestParameterMap["id"]

            var path: String? = null

            if (id != null) {
                if (thumbnailList.any { p -> p.thumbnail == id })
                    path = id
            } else {
                path = selectedContainer?.thumbnail ?: null
            }

            if (path != null && SpringContextBridge.services().mediaRepository.isFile(path)) {
                val img = SpringContextBridge.services().mediaRepository.getBytes(path)
                return DefaultStreamedContent(ByteArrayInputStream(img), "image/png")
            } else
                return DefaultStreamedContent(
                        ByteArrayInputStream(SpringContextBridge.services().mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF)), "image/png")
        }
    }

}