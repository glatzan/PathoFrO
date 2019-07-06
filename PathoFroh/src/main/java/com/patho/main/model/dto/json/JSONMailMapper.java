package com.patho.main.model.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.patho.main.template.MailTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONMailMapper {

	private String subject;
	private String body;

	public MailTemplate updateMailTemplate(MailTemplate template) {
		template.setSubject(subject);
		template.setBody(body);
		return template;
	}
}
