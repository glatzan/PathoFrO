package com.patho.main.template.print.ui.document;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.InitializeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class AbstractDocumentUi<T extends PrintDocument, S extends AbstractDocumentUi.SharedData> implements ID {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;

	/**
	 * The print Template
	 */
	protected T printDocument;

	/**
	 * Shared Data between ui objects
	 */
	protected S sharedData;

	/**
	 * Task
	 */
	protected Task task;

	/**
	 * True if task is set
	 */
	protected boolean initialized;

	/**
	 * If true the pdf will be updated on every settings change
	 */
	protected boolean updatePdfOnEverySettingChange = false;

	/**
	 * If true the first selected contact will be rendered
	 */
	protected boolean renderSelectedContact = false;

	/**
	 * String for input include
	 */
	protected String inputInclude = "include/empty.xhtml";

	public AbstractDocumentUi(T printDocument, S sharedData) {
		this.printDocument = printDocument;
		this.sharedData = sharedData;
	}

	public void initialize(Task task) {
		this.task = task;
	}

	/**
	 * Starts an iteration through all template configuration
	 */
	public void beginNextTemplateIteration() {
	}

	/**
	 * Returns true if a template configuration is left
	 * 
	 * @return
	 */
	public boolean hasNextTemplateConfiguration() {
		return false;
	}

	/**
	 * Returns the next template configuration
	 * 
	 * @return
	 */
	public TemplateConfiguration<T> getNextTemplateConfiguration() {
		return null;
	}

	/**
	 * Returns a default template configuration for rendering in the gui
	 * 
	 * @return
	 */
	public TemplateConfiguration<T> getDefaultTemplateConfiguration() {
		printDocument.initilize(new InitializeToken("task", task));
		return new TemplateConfiguration<T>(printDocument);
	}

	@Override
	public long getId() {
		return printDocument.getId();
	}

	/**
	 * Loads for a list of print templates all ui objects, if a sharedData context
	 * exsists the data will be shared between objects.
	 * 
	 * @param documents
	 * @return
	 */
	public static List<AbstractDocumentUi<?, ?>> factory(List<PrintDocument> documents) {
		HashMap<Integer, SharedData> sharedGroups = new HashMap<Integer, SharedData>();

		List<AbstractDocumentUi<?, ?>> result = new ArrayList<AbstractDocumentUi<?, ?>>(documents.size());

		for (PrintDocument printDocument : documents) {
			// shared group exsist
			if (printDocument.getSharedContextGroup() != 0) {
				AbstractDocumentUi.SharedData sharedData = sharedGroups.get(printDocument.getSharedContextGroup());

				if (sharedData != null) {
					result.add(PrintDocumentRepository.loadUiClass(printDocument, sharedData));
				} else {
					AbstractDocumentUi<?, ?> tmp = PrintDocumentRepository.loadUiClass(printDocument);
					sharedGroups.put(printDocument.getSharedContextGroup(), tmp.getSharedData());
					result.add(tmp);
				}
			} else {
				result.add(PrintDocumentRepository.loadUiClass(printDocument));
			}
		}

		return result;
	}

	/**
	 * Return container for generated template
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	public class TemplateConfiguration<I extends PrintDocument> {
		private I documentTemplate;
		private AssociatedContact contact;
		private String address;
		private int copies;

		public TemplateConfiguration(I documentTemplate) {
			this(documentTemplate, null, "", 1);
		}

		public TemplateConfiguration(I documentTemplate, AssociatedContact contact, String address, int copies) {
			this.documentTemplate = documentTemplate;
			this.contact = contact;
			this.address = address;
			this.copies = copies;
		}
	}

	/**
	 * Data which can be share between Ui Objects.
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	public static class SharedData {
		private boolean initialized;

		public void initializ() {
			this.initialized = true;
		}
	}
}
