package com.patho.main.util

import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Patient
import com.patho.main.service.impl.SpringContextBridge
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hibernate.Hibernate
import org.slf4j.LoggerFactory
import java.io.File


class PathoVersion2DatabaseConverter {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    fun convertDatabaseToVersion2() {

        val ignoredContainer: MutableList<PDFContainer> = mutableListOf()
        val patients: List<Patient> = SpringContextBridge.services().patientRepository.findAll()
        val entityManager = SpringContextBridge.services().entityManager

        runBlocking {
            val pipe = Channel<Deferred<Patient>>(20)

            launch {
                while (!(pipe.isEmpty && pipe.isClosedForSend)) {
                    val patient = (pipe.receive().await())
                    val ignored = convertPatient(patient)
                    ignoredContainer.addAll(ignored)
                }
                println("pipe closed")
            }

            patients.forEach { pipe.send(async { it }) }

            pipe.close()
        }

    }

    private fun convertPatient(patient: Patient): MutableList<PDFContainer> {
        logger.debug("${patient.piz} -Converting patient ${patient.piz}")

        val ignoredContainer: MutableList<PDFContainer> = mutableListOf()

        Hibernate.initialize(patient.attachedPdfs)

        val targetFolder = patient.fileRepositoryBase

        var tmpPatient = patient

        for (pdf in patient.attachedPdfs) {
            val result = convertPDF(patient, pdf, targetFolder)

            if (!result)
                ignoredContainer.add(pdf)
        }

        for (task in patient.tasks) {
            Hibernate.initialize(task.attachedPdfs)
            logger.debug("${patient.piz} --Converting task ${task.taskID}")
            for (pdf in task.attachedPdfs) {
                val result = convertPDF(patient, pdf, targetFolder)

                if (!result)
                    ignoredContainer.add(pdf)
            }

            for (council in task.councils) {
                Hibernate.initialize(council.attachedPdfs)

                logger.debug("${patient.piz} ---Converting consultation ${council.name}")

                for (pdf in council.attachedPdfs) {
                    val result = convertPDF(patient, pdf, targetFolder)

                    if (!result)
                        ignoredContainer.add(pdf)
                }
            }

            val bioBak = SpringContextBridge.services().bioBankRepository.findOptionalByTask(task)

            if (bioBak.isPresent) {
                Hibernate.initialize(bioBak.get().attachedPdfs)
                logger.debug("${patient.piz} ---Converting Biobank")
                for (pdf in bioBak.get().attachedPdfs) {
                    val result = convertPDF(patient, pdf, targetFolder)

                    if (!result)
                        ignoredContainer.add(pdf)
                }
            }
        }



        return ignoredContainer
    }

    private fun convertPDF(patient: Patient, pdf: PDFContainer, targetFolder: File): Boolean {
        if (!pdf.name.matches(Regex("^((.*\\.pdf)|([A-Za-z0-9 ]*))\$"))) {
            logger.error("${patient.piz} ----File not supported, ignoring.. ${pdf.name}, ${pdf.id}")
            return false
        }

        if (pdf.data.isNotEmpty()) {
            val uniqueFile = File(targetFolder, SpringContextBridge.services().mediaRepository.getUniqueName(targetFolder, ".pdf"))
            pdf.path = uniqueFile.path
            pdf.thumbnail = uniqueFile.path.replace(".pdf", ".png")
            val data: ByteArray = pdf.data
            pdf.data = ByteArray(0)

            logger.debug("${patient.piz} ----Saving pdf to disk ${pdf.name}")
            SpringContextBridge.services().mediaRepository.saveBytes(data, pdf.path)
            SpringContextBridge.services().pdfService.generateAndSaveThumbnail(data, pdf.path)
            SpringContextBridge.services().pdfRepository.save(pdf)
        } else {
            logger.debug("${patient.piz} ----!!! skipping is new file ${pdf.name}")
        }

        return true
    }
}