package com.patho.main.model.interfaces

import com.patho.main.config.PathoConfig
import com.patho.main.model.PDFContainer
import com.patho.main.template.PrintDocumentType
import java.io.File
import javax.persistence.Transient

interface DataList : ID, PatientAccessible {
    var attachedPdfs: MutableSet<PDFContainer>

    val publicName: String;

    open val fileRepositoryBase: File
        @Transient
        get() = File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + patient?.id)

    open fun containsReportType(type: PrintDocumentType): Boolean {
        return attachedPdfs.any { p -> p.type == type }
    }

    open fun addReport(pdfTemplate: PDFContainer) {
        attachedPdfs.add(pdfTemplate)
    }

    open fun removeReport(pdfTemplate: PDFContainer) {
        attachedPdfs.remove(pdfTemplate)
    }

    open fun containsReport(pdfTemplate: PDFContainer): Boolean {
        return attachedPdfs.contains(pdfTemplate)
    }
}