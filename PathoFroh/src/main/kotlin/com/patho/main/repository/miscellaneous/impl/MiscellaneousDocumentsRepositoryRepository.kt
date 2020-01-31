package com.patho.main.repository.miscellaneous.impl

import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.repository.miscellaneous.MiscellaneousDocumentsRepository
import com.patho.main.template.MiscellaneousTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(prefix = "patho.settings")
class MiscellaneousDocumentsRepositoryRepository @Autowired constructor(
        private val mediaRepository: MediaRepository) : AbstractMiscellaneousRepositoryImpl(), MiscellaneousDocumentsRepository {

    var miscellaneousDocuments: Array<MiscellaneousTemplate> = emptyArray()

    override fun initializeDocuments() {
        for (document in miscellaneousDocuments) {
            logger.debug("Initializing ${document.name}, ${document.type}, default: ${document.defaultOfType}")
            document.loadedContent = mediaRepository.getString(document.content)
        }
    }

    override fun findAll(): List<MiscellaneousTemplate> {
        return listOf(*miscellaneousDocuments).mapNotNull { loadDocument(it, MiscellaneousTemplate::class.java) }
    }

    override fun findByID(id: Long): MiscellaneousTemplate? {
        return miscellaneousDocuments.filter { it.id == id }.mapNotNull { loadDocument(it, MiscellaneousTemplate::class.java) }.firstOrNull()
    }

}