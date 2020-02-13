package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge

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

/**
 * Exception is thrown if pdf merge failed
 */
class PDFMergeException : DialogException("PDF could not merged",
        SpringContextBridge.services().resourceBundle["exceptions.pdfMergeException.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.pdfMergeException.text"])

/**
 * Exception is thrown if thumbnail could not be created
 */
class ThumbnailCreateException : DialogException("Thumbnail could not be created",
        SpringContextBridge.services().resourceBundle["exceptions.pdfMergeException.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.pdfMergeException.text"])

/**
 * Exception is thrown if a database entity was not found
 */
class EntityNotFoundException : DialogException("Entity could not be found",
        SpringContextBridge.services().resourceBundle["exceptions.entityNotFount.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.entityNotFount.text"])

/**
 * Exception is thrown if entity could not be saved to the database
 */
class EntityPersistException : DialogException("Entity could not be saved",
        SpringContextBridge.services().resourceBundle["exceptions.entityPersistError.headline"]
        , SpringContextBridge.services().resourceBundle["exceptions.entityPersistError.text"])