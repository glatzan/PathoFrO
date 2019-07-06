package com.patho.main.template.mail

import com.patho.main.template.MailTemplate

class UnlockRequestMail : MailTemplate() {

//    @Transient
//    private var histoUser: HistoUser? = null
//
//    fun prepareTemplate(histoUser: HistoUser) {
//        prepareTemplate()
//        this.histoUser = histoUser
//    }
//
//    fun fillTemplate() {
//        AbstractTemplate.initVelocity()
//
//        /* create a context and add data */
//        val context = VelocityContext()
//
//        context.put("histoUser", histoUser)
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