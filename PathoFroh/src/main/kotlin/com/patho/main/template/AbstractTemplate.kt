package com.patho.main.template

import com.patho.main.model.AbstractPersistable
import com.patho.main.util.DateTool
import com.patho.main.util.VelocityNoOutputLogger
import com.patho.main.util.helper.HistoUtil
import com.patho.main.util.helper.TextToLatexConverter
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.context.Context
import java.util.*

//@Entity
//@Audited
//@SelectBeforeUpdate(true)
//@DynamicUpdate(true)
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@SequenceGenerator(name = "template_sequencegenerator", sequenceName = "template_sequence")
open class AbstractTemplate : AbstractPersistable, Cloneable {

    /**
     * ID of the template
     */
    override var id: Long = 0

    /**
     * Name of the template
     */
    var name: String = ""

    /**
     * Content one of the template
     */
    var content: String = ""

    /**
     * Alternate content of the template
     */
    var alternateContent: String = ""

    /**
     * Type of the template
     */
    var type: String = ""

    /**
     * If True the default of it's type
     */
    var defaultOfType: Boolean = false

    /**
     * If true the generated content should not be saved in the database
     */
    var transientContent: Boolean = false

    /**
     * Name of the template class
     */
    var templateName: String = ""

    /**
     * Addition atributes, e.g. for printing
     */
    var attributes: String = ""

    constructor()

    constructor(template : AbstractTemplate){
        copyIntoDocument(template)
    }

    /**
     * Prepares the template and loads data from disk
     */
    open fun prepareTemplate() : Boolean {return false}

    /**
     * Copies the content of the given template into this object
     */
    open fun copyIntoDocument(document: AbstractTemplate) {
        id = document.id
        name = document.name
        content = document.content
        alternateContent = document.alternateContent
        type = document.type
        attributes = document.attributes
        templateName = document.templateName
        defaultOfType = document.defaultOfType
        transientContent = document.defaultOfType
    }

    /**
     * Initializes values for the velocity template engine
     */
    open fun initialize(vararg token: DocumentToken): Pair<AbstractTemplate, Context> {
        return initialize(hashMapOf(*token.map { it.key to it.value }.toTypedArray()))
    }

    /**
     * Initializes values for the velocity template engine
     */
    open fun initialize(content: HashMap<String, Any?>): Pair<AbstractTemplate, Context> {
        AbstractTemplate.initVelocity()

        /* create a context and add data */
        val context = VelocityContext()

        for ((key, value) in content) {
            context.put(key, value)
        }

        // default date tool
        context.put("date", DateTool())
        context.put("latexTextConverter", TextToLatexConverter())
        context.put("histoUtil", HistoUtil())

        return Pair(this, context)
    }

    /**
     * Makes clone public
     */
    public override fun clone(): Any {
        return super.clone()
    }

    companion object {
        /**
         * Removes velocity logger
         */
        @JvmStatic
        fun initVelocity() {
            Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, VelocityNoOutputLogger())
            Velocity.init()
        }
    }

}