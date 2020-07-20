package com.patho.main.config

import com.patho.main.common.ContactRole
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.config.VersionContainer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
@ConfigurationProperties(prefix = "patho.settings")
public open class PathoConfig @Autowired @Lazy constructor(
        var mediaRepository: MediaRepository) {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    var fileSettings: FileSettings = FileSettings()

    var defaultNotification: DefaultNotification = DefaultNotification()

    var defaultDocuments: DefaultDocuments = DefaultDocuments()

    var miscellaneous: Miscellaneous = Miscellaneous()

    var schedule: Schedule = Schedule()

    var mailSettings: MailSettings = MailSettings()

    var pdfErrorFiles: PDFErrorFiles = PDFErrorFiles()

    var externalDocumentMapper: MutableList<ExternalDocumentMapper> = mutableListOf<ExternalDocumentMapper>()

//    init {
//        fileSettings = FileSettings();
//    }

    /**
     * Container for providing version information
     */
    var versionContainer: VersionContainer = VersionContainer()

    open fun initialize() {

        logger.debug("Initialize patho config ")

        val fileRepository = mediaRepository.getFileForPath(fileSettings.fileRepository)

        // checking directories
        if (!fileRepository.isDirectory && !fileRepository.mkdirs()) {
            logger.error("Error directory not found: fileRepository " + fileRepository.absolutePath)
        } else
            logger.debug("Directory found: fileRepository " + fileRepository.absolutePath)

        val workDirectory = mediaRepository.getFileForPath(fileSettings.workDirectory)

        // checking directories
        if (!workDirectory.isDirectory && !workDirectory.mkdirs()) {
            logger.error("Error directory not found: workDirectory " + workDirectory.absolutePath)
        } else
            logger.debug("Directory found: workDirectory " + workDirectory.absolutePath)

        val auxDirectory = mediaRepository.getFileForPath(fileSettings.auxDirectory)

        // checking directories
        if (!auxDirectory.isDirectory && !auxDirectory.mkdirs()) {
            logger.error("Error directory not found: auxDirectory " + auxDirectory.absolutePath)
        } else
            logger.debug("Directory found: auxDirectory " + auxDirectory.absolutePath)

        val errorDirectory = mediaRepository.getFileForPath(fileSettings.errorDirectory)

        // checking directories
        if (!errorDirectory.isDirectory && !errorDirectory.mkdirs()) {
            logger.error("Error directory not found: errorDirectory " + errorDirectory.absolutePath)
        } else
            logger.debug("Directory found: errorDirectory " + errorDirectory.absolutePath)

        val printDirectory = mediaRepository.getFileForPath(fileSettings.printDirectory)

        // checking directories
        if (!printDirectory.isDirectory && !printDirectory.mkdirs()) {
            logger.error("Error directory not found: printDirectory " + printDirectory.absolutePath)
        } else
            logger.debug("Directory found: printDirectory " + printDirectory.absolutePath)


        val programInfo = mediaRepository.getFileForPath(fileSettings.programInfo)

        // copy resources to working dir
        if (!programInfo.isFile) {
            logger.debug("###########################################")
            logger.debug("###########################################")
            logger.debug("############### First start ###############")
            logger.debug("###########################################")

            logger.debug("Copying files to working directory...")

            val copyRes = mediaRepository.getFilesOfDirectory("classpath:*");

            //TODO read from jar
            for (srcFile in copyRes) {
                logger.debug("Classpath... " + File(srcFile.filename).path + " " + File(srcFile.filename).absolutePath)
            }

            logger.debug("Classpath End")

            for (copyStr in fileSettings.copyOnFirstStartToWorkDirectory) {
                val targetDir = mediaRepository.getResource(copyStr.target).file
                if (!targetDir.isDirectory && !targetDir.mkdirs()) {
                    logger.error("Error directory not found: errorDirectory " + targetDir.absolutePath)
                } else {
                    val copyRes = mediaRepository.getFilesOfDirectory(copyStr.source)

                    logger.debug("Found... ${copyRes.size} in ${copyStr.source} ")
                    logger.debug("Source ${copyStr.source}, Target ${copyStr.target}")

                    for (srcFile in copyRes) {
                        logger.debug("Copying... " + File(targetDir, srcFile.filename).path)
                        mediaRepository.copyResourceToFile(srcFile, File(targetDir, srcFile.filename))
                    }
                }

                mediaRepository.getResource(copyStr.target).file.mkdir()
            }

            // marking als initialized
            mediaRepository.saveString("initilized", fileSettings.programInfo)
        }

        // loading versions
        versionContainer = VersionContainer(mediaRepository.getStrings(fileSettings.programVersionInfo))
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

        var copyOnFirstStartToWorkDirectory: Array<DirPair> = arrayOf<DirPair>()

        var thumbnailDPI: Int = 0

        var cleanup: Boolean = false

        var keepErrorFiles: Boolean = false

        companion object {
            @JvmStatic
            val FILE_REPOSITORY_PATH_TOKEN = "fileRepository:"
        }

        class DirPair {
            var source: String = ""
            var target: String = ""
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

        /**
         * Default email for rest upload error
         */
        var restUploadErrorEmail: Long = 0

        /**
         * Default document for creating patient
         */
        var createPDVPatientRequestDocument: Long = 0

        /**
         * Id of the email which should be used to inform the admins of pdv patient creation
         */
        var createPDVPatientStatusMail: Long = 0

        /**
         * Id of the email which is send if a scanned slide could not be match to a slide in the database
         */
        var scannedSlidesAdded : Long = 0
    }

    class ProgramInfo {
        var version: String? = null
    }

    class Schedule {
        var pdfCleanupCron: String? = null
    }

    class Miscellaneous {

        lateinit var phoneRegex: String

        /**
         * If true the admin will be notified for every Patient that is created in the pdv
         */
        var noticeAdminOnPDVPatientCreation: Boolean = false

        /**
         * If true the admin will be notified for every scanned slide that is added
         */
        var noticeAdminOnScannedSlideAdded: Boolean = false
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

    class PDFErrorFiles {
        var pdfNotFoundPDF = ""
        var pdfNotFoundIMG = ""
        var reportNotApprovedPDF = ""
        var reportNotApprovedIMG = ""
        var renderErrorPDF = ""
    }

    class ExternalDocumentMapper {
        lateinit var externalIdentifier: String
        lateinit var internalIdentifier: PrintDocumentType
    }

}