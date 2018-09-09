package com.patho.main.template.print.ui;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.InitializeToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class AbstractDocumentUi<T extends PrintDocument> implements ID {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;

	protected T printDocument;

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

	public AbstractDocumentUi(T printDocument) {
		this.printDocument = printDocument;
	}

	public void initialize(Task task) {
		this.task = task;
	}

	public void beginNextTemplateIteration() {
	}

	public boolean hasNextTemplateConfiguration() {
		return false;
	}

	public TemplateConfiguration<T> getNextTemplateConfiguration() {
		return null;
	}

	public TemplateConfiguration<T> getDefaultTemplateConfiguration() {
		printDocument.initilize(new InitializeToken("task", task));
		return new TemplateConfiguration<T>(printDocument);
	}

	@Override
	public long getId() {
		return printDocument.getId();
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

		public TemplateConfiguration(I documentTemplate) {
			this(documentTemplate, null, "");
		}

		public TemplateConfiguration(I documentTemplate, AssociatedContact contact, String address) {
			this.documentTemplate = documentTemplate;
			this.contact = contact;
			this.address = address;
		}
	}

}
