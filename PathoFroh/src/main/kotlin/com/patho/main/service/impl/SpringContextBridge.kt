package com.patho.main.service.impl

import com.patho.main.action.handler.CentralHandler
import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.config.PathoConfig
import com.patho.main.config.util.ApplicationContextProvider
import com.patho.main.config.util.ResourceBundle
import com.patho.main.repository.jpa.*
import com.patho.main.repository.miscellaneous.HttpRestRepository
import com.patho.main.repository.miscellaneous.LDAPRepository
import com.patho.main.repository.miscellaneous.MediaRepository
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import javax.persistence.EntityManager


/**
 * Register this SpringContextBridge as a Spring Component. This is for non manged object to get spring beans
 */
@Component
class SpringContextBridge : SpringContextBridgedServices {

    @Autowired
    override lateinit var reportService: ReportService

    @Autowired
    override lateinit var reportIntentService: ReportIntentService

    @Autowired
    override lateinit var currentUserHandler: CurrentUserHandler

    @Autowired
    override lateinit var resourceBundle: ResourceBundle

    @Autowired
    override lateinit var organizationService: OrganizationService

    @Autowired
    override lateinit var pathoConfig: PathoConfig

    @Autowired
    override lateinit var mediaRepository: MediaRepository

    @Autowired
    override lateinit var physicianService: PhysicianService

    @Autowired
    override lateinit var listItemRepository: ListItemRepository

    @Autowired
    override lateinit var userService: UserService

    @Autowired
    override lateinit var favouriteListRepository: FavouriteListRepository

    @Autowired
    override lateinit var centralHandler: CentralHandler

    @Autowired
    override lateinit var messageSource: MessageSource

    @Autowired
    override lateinit var taskRepository: TaskRepository

    @Autowired
    override lateinit var worklistHandler: WorklistHandler

    @Autowired
    override lateinit var sampleService: SampleService

    @Autowired
    override lateinit var entityManager: EntityManager

    @Autowired
    override lateinit var patientRepository: PatientRepository

    @Autowired
    override lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    override lateinit var groupService: GroupService

    @Autowired
    override lateinit var groupRepository: GroupRepository

    @Autowired
    override lateinit var diagnosisService: DiagnosisService

    @Autowired
    override lateinit var pdfService: PDFService

    @Autowired
    override lateinit var pdfRepository: PDFRepository

    @Autowired
    override lateinit var patientService: PatientService

    @Autowired
    override lateinit var taskService: TaskService

    @Autowired
    override lateinit var organizationRepository: OrganizationRepository

    @Autowired
    override lateinit var personRepository: PersonRepository

    @Autowired
    override lateinit var accountingDataRepository: AccountingDataRepository

    @Autowired
    override lateinit var favouriteListService: FavouriteListService

    @Autowired
    override lateinit var associatedContactRepository: AssociatedContactRepository

    @Autowired
    override lateinit var bioBankRepository: BioBankRepository

    @Autowired
    override lateinit var logRepository: LogRepository

    @Autowired
    override lateinit var faxService: FaxService

    @Autowired
    override lateinit var stainingPrototypeRepository: StainingPrototypeRepository

    @Autowired
    override lateinit var slideRepository: SlideRepository

    @Autowired
    override lateinit var slideService: SlideService

    @Autowired
    override lateinit var userRepository: UserRepository

    @Autowired
    override lateinit var physicianRepository: PhysicianRepository

    @Autowired
    override lateinit var councilService: CouncilService

    @Autowired
    override lateinit var councilRepository: CouncilRepository

    @Autowired
    override lateinit var printDocumentRepository: PrintDocumentRepository

    @Autowired
    override lateinit var materialPresetRepository: MaterialPresetRepository

    @Autowired
    override lateinit var diagnosisPresetRepository: DiagnosisPresetRepository

    @Autowired
    override lateinit var ldapRepository: LDAPRepository

    @Autowired
    override lateinit var diagnosisPresetService: DiagnosisPresetService

    @Autowired
    override lateinit var materialPresetService: MaterialPresetService

    @Autowired
    override lateinit var stainingPrototypeService: StainingPrototypeService

    @Autowired
    override lateinit var listItemService: ListItemService

    @Autowired
    override lateinit var printService: PrintService

    @Autowired
    override lateinit var taskExecutor: ThreadPoolTaskExecutor

    @Autowired
    override lateinit var bioBankService: BioBankService

    @Autowired
    override lateinit var blockService: BlockService

    @Autowired
    override lateinit var httpRestRepository: HttpRestRepository

    companion object {
        @JvmStatic
        fun services(): SpringContextBridgedServices {
            return ApplicationContextProvider.getContext().getBean(SpringContextBridgedServices::class.java)
        }
    }
}