package com.patho.main.repository.miscellaneous

import com.patho.main.template.MiscellaneousTemplate

/**
 * Repository for miscellaneous documents templates
 */
interface MiscellaneousDocumentsRepository : FileBasedTemplateRepository<MiscellaneousTemplate> {
    /**
     * Loads document from the harddrive at startup. The mails array is initialized via configurationProperties
     */
    fun initializeDocuments()

    /**
     * Returns all documents. Copies of the
     * original template are returned.
     */
    fun findAll(): List<MiscellaneousTemplate>

    /**
     * Returns the template with the given id. Copies of the
     * original template are returned.
     */
    fun findByID(id: Long): MiscellaneousTemplate?
}