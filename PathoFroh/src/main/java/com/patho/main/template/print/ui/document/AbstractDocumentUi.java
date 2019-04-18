package com.patho.main.template.print.ui.document;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Configurable
public class AbstractDocumentUi<T extends PrintDocument, S extends AbstractDocumentUi.SharedData> implements ID {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
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

	/**
	 * Returns the first contact which is selected, for templates without contacts
	 * null is returned
	 * 
	 * @return
	 */
	public ReportIntent getFirstSelectedContact() {
		return null;
	}

	@Override
	public long getId() {
		return printDocument.getId();
	}

	/**
	 * Loads a gui object for a print template
	 * 
	 * @param document
	 * @return
	 */
	public static AbstractDocumentUi<?, ?> factory(PrintDocument document) {
		return factory(Arrays.asList(document)).get(0);
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

	@Override
	public void setId(long l) {
	}

	public Logger getLogger() {
		return this.logger;
	}

	public T getPrintDocument() {
		return this.printDocument;
	}

	public S getSharedData() {
		return this.sharedData;
	}

	public Task getTask() {
		return this.task;
	}

	public String getInputInclude() {
		return this.inputInclude;
	}

	public void setPrintDocument(T printDocument) {
		this.printDocument = printDocument;
	}

	public void setSharedData(S sharedData) {
		this.sharedData = sharedData;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setInputInclude(String inputInclude) {
		this.inputInclude = inputInclude;
	}

	/**
	 * Return container for generated template
	 * 
	 * @author andi
	 *
	 */
	public class TemplateConfiguration<I extends PrintDocument> {
		private I documentTemplate;
		private ReportIntent contact;
		private String address;
		private int copies;

		public TemplateConfiguration(I documentTemplate) {
			this(documentTemplate, null, "", 1);
		}

		public TemplateConfiguration(I documentTemplate, ReportIntent contact, String address, int copies) {
			this.documentTemplate = documentTemplate;
			this.contact = contact;
			this.address = address;
			this.copies = copies;
		}

		public I getDocumentTemplate() {
			return this.documentTemplate;
		}

		public ReportIntent getContact() {
			return this.contact;
		}

		public String getAddress() {
			return this.address;
		}

		public int getCopies() {
			return this.copies;
		}

		public void setDocumentTemplate(I documentTemplate) {
			this.documentTemplate = documentTemplate;
		}

		public void setContact(ReportIntent contact) {
			this.contact = contact;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setCopies(int copies) {
			this.copies = copies;
		}
	}

	/**
	 * Data which can be share between Ui Objects.
	 * 
	 * @author andi
	 *
	 */
	public static class SharedData {

		private boolean initialized;

		public void initializ() {
			this.initialized = true;
		}

		public ReportIntent getSelectedContact() {
			return null;
		}

		public boolean isInitialized() {
			return this.initialized;
		}

		public void setInitialized(boolean initialized) {
			this.initialized = initialized;
		}
	}
}
