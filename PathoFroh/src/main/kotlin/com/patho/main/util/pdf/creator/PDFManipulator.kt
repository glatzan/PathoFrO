package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.model.transitory.PDFContainerLoaded
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.exceptions.PDFMergeException
import com.patho.main.util.exceptions.ThumbnailCreateException
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class PDFManipulator {

    companion object {

        private val logger = LoggerFactory.getLogger(this.javaClass)

        @JvmStatic
        fun mergePDFs(containers: List<PDFContainerLoaded>, createThumbnail: Boolean): PDFContainerLoaded {
            val loadedPDFs = containers.map { PDFContainerLoaded(it) }
            return mergeLoadedPDFs(loadedPDFs, createThumbnail)
        }

        /**
         * Merges a list of loaded pdfs and saves them.
         */
        @JvmStatic
        fun mergeLoadedPDFs(containers: List<PDFContainerLoaded>, createThumbnail: Boolean): PDFContainerLoaded {

            val result = mergePDFs(containers, "", PrintDocumentType.UNKNOWN)

            if (result.pdfData.isEmpty()) {
                logger.error("Error while merging pdfs!")
                throw PDFMergeException()
            }

            if (createThumbnail) {
                val img = services().pdfService.generateThumbnail(result.pdfData, 0)
                if (img != null)
                    result.thumbnailData = services().pdfService.thumbnailToByteArray(img)
                else {
                    logger.error("Error while creating thumbnail!")
                    throw ThumbnailCreateException()
                }
            }
            return PDFContainerLoaded(PrintDocumentType.UNKNOWN, "", result.pdfData, result.thumbnailData)
        }


        /**
         * Merges a list of loaded pdfs.
         */
        @JvmStatic
        fun mergePDFs(containers: List<PDFContainerLoaded>, name: String, type: PrintDocumentType): PDFContainerLoaded {
            val outPut = ByteArrayOutputStream()

            val merger = PDFMergerUtility()
            merger.destinationStream = outPut
            merger.addSources(containers.map { ByteArrayInputStream(it.pdfData) })
            merger.mergeDocuments();

            return PDFContainerLoaded(type, name, outPut.toByteArray(), null)
        }


        /**
         * Counts pages of the pdf
         */
        @JvmStatic
        fun countPDFPages(container: PDFContainerLoaded): Int {
            val doc: PDDocument = PDDocument.load(ByteArrayInputStream(container.pdfData))
            val count = doc.numberOfPages
            logger.debug("Counted $count pages!!")
            doc.close()
            return count
        }

    }
}