package com.patho.main.config

import com.patho.main.repository.MediaRepository
import com.patho.main.util.version.Version
import com.patho.main.util.version.VersionContainer
import org.apache.commons.io.FileUtils
import org.apache.tomcat.jni.File
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.IOException
import javax.annotation.PostConstruct

public open class PathoConfig @Autowired constructor(
        var mediaRepository: MediaRepository) {

    companion object {
        @JvmStatic
        val PDF_NOT_FOUND_PDF = "classpath:templates/print/pdfnotfound.pdf"

        @JvmStatic
        val PDF_NOT_FOUND_IMG = "classpath:templates/print/pdfnotfound.png"

        @JvmStatic
        val REPORT_NOT_APPROVED_PDF = "classpath:templates/print/reportnotapproved.pdf"

        @JvmStatic
        val REPORT_NOT_APPROVED_IMG = "classpath:templates/print/reportnotapproved.png"

        @JvmStatic
        val RENDER_ERROR_PDF = "classpath:templates/print/pdfnotfound.pdf"

        @JvmStatic
        val LOGIN_PAGE = "/login.xhtml"
    }

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    var lateinit fileSettings: FileSettings

    @PostConstruct
    open fun initialize() {

        val fileRepository = File(fileSettings.getFileRepository())

        // checking directories
        if (!fileRepository.isDirectory() && !fileRepository.mkdirs()) {
            logger.error("Error directory not found: fileRepository " + fileRepository.getAbsolutePath())
        }

        val workDirectory = mediaRepository.getWriteFile(fileSettings.getWorkDirectory())

        // checking directories
        if (!workDirectory.isDirectory() && !workDirectory.mkdirs()) {
            logger.error("Error directory not found: workDirectory " + workDirectory.getAbsolutePath())
        }

        val auxDirectory = mediaRepository.getWriteFile(fileSettings.getAuxDirectory())

        // checking directories
        if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
            logger.error("Error directory not found: auxDirectory " + auxDirectory.getAbsolutePath())
        }

        val errorDirectory = mediaRepository.getWriteFile(fileSettings.getErrorDirectory())

        // checking directories
        if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
            logger.error("Error directory not found: errorDirectory " + errorDirectory.getAbsolutePath())
        }

        val printDirectory = mediaRepository.getWriteFile(fileSettings.getPrintDirectory())

        // checking directories
        if (!printDirectory.isDirectory() && !printDirectory.mkdirs()) {
            logger.error("Error directory not found: errorDirectory " + printDirectory.getAbsolutePath())
        }

        logger.debug("Copying files to working directory...")
        for (copyStr in fileSettings.getCopyFromClasspathToWorkDirectory()) {
            logger.debug("Copying files from '$copyStr' to working directory...")
            val printResouces = mediaRepository.getFilesOfDirectory(copyStr)

            for (i in printResouces.indices) {
                try {
                    FileUtils.copyFileToDirectory(printResouces[i], workDirectory)
                    logger.debug("Copying... " + printResouces[i].getName())
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        // loading versions
        val versions = Version.factroy(mediaRepository.getStrings(fileSettings.getProgramVersionInfo()))
        setVersionContainer(VersionContainer(versions))

        // setting current version
        if (versions != null && versions.size > 0) {
            getVersionContainer().setCurrentVersion(versions[0].version)
        }

        if (File(fileSettings.getProgramInfo()).exists()) {
            val programVersionJson = mediaRepository.getString(fileSettings.getProgramInfo())
        } else {
            logger.debug("First program start")
        }
    }


    class FileSettings {

        var lateinit  fileRepository: String

        var lateinit  workDirectory: String

        var lateinit auxDirectory: String

        var lateinit errorDirectory: String

        var lateinit printDirectory: String

        var lateinit programInfo: String? = null

        var lateinit programVersionInfo: String? = null

        var lateinit copyFromClasspathToWorkDirectory: Array<String>? = null

        var lateinit thumbnailDPI: Int = 0

        var lateinit cleanup: Boolean = false

        var lateinit keepErrorFiles: Boolean = false

        companion object {
            @JvmStatic
            val FILE_REPOSITORY_PATH_TOKEN = "fileRepository:"
        }
    }
}