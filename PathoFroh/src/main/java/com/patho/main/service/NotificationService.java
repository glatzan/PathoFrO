package com.patho.main.service;

import java.util.Date;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;

import com.patho.main.template.mail.DiagnosisReportMail;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.template.print.SendReport;
import com.patho.main.util.notification.FaxExecutor;
import com.patho.main.util.notification.LetterExecutor;
import com.patho.main.util.notification.MailContainer;
import com.patho.main.util.notification.MailContainerList;
import com.patho.main.util.notification.MailExecutor;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationContainerList;
import com.patho.main.util.notification.NotificationExecutor;
import com.patho.main.util.notification.NotificationFeedback;
import com.patho.main.util.pdf.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
public class NotificationService {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

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

	public boolean executeNotification(NotificationFeedback feedback, Task task, MailContainerList mailContainerList,
			NotificationContainerList faxContainerList, NotificationContainerList letterContainerList,
			NotificationContainerList phoneContainerList, NotificationContainerList printContainerList,
			boolean temporaryNotification) {

		boolean emailSendSuccessful = executeMailNotification(feedback, task, mailContainerList, temporaryNotification);
		boolean faxSendSuccessful = executeFaxNotification(feedback, task, faxContainerList, temporaryNotification);
		boolean letterSendSuccessful = executeLetterNotification(feedback, task, letterContainerList,
				temporaryNotification);

		if (printContainerList.isUse() && printContainerList.getDefaultReport() != null) {
			// addition templates
			((DiagnosisReport) printContainerList.getDefaultReport()).initData(task, "");
			PDFContainer report = (new PDFGenerator())
					.getPDF(((DiagnosisReport) printContainerList.getDefaultReport()));

			userHandlerAction.getSelectedPrinter().print(report, printContainerList.getPrintCount(),
					printContainerList.getDefaultReport().getAttributes());

		}

		feedback.progressStep();

		PDFContainer sendReport = generateSendReport(feedback, task, mailContainerList, faxContainerList,
				letterContainerList, phoneContainerList, new Date(), temporaryNotification);

		// setting notification als completed
		for (DiagnosisRevision revision : printContainerList.getSelectedRevisions()) {
			revision.setNotificationPending(false);
			revision.setNotificationDate(System.currentTimeMillis());
		}

		genericDAO.savePatientData(task, "log.patient.task.notification.send");

		pdfService.attachPDF(task.getPatient(), task, sendReport);

		return emailSendSuccessful && faxSendSuccessful && letterSendSuccessful;
	}

	public boolean executeMailNotification(NotificationFeedback feedback, Task task,
			MailContainerList mailContainerList, boolean temporaryNotification) {
		// pdf container if no individual address is needed successful

		boolean success = true;

		MailExecutor mailExecutor = new MailExecutor(feedback);

		for (NotificationContainer container : mailContainerList.getContainerToNotify()) {
			try {
				// copy contact address before sending -> save before error
				container.getNotification().setContactAddress(container.getContactAddress());

				log.debug("Send mail to " + container.getContactAddress());

				if (!mailExecutor.isAddressApproved(container.getContactAddress()))
					throw new IllegalArgumentException("dialog.notification.sendProcess.mail.error.mailNotValid");

				// setting mail
				((MailContainer) container).setMail((DiagnosisReportMail) mailContainerList.getSelectedMail().clone());

				container.setPdf(
						mailExecutor.getPDF((MailContainer) container, task, mailContainerList.getDefaultReport(),
								mailContainerList.getSelectedRevisions(), mailContainerList.isIndividualAddresses()));

				if (!mailExecutor.performNotification((MailContainer) container, true, false))
					throw new IllegalArgumentException("dialog.notification.sendProcess.mail.error.failed");

				mailExecutor.finishSendProecess((MailContainer) container, true,
						resourceBundle.get("dialog.notification.sendProcess.mail.success"));

				log.debug("Sending completed " + container.getNotification().getCommentary());

			} catch (IllegalArgumentException e) {
				success = false;
				mailExecutor.finishSendProecess((MailContainer) container, false,
						resourceBundle.get(e.getMessage(), container.getContactAddress()));
				log.debug("Sending failed" + container.getNotification().getCommentary());
			}

			// renew if temporary notification
			if (temporaryNotification)
				contactDAO.renewNotification(task, container.getContact(), container.getNotification());

			feedback.progressStep();
		}

		return success;
	}

