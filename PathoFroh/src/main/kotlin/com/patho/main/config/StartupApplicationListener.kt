package com.patho.main.config

import com.patho.main.repository.miscellaneous.MailRepository
import com.patho.main.repository.miscellaneous.MiscellaneousDocumentsRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.PrintService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class StartupApplicationListener  @Autowired @Lazy constructor(
        private val pathoConfig: PathoConfig,
        private val printDocumentRepository : PrintDocumentRepository,
        private val mailRepository: MailRepository,
        private val printService : PrintService,
        private val miscellaneousDocumentsRepository: MiscellaneousDocumentsRepository): ApplicationListener<ContextRefreshedEvent> {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Method is called after startup
     */
    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        pathoConfig.initialize();
        printDocumentRepository.initializeDocuments();
        mailRepository.initializeDocuments();
        printService.initializePrinters();
        miscellaneousDocumentsRepository.initializeDocuments()

        logger.info("Initializing documents ended")
    }

}