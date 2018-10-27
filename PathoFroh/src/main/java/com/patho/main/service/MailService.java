package com.patho.main.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.template.MailTemplate;

import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
@ConfigurationProperties(prefix = "patho.mail")
@Getter
@Setter
public class MailService {

	private Settings settings;

	public boolean sendErrorMail(MailTemplate mail) {
		return sendMail(settings.getErrorAddresses(), settings.getSystemMail(), settings.getSystemName(), mail);
	}

	public boolean sendAdminMail(MailTemplate mail) {
		return sendMail(settings.getAdminAddresses(), settings.getSystemMail(), settings.getSystemName(), mail);
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
	public static class Settings {
		private String server;

		private int port;

		private boolean ssl;

		private boolean debug;

		private String systemMail;

		private String systemName;

		private String[] adminAddresses;

		private String[] errorAddresses;
	}
}
