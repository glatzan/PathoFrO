package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import java.io.IOException

open class PDFUpdater(workDirectory: String = SpringContextBridge.services().pathoConfig.fileSettings.workDirectory) : PDFCreator(workDirectory) {

    /**
     * Updates the content of a pdf. Blocks the program until the pdf was created.
     */
    fun update(template: PrintDocument, container: PDFContainer, createThumbnail: Boolean = false): PDFContainer {
        //TODO Handle Error
        var success = false
        var tmp = container
        val targetDirectory = container.path.substringBeforeLast(".")

        logger.debug("Updating pdf container, path to file: ${tmp.path}, path to targetDirectory: ${targetDirectory}")

        try {
            success = runPDFCreation(template.finalContent, createThumbnail, tmp, workDirectory, auxDirectory, targetDirectory, errorDirectory)
            if (template.afterPDFCreationHook)
                tmp = template.onAfterPDFCreation(tmp, this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return tmp
    }
}