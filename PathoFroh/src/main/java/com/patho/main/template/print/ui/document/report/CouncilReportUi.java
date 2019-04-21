package com.patho.main.template.print.ui.document.report;

import java.util.ArrayList;
import java.util.Set;

import com.patho.main.model.patient.miscellaneous.Council;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.print.CustomAddressDialog.CustomAddressReturn;
import com.patho.main.common.ContactRole;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import com.patho.main.model.patient.Task;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.print.CouncilReport;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class CouncilReportUi extends AbsctractContactUi<CouncilReport, CouncilReportUi.CouncilSharedData> {

	public CouncilReportUi(CouncilReport templateCouncil) {
		this(templateCouncil, new CouncilSharedData());
	}

	public CouncilReportUi(CouncilReport templateCouncil, CouncilReportUi.CouncilSharedData sharedData) {
		this(templateCouncil, (AbstractDocumentUi.SharedData) sharedData);
	}

	public CouncilReportUi(CouncilReport templateCouncil, AbstractDocumentUi.SharedData sharedData) {
		super(templateCouncil, (CouncilSharedData) sharedData);
		inputInclude = "include/councilReport.xhtml";
	}

	public void initialize(Task task) {
		this.initialize(task, new Council());
	}

	public void initialize(Task task, Council selectedCouncil) {
		getSharedData().initializ(task, selectedCouncil);
		super.initialize(task);
	}

	/**
	 * 
	 * Change Address Dialog return
	 * 
	 * @param event
	 */
	public void onCustomAddressReturn(SelectEvent event) {
		logger.debug("Returning from custom address dialog");
		if (event.getObject() != null && event.getObject() instanceof CustomAddressReturn) {
			// setting manually altered
			((CustomAddressReturn) event.getObject()).getContactSelector().setManuallyAltered(true);
			((CustomAddressReturn) event.getObject()).getContactSelector()
					.setCustomAddress(((CustomAddressReturn) event.getObject()).getCustomAddress());

			logger.debug("Custom address set to {}", ((CustomAddressReturn) event.getObject()).getCustomAddress());
		}
	}

	/**
	 * Return default template configuration for printing
	 */
	public TemplateConfiguration<CouncilReport> getDefaultTemplateConfiguration() {
		printDocument.initialize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("council", getSharedData().getSelectedCouncil()), new InitializeToken("address",
						getSharedData().renderSelectedContact ? getAddressOfFirstSelectedContact() : ""));

		return new TemplateConfiguration<CouncilReport>(printDocument);
	}

	/**
	 * Sets the data for the next print
	 */
	public TemplateConfiguration<CouncilReport> getNextTemplateConfiguration() {
		String address = contactListPointer != null ? contactListPointer.getCustomAddress() : "";
		printDocument.initialize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("council", getSharedData().getSelectedCouncil()),
				new InitializeToken("address", address));
		return new TemplateConfiguration<CouncilReport>(printDocument,
				contactListPointer != null ? contactListPointer.getContact() : null, address,
				contactListPointer != null ? contactListPointer.getCopies() : 1);
	}

	@Getter
	@Setter
	@Configurable
	public static class CouncilSharedData extends AbsctractContactUi.SharedContactData {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		protected ResourceBundle resourceBundle;

		private Task task;

		/**
		 * List of all councils
		 */
		private Set<Council> councilList;

		/**
		 * Transformer for council
		 */
		private DefaultTransformer<Council> councilListTransformer;

		/**
		 * Council to print
		 */
		private Council selectedCouncil;

		public void initializ(Task task, Council selectedCouncil) {

			if (isInitialized())
				return;

			setCouncilList(task.getCouncils());
			setCouncilListTransformer(new DefaultTransformer<Council>(councilList));

			setSelectedCouncil(selectedCouncil);

			setTask(task);

			updateContactList();

			super.initializ();
		}

		/**
		 * Updates the contact list for the selected council
		 */
		public void updateContactList() {
			// contacts for printing
			setContactList(new ArrayList<ContactSelector>());

			// only one adress so set as chosen
			if (getSelectedCouncil() != null && getSelectedCouncil().getCouncilPhysician() != null) {
				ContactSelector chosser = new ContactSelector(task,
						getSelectedCouncil().getCouncilPhysician().getPerson(), ContactRole.CASE_CONFERENCE);
				chosser.setSelected(true);
				// setting patient
				getContactList().add(chosser);
			}

			getContactList().add(
					new ContactSelector(task, new Person("Individuelle Addresse", new Contact()), ContactRole.NONE));
		}
	}
}