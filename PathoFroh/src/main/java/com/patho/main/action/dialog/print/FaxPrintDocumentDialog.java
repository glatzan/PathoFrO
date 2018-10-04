package com.patho.main.action.dialog.print;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.common.Dialog;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class FaxPrintDocumentDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactNotificationRepository associatedContactNotificationRepository;

	private PDFContainer pdf;

	private AssociatedContact contact;

	private String number;

	private boolean faxButtonDisabled;

	public void initAndPrepareBean(Task task, AssociatedContact contact, PDFContainer pdf) {
		initBean(task, contact, pdf);
		prepareDialog();
	}

	public void initBean(Task task, AssociatedContact contact, PDFContainer pdf) {
		this.contact = contact;
		this.pdf = pdf;

		if (contact != null)
			this.number = contact.getPerson().getContact().getFax();
		else
			this.number = null;

		updateFaxButton();

		super.initBean(task, Dialog.PRINT_FAX, false);
	}

	public void updateFaxButton() {
		if (HistoUtil.isNotNullOrEmpty(getNumber()))
			setFaxButtonDisabled(false);
		else
			setFaxButtonDisabled(true);
	}

	public void sendFax() {

		try {

			if (getContact() != null) {
				// adding new contact method fax
				AssociatedContactNotification associatedContactNotification = associatedContactService
						.addNotificationType(task, getContact(), NotificationTyp.FAX);

				associatedContactNotification.setPerformed(true);
				associatedContactNotification.setDateOfAction(new Date(System.currentTimeMillis()));
				associatedContactNotification.setContactAddress(getNumber());

				associatedContactNotificationRepository.save(associatedContactNotification,
						resourceBundle.get("log.contact.notification.performed",
								associatedContactNotification.toString(), getContact().toString()));

				log.debug("saving notification status");

			}

			globalSettings.getFaxHandler().sendFax(number, pdf);

		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}

	}
}
