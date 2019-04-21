package com.patho.main.service

import com.patho.main.action.UserHandlerAction
import com.patho.main.config.PathoConfig
import com.patho.main.model.patient.Slide
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.template.InitializeToken
import com.patho.main.util.helper.HistoUtil
import com.patho.main.util.print.UnknownPrintingException
import com.patho.main.util.templates.TemplateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*


/**
 * Service for printing lables and other documents
 */
@Service()
open class PrintExecutorService @Autowired constructor(
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val userHandlerAction: UserHandlerAction) : AbstractService() {

    /**
     * Prints all slides, uses the default label template
     */
    fun printLabel(vararg slides: Slide) {
        try {
            val template = printDocumentRepository.findByID(pathoConfig.defaultDocuments.slideLabelDocument).get()

            val result: List<String> = slides.map { p ->
                template.initialize(InitializeToken("slide", p), InitializeToken("date", Date()),
                        InitializeToken("uniqueID",
                                p.task!!.taskID + "" + HistoUtil.fitString(p.uniqueIDinTask, 3, '0'))).finalContent
            }

            printLabel(*result.toTypedArray())
        } catch (e: NoSuchElementException) {
            throw TemplateNotFoundException("ID: ${pathoConfig.defaultDocuments.slideLabelDocument}")
        }
    }

    /**
     * Prints all passed command strings
     */
    fun printLabel(vararg commandStrings: String) {
        try {
            logger.debug("Printing labels..")
            userHandlerAction.selectedLabelPrinter.print(*commandStrings)
        } catch (e: Exception) {
            logger.debug("Printing labels.., Error")
            throw UnknownPrintingException()
        }
    }
}