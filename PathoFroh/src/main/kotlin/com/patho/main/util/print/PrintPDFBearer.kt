package com.patho.main.util.print

import com.patho.main.action.handler.CentralHandler
import com.patho.main.model.PDFContainer
import com.patho.main.template.PrintDocument

/**
 * Class for encapsulating print template and generated result
 */
open class PrintPDFBearer(val pdfContainer: PDFContainer, val printDocument: PrintDocument){

    /**
     * Converts the template to a loaded pdf bearer
     */
    open fun toLoadedPrintPDFBearer() : LoadedPrintPDFBearer{
        return LoadedPrintPDFBearer(pdfContainer,printDocument)
    }
}