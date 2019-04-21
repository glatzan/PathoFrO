package com.patho.main.template.mail

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.template.MailTemplate
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.io.StringWriter
import javax.persistence.Transient

class DiagnosisReportMail : MailTemplate() {

//    @Transient
//    private var patient: Patient? = null
//
//    @Transient
//    private var task: Task? = null
//
//    @Transient
//    private var contact: ReportIntent? = null
//
//    fun prepareTemplate(patient: Patient, task: Task, contact: ReportIntent) {
//        prepareTemplate()
//
//        this.patient = patient
//        this.task = task
//        this.contact = contact
//
//    }
//
//    fun fillTemplate() {
//        AbstractTemplate.initVelocity()
//
//        /* create a context and add data */
//        val context = VelocityContext()
//
//        //		context.put("date", new DateTool());
//
//        if (patient != null)
//            context.put("patient", patient)
//
//        if (task != null)
//            context.put("task", task)
//
//        if (contact != null)
//            context.put("contact", contact)
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