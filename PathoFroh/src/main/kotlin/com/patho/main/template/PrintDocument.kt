package com.patho.main.template

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.helper.TimeUtil
import com.patho.main.util.pdf.creator.PDFCreator
import org.apache.velocity.app.Velocity
import org.apache.velocity.context.Context
import java.io.StringWriter
import java.util.*

open class PrintDocument : AbstractTemplate {
    /**
     * Class of the ui Element
     */
    var uiClass: String = ""

    /**
     * Group of uis which share the same data context. If 0 there is no shared
     * group.
     */
    var sharedContextGroup: Int = 0

    /**
     * Raw file content from hdd
     */
    var loadedContent: String = ""

    /**
     * Processed final document
     */
    var finalContent: String = ""

    /**
     * If true the pdf generator will call onAfterPDFCreation to allow the template
     * to change or attach other things to the pdf
     */
    var afterPDFCreationHook: Boolean = false

    /**
     * If true duplex printing is used per default
     */
    var duplexPrinting: Boolean = false

    /**
     * Only in use if duplexPrinting is true. IF printEvenPageCounts is true a blank
     * page will be added if there is an odd number of pages to print.
     */
    var printEvenPageCount: Boolean = false

    /**
     * Returns the printDocument type as an enum object
     */
    val documentType
        get() = PrintDocumentType.fromString(type);

    /**
     * Returns a dynamic generated name for this document
     */
    val generatedFileName
        get() = "${SpringContextBridge.services().resourceBundle.get("enum.documentType.$type")}-${TimeUtil.formatDate(Date(), "dd.MM.yyyy")}.pdf"

    constructor()

    constructor(documentType: PrintDocumentType) {
        type = documentType.name
    }

    constructor(printDocument: PrintDocument) {
        copyIntoDocument(printDocument)
    }

    /**
     * Copies the values of the given printDocument into the current object
     */
    open fun copyIntoDocument(printDocument: PrintDocument) {
        super.copyIntoDocument(printDocument)
        uiClass = printDocument.uiClass
        sharedContextGroup = printDocument.sharedContextGroup
        loadedContent = printDocument.loadedContent
        afterPDFCreationHook = printDocument.afterPDFCreationHook
        duplexPrinting = printDocument.duplexPrinting
        printEvenPageCount = printDocument.printEvenPageCount
    }

    /**
     * Makes clone public
     */
    public open override fun clone(): Any {
        return super.clone()
    }

    /**
     * Initializes values for the velocity template engine
     */
    override fun initialize(vararg token: DocumentToken): Pair<PrintDocument, Context> {
        return initialize(hashMapOf(*token.map { it.key to it.value }.toTypedArray()))
    }

    /**
     * Initializes values for the velocity template engine. Generates a
     * final document version and saves this version into finalContent
     */
    override fun initialize(content: HashMap<String, Any?>): Pair<PrintDocument, Context> {
        val context = super.initialize(content).second

        /* now render the template into a StringWriter */
        val writer = StringWriter()

        Velocity.evaluate(context, writer, "test", loadedContent)

        finalContent = writer.toString()

        return Pair(this, context)
    }

    /**
     * Is called if afterPDFCreationHook is set and the pdf was created.
     */
    open fun onAfterPDFCreation(container: PDFContainer, creator: PDFCreator): PDFContainer {
        logger.trace("OnAfterCreationHook end")
        return container
    }

}