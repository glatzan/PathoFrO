package com.patho.main.repository.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PrintDocumentRepository;

import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
@ConfigurationProperties(prefix = "patho.settings")
public class PrintDocumentRepositoryImpl implements PrintDocumentRepository {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	private PrintDocument[] documents;

	@PostConstruct
	private void initilizeDocuments() {
		for (PrintDocument printDocument : documents) {
			logger.debug("Initilizing " + printDocument.getName() + " - " + printDocument.getType() + " - Default "
					+ printDocument.isDefaultOfType());
			printDocument.setLoadedContent(mediaRepository.getString(printDocument.getContent()));
		}
	}

	public List<PrintDocument> findAllByTypes(DocumentType... types) {
		return findAllByTypes(Arrays.asList(types));
	}

	public List<PrintDocument> findAllByTypes(List<DocumentType> types) {
		List<PrintDocument> result = new ArrayList<PrintDocument>();

		if (types == null || types.size() == 0)
			return result;

		for (PrintDocument printDocument : documents) {
			for (DocumentType documentType : types) {
				if (printDocument.getDocumentType() == documentType) {
					result.add(loadDocument(printDocument));
				}
			}
		}

		return result;
	}

	public List<PrintDocument> findAll() {
		return Arrays.asList(documents).stream().map(p -> loadDocument(p)).collect(Collectors.toList());
	}

	public Optional<PrintDocument> findByID(long id) {
		for (PrintDocument printDocument : documents) {
			if (printDocument.getId() == id)
				return Optional.ofNullable(loadDocument(printDocument));
		}

		return Optional.empty();
	}

	public Optional<PrintDocument> findByTypeAndDefault(DocumentType type) {
		List<PrintDocument> documents = findAllByTypes(type);
		for (PrintDocument printDocument : documents) {
			if (printDocument.isDefaultOfType())
				return Optional.ofNullable(loadDocument(printDocument));
		}

		return Optional.empty();
	}

	public PrintDocument loadDocument(PrintDocument document) {
		PrintDocument copy;

		if (document.getTemplateName() == null)
			copy = (PrintDocument) document.clone();
		else {
			try {
				Class<?> myClass = Class.forName(document.getTemplateName());
				Constructor<?> constructor = myClass.getConstructor(new Class[] { PrintDocument.class });
				copy = (PrintDocument) constructor.newInstance(new Object[] { document });
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.error("Unknow or Argument class: " + document.getTemplateName());
				copy = (PrintDocument) document.clone();
			}
		}
		return copy;
	}

}
