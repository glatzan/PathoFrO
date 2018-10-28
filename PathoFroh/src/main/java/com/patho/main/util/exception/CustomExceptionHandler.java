package com.patho.main.util.exception;

import java.util.Iterator;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.hibernate.HibernateException;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;

import com.patho.main.action.MainHandlerAction;
import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.View;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * <!-- <factory> <exception-handler-factory> org.histo.config.exception.
 * CustomExceptionHandlerFactory </exception-handler-factory> </factory>--> can
 * be actived in faces config again if needed
 * 
 * <mvc:interceptors> <bean id="webContentInterceptor" class=
 * "org.springframework.web.servlet.mvc.WebContentInterceptor">
 * <property name= "cacheSeconds" value="0" />
 * <property name="useExpiresHeader" value="true" />
 * <property name="useCacheControlHeader" value="true" />
 * <property name= "useCacheControlNoStore" value="true" /> </bean>
 * </mvc:interceptors>
 * 
 * this is used instead in spring config
 * 
 * @author andi
 *
 */
@Slf4j
@Configurable
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

	@Autowired
	@Lazy
	private MainHandlerAction mainHandlerAction;

	@Autowired
	@Lazy
	private ResourceBundle resourceBundle;

	@Autowired
	@Lazy
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Lazy
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Lazy
	private GlobalEditViewHandler globalEditViewHandler;
	
	private ExceptionHandler wrapped;

	CustomExceptionHandler(ExceptionHandler exception) {
		this.wrapped = exception;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void handle() throws FacesException {

		final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
		while (i.hasNext()) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

			// get the exception from context
			Throwable cause = context.getException();

			final FacesContext fc = FacesContext.getCurrentInstance();
			final Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
			final NavigationHandler nav = fc.getApplication().getNavigationHandler();

			log.debug("Global exeption handler - " + cause);

			// getting root excepetion
			while (cause instanceof FacesException || cause instanceof ELException) {
				if (cause instanceof FacesException)
					cause = ((FacesException) cause).getCause();
				else
					cause = ((ELException) cause).getCause();
			}

			boolean hanled = true;

			log.debug("Global exeption handler - " + cause);

			if (cause != null) {

				if (cause instanceof CustomNotUniqueReqest) {
					log.debug("Not Unique Reqest Error");
					PrimeFaces.current().dialog().closeDynamic(null);
					mainHandlerAction.sendGrowlMessages("Fehler!", "Doppelte Anfrage", FacesMessage.SEVERITY_ERROR);
				} else if (cause instanceof HistoDatabaseInconsistentVersionException) {

					log.debug("Database Version Conflict");

					if (((HistoDatabaseInconsistentVersionException) cause).getOldVersion() instanceof Patient) {
						log.debug("Version Error, replacing Patient");
						worklistViewHandlerAction.replacePatientInCurrentWorklist(
								((Patient) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion()));
					} else if (((HistoDatabaseInconsistentVersionException) cause).getOldVersion() instanceof Task) {
						log.debug("Version Error, replacing task");
						worklistViewHandlerAction.replaceTaskInCurrentWorklist(
								((Task) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion()));
					} else if (((HistoDatabaseInconsistentVersionException) cause)
							.getOldVersion() instanceof Parent<?>) {
						log.debug("Version Error, replacing parent -> task");
						worklistViewHandlerAction.replaceTaskInCurrentWorklist(
								((Parent<?>) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion())
										.getTask());
					} else {
						log.debug("Version Error,"
								+ ((HistoDatabaseInconsistentVersionException) cause).getOldVersion().getClass());
					}

					mainHandlerAction.sendGrowlMessagesAsResource("growl.error", "growl.error.version");

					PrimeFaces.current().executeScript("clickButtonFromBean('#globalCommandsForm\\\\:refreshContentBtn')");

					// TODO implement
				} else if (cause instanceof AbortProcessingException) {
					log.debug("Error aboring all actions!");
				} else if (cause instanceof BadCredentialsException) {
					hanled = false;
				}else if(cause instanceof HibernateException) {
					System.out.println("datenbank exception");
					globalEditViewHandler.setDisplayView(View.WORKLIST_DATA_ERROR);
				}else {
					log.debug("Other exception!");
					cause.printStackTrace();
				}

				// ErrorMail mail = new ErrorMail();
				// mail.prepareTemplate(userHandlerAction.getCurrentUser(), "Ehandler " +
				// cause.getMessage(),
				// new Date(System.currentTimeMillis()));
				// mail.fillTemplate();
				// globalSettings.getMailHandler().sendErrorMail(mail);
			}else if(event instanceof ExceptionQueuedEvent) {
				
			}

			if (hanled)
				i.remove();
		}
		// parent hanle
		getWrapped().handle();
	}
}