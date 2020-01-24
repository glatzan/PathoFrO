package com.patho.main.util.pdf.creator

import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfWriter
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.print.LoadedPrintPDFBearer
import java.io.ByteArrayOutputStream
import java.io.IOException

class PDFManipulator {

    companion object {

        /**
         * Merges a list of loaded pdfs and saves them.
         */
        @JvmStatic
        fun mergePDFs(target: PDFContainer, containers: List<LoadedPrintPDFBearer>): PDFContainer {
            val loadedContainer: LoadedPrintPDFBearer? = mergePDFs(containers, "", target.type)
                    ?: throw IOException("Could not merge PDFs")

            services().mediaRepository.saveBytes(loadedContainer?.pdfData!!, target.path)

            if (target.thumbnail.isNotEmpty()) services().pdfService.generateAndSaveThumbnail(loadedContainer?.pdfData!!, target.thumbnail)

            return target
        }

        /**
         * Merges a list of loaded pdfs.
         */
        @JvmStatic
        fun mergePDFs(containers: List<LoadedPrintPDFBearer>, name: String, type: PrintDocumentType): LoadedPrintPDFBearer? {
            return try {
                val document = Document()
                val out = ByteArrayOutputStream()
                val writer = PdfWriter.getInstance(document, out)
                document.open()
                val cb = writer.directContent
                for (pdfContainer in containers) {
                    val pdfReader = PdfReader(pdfContainer.pdfData)
                    for (i in 1..pdfReader.numberOfPages) {
                        document.newPage()
                        // import the page from source pdf
                        val page = writer.getImportedPage(pdfReader, i)
                        // add the page to the destination pdf
                        cb.addTemplate(page, 0f, 0f)
                    }
                }
                document.close()
                LoadedPrintPDFBearer(type!!, name!!, out.toByteArray(), null)
            } catch (e: DocumentException) {
                e.printStackTrace()
                null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }


        /**
         * Counts pages of the pdf
         */
        @JvmStatic
        fun countPDFPages(container: LoadedPrintPDFBearer): Int {
            return try {
                val pdfReader = PdfReader(container.pdfData)
                pdfReader.close()
                pdfReader.numberOfPages
            } catch (e: IOException) {
                e.printStackTrace()
                0
            }
        }

    }
}