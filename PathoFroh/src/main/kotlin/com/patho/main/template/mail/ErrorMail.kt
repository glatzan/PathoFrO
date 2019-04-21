package com.patho.main.template.mail

import com.patho.main.model.user.HistoUser
import com.patho.main.template.MailTemplate
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.io.StringWriter
import java.util.*
import javax.persistence.Transient

class ErrorMail : MailTemplate() {
//    @Transient
//    private var histoUser: HistoUser? = null
//
//    private var errorContent: String? = null
//
//    private var currentDate: Date? = null
//
//    fun prepareTemplate(histoUser: HistoUser, errorContent: String, currentDate: Date) {
//        prepareTemplate()
//
//        this.histoUser = histoUser
//        this.errorContent = errorContent
//        this.currentDate = currentDate
//    }
//
//    fun fillTemplate() {
//        AbstractTemplate.initVelocity()
//
//        /* create a context and add data */
//        val context = VelocityContext()
//
//        context.put("histoUser", histoUser)
//        context.put("bodyText", errorContent)
//        context.put("currentDate", currentDate)
//
//        /* now render the template into a StringWriter */
//        val writer = StringWriter()
//
//        Velocity.evaluate(context, writer, "mystring", subject)
//        subject = writer.toString()
//
//        writer.buffer.setLength(0)
//
//        Velocity.evaluate(context, writer, "mystring", body)
//        body = writer.toString()
//    }
}