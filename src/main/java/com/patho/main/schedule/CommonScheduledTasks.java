package com.patho.main.schedule;

import com.patho.main.config.PathoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class CommonScheduledTasks {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PathoConfig pathoConfig;

    @Scheduled(cron = "${patho.settings.schedule.pdfCleanupCron}")
    public void pdfCleanup() {
        logger.debug("Running cleanup");
    }
}
