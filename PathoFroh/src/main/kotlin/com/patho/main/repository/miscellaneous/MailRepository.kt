package com.patho.main.repository.miscellaneous

import com.patho.main.template.MailTemplate

/**
 * Repository for acquiring mail templates
 */
interface MailRepository {

    /**
     * Loads document from the harddrive at startup. The mails array is initialized via configurationProperties
     */
    fun initializeDocuments()

    /**
     * Returns all mailTemplates. Copies of the
     * original template are returned.
     */
    fun findAll(): List<MailTemplate>

    /**
     * Returns the template with the given id. Copies of the
     * original template are returned.
     */
    fun findByID(id: Long): MailTemplate?

}