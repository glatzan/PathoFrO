package com.patho.main.repository.miscellaneous.impl

import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.repository.miscellaneous.DocumentRepository
import com.patho.main.template.AbstractTemplate
import com.patho.main.template.DefaultTemplateImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(prefix = "patho.settings")
class DocumentRepositoryImpl @Autowired constructor(
        private val mediaRepository: MediaRepository) : AbstractMiscellaneousRepositoryImpl(), DocumentRepository {

    var documentTemplates: Array<AbstractTemplate> = emptyArray()

    override fun initializeDocuments() {
        for ((i, document) in documentTemplates.withIndex()) {
            logger.debug("Initializing ${document.name}, ${document.type}, default: ${document.defaultOfType}")

            if(document.templateName.isNotEmpty()) {
                val myClass = Class.forName(document.templateName)
                val constructor = myClass.getConstructor(*arrayOf<Class<AbstractTemplate>>(AbstractTemplate::class.java))
                documentTemplates[i] = constructor.newInstance(*arrayOf<Any>(document)) as AbstractTemplate
            }

            documentTemplates[i].prepareTemplate()
        }
    }

    override fun <T : AbstractTemplate> findAll(): List<T> {
        return listOf(*documentTemplates).mapNotNull { loadDocument<T>(it as T, Class.forName(it.templateName) as Class<T>  ) } as List<T>
    }

    override fun <T : AbstractTemplate> findByID(id: Long): T? {
        return documentTemplates.filter { it.id == id }.mapNotNull { loadDocument<T>(it as T, Class.forName(it.templateName) as Class<T>  )  }.firstOrNull()
    }

}