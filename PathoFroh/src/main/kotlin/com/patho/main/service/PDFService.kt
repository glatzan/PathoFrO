package com.patho.main.service

import com.patho.main.model.PDFContainer
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.BioBank
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.repository.jpa.*
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.util.pdf.LazyPDFReturnHandler
import com.patho.main.util.pdf.creator.PDFUpdater
import com.patho.main.util.pdf.creator.PDFUpdaterNonBlocking
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO


@Service()
open class PDFService @Autowired constructor(
        private val patientRepository: PatientRepository,
        private val taskRepository: TaskRepository,
        private val bioBankRepository: BioBankRepository,
        private val councilRepository: CouncilRepository,
        private val pdfRepository: PDFRepository,
        private val mediaRepository: MediaRepository) : AbstractService() {

    companion object {
        /**
         * Search a list of datalist for the list containing the pdf. Returns null if not found
         */
        @JvmStatic
        fun getParentDatalistOfPDF(dataLists: List<DataList>, container: PDFContainer): DataList? {
            for (dataList in dataLists) {
                for (pContainer in dataList.attachedPdfs) {
                    if (pContainer.equals(container)) return dataList
                }
            }
            return null
        }

        /**
         * Searches for a pdf matching the diagnosis number, determine by internal id
         */
        @JvmStatic
        fun findDiagnosisReport(task: Task, diagnosisRevision: DiagnosisRevision): PDFContainer? {
            val matcher = PDFContainer.MARKER_DIAGNOSIS.replace("\$id", diagnosisRevision.id.toString())
            for (container in task.attachedPdfs) {
                if (container.intern.matches(Regex(matcher))) {
                    return container
                }
            }
            return null
        }

        /**
         * Search for a datalist in list of datalists.This is to find the datalist with the current version.
         */
        @JvmStatic
        fun findDataList(dataLists: List<DataList>, find: DataList): DataList? {
            return dataLists.firstOrNull { it.id == find.id && it.javaClass == find.javaClass }
        }

        /**
         * Returns a flat list of all datalists of a patient
         */
        @JvmStatic
        fun flatListOfDataLists(p: Patient): List<DataList>? {
            val result: MutableList<DataList> = ArrayList()
            result.add(p)
            p.tasks.forEach { t ->
                result.add(t)

                val bio: Optional<BioBank> = SpringContextBridge.services().bioBankRepository.findOptionalByTask(t)
                if (bio.isPresent) result.add(bio.get())

                t.councils.forEach { c ->
                    result.add(c)
                }
            }
            return result
        }
    }


    /**
     *  Creates a new PDF and adds it to a datalist. Stores the list in the database and the pdf data to the disk
     */
    @Transactional
    open fun createPDFAndAddToDataList(dataList: DataList, content: ByteArray?, createThumbnail: Boolean, name: String, type: PrintDocumentType, commentary: String = "", commentaryIntern: String = "", targetDirectory: String = dataList.fileRepositoryBase.path): PDFReturn {
        var resultPDF = getSavedUniquePDF(targetDirectory, createThumbnail, name, type, commentary, commentaryIntern)

        var dataListResult = saveAndAttachPDFToList(dataList, resultPDF)

        try {
            if (content != null) {
                logger.debug("Saving pdf to disk")
                mediaRepository.saveBytes(content, resultPDF.path)

                if (createThumbnail)
                    generateAndSaveThumbnail(content, resultPDF.thumbnail)
            }
        } catch (e: Exception) {
            logger.debug("Error while saving file, removing file from datalists")
            e.printStackTrace()
            dataListResult = removeAndDeletePDF(dataListResult, resultPDF)
        }

        return PDFReturn(dataListResult, resultPDF)
    }


    /**
     *  Creates a new PDF and adds it to a datalist. Stores the list in the database and the pdf data to the disk
     */
    @Transactional
    open fun createPDFAndAddToDataList(dataList: DataList, printDocument: PrintDocument, createThumbnail: Boolean, name: String = printDocument.generatedFileName, type: PrintDocumentType = printDocument.documentType, commentary: String = "", commentaryIntern: String = "", targetDirectory: String = dataList.fileRepositoryBase.path, nonBlocking: Boolean = false, returnHandler: LazyPDFReturnHandler? = null): PDFReturn {
        var resultPDF = getSavedUniquePDF(targetDirectory, createThumbnail, name, type, commentary, commentaryIntern)

        resultPDF = pdfRepository.save(resultPDF)

        val dataListResult = saveAndAttachPDFToList(dataList, resultPDF)

        if (!nonBlocking)
            resultPDF = PDFUpdater().update(printDocument, resultPDF, createThumbnail)
        else
            PDFUpdaterNonBlocking().update(printDocument, resultPDF, createThumbnail, returnHandler
                    ?: LazyPDFReturnHandler() { _: PDFContainer, _: String -> })

        return PDFReturn(dataListResult, resultPDF)
    }

    /**
     * Updates an existing pdf. Blocks the program
     */
    @Transactional
    open fun updatePDF(container: PDFContainer, printDocument: PrintDocument, createThumbnail: Boolean, nonBlocking: Boolean = false, returnHandler: LazyPDFReturnHandler? = null) {
        if (!nonBlocking)
            PDFUpdater().update(printDocument, container, createThumbnail)
        else
            PDFUpdaterNonBlocking().update(printDocument, container, createThumbnail, LazyPDFReturnHandler() { _: PDFContainer, _: String -> })
    }

    @Transactional
    open fun removePDF(dataList: DataList, pdfContainer: PDFContainer): DataList {
        dataList.removeReport(pdfContainer)
        return saveDataList(dataList, resourceBundle["log.pdf.removed", pdfContainer.name])
    }

    /**
     * Adds a pdf to a datalist an save the changes in the database
     */
    @Transactional
    open fun saveAndAttachPDFToList(dataList: DataList, pdfContainer: PDFContainer): DataList {
        if (!dataList.attachedPdfs.contains(pdfContainer)) {
            dataList.attachedPdfs.add(pdfContainer)
        }
        return saveDataList(dataList, resourceBundle.get("log.pdf.uploaded", pdfContainer.name))
    }

    /**
     * Saves all know types of dataLists
     */
    @Transactional
    open fun saveDataList(dataList: DataList, log: String): DataList {
        return when (dataList) {
            is Patient -> {
                patientRepository.save(dataList as Patient, log) as DataList
            }
            is Task -> {
                taskRepository.save(dataList as Task, log) as DataList
            }
            is BioBank -> {
                bioBankRepository.save(dataList as BioBank, log) as DataList
            }
            is Council -> {
                councilRepository.save(dataList as Council, log) as DataList
            }
            else -> {
                throw IllegalArgumentException("List type not supported ($dataList)")
            }
        }
    }

    /**
     * Returns a pdf with a unique name within the target dir, does not save content to disk. Sets passed variables. Saves
     * pdfContainer in database
     */
    @Transactional
    open fun getSavedUniquePDF(targetDirectory: String, createThumbnail: Boolean, name: String, type: PrintDocumentType, commentary: String, commentaryIntern: String): PDFContainer {
        val resultPDF = getUniquePDF(targetDirectory, createThumbnail, name, type, commentary, commentaryIntern)
        return pdfRepository.save(resultPDF)
    }

    /**
     * Returns a pdf with a unique name within the target dir, does not save to disk. Sets passed variables.
     */
    @Transactional
    open fun getUniquePDF(targetDirectory: String, createThumbnail: Boolean, name: String, type: PrintDocumentType, commentary: String, commentaryIntern: String): PDFContainer {
        val pdf = getUniquePDF(targetDirectory, createThumbnail)
        pdf.name = name
        pdf.type = type
        pdf.commentary = commentary
        pdf.intern = commentaryIntern
        return pdf
    }

    /**
     * Returns a pdf with a unique name within the target dir, does not save to disk
     */
    @Transactional
    open fun getUniquePDF(targetDirectory: String, createThumbnail: Boolean): PDFContainer {
        val outputName = SpringContextBridge.services().mediaRepository.getUniqueName(targetDirectory, ".pdf")
        val outputFile = File(targetDirectory, outputName)
        val outputIMG = File(targetDirectory, outputName.replace(".pdf", ".png"))

        logger.debug("PDF with output file ${outputFile.path}; dir ${SpringContextBridge.services().mediaRepository.getFileForPath(targetDirectory).absolutePath}")

        val pdf = PDFContainer()
        pdf.path = outputFile.path
        pdf.thumbnail = if (createThumbnail) outputIMG.path else ""

        return pdf
    }

    /**
     * Removes a pdf from the list. Changes are also store in database
     */
    @Transactional
    open fun removePDFt(dataList: DataList, pdfContainer: PDFContainer): DataList {
        dataList.removeReport(pdfContainer)
        return saveDataList(dataList, resourceBundle["log.pdf.removed", pdfContainer.name])
    }

    /**
     * Removes a pdf from the list an deletes it content from the disk. Changes are also store in database
     */
    @Transactional
    open fun removeAndDeletePDF(dataList: DataList, pdfContainer: PDFContainer): DataList {
        val result = removePDFt(dataList, pdfContainer)
        mediaRepository.delete(pdfContainer.path)
        mediaRepository.delete(pdfContainer.thumbnail)
        return dataList
    }

    /**
     * Moves a pdf from one datalist to another
     */
    @Transactional
    open fun movePDF(source: DataList, target: DataList, pdfContainer: PDFContainer) {
        removePDF(source, pdfContainer)
        saveAndAttachPDFToList(target, pdfContainer)
    }

    /**
     * Search a list of dataList for the parent of the pdf. If found the pdf will be moved to the target list
     */
    @Transactional
    open fun movePDF(source: List<DataList>, target: DataList, pdfContainer: PDFContainer) {
        val sourceList = source.firstOrNull { it.containsReport(pdfContainer) }
        if (sourceList == target || sourceList == null)
            return

        movePDF(sourceList, target, pdfContainer)
    }

    /**
     * Generates a thumbnail from a byteArray (pdf content). Reads source pdf from disk. Saves the result to the target file.
     */
    @Transactional
    open fun generateAndSaveThumbnail(source: String, target: String): Boolean {
        return generateAndSaveThumbnail(source, target, 0, SpringContextBridge.services().pathoConfig.fileSettings.thumbnailDPI)
    }

    /**
     * Generates a thumbnail from a byteArray (pdf content). Reads source pdf from disk. Saves the result to the target file.
     */
    @Transactional
    open fun generateAndSaveThumbnail(source: String, target: String, pageNo: Int, dpi: Int): Boolean {
        val img = generateThumbnail(source, pageNo, dpi) ?: return false
        mediaRepository.saveImage(img, target);
        return true
    }

    /**
     * Generates a thumbnail from a byteArray (pdf content). Saves the result to the target file.
     */
    @Transactional
    open fun generateAndSaveThumbnail(source: ByteArray, target: String): Boolean {
        return generateAndSaveThumbnail(source, target, 0, SpringContextBridge.services().pathoConfig.fileSettings.thumbnailDPI)
    }

    /**
     * Generates a thumbnail from a byteArray (pdf content). Saves the result to the target file.
     */
    @Transactional
    open fun generateAndSaveThumbnail(source: ByteArray, target: String, pageNo: Int, dpi: Int): Boolean {
        val img = generateThumbnail(source, pageNo, dpi) ?: return false
        mediaRepository.saveImage(img, target);
        return true
    }

    /**
     * Generates a thumbnail from a byteArry (pdf content). Reads pdf from disk
     */
    @Transactional
    open fun generateThumbnail(path: String, pageNo: Int, dpi: Int): BufferedImage? {
        return generateThumbnail(mediaRepository.getBytes(path), pageNo, dpi)
    }

    /**
     * Generates a thumbnail from a byteArry (pdf content). Reads pdf from disk
     */
    @Transactional
    open fun generateThumbnail(content: ByteArray, pageNo: Int): BufferedImage? {
        return generateThumbnail(content, pageNo, SpringContextBridge.services().pathoConfig.fileSettings.thumbnailDPI)
    }

    /**
     * Generates a thumbnail from a byteArray (pdf content)
     */
    @Transactional
    open fun generateThumbnail(content: ByteArray, pageNo: Int, dpi: Int): BufferedImage? {
        var document: PDDocument? = null
        try {
            document = PDDocument.load(content)
            val pdfRenderer = PDFRenderer(document)
            if (pageNo < document!!.numberOfPages) return pdfRenderer.renderImageWithDPI(pageNo, dpi.toFloat(), ImageType.RGB)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (document != null) try {
                document.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Returns an images as an bytearray
     */
    open fun thumbnailToByteArray(image: BufferedImage): ByteArray {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        baos.flush()
        return baos.toByteArray()
    }

    /**
     * Initializes all datalists of the patient
     */
    @Transactional
    open fun initializeDataListTree(p: Patient): Patient {
        val patient = patientRepository.save(p)

        Hibernate.initialize(patient.attachedPdfs)
        for (task in patient.tasks) {
            Hibernate.initialize(task.attachedPdfs)
            Hibernate.initialize(task.councils)
            for (council in task.councils) {
                Hibernate.initialize(council.attachedPdfs)
            }
        }
        return patient
    }

    class PDFReturn(val dataList: DataList, val container: PDFContainer)
}