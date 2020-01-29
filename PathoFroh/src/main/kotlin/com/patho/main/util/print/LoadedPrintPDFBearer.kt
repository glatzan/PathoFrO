package com.patho.main.util.print

import com.patho.main.model.PDFContainer
import com.patho.main.model.transitory.PDFContainerLoaded
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType

/**
 * Container which loads the pdf data in ram
 */
class LoadedPrintPDFBearer: PrintPDFBearer {

    constructor(pdfContainer: PDFContainer) : this(pdfContainer, PrintDocument())

    constructor(pdfContainer: PDFContainer, printDocument: PrintDocument) : super(PDFContainerLoaded(pdfContainer), printDocument)

    constructor(pdfContainer: PDFContainerLoaded, printDocument: PrintDocument) : super(pdfContainer, printDocument)

    constructor(type: PrintDocumentType, name: String, pdfData: ByteArray, thumbnailData: ByteArray?) : super(PDFContainerLoaded(type, name, pdfData, thumbnailData), PrintDocument())
}