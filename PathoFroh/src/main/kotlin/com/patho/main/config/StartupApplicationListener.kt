package com.patho.main.config

import com.patho.main.repository.miscellaneous.MailRepository
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.repository.miscellaneous.impl.PrintDocumentRepositoryImpl
import com.patho.main.service.PrintService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class StartupApplicationListener  @Autowired @Lazy constructor(
        var pathoConfig: PathoConfig,
        var printDocumentRepository : PrintDocumentRepository,
        var mailRepository: MailRepository,
        var printService : PrintService): ApplicationListener<ContextRefreshedEvent> {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Method is called after startup
     */
    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        pathoConfig.initialize();
        printDocumentRepository.initializeDocuments();
        mailRepository.initializeDocuments();
        printService.initializePrinters();

        logger.info("------------------------------- Increment counter")
    }

}