	public boolean executeFaxNotification(NotificationFeedback feedback, Task task,
			NotificationContainerList faxContainerList, boolean temporaryNotification) {

		FaxExecutor faxExecutor = new FaxExecutor(feedback);

		boolean success = true;

		for (NotificationContainer container : faxContainerList.getContainerToNotify()) {
			try {

				// copy contact address before sending -> save before error
				container.getNotification().setContactAddress(container.getContactAddress());

				if (!faxExecutor.isAddressApproved(container.getContactAddress()))
					throw new IllegalArgumentException("dialog.notification.sendProcess.fax.error.numberNotValid");

				container.setPdf(faxExecutor.getPDF(container, task, faxContainerList.getDefaultReport(),
						faxContainerList.getSelectedRevisions(), faxContainerList.isIndividualAddresses()));

				if (!faxExecutor.performNotification(container, faxContainerList.isSend(), faxContainerList.isPrint()))
					throw new IllegalArgumentException("dialog.notification.sendProcess.fax.error.failed");

				faxExecutor.finishSendProecess(container, true,
						resourceBundle.get("dialog.notification.sendProcess.fax.success"));

			} catch (IllegalArgumentException e) {
				success = false;
				faxExecutor.finishSendProecess(container, false,
						resourceBundle.get(e.getMessage(), container.getContactAddress()));
				log.debug("Sending failed" + container.getNotification().getCommentary());
			}

			// renew if temporary notification
			if (temporaryNotification)
				contactDAO.renewNotification(task, container.getContact(), container.getNotification());

			feedback.progressStep();
		}

		return success;
	}

	public boolean executeLetterNotification(NotificationFeedback feedback, Task task,
			NotificationContainerList letterContainerList, boolean temporaryNotification) {

		NotificationExecutor<NotificationContainer> notificationExecutor = new LetterExecutor(feedback);

		boolean success = true;

		for (NotificationContainer container : letterContainerList.getContainerToNotify()) {
			try {

				// copy contact address before sending -> save before error
				container.getNotification().setContactAddress(container.getContactAddress());

				if (!notificationExecutor.isAddressApproved(container.getContactAddress()))
					throw new IllegalArgumentException("");

				container.setPdf(notificationExecutor.getPDF(container, task, letterContainerList.getDefaultReport(),
						letterContainerList.getSelectedRevisions(), letterContainerList.isIndividualAddresses()));

				if (!notificationExecutor.performNotification(container, false, letterContainerList.isPrint()))
					throw new IllegalArgumentException("dialog.notification.sendProcess.pdf.error.failed");

				notificationExecutor.finishSendProecess(container, true,
						resourceBundle.get("dialog.notification.sendProcess.pdf.print"));

			} catch (IllegalArgumentException e) {
				success = false;
				notificationExecutor.finishSendProecess(container, false,
						resourceBundle.get(e.getMessage(), container.getContactAddress()));
				log.debug("Sending failed" + container.getNotification().getCommentary());
			}

			// renew if temporary notification
			if (temporaryNotification)
				contactDAO.renewNotification(task, container.getContact(), container.getNotification());

			feedback.progressStep();
		}

		return success;
	}

	public void executePhoneNotification(NotificationFeedback feedback, Task task,
			NotificationContainerList phoneContainerList, boolean temporaryNotification) {
		NotificationExecutor<NotificationContainer> notificationExecutor = new NotificationExecutor<NotificationContainer>(
				feedback);
		for (NotificationContainer container : phoneContainerList.getContainerToNotify()) {
			notificationExecutor.finishSendProecess(container, true,
					resourceBundle.get("dialog.notification.sendProcess.pdf.print"));

			// renew if temporary notification
			if (temporaryNotification)
				contactDAO.renewNotification(task, container.getContact(), container.getNotification());
		}

		feedback.progressStep();
	}

	public PDFContainer generateSendReport(NotificationFeedback feedback, Task task,
			MailContainerList mailContainerList, NotificationContainerList faxContainerList,
			NotificationContainerList letterContainerList, NotificationContainerList phoneContaienrList,
			Date notificationDate, boolean temporarayNotification) {

		feedback.setFeedback("log.notification.pdf.sendReport.generation");

		SendReport sendReport = DocumentTemplate
				.getTemplateByID(globalSettings.getDefaultDocuments().getNotificationSendReport());

		sendReport.initializeTempalte(task, mailContainerList, faxContainerList, letterContainerList,
				phoneContaienrList, notificationDate, temporarayNotification);

		PDFContainer container = (new PDFGenerator()).getPDF(sendReport);

		return container;
	}
}
