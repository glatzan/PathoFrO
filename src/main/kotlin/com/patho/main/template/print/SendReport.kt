package com.patho.main.template.print

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.util.pdf.creator.PDFCreator
import com.patho.main.util.pdf.creator.PDFManipulator
import com.patho.main.util.report.ui.ReportIntentNotificationUIContainer
import org.apache.velocity.context.Context
import java.util.*

/**
 * DiagnosisReport <br>
 * Values to replace: <br>
 * patient, <br>
 * task, <br>
 * diagnosisRevisions. <br>
 * useMail, <br>
 * mails, <br>
 * useFax, <br>
 * faxes, <br>
 * useLetter, <br>
 * letters, <br>
 * usePhone,<br>
 * phonenumbers, <br>
 * reportDate
 */
class SendReport(printDocument: PrintDocument) : PrintDocument(printDocument) {

    private var useMail: Boolean = false
    private var mails: List<ReportIntentNotificationUIContainer> = listOf()

    private var useFax: Boolean = false
    private var faxes: List<ReportIntentNotificationUIContainer> = listOf()

    private var useLetters: Boolean = false
    private var letters: List<ReportIntentNotificationUIContainer> = listOf()

    private var usePhone: Boolean = false
    private var phonenumbers: List<ReportIntentNotificationUIContainer> = listOf()

    init {
        afterPDFCreationHook = true
    }

    override fun initialize(content: HashMap<String, Any?>): Pair<out PrintDocument, Context> {
        content.forEach { p ->
            when (p.key) {
                "useMail" -> useMail = p.value as Boolean
                "mails" -> mails = p as List<ReportIntentNotificationUIContainer>
                "useFax" -> useFax = p.value as Boolean
                "faxes" -> faxes = p as List<ReportIntentNotificationUIContainer>
                "useLetter" -> useLetters = p.value as Boolean
                "letters" -> letters = p as List<ReportIntentNotificationUIContainer>
                "usePhone" -> usePhone = p.value as Boolean
                "phonenumbers" -> phonenumbers = p as List<ReportIntentNotificationUIContainer>
                else -> {
                }
            }
        }
        return super.initialize(content)
    }

    override fun onAfterPDFCreation(container: PDFContainer, creator: PDFCreator): PDFContainer {
        val attachPdf = ArrayList<PDFContainer>()

        attachPdf.add(container)

        if (useMail && mails.isEmpty())
            attachPdf.addAll(mails.filter { p -> p.pdf != null }.map { it.pdf!! })

        if (useFax && faxes.isEmpty())
            attachPdf.addAll(faxes.filter { p -> p.pdf != null }.map { it.pdf!! })

        if (useLetters && letters.isEmpty())
            attachPdf.addAll(letters.filter { p -> p.pdf != null }.map { it.pdf!! })

        if (usePhone && phonenumbers.isEmpty())
            attachPdf.addAll(phonenumbers.filter { p -> p.pdf != null }.map { it.pdf!! })

        val result = PDFManipulator.mergePDFs(attachPdf, container.isThumbnailPreset)

        SpringContextBridge.services().mediaRepository.saveBytes(result.pdfData, container.path)

        val thumbnailData = result.thumbnailData
        if (container.isThumbnailPreset && thumbnailData != null)
            SpringContextBridge.services().mediaRepository.saveBytes(thumbnailData, container.thumbnail)

        return container
    }
}