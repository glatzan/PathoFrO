package com.patho.main.util.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Executes mail notifications. Checks if valid mail and sends the mail.
 * 
 * @author andi
 *
 */
@Configurable
public class LetterExecutor extends NotificationExecutor<NotificationContainer> {

	public LetterExecutor(NotificationFeedback feedback) {
		super(feedback);
	}

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	/**
	 * Checks if the given string is empty and if the string is an email.
	 */
	@Override
	public boolean isAddressApproved(String address) {
		if (!HistoUtil.isNotNullOrEmpty(address))
			return false;

		return true;
	}
}
