package com.patho.main.service;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.ValidatorUtil;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationFeedback;
import com.patho.main.util.pdf.PDFGenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class NotificationService extends AbstractService {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MailService mailService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FaxService faxService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	public void startNotificationPhase(Task task) {
		try {

			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

					task.setNotificationCompletionDate(0);

					genericDAO.savePatientData(task, "log.patient.task.phase.notification.enter");

					if (!task.isListedInFavouriteList(PredefinedFavouriteList.NotificationList)) {
						favouriteListDAO.addReattachedTaskToList(task, PredefinedFavouriteList.NotificationList);
					}
				}
			});
		} catch (Exception e) {
			throw new HistoDatabaseInconsistentVersionException(task);
		}
	}

	public void endNotificationPhase(Task task) {
		try {

			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

					task.setNotificationCompletionDate(System.currentTimeMillis());

					genericDAO.savePatientData(task, "log.patient.task.phase.notification.end");

					favouriteListDAO.removeReattachedTaskFromList(task, PredefinedFavouriteList.NotificationList);
				}
			});
		} catch (Exception e) {
			throw new HistoDatabaseInconsistentVersionException(task);
		}
	}

//	public boolean executeNotification(NotificationFeedback feedback, Task task, MailContainerList mailContainerList,
//			NotificationContainerList faxContainerList, NotificationContainerList letterContainerList,
//			NotificationContainerList phoneContainerList, NotificationContainerList printContainerList,
//			boolean temporaryNotification) {
//
//		boolean emailSendSuccessful = executeMailNotification(feedback, task, mailContainerList, temporaryNotification);
//		boolean faxSendSuccessful = executeFaxNotification(feedback, task, faxContainerList, temporaryNotification);
//		boolean letterSendSuccessful = executeLetterNotification(feedback, task, letterContainerList,
//				temporaryNotification);
//
//		if (printContainerList.isUse() && printContainerList.getDefaultReport() != null) {
//			// addition templates
//			((DiagnosisReport) printContainerList.getDefaultReport()).initData(task, "");
//			PDFContainer report = (new PDFGenerator())
//					.getPDF(((DiagnosisReport) printContainerList.getDefaultReport()));
//
//			userHandlerAction.getSelectedPrinter().print(report, printContainerList.getPrintCount(),
//					printContainerList.getDefaultReport().getAttributes());
//
//		}
//
//		feedback.progressStep();
//

	public boolean endNotification(List<NotificationContainer> emails, List<NotificationContainer> faxes, List<NotificationContainer> letters, List<NotificationContainer> phones) {
		
		PDFContainer sendReport = generateSendReport(feedback, task, mailContainerList, faxContainerList,
				letterContainerList, phoneContainerList, new Date(), temporaryNotification);

		// setting notification als completed
		for (DiagnosisRevision  : printContainerList.getSelectedRevisions()) {
			revision.setNotificationPending(false);
			revision.setNotificationDate(System.currentTimeMillis());
		}

		genericDAO.savePatientData(task, "log.patient.task.notification.send");

		pdfService.attachPDF(task.getPatient(), task, sendReport);

		return emailSendSuccessful && faxSendSuccessful && letterSendSuccessful;

	}

	public boolean emailNotification(NotificationContainer notificationContainer, Task task, MailTemplate template,
			NotificationFeedback feedback, boolean recreateAfterNotification) {
		boolean success = true;

		ValidatorUtil val = new ValidatorUtil();

		try {

			// copy contact address before sending -> save before error
			notificationContainer.getNotification().setContactAddress(notificationContainer.getContactAddress());

			logger.debug("Send mail to " + notificationContainer.getContactAddress());

			// checking email
			if (!val.approveMailAddress(notificationContainer.getContactAddress()))
				throw new IllegalArgumentException("dialog.notification.sendProcess.mail.error.mailNotValid");

			template.setAttachment(notificationContainer.getPdf());

			if (!mailService.sendMail(notificationContainer.getContactAddress(), template))
				throw new IllegalArgumentException("dialog.notification.sendProcess.mail.error.failed");

			feedback.setFeedback("dialog.notification.sendProcess.mail.send",
					notificationContainer.getContactAddress());

			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get("dialog.notification.sendProcess.mail.success"));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(false);

			logger.debug("Sending completed " + notificationContainer.getNotification().getCommentary());

		} catch (IllegalArgumentException e) {
			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get(e.getMessage(), notificationContainer.getContactAddress()));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(false);

			success = false;

			logger.debug("Sending failed " + notificationContainer.getNotification().getCommentary());
		}

		// renew if temporary notification
		if (recreateAfterNotification)
			associatedContactService.renewNotification(notificationContainer.getContact(),
					notificationContainer.getNotification(), false);

		feedback.progressStep();

		return success;
	}

	public boolean faxNotification(NotificationContainer notificationContainer, Task task,
			NotificationFeedback feedback, boolean print, boolean send, boolean recreateAfterNotification) {

		boolean success = true;
		ValidatorUtil val = new ValidatorUtil();

		try {
			// copy contact address before sending -> save before error
			notificationContainer.getNotification().setContactAddress(notificationContainer.getContactAddress());

			if (!val.approveFaxAddress(notificationContainer.getContactAddress()))
				throw new IllegalArgumentException("dialog.notification.sendProcess.fax.error.numberNotValid");

			if (print) {
				// sending feedback
				feedback.setFeedback("dialog.notification.sendProcess.pdf.print");
				userHandlerAction.getSelectedPrinter().print(notificationContainer.getPdf());
			}

			if (send) {
				faxService.sendFax(notificationContainer.getContactAddress(), notificationContainer.getPdf());
				feedback.setFeedback("dialog.notification.sendProcess.fax.send",
						notificationContainer.getContactAddress());
			}

			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get("dialog.notification.sendProcess.fax.success"));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(true);

		} catch (IllegalArgumentException e) {
			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get(e.getMessage(), notificationContainer.getContactAddress()));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(true);

			success = false;

			logger.debug("Sending failed" + notificationContainer.getNotification().getCommentary());
		}

		// renew if temporary notification
		if (recreateAfterNotification)
			associatedContactService.renewNotification(notificationContainer.getContact(),
					notificationContainer.getNotification(), false);

		return success;
	}

	public boolean letterNotification(NotificationContainer notificationContainer, Task task,
			NotificationFeedback feedback, boolean print, boolean recreateAfterNotification) {

		boolean success = true;
		ValidatorUtil val = new ValidatorUtil();

		try {
			// copy contact address before sending -> save before error
			notificationContainer.getNotification().setContactAddress(notificationContainer.getContactAddress());

			if (!val.approvePostalAddress(notificationContainer.getContactAddress()))
				throw new IllegalArgumentException("");

			feedback.setFeedback("dialog.notification.sendProcess.pdf.print");
			userHandlerAction.getSelectedPrinter().print(notificationContainer.getPdf());

			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get("dialog.notification.sendProcess.pdf.print"));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(true);

		} catch (IllegalArgumentException e) {
			notificationContainer.getNotification().setPerformed(true);
			notificationContainer.getNotification().setDateOfAction(new Date());
			notificationContainer.getNotification()
					.setCommentary(resourceBundle.get(e.getMessage(), notificationContainer.getContactAddress()));
			// if success = performed, nothing to do = inactive, if failed = active
			notificationContainer.getNotification().setActive(false);
			// if success = !failed = false
			notificationContainer.getNotification().setFailed(true);

			success = false;

			logger.debug("Sending failed" + notificationContainer.getNotification().getCommentary());
		}

		// renew if temporary notification
		if (recreateAfterNotification)
			associatedContactService.renewNotification(notificationContainer.getContact(),
					notificationContainer.getNotification(), false);

		return success;

	}

	public boolean phoneNotification(NotificationContainer notificationContainer, Task task,
			NotificationFeedback feedback, boolean recreateAfterNotification) {

		notificationContainer.getNotification().setPerformed(true);
		notificationContainer.getNotification().setDateOfAction(new Date());
		notificationContainer.getNotification()
				.setCommentary(resourceBundle.get("dialog.notification.sendProcess.pdf.print"));
		// if success = performed, nothing to do = inactive, if failed = active
		notificationContainer.getNotification().setActive(false);
		// if success = !failed = false
		notificationContainer.getNotification().setFailed(true);

		// renew if temporary notification
		if (recreateAfterNotification)
			associatedContactService.renewNotification(notificationContainer.getContact(),
					notificationContainer.getNotification(), false);

		return true;
	}

