package com.patho.main.config

import com.patho.main.common.ContactRole
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.repository.MediaRepository
import com.patho.main.util.config.VersionContainer
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties(prefix = "patho.settings")
public open class PathoConfig @Autowired @Lazy constructor(
        var mediaRepository: MediaRepository) {

    companion object {
        val PDF_NOT_FOUND_PDF = "classpath:templates/print/pdfnotfound.pdf"

        val PDF_NOT_FOUND_IMG = "classpath:templates/print/pdfnotfound.png"

        val REPORT_NOT_APPROVED_PDF = "classpath:templates/print/reportnotapproved.pdf"

        val REPORT_NOT_APPROVED_IMG = "classpath:templates/print/reportnotapproved.png"

        val RENDER_ERROR_PDF = "classpath:templates/print/pdfnotfound.pdf"

        val LOGIN_PAGE = "/login.xhtml"
    }

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    var fileSettings: FileSettings = FileSettings()

    var defaultNotification: DefaultNotification = DefaultNotification()

    var defaultDocuments: DefaultDocuments = DefaultDocuments()

    var miscellaneous: Miscellaneous = Miscellaneous()

    var schedule: Schedule = Schedule()

    var mailSettings: MailSettings = MailSettings()

    /**
     * Container for providing version information
     */
    var versionContainer: VersionContainer = VersionContainer()

    @PostConstruct
    open fun initialize() {

        val fileRepository = File(fileSettings.fileRepository)

        // checking directories
        if (!fileRepository.isDirectory() && !fileRepository.mkdirs()) {
            logger.error("Error directory not found: fileRepository " + fileRepository.getAbsolutePath())
        }

        val workDirectory = mediaRepository.getWriteFile(fileSettings.workDirectory)

        // checking directories
        if (!workDirectory.isDirectory() && !workDirectory.mkdirs()) {
            logger.error("Error directory not found: workDirectory " + workDirectory.getAbsolutePath())
        }

        val auxDirectory = mediaRepository.getWriteFile(fileSettings.auxDirectory)

        // checking directories
        if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
            logger.error("Error directory not found: auxDirectory " + auxDirectory.getAbsolutePath())
        }

        val errorDirectory = mediaRepository.getWriteFile(fileSettings.errorDirectory)

        // checking directories
        if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
            logger.error("Error directory not found: errorDirectory " + errorDirectory.getAbsolutePath())
        }

        val printDirectory = mediaRepository.getWriteFile(fileSettings.printDirectory)

        // checking directories
        if (!printDirectory.isDirectory() && !printDirectory.mkdirs()) {
            logger.error("Error directory not found: errorDirectory " + printDirectory.getAbsolutePath())
        }

        logger.debug("Copying files to working directory...")
        for (copyStr in fileSettings.copyFromClasspathToWorkDirectory) {
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
        versionContainer = VersionContainer(mediaRepository.getStrings(fileSettings.programVersionInfo))

        if (File(fileSettings.programInfo).exists()) {
            val programVersionJson = mediaRepository.getString(fileSettings.programInfo)
        } else {
            logger.debug("First program start")
        }
    }

    /**
     * File settings container
     */
    class FileSettings {

        var fileRepository: String = ""

        var workDirectory: String = ""

        var auxDirectory: String = ""

        var errorDirectory: String = ""

        var printDirectory: String = ""

        var programInfo: String = ""

        var programVersionInfo: String = ""

        var copyFromClasspathToWorkDirectory: Array<String> = arrayOf<String>()

        var thumbnailDPI: Int = 0

        var cleanup: Boolean = false

        var keepErrorFiles: Boolean = false

        companion object {
            @JvmStatic
            val FILE_REPOSITORY_PATH_TOKEN = "fileRepository:"
        }
    }

    /**
     * Mail settings
     */
    class MailSettings {
        /**
         * Server IP
         */
        var server: String = ""

        /**
         * Server Port
         */
        var port: Int = 0

        /**
         * SLL
         */
        var ssl: Boolean = false

        var debug: Boolean = false

        var systemMail: String = ""

        var systemName: String = ""

        var adminAddresses: Array<String> = arrayOf<String>()

        var errorAddresses: Array<String> = arrayOf<String>()
    }

    class DefaultDocuments {

        /**
         * Document-Template which is used on reportIntent phase exit.
         */
        var diagnosisApprovedDocument: Long = 0

        /**
         * Document which can be printed on task creation.
         */
        var taskCreationDocument: Long = 0

        /**
         * Default document for email notification
         */
        var notificationDefaultEmailDocument: Long = 0

        /**
         * Default Email template which is used to notify physicians if task was
         * completed
         */
        var notificationDefaultEmail: Long = 0

        /**
         * Default document for fax notification
         */
        var notificationDefaultFaxDocument: Long = 0

        /**
         * Default document for letter notification
         */
        var notificationDefaultLetterDocument: Long = 0

        /**
         * Default document for printing in order to sign
         */
        var notificationDefaultPrintDocument: Long = 0

        /**
         * Sendreport which is created after the notification dialog was processed.
         */
        var notificationSendReport: Long = 0

        /**
         * ID of the default slide label
         */
        var slideLabelDocument: Long = 0

        /**
         * Template for reportIntent report for program users
         */
        var diagnosisReportForUsers: Long = 0

        /**
         * ID of the testlabel for slide printing
         */
        var slideLableTestDocument: Long = 0

        /**
         * ID of the test page for document printing
         */
        var printerTestDocument: Long = 0

        /**
         * Path of one empty pdf page
         */
        var emptyPage: String? = null

        /**
         * Testpage for label printer
         */
        var lablePrinterTestPage: String? = null

        /**
         * Test page for cups printer
         */
        var cupsPrinterTestPage: String? = null
    }

    class ProgramInfo {
        var version: String? = null
    }

    class Schedule {
        var pdfCleanupCron: String? = null
    }

    class Miscellaneous {
        var phoneRegex: String = ""
    }

    class DefaultNotification {

        var defaultNotifications: List<DefaultNotificationEntity> = listOf<DefaultNotificationEntity>()

        fun getDefaultNotificationForRole(role: ContactRole): List<NotificationTyp> {
            var tmp = defaultNotifications
            try {
                return tmp.singleOrNull { p -> p.role == role }?.notificationTyps ?: listOf()
            } catch (e: IllegalStateException) {
                return ArrayList()
            }
        }
    }

    class DefaultNotificationEntity {
        var role: ContactRole? = null
        var notificationTyps: List<NotificationTyp> = listOf<NotificationTyp>()

        constructor()

        constructor(role: ContactRole) {
            this.role = role
        }
    }


}