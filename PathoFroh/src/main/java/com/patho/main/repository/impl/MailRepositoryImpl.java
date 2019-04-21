package com.patho.main.repository.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.patho.main.template.MailType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patho.main.model.dto.json.JSONMailMapper;
import com.patho.main.repository.MailRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.template.MailTemplate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@ConfigurationProperties(prefix = "patho.settings.mail")
@Getter
@Setter
public class MailRepositoryImpl implements MailRepository {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	private MailTemplate[] mails;

	@PostConstruct
	private void initializeDocuments() {
		for (MailTemplate mail : mails) {
			logger.debug(
					"Initilizing " + mail.getName() + " - " + mail.getType() + " - Default " + mail.getDefaultOfType());

			String jsonContent = mediaRepository.getString(mail.getContent());
			
			ObjectMapper mapper = new ObjectMapper();

			try {
				JSONMailMapper userMapper = mapper.readValue(jsonContent, JSONMailMapper.class);
				userMapper.updateMailTemplate(mail);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<MailTemplate> findAllByTypes(MailType... types) {
		return findAllByTypes(Arrays.asList(types));
	}

	public List<MailTemplate> findAllByTypes(List<MailType> types) {
		List<MailTemplate> result = new ArrayList<MailTemplate>();

		if (types == null || types.size() == 0)
			return result;

		// return a copy of the printDocument
		for (MailTemplate mail : mails) {
			for (MailType type : types) {
				if (mail.getDocumentType() == type) {
					result.add(MailRepository.loadDocument(mail));
				}
			}
		}

		return result;
	}

	public List<MailTemplate> findAll() {
		return Arrays.asList(mails).stream().map(p -> MailRepository.loadDocument(p)).collect(Collectors.toList());
	}

	public Optional<MailTemplate> findByID(long id) {
		for (MailTemplate mail : mails) {
			if (mail.getId() == id)
				return Optional.ofNullable(MailRepository.loadDocument(mail));
		}

		return Optional.empty();
	}

	public Optional<MailTemplate> findByTypeAndDefault(MailType type) {
		List<MailTemplate> mails = findAllByTypes(type);
		for (MailTemplate mail : mails) {
			if (mail.getDefaultOfType())
				return Optional.ofNullable(MailRepository.loadDocument(mail));
		}

		return Optional.empty();
	}
}
