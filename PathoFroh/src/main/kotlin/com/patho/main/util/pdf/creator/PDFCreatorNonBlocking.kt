package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.PrintDocument
import com.patho.main.util.pdf.LazyPDFReturnHandler
import java.util.*
import java.util.concurrent.Semaphore

class PDFCreatorNonBlocking(workDirectory: String = SpringContextBridge.services().pathoConfig.fileSettings.workDirectory) : PDFCreator(workDirectory) {

    /**
     * Lock for creating tasks in the background
     */
    var lock = Semaphore(1)

    /**
     * Creates a new pdf. Does not Block the program until the pdf was created.
     */
    fun create(template: PrintDocument, targetDirectory: String = this.targetDirectory, createThumbnail: Boolean = false, returnHandler: LazyPDFReturnHandler): String {

        val uuid = UUID.randomUUID().toString()

        services().taskExecutor.execute {
            try {
                logger.debug("Starting PDF Generation in new Thread")
                lock.acquire()
                val returnPDF: PDFContainer = create(template, targetDirectory, createThumbnail)
                returnHandler.returnPDFContent(returnPDF, uuid)
                logger.debug("PDF Generation completed, thread ended")
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