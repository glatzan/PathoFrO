package com.patho.main.model.util.log;


import com.patho.main.model.log.Log;
import com.patho.main.model.log.LogInfo;
import com.patho.main.util.helper.SecurityContextHolderUtil;
import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Optional;

/**
 * Listener is called every Time an object is saved to database. Creates a log
 * entry.
 * 
 * @author glatza
 *
 */
public class LogListener implements RevisionListener {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Key for the securityContext, workaround in order to pass a string to this
	 * listener.
	 */
	public static final String LOG_KEY_INFO = "logInfo";

	/**
	 * Method is called if an object revision is saved to the database.
	 */
	@Override
	public void newRevision(Object revisionEntity) {
		Log revEntity = (Log) revisionEntity;
		try {
			// sets the log info if present, gets the info from the
			// securityContext, Workaround to pass additional data to this
			// listener
			Optional<LogInfo> logInfo = SecurityContextHolderUtil.<LogInfo>getObjectFromSecurityContext(LOG_KEY_INFO);
			revEntity.setLogInfo(logInfo.orElse(new LogInfo()));
			logger.debug("Saving with log: " + revEntity.getLogInfo().getLogString());
			// clearing loginfo from context
			SecurityContextHolderUtil.clearObjectFromSecruityContext(LOG_KEY_INFO);
			
		} catch (NullPointerException e) {
			logger.error("Nullpointer expection",e);
		}
	}

}
