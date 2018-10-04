package com.patho.main.template;

import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.model.PDFContainer;
import com.patho.main.util.helper.FileUtil;
import com.patho.main.util.helper.StreamUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailTemplate extends AbstractTemplate {

	private PDFContainer attachment;

	private String subject;

	private String body;

	public static <T extends MailTemplate> T getDefaultTemplate(Class<T> mailType) {
		return getTemplates(mailType).stream().filter(p -> p.isDefaultOfType())
				.collect(StreamUtils.singletonCollector());
	}

	public void initilize(HashMap<String, Object> content) {
		initVelocity();
		
		/* create a context and add data */
		VelocityContext context = new VelocityContext();
		
		for(Map.Entry<String, Object> entry : content.entrySet()) {
		    context.put(entry.getKey(), entry.getValue());
		}
		
		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();

		Velocity.evaluate(context, writer, "", getSubject());
		setSubject(writer.toString());

		writer.getBuffer().setLength(0);

		Velocity.evaluate(context, writer, "", getBody());
		setBody(writer.toString());
	}
	
	public static <T extends MailTemplate> List<T> getTemplates(Class<T> mailType) {

		// TODO move to Database
		Type type = new TypeToken<List<MailTemplate>>() {
		}.getType();

		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(type, new TemplateDeserializer<MailTemplate>());

		Gson gson = gb.create();

		ArrayList<MailTemplate> jsonArray = gson.fromJson(FileUtil.getContentOfFile(GlobalSettings.MAIL_TEMPLATES),
				type);

		List<T> result = new ArrayList<T>();

		for (MailTemplate mailTemplate : jsonArray) {
			if (mailTemplate.getTemplateName().equals(mailType.getName())) {
				result.add((T) mailTemplate);
			}
		}

		return result;
	}

	public static List<MailTemplate> loadTemplates() {

		// TODO move to Database
		Type type = new TypeToken<List<MailTemplate>>() {
		}.getType();

		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(type, new TemplateDeserializer<MailTemplate>());

		Gson gson = gb.create();

		ArrayList<MailTemplate> jsonArray = gson.fromJson(FileUtil.getContentOfFile(GlobalSettings.MAIL_TEMPLATES),
				type);

		return jsonArray;
	}

	public static <T extends MailTemplate> T getTemplateByID(long id) {
		return getTemplateByID(loadTemplates(), id);
	}

	public static <T extends MailTemplate> T getTemplateByID(List<MailTemplate> tempaltes, long id) {
		return (T) tempaltes.stream().filter(p -> p.getId() == id).collect(StreamUtils.singletonCollector());
	}

	@Override
	// TODO should be saved in database
	public void prepareTemplate() {
		String file = FileUtil.getContentOfFile(getContent());

		System.out.println(file);

		String[] arr = file.split("\r\n", 2);

		if (arr.length != 2) {
			subject = "Template not found";
			body = "";
			return;
		}

		subject = arr[0].replaceAll("Subject: ", "");
		body = arr[1].replaceAll("Body: ", "");

	}

	public void fillTemplate() {

	}

	@Getter
	public static enum MailType {

		ERROR_MAIL("error.mail"), 
		REQUEST_UNLOCK_MAIL("unlock.mail"), 
		SUCCESS_UNLOCK_MAIL("unlockOk.mail"), 
		DIAGNOSIS_REPORT_MAIL("diagnosisReport.mail");

		private final String templateName;

		private MailType(String templateName) {
			this.templateName = templateName;
		}

	}
}