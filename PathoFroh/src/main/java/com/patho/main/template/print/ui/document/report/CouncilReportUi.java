package com.patho.main.template.print.ui.document.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.patho.main.common.ContactRole;
import com.patho.main.model.Contact;
import com.patho.main.model.Council;
import com.patho.main.model.Person;
import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument.InitializeToken;
import com.patho.main.template.print.CouncilReport;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class CouncilReportUi extends AbsctractContactUi<CouncilReport, AbsctractContactUi.SharedContactData> {

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

	public CouncilReportUi(CouncilReport templateCouncil) {
		this(templateCouncil, new AbsctractContactUi.SharedContactData());
	}

	public CouncilReportUi(CouncilReport templateCouncil, AbsctractContactUi.SharedContactData sharedData) {
		super(templateCouncil, sharedData);
		inputInclude = "include/councilReport.xhtml";
	}

	public CouncilReportUi(CouncilReport templateCouncil, AbstractDocumentUi.SharedData sharedData) {
		super(templateCouncil, (AbsctractContactUi.SharedContactData) sharedData);
		inputInclude = "include/councilReport.xhtml";
	}

	public void initialize(Task task) {
		this.initialize(task, new Council());
	}

	public void initialize(Task task, Council council) {
		super.initialize(task);

		setCouncilList(task.getCouncils());
		setCouncilListTransformer(new DefaultTransformer<Council>(councilList));

		setSelectedCouncil(council);

		updateContactList();
	}

	/**
	 * Updates the contact list for the selected council
	 */
	public void updateContactList() {
		// contacts for printing
		getSharedData().setContactList(new ArrayList<ContactSelector>());

		// only one adress so set as chosen
		if (getSelectedCouncil() != null && getSelectedCouncil().getCouncilPhysician() != null) {
			ContactSelector chosser = new ContactSelector(task, getSelectedCouncil().getCouncilPhysician().getPerson(),
					ContactRole.CASE_CONFERENCE);
			chosser.setSelected(true);
			// setting patient
			getSharedData().getContactList().add(chosser);
		}

		getSharedData().getContactList()
				.add(new ContactSelector(task, new Person("Individuelle Addresse", new Contact()), ContactRole.NONE));

		// getContactList().add(new ContactSelector(task,
		// new Person(resourceBundle.get("dialog.print.individualAddress"), new
		// Contact()), ContactRole.NONE));
	}

	/**
	 * Return default template configuration for printing
	 */
	public TemplateConfiguration<CouncilReport> getDefaultTemplateConfiguration() {
		printDocument.initilize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("council", getSelectedCouncil()),
				new InitializeToken("address", renderSelectedContact ? getAddressOfFirstSelectedContact() : ""));

		return new TemplateConfiguration<CouncilReport>(printDocument);
	}

	/**
	 * Sets the data for the next print
	 */
	public TemplateConfiguration<CouncilReport> getNextTemplateConfiguration() {
		String address = contactListPointer != null ? contactListPointer.getCustomAddress() : "";
		printDocument.initilize(new InitializeToken("patient", task.getParent()), new InitializeToken("task", task),
				new InitializeToken("council", getSelectedCouncil()), new InitializeToken("address", address));
		return new TemplateConfiguration<CouncilReport>(printDocument,
				contactListPointer != null ? contactListPointer.getContact() : null, address,
				contactListPointer != null ? contactListPointer.getCopies() : 1);
	}
}