package com.patho.main.adaptors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.gson.Gson;
import com.patho.main.model.interfaces.GsonAble;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.MailTemplate.MailType;
import com.patho.main.util.helper.FileUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailHandler implements GsonAble {

	private MailSettings settings;

	private MailFactory factory;

	private String[] adminRecipients;

	private String[] errorRecipients;

	public boolean sendErrorMail(MailTemplate mail) {
		return sendMail(getErrorRecipients(), settings.getSystemMail(), settings.getSystemName(), mail);
	}

	public boolean sendAdminMail(MailTemplate mail) {
		return sendMail(getAdminRecipients(), settings.getSystemMail(), settings.getSystemName(), mail);
	}

	public boolean sendMail(String mailTo, MailTemplate template) {
		return sendMail(new String[] { mailTo }, settings.getSystemMail(), settings.getSystemName(), template);
	}

	public boolean sendMail(List<String> mailTo, MailTemplate template) {
		return sendMail(mailTo, settings.getSystemMail(), settings.getSystemName(), template);
	}

	public boolean sendMail(String[] mailTo, String mailFrom, String nameFrom, MailTemplate mail) {
		return sendMail(Arrays.asList(mailTo), mailFrom, nameFrom, mail);
	}

	public boolean sendMail(List<String> mailTo, String mailFrom, String nameFrom, MailTemplate mail) {

		if (mail.getAttachment() == null) {
			SimpleEmail email = new SimpleEmail();

			email.setHostName(settings.getServer());
			email.setDebug(settings.isDebug());
			email.setSmtpPort(settings.getPort());
			email.setSSLOnConnect(settings.isSsl());

			try {
				for (String to : mailTo) {
					email.addTo(to);
				}

				email.setFrom(mailFrom, nameFrom);

				email.setSubject(mail.getSubject());
				email.setMsg(mail.getBody());
				email.send();
			} catch (EmailException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			// Create the email message
			MultiPartEmail email = new MultiPartEmail();

			email.setHostName(settings.getServer());
			email.setDebug(settings.isDebug());
			email.setSmtpPort(settings.getPort());
			email.setSSLOnConnect(settings.isSsl());

			try {
				for (String to : mailTo) {
					email.addTo(to);
				}
				email.setFrom(mailFrom, nameFrom);

				email.setSubject(mail.getSubject());
				email.setMsg(mail.getBody());

				InputStream is = new ByteArrayInputStream(mail.getAttachment().getData());

				DataSource source = new ByteArrayDataSource(is, "application/pdf");

				// add the attachment
				email.attach(source, mail.getAttachment().getName(), "");

				// send the email
				email.send();

				is.close();
			} catch (EmailException | IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	@Getter
	@Setter
	public static class MailSettings {

		private String server;

		private int port;

		private boolean ssl;

		private boolean debug;

		private String systemMail;

		private String systemName;
	}

	@Getter
	@Setter
	@Configurable
	public static class MailFactory {

		@Autowired
		private ResourceLoader resourceLoader;

		private String basePath;

		public MailTemplate get(MailType type, HashMap<String, Object> maps) {
			Gson gson = new Gson();
			final Resource fileResource = resourceLoader.getResource(basePath + "/" + type.getTemplateName());
			String result = FileUtil.getContentOfFile(fileResource);

			MailTemplate mailTemplate = gson.fromJson(result, MailTemplate.class);
			mailTemplate.initilize(maps);
			
			return mailTemplate;
		}
	}
}
