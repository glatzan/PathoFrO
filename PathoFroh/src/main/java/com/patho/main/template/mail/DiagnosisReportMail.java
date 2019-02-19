package com.patho.main.template.mail;

import java.io.StringWriter;

import javax.persistence.Transient;

import com.patho.main.model.patient.notification.ReportTransmitter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.template.MailTemplate;

import lombok.Getter;
import lombok.Setter;

//@Entity
@Getter
@Setter
public class DiagnosisReportMail extends MailTemplate {

	@Transient
	private Patient patient;

	@Transient
	private Task task;

	@Transient
	private ReportTransmitter contact;

	public void prepareTemplate(Patient patient, Task task, ReportTransmitter contact) {
		prepareTemplate();

		this.patient = patient;
		this.task = task;
		this.contact = contact;

	}

	public void fillTemplate() {
		initVelocity();

		/* create a context and add data */
		VelocityContext context = new VelocityContext();

//		context.put("date", new DateTool());

		if (patient != null)
			context.put("patient", patient);

		if (task != null)
			context.put("task", task);

		if (contact != null)
			context.put("contact", contact);

		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();

		Velocity.evaluate(context, writer, "mystring", getSubject());
		setSubject(writer.toString());

		writer.getBuffer().setLength(0);

		Velocity.evaluate(context, writer, "mystring", getBody());
		setBody(writer.toString());
	}
}
