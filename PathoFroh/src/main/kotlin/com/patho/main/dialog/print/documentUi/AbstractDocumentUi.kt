package com.patho.main.dialog.print.documentUi

import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.template.PrintDocument
import org.slf4j.LoggerFactory
import java.io.Serializable
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.dialog.print.documentUi.AbstractDocumentUi.SharedData
import com.patho.main.template.PrintDocument
import java.util.ArrayList
import java.util.HashMap



/**
 * UI class for displaying individual template settings to the user
 */
open class AbstractDocumentUi<T : PrintDocument, S : AbstractDocumentUi.SharedData>(val printDocument: T, val sharedData: S) : ID, Serializable {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * String for input include
     */
    var inputInclude = "include/empty.xhtml"

    /**
     * Returns the printDocument ID as id
     */
    override var id: Long
        get() = printDocument.id
        set(value) {}

    /**
     * Initializes the documentUI
     */
    open fun initialize() {
    }

    /**
     * Starts an new iteration through all template configuration
     */
    open fun beginNextTemplateIteration() {}

    /**
     * Returns true if a template configuration is left
     */
    open fun hasNextTemplateConfiguration(): Boolean {
        return false
    }

    /**
     * Returns the next template configuration
     */
    open fun getNextTemplateConfiguration(): TemplateConfiguration<T>? {
        return null
    }

    /**
     * Returns a default template configuration for rendering in the gui
     */
    open fun getDefaultTemplateConfiguration(): TemplateConfiguration<T> {
        return TemplateConfiguration<T>(printDocument)
    }

    /**
     * Returns the first contact which is selected, for templates without contacts
     * null is returned
     */
    open fun getFirstSelectedContact(): ReportIntent? {
        return null
    }

    /**
     * Loads a gui object for a print template
     */
    fun factory(document: PrintDocument): AbstractDocumentUi<*, *> {
        return factory(Arrays.asList(document)).get(0)
    }

    /**
     * Loads for a list of print templates all ui objects, if a sharedData context
     * exists the data will be shared between objects.
     */
    fun factory(documents: List<PrintDocument>): List<AbstractDocumentUi<*, *>> {
        val sharedGroups = HashMap<Int, SharedData>()

        val result = ArrayList<AbstractDocumentUi<*, *>>(documents.size)

        for (printDocument in documents) {
            // shared group exsist
            if (printDocument.sharedContextGroup != 0) {
                val sharedData = sharedGroups[printDocument.sharedContextGroup]

                if (sharedData != null) {
                    result.add(PrintDocumentRepository.loadUiClass(printDocument, sharedData))
                } else {
                    val tmp = PrintDocumentRepository.loadUiClass(printDocument)
                    sharedGroups[printDocument.sharedContextGroup] = tmp.sharedData
                    result.add(tmp)
                }
            } else {
                result.add(PrintDocumentRepository.loadUiClass(printDocument))
            }
        }

        return result
    }

    /**
     * Return container for generated template
     */
    class TemplateConfiguration<I : PrintDocument> {
        private var documentTemplate: I
        private var contact: ReportIntent?
        private var address: String
        private var copies: Int = 0

        constructor(documentTemplate: I) : this(documentTemplate, null, "", 1)

        constructor(documentTemplate: I, contact: ReportIntent?, address: String, copies: Int) {
            this.documentTemplate = documentTemplate
            this.contact = contact
            this.address = address
            this.copies = copies
        }
    }

    /**
     * Data class which can be share between AbstractDocumentUi.
     */
    open class SharedData {

        var initialized: Boolean = false

        fun initialize(): Boolean {
            if (initialized)
                return true
            else {
                initialized = true
                return false
            }

        }

        fun getSelectedContact(): ReportIntent? {
            return null
        }

    }
}