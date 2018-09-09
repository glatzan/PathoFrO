package com.patho.main.util.notification;

import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.template.mail.DiagnosisReportMail;

import lombok.Getter;
import lombok.Setter;

/**
 * Child of NotificationContainer, adds a field for the mail object
 * 
 * @author andi
 *
 */
@Getter
@Setter
public class MailContainer extends NotificationContainer {

	private DiagnosisReportMail mail;
	
	public MailContainer(AssociatedContact contact, AssociatedContactNotification notification) {
		super(contact, notification);
	}

}
