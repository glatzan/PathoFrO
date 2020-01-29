package com.patho.main.rest

import com.patho.main.action.handler.AbstractHandler
import com.patho.main.config.PathoConfig
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.transitory.PDFContainerLoaded
import com.patho.main.repository.jpa.PatientRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.repository.miscellaneous.MailRepository
import com.patho.main.service.MailService
import com.patho.main.service.PDFService
import com.patho.main.template.DocumentToken
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = ["/rest"])
open class PDFRestHandler @Autowired constructor(
        private val patientRepository: PatientRepository,
        private val taskRepository: TaskRepository,
        private val pdfService: PDFService,
        private val pathoConfig: PathoConfig,
        private val mailService: MailService,
        private val mailRepository: MailRepository) : AbstractHandler() {

    override fun loadHandler() {
    }

    @RequestMapping(value = ["/pdf"], method = [RequestMethod.POST])
    open fun handlePDFUpload(@RequestParam("file") file: MultipartFile,
                             @RequestParam(value = "piz", required = false) piz: String = "",
                             @RequestParam(value = "taskID", required = false) caseID: String = "",
                             @RequestParam(value = "fileName", required = false) fileName: String = "",
                             @RequestParam(value = "documentType", required = false) documentType: String = ""): String {


        if (!file.isEmpty) {

            if (piz.isEmpty() && caseID.isEmpty()) {
                logger.error("Error: No Target for uploading the file was provided!")
                //mailService.sendAdminMail()
                return resourceBundle["rest.upload.error.noTarget"]
            }

            var patient: Patient? = null
            var task: Task? = null

            // piz
            if (piz.isNotEmpty()) {

                if (!piz.matches(Regex("[0-9]]{8,}"))) {
                    logger.error("Error: No Target for uploading the file was provided!")
                    return resourceBundle["rest.upload.error.wrongPiz", piz]
                }


                val patientList = patientRepository.findByPiz(piz, true, true, true)

                if (patientList.size == 0) {
                    logger.error("Error: No Target for uploading the file was provided!")
                    return resourceBundle["rest.upload.error.patientNotFound", piz]
                }

                patient = patientList.first()
            }

            if (piz.isNotEmpty()) {
                if (!caseID.matches(Regex("[0-9]{6,}"))) {
                    logger.error("Error: No Target for uploading the file was provided!")
                    return resourceBundle["rest.upload.error.wrongCaseID", caseID]
                }


                task = if (patient != null)
                    patient.tasks.firstOrNull { it.taskID == caseID }
                else
                    taskRepository.findByTaskID(caseID, false, false, true, false, false)

                if (task == null) {
                    logger.error("Error: Task was not found. (Task ID: $caseID)")
                    return resourceBundle["rest.upload.error.taskNotFound", caseID]
                }
            }

            val document: PrintDocumentType? = if (documentType.isEmpty()) PrintDocumentType.UNKNOWN else pathoConfig.externalDocumentMapper.firstOrNull {
                it.externalIdentifier == documentType
            }?.internalIdentifier

            if (document == null) {
                logger.error("Error: DocumentType not recognized. ( $documentType)")
                return resourceBundle["rest.upload.error.documentTypeNotRecognized", documentType]
            }

            val name = if (fileName.isEmpty()) PrintDocument(document).generatedFileName else fileName

            if (task != null)
                pdfService.createPDFAndAddToDataList(task, file.bytes, true, name, document,
                        "", "", task.fileRepositoryBase.path)
            else if (patient != null) {
                pdfService.createPDFAndAddToDataList(patient, file.bytes, true, name, document,
                        "", "", patient.fileRepositoryBase.path)
            } else {
                logger.debug("Error: No Target for uploading the file was provided!")
                return resourceBundle["rest.upload.error.noTarget"]
            }
        }

        logger.error("Error: Upload failed, file is empty.")
        return resourceBundle["rest.upload.error.fileEmpty"]
    }

    private fun sendErrorMail(reason: String, piz: String, caseID: String, fileName: String, documentType: String, file: MultipartFile) {
        val mailTemplate = mailRepository.findByID(pathoConfig.defaultDocuments.restUploadErrorEmail) ?: return

        mailTemplate.initialize(
                DocumentToken("reason", reason),
                DocumentToken("piz", if (piz.isEmpty()) "--" else piz),
                DocumentToken("caseID", if (caseID.isEmpty()) "--" else caseID),
                DocumentToken("fileName", if (fileName.isEmpty()) "--" else fileName),
                DocumentToken("documentType", if (documentType.isEmpty()) "--" else documentType))

        if (!file.isEmpty)
            mailTemplate.attachment = PDFContainerLoaded(PrintDocumentType.UNKNOWN, "Upload.pdf", file.bytes, null)

        mailService.sendAdminMail(mailTemplate)
    }


}