package com.patho.main.template

import org.apache.velocity.app.Velocity
import org.apache.velocity.context.Context
import java.io.StringWriter
import java.util.HashMap

class MiscellaneousTemplate  : AbstractTemplate() {

    /**
     * Raw file content from hdd
     */
    var loadedContent: String = ""

    /**
     * Processed final document
     */
    var finalContent: String = ""

    /**
     * Initializes values for the velocity template engine. Generates a
     * final document version and saves this version into finalContent
     */
    override fun initialize(content: HashMap<String, Any?>): Pair<MiscellaneousTemplate, Context> {
        val context = super.initialize(content).second

        /* now render the template into a StringWriter */
        val writer = StringWriter()

        Velocity.evaluate(context, writer, "test", loadedContent)

        finalContent = writer.toString()

        return Pair(this, context)
    }
}