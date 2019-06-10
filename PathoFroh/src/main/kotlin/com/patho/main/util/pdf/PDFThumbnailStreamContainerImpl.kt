package com.patho.main.util.pdf

import com.patho.main.model.PDFContainer

/**
 * Default implementation of the PDFThumbnailStreamContainer
 */
class PDFThumbnailStreamContainerImpl : IPDFThumbnailStreamContainer {
    override var displayPDF: PDFContainer? = null
    override var thumbnail: PDFContainer? = null
}