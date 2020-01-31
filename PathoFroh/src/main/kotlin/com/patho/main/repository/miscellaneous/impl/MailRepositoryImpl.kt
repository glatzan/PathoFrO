package com.patho.main.repository.miscellaneous.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.patho.main.model.dto.json.JSONMailMapper
import com.patho.main.repository.miscellaneous.MailRepository
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.template.MailTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.io.IOException
import java.lang.reflect.InvocationTargetException

@Service
@ConfigurationProperties(prefix = "patho.settings")
open class MailRepositoryImpl @Autowired constructor(
        private val mediaRepository: MediaRepository) :  AbstractMiscellaneousRepositoryImpl(), MailRepository {

    var mails: Array<MailTemplate> = emptyArray()

    override fun initializeDocuments() {
        for (mail in mails) {
            logger.debug("Initializing ${mail.name}, ${mail.type}, default: ${mail.defaultOfType}")
            val jsonContent: String = mediaRepository.getString(mail.content)
            try {
                val userMapper = ObjectMapper().readValue(jsonContent, JSONMailMapper::class.java)
                userMapper.updateMailTemplate(mail)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun findAll(): List<MailTemplate> {
        return listOf(*mails).mapNotNull { loadDocument(it, MailTemplate::class.java) }
    }

    override fun findByID(id: Long): MailTemplate? {
        return mails.filter{ it.id == id }.mapNotNull { loadDocument(it, MailTemplate::class.java) }.firstOrNull()
    }

}