package com.patho.main.repository.miscellaneous

import com.patho.main.template.AbstractTemplate
import com.patho.main.template.DefaultTemplateImpl

/**
 * Repository for miscellaneous documents templates
 */
interface DocumentRepository : FileBasedTemplateRepository {
    /**
     * Loads document from the harddrive at startup. The mails array is initialized via configurationProperties
     */
    fun initializeDocuments()

    /**
     * Returns all documents. Copies of the
     * original template are returned.
     */
    fun <T : AbstractTemplate> findAll() :  List<T>

    /**
     * Returns the template with the given id. Copies of the
     * original template are returned.
     */
    fun <T : AbstractTemplate> findByID(id: Long): T?
}