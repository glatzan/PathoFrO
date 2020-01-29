package com.patho.main.model.transitory

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocumentType

/**
 * Container for pdfs which contains  also the pdf an thumbnail as byteArrays
 */
class PDFContainerLoaded : PDFContainer {

    /**
     * PDF as byteArray
     */
    var pdfData: ByteArray = byteArrayOf()

    /**
     * Thumbnail as byteArray
     */
    var thumbnailData: ByteArray? = null

    constructor(pdfContainer: PDFContainer) {
        loadIntoObject(pdfContainer)
        this.pdfData = SpringContextBridge.services().mediaRepository.getBytes(pdfContainer.path)

        if (pdfContainer.isThumbnailPreset)
            SpringContextBridge.services().mediaRepository.getBytes(pdfContainer.thumbnail)
    }

    constructor(pdfContainer: PDFContainer, pdfData: ByteArray, thumbnailData: ByteArray? = null) {
        loadIntoObject(pdfContainer)
        this.pdfData = pdfData
        this.thumbnailData = thumbnailData
    }

    constructor(type: PrintDocumentType, name: String, pdfData: ByteArray, thumbnailData: ByteArray?  = null) {
        this.pdfData = pdfData
        this.thumbnailData = thumbnailData
        this.type = type
        this.name = name
    }

    private fun loadIntoObject(container: PDFContainer) {
        this.id = container.id
        this.type = container.type
        this.name = container.name
        this.audit = container.audit
        this.finalDocument = container.finalDocument
        this.commentary = container.commentary
        this.intern = container.intern
        this.path = container.path
        this.thumbnail = container.thumbnail
        this.restricted = container.restricted
    }


}

