package com.patho.main.util.print

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument

/**
 * Container which loads the pdf data in ram
 */
class LoadedPrintPDFBearer : PrintPDFBearer {

    var pdfData: ByteArray? = null

    var thumbnailData: ByteArray? = null

    constructor(pdfContainer: PDFContainer) : this(pdfContainer, PrintDocument())

    constructor(pdfContainer: PDFContainer, printDocument: PrintDocument) : super(pdfContainer, printDocument) {
        pdfData = SpringContextBridge.services().mediaRepository.getBytes(pdfContainer.path)

        if (pdfContainer.thumbnail != null)
            thumbnailData = SpringContextBridge.services().mediaRepository.getBytes(pdfContainer.thumbnail)
    }

    constructor(type: PrintDocument.DocumentType, name: String, pdfData: ByteArray, thumbnailData: ByteArray?) : super(PDFContainer(type, name), PrintDocument()) {
        this.pdfData = pdfData
        this.thumbnailData = thumbnailData
    }

}