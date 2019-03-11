package com.patho.main.action.dialog.print;

import java.util.ArrayList;
import java.util.List;

import com.patho.main.model.patient.notification.ReportIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.person.Organization;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.service.FaxService;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class FaxDocumentDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactNotificationRepository associatedContactNotificationRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FaxService faxService;

	private PDFContainer pdf;

	private ReportIntent contact;

	private List<FaxNumberContainer> numbers;

	private String number;

	private boolean faxButtonDisabled;

	public FaxDocumentDialog initAndPrepareBean(Task task, ReportIntent contact, PDFContainer pdf) {
		if (initBean(task, contact, pdf))
			prepareDialog();
		return this;
	}

	public boolean initBean(Task task, ReportIntent contact, PDFContainer pdf) {
		this.contact = contact;
		this.pdf = pdf;

		numbers = new ArrayList<FaxNumberContainer>();
		int i = 0;

		if (contact != null) {
			this.number = contact.getPerson().getContact().getFax();

			// creating number tree for autocomplete
			if (HistoUtil.isNotNullOrEmpty(contact.getPerson().getContact().getFax()))
				numbers.add(new FaxNumberContainer(i++, contact.getPerson().getFullName(),
						contact.getPerson().getContact().getFax()));

			// adding organiziation numbers
			for (Organization organization : contact.getPerson().getOrganizsations()) {
				if (HistoUtil.isNotNullOrEmpty(organization.getContact().getFax()))
					numbers.add(
							new FaxNumberContainer(i++, organization.getName(), organization.getContact().getFax()));
			}

		} else
			this.number = null;

		updateFaxButton();

		return super.initBean(task, Dialog.PRINT_FAX, false);
	}

	public void updateFaxButton() {
		if (HistoUtil.isNotNullOrEmpty(getNumber()))
			setFaxButtonDisabled(false);
		else
			setFaxButtonDisabled(true);
	}

	public void sendFax() {

//		if (getContact() != null) {
//			// adding new contact method fax
//			AssociatedContactNotification associatedContactNotification = associatedContactService
//					.addNotificationType(task, getContact(), NotificationTyp.FAX);
//
//			associatedContactNotification.setPerformed(true);
//			associatedContactNotification.setDateOfAction(new Date(System.currentTimeMillis()));
//			associatedContactNotification.setContactAddress(getNumber());
//
//			associatedContactNotificationRepository.save(associatedContactNotification,
//					resourceBundle.get("log.contact.notification.performed", associatedContactNotification.toString(),
//							getContact().toString()));
//
//			log.debug("saving notification status");
//
//		}

		logger.debug("Sending fax");

		faxService.sendFax(number, pdf);

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class FaxNumberContainer {
		private int id;
		private String name;
		private String number;
	}
}
