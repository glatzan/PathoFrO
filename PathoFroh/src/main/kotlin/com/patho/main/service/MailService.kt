package com.patho.main.service

import com.patho.main.config.PathoConfig
import com.patho.main.repository.MediaRepository
import com.patho.main.template.MailTemplate
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.MultiPartEmail
import org.apache.commons.mail.SimpleEmail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.mail.util.ByteArrayDataSource

@Service
class MailService @Autowired constructor(
        private val mediaRepository: MediaRepository,
        private val pathoConfig: PathoConfig) : AbstractService() {

    fun sendErrorMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.errorAddresses, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendAdminMail(mail: MailTemplate): Boolean {
        return sendMail(pathoConfig.mailSettings.adminAddresses, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, mail)
    }

    fun sendMail(mailTo: String, template: MailTemplate): Boolean {
        return sendMail(arrayOf(mailTo), pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMail(mailTo: List<String>, template: MailTemplate): Boolean {
        return sendMail(mailTo, pathoConfig.mailSettings.systemMail, pathoConfig.mailSettings.systemName, template)
    }

    fun sendMail(mailTo: Array<String>, mailFrom: String, nameFrom: String, mail: MailTemplate): Boolean {
        return sendMail(Arrays.asList(*mailTo), mailFrom, nameFrom, mail)
    }

    fun sendMail(mailTo: List<String>, mailFrom: String, nameFrom: String, mail: MailTemplate): Boolean {

        if (mail.attachment == null) {
            val email = SimpleEmail()

            email.hostName = pathoConfig.mailSettings.server
            email.setDebug(pathoConfig.mailSettings.debug)
            email.setSmtpPort(pathoConfig.mailSettings.port)
            email.isSSLOnConnect = pathoConfig.mailSettings.ssl

            try {
                for (to in mailTo) {
                    email.addTo(to)
                }
                email.setFrom(mailFrom, nameFrom)
                email.subject = mail.subject
                email.setMsg(mail.body)
                email.send()
            } catch (e: EmailException) {
                e.printStackTrace()
                return false
            }

        } else {
            // Create the email message
            val email = MultiPartEmail()

            email.hostName = pathoConfig.mailSettings.server
            email.setDebug(pathoConfig.mailSettings.debug)
            email.setSmtpPort(pathoConfig.mailSettings.port)
            email.isSSLOnConnect = pathoConfig.mailSettings.ssl

            try {
                for (to in mailTo) {
                    email.addTo(to)
                }
                email.setFrom(mailFrom, nameFrom)

                email.subject = mail.subject
                email.setMsg(mail.body)

                val `is` = ByteArrayInputStream(mediaRepository.getBytes(mail.attachment?.path))

                val source = ByteArrayDataSource(`is`, "application/pdf")

                // add the attachment
                email.attach(source, mail.attachment?.name, "")

                // send the email
                email.send()

                `is`.close()
            } catch (e: EmailException) {
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

        }

        return true
    }

}