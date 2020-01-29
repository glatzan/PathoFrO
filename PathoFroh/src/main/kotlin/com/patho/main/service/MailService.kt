package com.patho.main.service

import com.patho.main.config.PathoConfig
import com.patho.main.model.transitory.PDFContainerLoaded
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.template.MailTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService @Autowired constructor(
        private val mediaRepository: MediaRepository,
        private val emailSender: JavaMailSender,
        private val pathoConfig: PathoConfig) : AbstractService() {

    fun sendErrorMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.errorAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendAdminMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.adminAddresses.toList(), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendMail(mailTo: String, template: MailTemplate): Boolean {
        return sendMail(listOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
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

}