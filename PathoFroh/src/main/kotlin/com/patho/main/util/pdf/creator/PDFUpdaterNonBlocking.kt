package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.util.pdf.LazyPDFReturnHandler
import java.util.*
import java.util.concurrent.Semaphore

class PDFUpdaterNonBlocking(workDirectory: String = SpringContextBridge.services().pathoConfig.fileSettings.workDirectory) : PDFUpdater(workDirectory) {

    /**
     * Lock for creating tasks in the background
     */
    var lock = Semaphore(1)

    /**
     * Updates the content of a pdf. Does not Block the program until the pdf was created.
     */
    fun update(template: PrintDocument, container: PDFContainer, createThumbnail: Boolean = false, returnHandler: LazyPDFReturnHandler): String {
        val uuid = UUID.randomUUID().toString()

        SpringContextBridge.services().taskExecutor.execute {
            try {
                logger.debug("Starting PDF Update in new Thread")
                lock.acquire()
                val returnPDF: PDFContainer = update(template, container, createThumbnail)
                returnHandler.returnPDFContent(returnPDF, uuid)
                logger.debug("PDF Update completed, thread ended")
            } catch (e: Exception) {
                logger.debug("Error")
                e.printStackTrace()
            } finally {
                lock.release()
            }
        }
        return uuid
    }
}