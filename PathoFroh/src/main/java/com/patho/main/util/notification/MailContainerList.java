package com.patho.main.util.notification;

import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.template.mail.DiagnosisReportMail;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.Getter;
import lombok.Setter;

/**
 * Class Containing data for mail notification
 * 
 * @author andi
 *
 */
@Configurable
@Getter
@Setter
public class MailContainerList extends NotificationContainerList {

	private DiagnosisReportMail selectedMail;

	public MailContainerList(NotificationTyp notificationTyp) {
		super(notificationTyp);
	}
}
