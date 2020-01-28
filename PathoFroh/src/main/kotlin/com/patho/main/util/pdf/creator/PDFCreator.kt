package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.template.PrintDocument
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


open class PDFCreator(val workDirectory: String = services().pathoConfig.fileSettings.workDirectory) {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private val newline = System.getProperty("line.separator")
    }


    val targetDirectory: String = workDirectory
    val auxDirectory: String = services().pathoConfig.fileSettings.auxDirectory
    val errorDirectory: String = services().pathoConfig.fileSettings.errorDirectory


    /**
     * Creates a new pdf.  Blocks the program until the pdf was created.
     */
    open fun create(template: PrintDocument, targetDirectory: String = this.targetDirectory, createThumbnail: Boolean = false): PDFContainer {
        var container = services().pdfService.getUniquePDF(targetDirectory, createThumbnail)
        container.type = template.documentType
        container.name = template.generatedFileName

        //TODO Handle Error
        var success = false
        try {
            success = runPDFCreation(template.finalContent, createThumbnail, container, workDirectory, auxDirectory, targetDirectory, errorDirectory)
            if (template.afterPDFCreationHook)
                container = template.onAfterPDFCreation(container, this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return container
    }

    /**
     * Runs the pdf creation with all necessary checks
     */
    protected fun runPDFCreation(content: String, createThumbnail: Boolean, pdfContainer: PDFContainer, workDirectory: String, auxDirectory: String, targetDirectory: String, errorDirectory: String): Boolean {
        val supporter = PDFLatexSupporter()

        try {
            // validate environment
            if (!supporter.validateEnvironment(workDirectory, auxDirectory, targetDirectory, errorDirectory))
                throw IOException("Could not create environment")

            // creating template files
            if (!supporter.initializeTmpFiles(pdfContainer, workDirectory, auxDirectory))
                throw IOException("Could not create temp files")

            // load input tex
            if (!supporter.initializeInputFile(content))
                throw IOException("Could not create Input files")

            // create pdf
            if (!runPDFLatex(supporter, workDirectory, auxDirectory))
                throw IOException("Could not create PDF")

            // validate the output
            if (!supporter.validatePDFOutput())
                throw IOException("PDF Output incorrect or not found")

            // creat thumbnail
            if (createThumbnail) {
                if (!runThumbnailCreation(supporter))
                    throw IOException("Could not create Thumbnail")

                if (!supporter.validatePDFOutput())
                    throw IOException("Thumbnail Output incorrect or not found")
            }

            // moving files to final location, this is always needed, because the  files used for pdf creation
            // are always other files
            if (!supporter.copyOutputFilesToTargetLocation()) {
                throw IOException("Could not move PDF oder Thumbnail File to target directory")
            }

            // cleaning up
            if (services().pathoConfig.fileSettings.cleanup) {
                supporter.cleanUp(false, errorDirectory)
                logger.debug("Cleanup Completed")
            }
            return true
        } catch (e: IOException) {
            logger.debug("Error while creating a pdf")
            supporter.cleanUp(true, errorDirectory)
            throw e
        }
    }

    /**
     * Executes the latex process
     */
    protected fun runPDFLatex(supporter: PDFLatexSupporter, workDirectory: String, auxDirectory: String, pdfLatexCommand: String = "pdflatex"): Boolean {
        val parameter1 = pdfLatexCommand
        val parameter2 = "--interaction=nonstopmode"
        val parameter3 = "--output-directory=${services().mediaRepository.getFileForPath(workDirectory).absolutePath}"
        val parameter4 = "--aux-directory=${services().mediaRepository.getFileForPath(auxDirectory).absolutePath}"
        val parameter5 = services().mediaRepository.getFileForPath(supporter.inputFile).absolutePath

        val localProcessBuilder = ProcessBuilder(*arrayOf(parameter1, parameter2, parameter3, parameter4, parameter5))
        localProcessBuilder.redirectErrorStream(true)
        localProcessBuilder.directory(services().mediaRepository.getFileForPath(workDirectory))

        logger.debug("Running pdflatex ${parameter1}, ${parameter2} , ${parameter3}, ${parameter4}, ${parameter5} ")
        val loops = 2

        for (i in 1..loops) {
            val localProcess = localProcessBuilder.start()
            val localInputStreamReader = InputStreamReader(localProcess.inputStream)
            val localStringBuilder = StringBuilder()

            BufferedReader(localInputStreamReader).use { localBufferedReader ->
                var res: String? = null
                while (localBufferedReader.readLine().also { res = it } != null) {
                    localStringBuilder.append(res + PDFCreator.newline)
                }
            }

            supporter.resultMessage = localStringBuilder.toString()

            try {
                val j = localProcess.waitFor()

                if (j != 0) {
                    supporter.errorMessage = "Errors occurred while executing pdfLaTeX! Exit value of the process: $j"
                }

            } catch (localInterruptedException: InterruptedException) {
                supporter.errorMessage = ("The process pdfLaTeX was interrupted and an exception occurred!" + PDFCreator.newline
                        + localInterruptedException.toString())
            }
        }

        return true
    }

    /**
     * Executes the thumbnail creation process
     */
    protected fun runThumbnailCreation(supporter: PDFLatexSupporter): Boolean {
        logger.debug("Creating thumbnail ${supporter.tmpOutputThumbnail} ")
        services().pdfService.generateAndSaveThumbnail( supporter.tmpOutputFile, supporter.tmpOutputThumbnail,0,
                services().pathoConfig.fileSettings.thumbnailDPI)
        return true
    }

}