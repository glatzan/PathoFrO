package com.patho.main.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import com.patho.main.template.MailTemplate;

public interface MailRepository {

	public List<MailTemplate> findAllByTypes(MailTemplate.MailType... types);

	public List<MailTemplate> findAllByTypes(List<MailTemplate.MailType> types);

	public List<MailTemplate> findAll();

	public Optional<MailTemplate> findByID(long id);

	public Optional<MailTemplate> findByTypeAndDefault(MailTemplate.MailType type);

	/**
	 * Loads documents with the correct document classes, copies content form
	 * document to the new object
	 * 
	 * @param document
	 * @return
	 */
	public static MailTemplate loadDocument(MailTemplate document) {
		MailTemplate copy;

		if (document.getTemplateName() == null)
			copy = (MailTemplate) document.clone();
		else {
			try {
				Class<?> myClass = Class.forName(document.getTemplateName());
				Constructor<?> constructor = myClass.getConstructor(new Class[] { MailTemplate.class });
				copy = (MailTemplate) constructor.newInstance(new Object[] { document });
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				copy = (MailTemplate) document.clone();
			}
		}
		return copy;
	}
}
