package com.patho.main.repository.miscellaneous

import com.patho.main.template.AbstractTemplate

/**
 * Generic interface for file based templates
 * TODO: PritnDocumentRepository should use this as well
 */
interface FileBasedTemplateRepository<T> where T : AbstractTemplate {

    fun loadDocument(document: T, cl: Class<T>): T? {
        val copy: T
        copy = if (document.templateName.isNotEmpty()) document.clone() as T else {
            val myClass = Class.forName(document.templateName)
            val constructor = myClass.getConstructor(*arrayOf<Class<*>>(cl))
            constructor.newInstance(*arrayOf<Any>(document)) as T
        }
        return copy
    }
}