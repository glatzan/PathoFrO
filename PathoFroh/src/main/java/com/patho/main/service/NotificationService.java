package com.patho.main.service;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.patho.main.model.patient.DiagnosisRevision.NotificationStatus;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.DiagnosisRevisionRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.ValidatorUtil;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationPerformer;
import com.patho.main.util.notification.NotificationFeedback;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.pdf.PrintOrder;
import com.patho.main.util.printer.TemplatePDFContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class NotificationService extends AbstractService {

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
	private DiagnosisRevisionRepository diagnosisRevisionRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

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

	public boolean performeNotification(NotificationPerformer performer, NotificationFeedback feedback) {

		feedback.setFeedback("Generating Generic Report");
		TemplatePDFContainer genericReport = performer.getGenericReport();

		if (performer.isPrintDocument()) {
			// printing generic report
			userHandlerAction.getSelectedPrinter().print(new PrintOrder(genericReport, performer.getPrintCount()));
		}

		boolean success = true;
		
		if (performer.isUseMail()) {
			for (NotificationContainer mail : performer.getMails()) {
				feedback.setFeedback("dialog.notification.sendProcess.mail.send", mail.getContactAddress());
				mail.setPdf(performer.getPDFForContainer(mail, performer.getMailTemplate(),
						performer.isIndividualMailAddress(), genericReport));
				feedback.setFeedback("dialog.notification.sendProcess.mail.send", mail.getContactAddress());
				success &= emailNotification(mail, performer.getTask(), performer.getMail(), performer.isReperformNotification());
				feedback.progressStep();
			}
		}

		if (performer.isUseFax()) {
			for (NotificationContainer fax : performer.getFaxes()) {

				feedback.setFeedback("dialog.notification.sendProcess.pdf.generating");
				fax.setPdf(performer.getPDFForContainer(fax, performer.getFaxTemplate(),
						performer.isIndividualFaxAddress(), genericReport));

				feedback.setFeedback("dialog.notification.sendProcess.fax.send", fax.getContactAddress());
				success &=  faxNotification(fax, performer.getTask(), performer.isSendFax(), performer.isPrintFax(),
						performer.isReperformNotification());

				feedback.progressStep();
			}
		}

		if (performer.isUseLetter()) {
			for (NotificationContainer letter : performer.getFaxes()) {
				feedback.setFeedback("dialog.notification.sendProcess.pdf.print");
				letter.setPdf(performer.getPDFForContainer(letter, performer.getLetterTemplate(),
						performer.isIndividualLetterAddress(), genericReport));
				feedback.setFeedback("dialog.notification.sendProcess.pdf.generating");
				success &= letterNotification(letter, performer.getTask(), true, performer.isReperformNotification());
			}
		}
		
		if(performer.isUsePhone()) {
			for (NotificationContainer phone : performer.getPhonenumbers()) {
				success &= phoneNotification(phone, performer.getTask(), performer.isReperformNotification());
			}
		}
		
		endNotification(document, task, diagnosisRevision, emails, faxes, letters, phones)

		return true;
	}

	public boolean endNotification(Task task, DiagnosisRevision diagnosisRevision, List<NotificationContainer> emails,
			List<NotificationContainer> faxes, List<NotificationContainer> letters, List<NotificationContainer> phones,
			boolean success) {

		Optional<PrintDocument> document = printDocumentRepository
				.findByID(pathoConfig.getDefaultDocuments().getNotificationSendReport());

		if (!document.isPresent()) {
			logger.debug("Printdocument not found");
			return false;
		}


		
		document.get().initilize(new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
				new InitializeToken("patient", task.getPatient()), new InitializeToken("useMail", !emails.isEmpty()),
				new InitializeToken("useMail", !emails.isEmpty()), new InitializeToken("mails", emails),
				new InitializeToken("useFax", !emails.isEmpty()), new InitializeToken("faxes", emails),
				new InitializeToken("useLetter", !emails.isEmpty()), new InitializeToken("letters", emails),
				new InitializeToken("usePhone", !emails.isEmpty()), new InitializeToken("phonenumbers", emails),
				new InitializeToken("reportDate", new Date()));

		new PDFCreator()
		
		PDFReturn pdfReturn = pdfService.createAndAttachPDF(task, document.get().getDocumentType(),
				document.get().getGeneratedFileName(), "", "", true, task.getParent());

		PDFContainer container = generator.getPDF(document, pdfReturn.getContainer());

		diagnosisRevision.setNotificationStatus(NotificationStatus.NOTIFICATION_COMPLETED);
		diagnosisRevision.setNotificationDate(System.currentTimeMillis());

		diagnosisRevisionRepository.save(diagnosisRevision, "log.patient.task.notification.send", task.getPatient());

		return true;
	}

	public boolean emailNotification(NotificationContainer notificationContainer, Task task, MailTemplate template,
			boolean recreateAfterNotification) {
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

		return success;
	}

	public boolean faxNotification(NotificationContainer notificationContainer, Task task, boolean print, boolean send,
			boolean recreateAfterNotification) {

		boolean success = true;
		ValidatorUtil val = new ValidatorUtil();

		try {
			// copy contact address before sending -> save before error
			notificationContainer.getNotification().setContactAddress(notificationContainer.getContactAddress());

			if (!val.approveFaxAddress(notificationContainer.getContactAddress()))
				throw new IllegalArgumentException("dialog.notification.sendProcess.fax.error.numberNotValid");

			if (print) {
				// sending feedback
				userHandlerAction.getSelectedPrinter().print(new PrintOrder(notificationContainer.getPdf(), 1));
			}

			if (send) {
				faxService.sendFax(notificationContainer.getContactAddress(), notificationContainer.getPdf());
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

	public boolean letterNotification(NotificationContainer notificationContainer, Task task, boolean print,
			boolean recreateAfterNotification) {

		boolean success = true;
		ValidatorUtil val = new ValidatorUtil();

		try {
			// copy contact address before sending -> save before error
			notificationContainer.getNotification().setContactAddress(notificationContainer.getContactAddress());

			if (!val.approvePostalAddress(notificationContainer.getContactAddress()))
				throw new IllegalArgumentException("");

			userHandlerAction.getSelectedPrinter().print(new PrintOrder(notificationContainer.getPdf(), 1));

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
			boolean recreateAfterNotification) {

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

}
