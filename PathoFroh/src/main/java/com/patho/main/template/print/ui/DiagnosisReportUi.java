package com.patho.main.template.print.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Contact;
import com.patho.main.model.Person;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument.InitializeToken;
import com.patho.main.template.print.CouncilReport;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class DiagnosisReportUi extends AbsctractContactUi<DiagnosisReport> {

	/**
	 * List of all diagnoses
	 */
	private Set<DiagnosisRevision> diagnoses;

	/**
	 * Transformer for diagnoses
	 */
	private DefaultTransformer<DiagnosisRevision> diagnosesTransformer;

	/**
	 * Selected diangosis
	 */
	private DiagnosisRevision selectedDiagnosis;

	public DiagnosisReportUi(DiagnosisReport report) {
		super(report);
		inputInclude = "include/diagnosisReport.xhtml";
	}

	public void initialize(Task task) {
		initialize(task, null);
	}

	public void initialize(Task task, List<ContactSelector> contactList) {
		super.initialize(task);

		setDiagnoses(task.getDiagnosisRevisions());
		setDiagnosesTransformer(new DefaultTransformer<>(getDiagnoses()));

		// getting last diagnosis
		setSelectedDiagnosis(getDiagnoses().iterator().next());

		if (contactList == null) {
			// contacts for printing
			setContactList(new ArrayList<ContactSelector>());

			// setting other contacts (physicians)
			getContactList().addAll(ContactSelector.factory(task));

			// TODO resources bundel == null
			System.out.println(resourceBundle);

			getContactList().add(
					new ContactSelector(task, new Person("Individuelle Addresse", new Contact()), ContactRole.NONE));

			getContactList().add(new ContactSelector(task, new Person("Leere Adresse", new Contact()), ContactRole.NONE,
					true, true));

			// getContactList().add(new ContactSelector(task,
			// new Person(resourceBundle.get("dialog.print.individualAddress"), new
			// Contact()), ContactRole.NONE));
			//
			// getContactList().add(new ContactSelector(task,
			// new Person(resourceBundle.get("dialog.print.blankAddress"), new Contact()),
			// ContactRole.NONE, true,
			// true));
		} else
			setContactList(contactList);

	}

	/**
	 * Return default template configuration for printing
	 */
	public TemplateConfiguration<DiagnosisReport> getDefaultTemplateConfiguration() {
		printDocument.initilize(
				new InitializeToken("patient", task.getParent()), 
				new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(selectedDiagnosis)),
				new InitializeToken("address", renderSelectedContact ? getAddressOfFirstSelectedContact() : ""));

		return new TemplateConfiguration<DiagnosisReport>(printDocument);
	}

	/**
	 * Sets the data for the next print
	 */
	public TemplateConfiguration<DiagnosisReport> getNextTemplateConfiguration() {
		String address = contactListPointer != null ? contactListPointer.getCustomAddress() : "";
		printDocument.initilize(
				new InitializeToken("patient", task.getParent()), 
				new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(selectedDiagnosis)),
				new InitializeToken("address", address));
		
		printDocument.setCopies(contactListPointer != null ? contactListPointer.getCopies() : 1);
		return new TemplateConfiguration<DiagnosisReport>(printDocument,
				contactListPointer != null ? contactListPointer.getContact() : null, address);
	}
}
