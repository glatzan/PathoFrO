package com.patho.main.dialog.print.documentUi

import com.patho.main.template.DocumentToken
import com.patho.main.template.print.CaseCertificate

class CaseCertificateUi : AbstractTaskDocumentUi<CaseCertificate, AbstractDocumentUi.SharedData> {

    /**
     * True if the case certificate was printed
     */
    private var printed: Boolean = false

    constructor(caseCertificate: CaseCertificate) : this(caseCertificate, SharedData())

    constructor(caseCertificate: CaseCertificate, sharedData: SharedData) : super(caseCertificate, sharedData)

    /**
     * Resets the printed flag to false
     */
    override fun beginNextTemplateIteration() {
        printed = false
    }

    /**
     * Returns true if the case certificate was not printed jet
     */
    override fun hasNextTemplateConfiguration(): Boolean {
        return !printed
    }

    /**
     * Return default template configuration for printing
     */
    override fun getDefaultTemplateConfiguration(): AbstractDocumentUi.TemplateConfiguration<CaseCertificate> {
        printDocument.initialize(DocumentToken("task", task), DocumentToken("patient", task?.parent))
        return AbstractDocumentUi.TemplateConfiguration<CaseCertificate>(printDocument)
    }

    /**
     * Sets the data for the next print
     */
    override fun getNextTemplateConfiguration(): AbstractDocumentUi.TemplateConfiguration<CaseCertificate>? {
        printed = true
        return getDefaultTemplateConfiguration()
    }
}