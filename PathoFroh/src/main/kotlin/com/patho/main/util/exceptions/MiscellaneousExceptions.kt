package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge
import java.io.FileNotFoundException

/**
 * Is thrown if a contact is about to be added twice to a task
 */
class DuplicatedReportIntentException : DialogException("Contact already in list",
        SpringContextBridge.services().resourceBundle["exceptions.reportintent.duplicated.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.reportintent.duplicated.info"])


/**
 *  Is thrown if the pdf template was not found
 */
class TemplateNotFoundException : DialogException("Template not found",
        SpringContextBridge.services().resourceBundle["exceptions.templateNotFound.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.templateNotFound.text"])

/**
 * Exceptions is thrown if pdf file could not be created
 */
class PDFCreationFailedException : DialogException("PDF could not be created",
        SpringContextBridge.services().resourceBundle["exceptions.pdfCreationFailed.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.pdfCreationFailed.text"])