package com.patho.main.template

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.patho.main.model.PDFContainer
import com.patho.main.service.impl.SpringContextBridge
import org.apache.velocity.app.Velocity
import org.apache.velocity.context.Context
import java.io.IOException
import java.io.StringWriter
import java.util.*

/**
 * Mail template
 */
open class MailTemplate : AbstractTemplate {

    /**
     * Attachment of the mail
     */
    var attachment: PDFContainer? = null

    /**
     * Returns true if attachment is set
     */
    val isAttachment
        get() = attachment != null

    /**
     * Subject of the mail, as a template
     */
    var subject: String = ""

    /**
     * Body of the mail, as a template
     */
    var body: String = ""

    /**
     * Subject of the mail, template wildcards replace by values
     */
    var finalSubject: String = ""

    /**
     * Body of the mail, template wildcards replace by values
     */
    var finalBody: String = ""

    constructor()

    constructor(mailTemplate: MailTemplate) {
        copyIntoDocument(mailTemplate)
    }

    /**
     * Copies the values of the given mailTemplate into the current object
     */
    fun copyIntoDocument(mailTemplate: MailTemplate) {
        super.copyIntoDocument(mailTemplate)
        subject = mailTemplate.subject
        body = mailTemplate.body
    }

    /**
     * Makes clone public
     */
    public override fun clone(): Any {
        return super<AbstractTemplate>.clone()
    }

    /**
     * Loads maildata from Dist
     */
    override fun prepareTemplate() :Boolean {
        logger.debug("Initializing ${name}, ${type}, default: ${defaultOfType}")
        val jsonContent: String = SpringContextBridge.services().mediaRepository.getString(content)
        try {
            val jsonMapper = ObjectMapper().readValue(jsonContent, JSONMailMapper::class.java)
            this.body = jsonMapper.body ?: ""
            this.subject = jsonMapper.subject ?: ""
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Initializes values for the velocity template engine. Generates a
     * final document version and saves this version into finalSubject and
     * finalBody
     */
    override fun initialize(content: HashMap<String, Any?>): Pair<out MailTemplate, Context> {
        val context = super.initialize(content).second

        /* now render the template into a StringWriter */
        val writer = StringWriter()

        Velocity.evaluate(context, writer, "test", subject)

        finalSubject = writer.toString()

        Velocity.evaluate(context, writer, "test", body)

        finalBody = writer.toString()

        return Pair(this, context)
    }

    /**
     * Mapperclass form mails loaded from disk as json
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class JSONMailMapper{
        var subject: String? = null
        var body: String? = null
    }
}