package com.patho.main.service

import com.patho.main.config.PathoConfig
import com.patho.main.model.transitory.PDFContainerLoaded
import com.patho.main.repository.miscellaneous.MailRepository
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.DocumentToken
import com.patho.main.template.MailTemplate
import com.patho.main.util.exceptions.TemplateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService @Autowired constructor(
        private val mediaRepository: MediaRepository,
        private val emailSender: JavaMailSender,
        private val pathoConfig: PathoConfig,
        private val mailRepository: MailRepository) : AbstractService() {

    fun sendErrorMail(templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMail(pathoConfig.mailSettings.errorAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendErrorMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.errorAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendAdminMail(templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMail(pathoConfig.mailSettings.adminAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendAdminMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.adminAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendMail(mailTo: String, templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMail(listOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendMail(mailTo: String, template: MailTemplate): Boolean {
        return sendMail(listOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMail(mailTo: List<String>, templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMail(mailTo, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendMail(mailTo: List<String>, template: MailTemplate): Boolean {
        return sendMail(mailTo, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMail(mailTo: List<String>, mailFrom: String, systemName: String, mail: MailTemplate): Boolean {

        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)

        helper.setTo(mailTo.toTypedArray())
        helper.setSubject(mail.finalSubject)
        helper.setFrom(mailFrom, systemName)
        helper.setText(mail.finalBody)

        val attachment = mail.attachment
        if (attachment != null) {
            if (attachment is PDFContainerLoaded)
                helper.addAttachment(attachment.name, ByteArrayResource(attachment.pdfData), "application/pdf")
            else
                helper.addAttachment(attachment.name, ByteArrayResource(mediaRepository.getBytes(mail.attachment!!.path)), "application/pdf")
        }
        emailSender.send(message)
        return true
    }

    fun sendErrorMailNoneBlocking(templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMailNoneBlocking(pathoConfig.mailSettings.errorAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendErrorMailNoneBlocking(mail: MailTemplate): Boolean {
        return sendMailNoneBlocking(pathoConfig.mailSettings.errorAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendAdminMailNoneBlocking(templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMailNoneBlocking(pathoConfig.mailSettings.adminAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendAdminMailNoneBlocking(mail: MailTemplate): Boolean {
        return sendMailNoneBlocking(pathoConfig.mailSettings.adminAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendMailNoneBlocking(mailTo: String, templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMailNoneBlocking(listOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }

    fun sendMailNoneBlocking(mailTo: String, template: MailTemplate): Boolean {
        return sendMailNoneBlocking(listOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMailNoneBlocking(mailTo: List<String>, templateID: Long, vararg tokens: DocumentToken): Boolean {
        return sendMailNoneBlocking(mailTo, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, getTemplate(templateID,*tokens))
    }


    fun sendMailNoneBlocking(mailTo: List<String>, template: MailTemplate): Boolean {
        return sendMailNoneBlocking(mailTo, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMailNoneBlocking(mailTo: List<String>, mailFrom: String, systemName: String, mail: MailTemplate): Boolean {
        SpringContextBridge.services().taskExecutor.execute(object : Thread() {
            override fun run() {
                try {
                    logger.debug("Sending mail in new Thread")
                    sendMail(mailTo, mailFrom, systemName, mail)
                } catch (e: Exception) {
                    logger.debug("Error while sending mail in new Thread")
                    e.printStackTrace()
                }
            }
        })
        return true
    }

    private fun getTemplate(templateID: Long, vararg token: DocumentToken): MailTemplate {
        val mail = mailRepository.findByID(pathoConfig.defaultDocuments.createPDVPatientStatusMail)

        if (mail == null) {
            logger.error("MailTemplate ${pathoConfig.defaultDocuments.createPDVPatientStatusMail} not found!")
            throw TemplateNotFoundException()
        }

        mail.initialize(*token)

        return mail
    }
}