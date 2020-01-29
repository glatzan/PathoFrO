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
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

@Service
@ConfigurationProperties(prefix = "patho.settings")
open class MailRepositoryImpl @Autowired constructor(
        private val mediaRepository: MediaRepository) : MailRepository {

    private val logger = LoggerFactory.getLogger(this.javaClass)

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
        return listOf(*mails).mapNotNull { loadDocument(it) }
    }

    override fun findByID(id: Long): MailTemplate? {
        return mails.filter{ it.id == id }.mapNotNull { loadDocument(it) }.firstOrNull()
    }

    /**
     * Clones the current mail if templateName is empty, otherwise a new mail Object will be created.
     */
    private fun loadDocument(document: MailTemplate): MailTemplate? {
        val copy: MailTemplate
        copy = if (document.templateName.isNotEmpty()) document.clone() as MailTemplate else {
            try {
                val myClass = Class.forName(document.templateName)
                val constructor = myClass.getConstructor(*arrayOf<Class<*>>(MailTemplate::class.java))
                constructor.newInstance(*arrayOf<Any>(document)) as MailTemplate
            } catch (e: ClassNotFoundException) {
                document.clone() as MailTemplate
            } catch (e: InstantiationException) {
                document.clone() as MailTemplate
            } catch (e: IllegalAccessException) {
                document.clone() as MailTemplate
            } catch (e: IllegalArgumentException) {
                document.clone() as MailTemplate
            } catch (e: InvocationTargetException) {
                document.clone() as MailTemplate
            } catch (e: NoSuchMethodException) {
                document.clone() as MailTemplate
            } catch (e: SecurityException) {
                document.clone() as MailTemplate
            }
        }
        return copy
    }
}