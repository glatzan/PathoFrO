package com.patho.main.repository.miscellaneous.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.patho.main.config.PathoConfig
import com.patho.main.config.excepion.ToManyEntriesException
import com.patho.main.model.json.JSONPatientMapper
import com.patho.main.model.patient.Patient
import com.patho.main.repository.miscellaneous.DocumentRepository
import com.patho.main.repository.miscellaneous.HttpRestRepository
import com.patho.main.service.MailService
import com.patho.main.template.DefaultTemplateImpl
import com.patho.main.template.DocumentToken
import org.hibernate.boot.model.naming.IllegalIdentifierException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors


@Service
@ConfigurationProperties(prefix = "patho.rest")
open class HttpRestRepositoryImpl @Autowired constructor(
        private val pathoConfig: PathoConfig,
        private val documentRepository: DocumentRepository,
        private val mailService: MailService) : AbstractMiscellaneousRepositoryImpl(), HttpRestRepository {

    lateinit var patientByPizUrl: String

    lateinit var patientByNameSurnameBirthday: String

    lateinit var createPatientInPDV: String

    var requestTimeout: Int = 5000

    override fun createPatientInPDV(patient: Patient): String? {
        fun sendMail(success: Boolean, request: String, responseHeader: String, responseBody: String) {
            if (success && !pathoConfig.miscellaneous.noticeAdminOnPDVPatientCreation) {
                logger.debug("No mail of patient creation in pdv should be send")
                return
            }

            mailService.sendAdminMailNoneBlocking(pathoConfig.defaultDocuments.createPDVPatientStatusMail, DocumentToken("success", success),
                    DocumentToken("request", request),
                    DocumentToken("responseStatus", responseHeader),
                    DocumentToken("responseBody", responseBody))
        }

        val document = documentRepository.findByID<DefaultTemplateImpl>(pathoConfig.defaultDocuments.createPDVPatientRequestDocument)

        document?.initialize(DocumentToken("person", patient.person))

        logger.debug("starting request to $createPatientInPDV")

        val restTemplate = RestTemplate()
        restTemplate.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8));
        (restTemplate.requestFactory as SimpleClientHttpRequestFactory).setReadTimeout(requestTimeout)
        (restTemplate.requestFactory as SimpleClientHttpRequestFactory).setConnectTimeout(requestTimeout)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_XML
        val request = HttpEntity<String>(document?.finalContent, headers)
        try {
            val response = restTemplate.postForEntity(createPatientInPDV, request, String::class.java)
            logger.debug("response status: " + response.statusCode)
            logger.debug("response body: " + response.body)

            return if (response.body.matches(Regex("[0-9]{8,}"))) {
                logger.debug("Patient created successfully, piz = ${response.body}")
                sendMail(true, document?.finalContent ?: "", response?.statusCode.toString() ?: "", response.body)
                response.body
            } else {
                sendMail(false, document?.finalContent ?: "", response?.statusCode.toString() ?: "", response.body)
                logger.error("Error, result is not a valid piz")
                null
            }


        } catch (e: RestClientException) {
            sendMail(false, document?.finalContent ?: "", "Error", "Error")
            logger.error("Error, patient could not be created")
            e.printStackTrace()
        }
        return null
    }

    override fun findPatientByPIZ(piz: String): Optional<Patient> {
        return findPatient(patientByPizUrl.replace("\$piz", piz))
    }

    @Throws(ToManyEntriesException::class)
    override fun findPatientByNameAndSurNameAndBirthday(name: String, surname: String, birthday: LocalDate?): List<Patient> {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val result = patientByNameSurnameBirthday.replace("\$name", name)
                .replace("\$surname", surname)
                .replace("\$birthday", if (birthday != null) format.format(birthday) else "")
        return findPatientAll(result)
    }


    override fun findPatient(url: String): Optional<Patient> {
        return try {
            val userMapper: JSONPatientMapper = jacksonObjectMapper().readValue(URL(url))
            Optional.ofNullable(userMapper.getPatient())
        } catch (e: IOException) {
            e.printStackTrace()
            Optional.empty()
        }
    }

    @Throws(ToManyEntriesException::class)
    override fun findPatientAll(url: String): List<Patient> {
        return try {
            val userMapper: List<JSONPatientMapper> = jacksonObjectMapper().readValue(URL(url))
            // catching an error, to many entries
            if (userMapper.size == 1 && userMapper[0].error != null) throw ToManyEntriesException()
            userMapper.map { it.getPatient() }
        } catch (e: IllegalIdentifierException) {
            throw ToManyEntriesException()
        } catch (e: IOException) {
            e.printStackTrace()
            ArrayList()
        }
    }
}
