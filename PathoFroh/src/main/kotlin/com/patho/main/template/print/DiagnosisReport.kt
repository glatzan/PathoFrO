package com.patho.main.template.print

import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.pdf.creator.PDFCreator
import com.patho.main.util.pdf.creator.PDFManipulator
import org.apache.velocity.context.Context
import java.util.*

/**
 * DiagnosisReport <br>
 * Values to replace: <br>
 *     patient, <br>
 *     task, <br>
 *     diagnosisRevisions, <br>
 *     address, <br>
 *     subject, <br>
 *     date <br>
 *     pdfContainer (if not provided, task is used)
 */
class DiagnosisReport(printDocument: PrintDocument) : PrintDocument(printDocument) {

    private var pdfsAttachedToTask: Set<PDFContainer> = setOf()

    override fun initialize(content: HashMap<String, Any?>): Pair<out PrintDocument, Context> {
        val pdfcontainerMap = content.filterKeys { it == "pdfContainer" }

        if(pdfcontainerMap.isEmpty()){
            val taskMap = content.filterKeys { it == "task" }
            if(taskMap.isNotEmpty()){
                val task =  taskMap.entries.first().value
                if(task is Task) {
                    logger.trace("Fetching attached pdf form task")
                    pdfsAttachedToTask = task.attachedPdfs
                    return super.initialize(content)
                }
            }
        }else{
           if(pdfcontainerMap.entries.first().value is Set<*>){
               pdfsAttachedToTask = pdfcontainerMap.entries.first().value as  Set<PDFContainer>
               logger.trace("Fetching attached pdf form pdfContainer")
               return super.initialize(content)
           }
        }
        logger.error("No attached pdf provided")
        return super.initialize(content)
    }

    override fun onAfterPDFCreation(container: PDFContainer, creator: PDFCreator): PDFContainer {

        var addUReport: PDFContainer? = null
        for (pdf in pdfsAttachedToTask) {
            if (pdf.type == PrintDocumentType.U_REPORT_COMPLETED) {
                val createdOnNew = pdf.audit?.createdOn ?: 0
                val createdOnOld = addUReport?.audit?.createdOn ?: 0
                if (createdOnOld < createdOnNew) {
                    addUReport = pdf
                }
            }
        }

        if (addUReport != null) {
            logger.debug("Found case U-Report, attaching ")
            val result = PDFManipulator.mergePDFs(listOf(container, addUReport), container.isThumbnailPreset)
            SpringContextBridge.services().mediaRepository.saveBytes(result.pdfData, container.path)

            val thumbnailData = result.thumbnailData
            if (container.isThumbnailPreset && thumbnailData != null)
                SpringContextBridge.services().mediaRepository.saveBytes(thumbnailData, container.thumbnail)
        }

        return container
    }
}