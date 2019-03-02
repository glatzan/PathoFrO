package com.patho.main.util.pdf

import java.io.FileNotFoundException

/**
 * Exceptions is thrown if pdf file could not be created
 */
class PDFCreationFailedException : FileNotFoundException("PDF could not be created")