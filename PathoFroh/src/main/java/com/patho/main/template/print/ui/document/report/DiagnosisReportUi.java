package com.patho.main.template.print.ui.document.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.print.CustomAddressDialog.CustomAddressReturn;
import com.patho.main.common.ContactRole;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class DiagnosisReportUi extends AbsctractContactUi<DiagnosisReport, DiagnosisReportUi.DiagnosisSharedData> {

	public DiagnosisReportUi(DiagnosisReport report) {
		this(report, new DiagnosisSharedData());
	}

	public DiagnosisReportUi(DiagnosisReport report, DiagnosisSharedData diagnosisSharedData) {
		this(report, (AbstractDocumentUi.SharedData) diagnosisSharedData);
	}

	public DiagnosisReportUi(DiagnosisReport report, AbstractDocumentUi.SharedData sharedData) {
		super(report, (DiagnosisSharedData) sharedData);
		inputInclude = "include/diagnosisReport.xhtml";
	}

	public void initialize(Task task) {
		initialize(task, null);
	}

	public void initialize(Task task, List<ContactSelector> contactList) {
		getSharedData().initializ(task, contactList);
		super.initialize(task);
	}

	/**
	 * 
	 * Change Address Dialog return
	 * 
	 * @param event
	 */
	public void onCustomAddressReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof CustomAddressReturn) {
			// setting manually altered
			((CustomAddressReturn) event.getObject()).getContactSelector().setManuallyAltered(true);
			((CustomAddressReturn) event.getObject()).getContactSelector()
					.setCustomAddress(((CustomAddressReturn) event.getObject()).getCustomAddress());
		}
	}

	/**
	 * Return default template configuration for printing
	 */
	public TemplateConfiguration<DiagnosisReport> getDefaultTemplateConfiguration() {
		printDocument.initilize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(getSharedData().getSelectedDiagnosis())),
				new InitializeToken("address", getSharedData().renderSelectedContact ? getAddressOfFirstSelectedContact() : ""));

		return new TemplateConfiguration<DiagnosisReport>(printDocument);
	}

	/**
	 * Sets the data for the next print
	 */
	public TemplateConfiguration<DiagnosisReport> getNextTemplateConfiguration() {
		String address = contactListPointer != null ? contactListPointer.getCustomAddress() : "";
		printDocument.initilize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(getSharedData().getSelectedDiagnosis())),
				new InitializeToken("address", address));

		return new TemplateConfiguration<DiagnosisReport>(printDocument,
				contactListPointer != null ? contactListPointer.getContact() : null, address,
				contactListPointer != null ? contactListPointer.getCopies() : 1);
	}

	@Getter
	@Setter
	@Configurable
	public static class DiagnosisSharedData extends AbsctractContactUi.SharedContactData {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		protected ResourceBundle resourceBundle;

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

		/**
		 * Initializes the shareddata context.
		 * 
		 * @param task
		 * @param contactList
		 */
		public void initializ(Task task, List<ContactSelector> contactList) {

			if (isInitialized())
				return;

			setDiagnoses(task.getDiagnosisRevisions());
			setDiagnosesTransformer(new DefaultTransformer<>(getDiagnoses()));

			// getting last diagnosis
			setSelectedDiagnosis(getDiagnoses().iterator().next());

			if (contactList == null) {
				// contacts for printing
				setContactList(new ArrayList<ContactSelector>());

				// setting other contacts (physicians)
				getContactList().addAll(ContactSelector.factory(task));

				getContactList().add(new ContactSelector(task,
						new Person(resourceBundle.get("dialog.print.individualAddress"), new Contact()),
						ContactRole.NONE));

				getContactList().add(new ContactSelector(task,
						new Person(resourceBundle.get("dialog.print.blankAddress"), new Contact()), ContactRole.NONE,
						true, true));
			} else
				setContactList(contactList);

			super.initializ();
		}
	}
}
