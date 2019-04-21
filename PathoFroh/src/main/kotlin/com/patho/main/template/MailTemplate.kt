package com.patho.main.template

import com.patho.main.model.PDFContainer
import org.apache.velocity.app.Velocity
import org.apache.velocity.context.Context
import java.io.StringWriter
import java.util.*

/**
 * Mail template
 */
open class MailTemplate : AbstractTemplate {

    var attachment: PDFContainer = PDFContainer()

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

    /**
     * Returns the mail type as an enum object
     */
    val documentType
        get() = MailType.fromString(type);

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
    override fun clone(): Any {
        return super.clone()
    }

    /**
     * Initializes values for the velocity template engine. Generates a
     * final document version and saves this version into finalSubject and
     * finalBody
     */
    override fun initialize(content: HashMap<String, Any>): Pair<out MailTemplate, Context> {
        val context = super.initialize(content).second

        /* now render the template into a StringWriter */
        val writer = StringWriter()

        Velocity.evaluate(context, writer, "test", subject)

        finalSubject = writer.toString()

        Velocity.evaluate(context, writer, "test", body)

        finalBody = writer.toString()

        return Pair(this, context)
    }
}