package com.patho.main.util.pdf.creator

import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

/**
 * Supporter Class fort pdf creation
 */
class PDFLatexSupporter() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    lateinit var inputFile: String
    lateinit var auxFile: String
    lateinit var logFile: String
    lateinit var tmpOutputFile: String
    lateinit var tmpOutputThumbnail: String

    lateinit var errorMessage: String
    lateinit var resultMessage: String

    /**
     * Target outpuFile always differs from tmpOuputfile
     */
    lateinit var targetOutputFile: String

    /**
     * Target thumbnailFile always differs from tmpThumbnailFile
     */
    lateinit var targetOutputThumbnail: String

    /**
     * Sets all temp files for pdf creation
     */
    public fun initializeTmpFiles(pdfContainer: PDFContainer, workingDirectory: String, auxDirectory: String): Boolean {
        logger.debug("Setting up temporary files")

        val createName: String = services().mediaRepository.getUniqueName(workingDirectory, ".tex")
        inputFile = File(workingDirectory, createName).path
        auxFile = File(auxDirectory, createName.replace(".tex", ".aux")).path
        logFile = File(auxDirectory, createName.replace(".tex", ".log")).path
        tmpOutputFile = File(workingDirectory, createName.replace(".tex", ".pdf")).path
        tmpOutputThumbnail = File(workingDirectory, createName.replace(".tex", ".png")).path

        targetOutputFile = pdfContainer.path
        targetOutputThumbnail = pdfContainer.thumbnail

        return true
    }

    /**
     * Saves the latex content to the input file
     */
    public fun initializeInputFile(input: String): Boolean {
        SpringContextBridge.services().mediaRepository.saveString(input, inputFile)
        return services().mediaRepository.isFile(inputFile)
    }

    /**
     * Copies the tmp files to theire final location
     */
    public fun copyOutputFilesToTargetLocation(): Boolean {
        logger.debug("Copy to target dest: ${services().mediaRepository.getFileForPath(targetOutputFile).path}")
        if (!services().mediaRepository.copyFile(tmpOutputFile, targetOutputFile))
            return false

        if (services().mediaRepository.isFile(tmpOutputThumbnail) && !services().mediaRepository.copyFile(tmpOutputThumbnail, targetOutputThumbnail))
            return false
        return true
    }

    /**
     * Deletes leftover tmp files. If error is true the files eventually created are also deleted.
     */
    public fun cleanUp(error: Boolean, errorDirectory: String) {
        if (!error || (error && !services().pathoConfig.fileSettings.keepErrorFiles)) {
            services().mediaRepository.delete(inputFile)
            services().mediaRepository.delete(auxFile)
            services().mediaRepository.delete(logFile)
            services().mediaRepository.delete(tmpOutputFile)
            if (services().mediaRepository.isFile(tmpOutputThumbnail))
                services().mediaRepository.delete(tmpOutputThumbnail)

            if (error) {
                logger.debug("Deleting files on error")
                services().mediaRepository.delete(targetOutputFile)
                services().mediaRepository.delete(targetOutputThumbnail)
            }
        } else {
            logger.debug("Moving files to error directory")
            services().mediaRepository.moveFileToDirectory(inputFile, errorDirectory)
            services().mediaRepository.moveFileToDirectory(auxFile, errorDirectory)
            services().mediaRepository.moveFileToDirectory(logFile, errorDirectory)
            services().mediaRepository.moveFileToDirectory(tmpOutputFile, errorDirectory)
            services().mediaRepository.moveFileToDirectory(tmpOutputThumbnail, errorDirectory)
            services().mediaRepository.moveFileToDirectory(targetOutputFile, errorDirectory)
            services().mediaRepository.moveFileToDirectory(targetOutputThumbnail, errorDirectory)
        }
    }

    /**
     * Validates the output of the pdf creation
     */
    public fun validatePDFOutput(): Boolean {
        var result = true

        if (!services().mediaRepository.isFile(auxFile)) {
            result = false;
            logger.error("aux file not Found! ${services().mediaRepository.getFileForPath(auxFile).absolutePath}")
        }

        if (!services().mediaRepository.isFile(logFile)) {
            result = false;
            logger.error("log file not Found! ${services().mediaRepository.getFileForPath(logFile).absolutePath}")
        }

        if (!services().mediaRepository.isFile(tmpOutputFile)) {
            result = false;
            logger.error("output file not Found! ${services().mediaRepository.getFileForPath(tmpOutputFile).absolutePath}")
        }

        return result
    }

    /**
     * Validates the output of the thumbnail
     */
    public fun validateThumbnailOutput(): Boolean {
        return services().mediaRepository.isFile(tmpOutputThumbnail)
    }

    /**
     * Validates if needed directory are present, if not they will be created
     */
    public fun validateEnvironment(workingDirectory: String, auxDirectory: String, targetDirectory: String, errorDirectory: String): Boolean {

        if (!SpringContextBridge.services().mediaRepository.isDirectoryCreateIfNotPresent(workingDirectory, true)) {
            logger.error("Error directory not found: work directory $workingDirectory")
            throw FileNotFoundException("Error directory not found: work directory $workingDirectory")
        }

        if (!SpringContextBridge.services().mediaRepository.isDirectoryCreateIfNotPresent(auxDirectory, true)) {
            logger.error("Error directory not found: aux directory $auxDirectory")
            throw FileNotFoundException("Error directory not found: aux directory $auxDirectory")
        }

        if (!SpringContextBridge.services().mediaRepository.isDirectoryCreateIfNotPresent(targetDirectory, true)) {
            logger.error("Error directory not found: output directory $targetDirectory")
            throw FileNotFoundException("Error directory not found: output directory $targetDirectory")
        }

        if (!SpringContextBridge.services().mediaRepository.isDirectoryCreateIfNotPresent(errorDirectory, true)) {
            logger.error("Error directory not found: error directory $errorDirectory")
            throw FileNotFoundException("Error directory not found: error directory $errorDirectory")
        }

        return true
    }

    /**
     * Prints all path variables
     */
    fun printToLog() {
        logger.debug("InputFile: $inputFile")
        logger.debug("AuxFile: $auxFile")
        logger.debug("LogFile: $logFile")
        logger.debug("TmpOutputFile $tmpOutputFile")
        logger.debug("TmpOutputThumbnail $targetOutputThumbnail")
        logger.debug("TargetOutputFile $tmpOutputFile")
        logger.debug("TargetOutputThumbnail $targetOutputThumbnail")
    }
}