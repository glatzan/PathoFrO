package com.patho.main.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.patho.main.action.handler.AbstractHandler;

@Configuration
public class CommonScheduledTasks extends AbstractHandler {

	@Autowired
	private PathoConfig pathoConfig;
	
	@Scheduled(cron = "${patho.settings.schedule.pdfCleanupCron}")
	public void pdfCleanup() {
		logger.debug("Running cleanup");
	}
}