//	public PDFContainer generateSendReport(NotificationFeedback feedback, Task task,
//			MailContainerList mailContainerList, NotificationContainerList faxContainerList,
//			NotificationContainerList letterContainerList, NotificationContainerList phoneContaienrList,
//			Date notificationDate, boolean temporarayNotification) {
//
//		feedback.setFeedback("log.notification.pdf.sendReport.generation");
//
//		SendReport sendReport = DocumentTemplate
//				.getTemplateByID(globalSettings.getDefaultDocuments().getNotificationSendReport());
//
//		sendReport.initializeTempalte(task, mailContainerList, faxContainerList, letterContainerList,
//				phoneContaienrList, notificationDate, temporarayNotification);
//
//		PDFContainer container = (new PDFGenerator()).getPDF(sendReport);
//
//		return container;
//	}

	/**
	 * Returns a pdf container for an contact. IF the contact has its own container,
	 * that container is returned. IF individualAddresses is true a new pdf with the
	 * address of the of the container will be generated. Otherwise a generic pdf
	 * will be returned.
	 */
	public PDFContainer getPDFForContainer(NotificationContainer container, Task task,
			DiagnosisRevision diagnosisRevision, PrintDocument template, boolean individualAddresses,
			PDFContainer genericPDF, NotificationFeedback feedback) {

		if (container.getPdf() != null) {
			// pdf was selected for the individual contact
			// adding pdf to generated pdf array
			return container.getPdf();
		} else if (template != null) {
			if (!individualAddresses) {
				// setting generic pdf
				container.setPdf(genericPDF);

				logger.debug("Returning generic pdf " + genericPDF);
			} else {
				// individual address
				String reportAddressField = AssociatedContactService.generateAddress(container.getContact(),
						container.getContact().getPerson().getDefaultAddress());

				logger.debug("Generating pdf for " + reportAddressField);
				feedback.setFeedback("dialog.notification.sendProcess.pdf.generating",
						container.getContact().getPerson().getFullName());

				container.setPdf(generatePDF(task, diagnosisRevision, reportAddressField, template));

				logger.debug("Returning individual address");
			}

			return container.getPdf();
		}

		logger.debug("Returning no pdf");

		return null;
	}

	public PDFContainer generatePDF(Task task, DiagnosisRevision diagnosisRevision, String address,
			PrintDocument document) {
		PDFGenerator generator = new PDFGenerator();

		document.initilize(new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
				new InitializeToken("patient", task.getPatient()), new InitializeToken("address", address),
				new InitializeToken("subject", ""));

		PDFContainer container = generator.getPDF(document, new File(pathoConfig.getFileSettings().getPrintDirectory()),
				false);

		return container;
	}
}
