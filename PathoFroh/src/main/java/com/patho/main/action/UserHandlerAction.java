package com.patho.main.action;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.GroupRepository;
import com.patho.main.service.PrintService;
import com.patho.main.template.mail.RequestUnlockMail;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.LabelPrinter;
import com.sun.mail.util.logging.MailHandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserHandlerAction implements Serializable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long serialVersionUID = -8314968695816748306L;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GroupRepository groupRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private PrintService printService;

	/********************************************************
	 * login
	 ********************************************************/
	/**
	 * True if unlock button was clicked
	 */
	private boolean unlockRequestSend;

	/********************************************************
	 * login
	 ********************************************************/

	/**
	 * Selected ClinicPrinter to print the document
	 */
	private ClinicPrinter selectedPrinter;

	/**
	 * Selected label pirnter
	 */
	private LabelPrinter selectedLabelPrinter;

	/**
	 * Method called on postconstruct. Initializes all important variables.
	 */
	@PostConstruct
	public void init() {
		setSelectedPrinter(printService.getCurrentPrinter(getCurrentUser()));
		setSelectedLabelPrinter(printService.getCurrentLabelPrinter(getCurrentUser()));
	}

	/**
	 * Checks if the session is associated with a user.
	 * 
	 * @return
	 */
	public boolean isCurrentUserAvailable() {
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof HistoUser)
			return true;
		return false;
	}

	/**
	 * Returns the current user.
	 * 
	 * @return
	 */
	public HistoUser getCurrentUser() {
		HistoUser user = (HistoUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user;
	}

	/**
	 * Checks if currentUser has the passed role.
	 * 
	 * @param role
	 * @return
	 */
	public boolean currentUserHasPermission(HistoPermissions... permissions) {
		return userHasPermission(getCurrentUser(), permissions);
	}

	/**
	 * Checks if user has the passed role.
	 * 
	 * @param user
	 * @param role
	 * @return
	 */
	public boolean userHasPermission(HistoUser user, HistoPermissions... role) {
		return user.getGroup().getPermissions().stream().anyMatch(p -> {
			for (int i = 0; i < role.length; i++)
				if (p == role[i])
					return true;
			return false;
		});
	}

	/**
	 * Sends an unlock Request to admins
	 */
	public void requestUnlock() {
		HistoUser currentUser = getCurrentUser();

		RequestUnlockMail mail = MailHandler.getDefaultTemplate(RequestUnlockMail.class);
		mail.prepareTemplate(currentUser);
		mail.fillTemplate();

		globalSettings.getMailHandler().sendAdminMail(mail);

		setUnlockRequestSend(true);
	}

}
