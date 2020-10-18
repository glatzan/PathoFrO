package com.patho.main.config.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.text.MessageFormat
import java.util.*
import javax.faces.context.FacesContext

@Component(value = "msg")
@Scope("singleton")
@Primary
class ResourceBundle @Autowired constructor(private val messageSource: MessageSource) : HashMap<Any, Any>() {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    val serialVersionUID = 1668009329184453712L

    private val local: Locale
        get() = if (FacesContext.getCurrentInstance() != null)
            FacesContext.getCurrentInstance().viewRoot.locale
        else
            Locale.GERMAN

    override operator fun get(key: Any): String {
        return try {
            return messageSource.getMessage(key.toString(), null, local)
        } catch (e: MissingResourceException) {
            System.err.println("Key $key not found")
            "???$key???"
        } catch (e: NoSuchMessageException) {
            System.err.println("Key $key not found")
            "???$key???"
        }
    }

    operator fun get(key: Any, vararg params: Any?): String {
        return try {
            getFormattedString(messageSource.getMessage(key.toString(), null, local), *params)
        } catch (e: MissingResourceException) {
            System.err.println("Key $key not found")
            "???$key???"
        } catch (e: NoSuchMessageException) {
            System.err.println("Key $key not found")
            "???$key???"
        }

    }

    private fun getFormattedString(str: String, vararg params: Any?): String {
        val arr = arrayListOf<Any>()
        for (i in params.indices) {
            when {
                params[i] == null -> {
                }
                params[i].toString().startsWith("date:") -> {
                }
                params[i].toString().startsWith("instant:") -> {

                }
            }
        }
        return MessageFormat.format(str, *params)
    }
}
