package com.patho.main.util.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.util.pdf.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for executing notification. Should be extended by notification types.
 */
@Configurable
@Slf4j
public class NotificationExecutor<T extends NotificationContainer> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected GlobalSettings globalSettings;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected UserHandlerAction userHandlerAction;

	/**
	 * Feedback handler
	 */
	protected NotificationFeedback feedback;

	/**
	 * List of all generated pdfs
	 */
	protected List<PDFContainer> generatedContainers = new ArrayList<PDFContainer>();

	/**
	 * Generic pdf for all notification with no individual addresses.
	 */
	protected PDFContainer genericPDF;

	public NotificationExecutor(NotificationFeedback feedback) {
		this.feedback = feedback;
	}

	/**
	 * Pattern check of the Address, returns true if matches
	 * 
	 * @return
	 */
	public boolean isAddressApproved(String address) {
		return false;
	}

	/**
	 * Returns a pdf container for an contact. IF the contact has its own container,
	 * that container is returned. IF individualAddresses is true a new pdf with the
	 * address of the of the container will be generated. Otherwise a generic pdf
	 * will be returned.
	 */
	public PDFContainer getPDF(T container, Task task, DiagnosisReport template,
			List<DiagnosisRevision> diagnosisRevisions, boolean individualAddresses) {
		if (container.getPdf() != null) {
			// pdf was selected for the individual contact
			// adding pdf to generated pdf array
			generatedContainers.add(container.getPdf());
			return container.getPdf();
		} else if (template != null) {

			// generating pdf for contact
			PDFContainer result;

			if (!individualAddresses) {
				// creating default report if not present
				if (genericPDF == null) {
					log.debug("Generating generic pdf");
					// setting feedback
					feedback.setFeedback("dialog.notification.sendProcess.pdf.generic");
					template.initData(task, diagnosisRevisions, "");
					genericPDF = new PDFGenerator().getPDF(template);

					// adding pdf to generated pdf array
					generatedContainers.add(genericPDF);
				}

				log.debug("Returning generic pdf " + genericPDF);

				result = genericPDF;
			} else {
				// individual address
				String reportAddressField = AssociatedContact.generateAddress(container.getContact(), container.getContact().getPerson().getDefaultAddress());
				log.debug("Generating pdf for " + reportAddressField);
				feedback.setFeedback("dialog.notification.sendProcess.pdf.generating",
						container.getContact().getPerson().getFullName());
				template.initData(task, diagnosisRevisions, reportAddressField);
				result = new PDFGenerator().getPDF(template);

				log.debug("Returning individual address");

				// adding pdf to generated pdf array
				generatedContainers.add(result);
			}

			return result;
		}

		log.debug("Returning no pdf");

		return null;
	}

	/**
	 * Prints and/or calls the send method to perform the notification
	 * 
	 * @param container
	 * @param send
	 * @param print
	 * @return
	 */
	public boolean performNotification(T container, boolean send, boolean print) {
		if (print) {
			// sending feedback
			feedback.setFeedback("dialog.notification.sendProcess.pdf.print");
			userHandlerAction.getSelectedPrinter().print(container.getPdf());
		}

		if (send)
			return performSend(container);

		return true;
	}

	/**
	 * Should perform the notification
	 * 
	 * @param container
	 * @return
	 */
	protected boolean performSend(T container) {
		return false;
	}

	/**
	 * Sets data for the AssociatedContactNotification on notification performed.
	 * 
	 * @param container
	 * @param success
	 * @param message
	 */
	public void finishSendProecess(T container, boolean success, String message) {
		container.getNotification().setPerformed(true);
		container.getNotification().setDateOfAction(new Date(System.currentTimeMillis()));
		container.getNotification().setCommentary(message);
		// if success = performed, nothing to do = inactive, if failed = active
		container.getNotification().setActive(!success);
		// if success = !failed = false
		container.getNotification().setFailed(!success);
	}

	/**
	 * Returns a list of all generated pdf containers generated within this
	 * notification executor.
	 * 
	 * @return
	 */
	public List<PDFContainer> getAllGeneratedPDFs() {
		return generatedContainers;
	}
}
