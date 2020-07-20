package com.patho.main.dialog.print.documentUi

import com.patho.main.model.patient.Task
import com.patho.main.template.DocumentToken
import com.patho.main.template.PrintDocument

/**
 * Ui class for task associated printDocuments
 */
abstract class AbstractTaskDocumentUi<T : PrintDocument, S : AbstractDocumentUi.SharedData>(printDocument: T, sharedData: S) : AbstractDocumentUi<T, S>(printDocument, sharedData) {
    /**
     * Task
     */
    lateinit var task: Task

    /**
     * Initializes the documentUI
     */
    open fun initialize(task: Task) {
        this.task = task
        super.initialize()
    }

    /**
     * Returns a default template configuration for rendering in the gui
     */
    override fun getDefaultTemplateConfiguration(): TemplateConfiguration<T> {
        printDocument.initialize(DocumentToken("task", task))
        return super.getDefaultTemplateConfiguration()
    }
}