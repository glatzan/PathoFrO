package com.patho.main.service

import com.patho.main.action.handler.CentralHandler
import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.config.PathoConfig
import com.patho.main.config.util.ResourceBundle
import com.patho.main.repository.*
import lombok.AccessLevel
import org.springframework.context.MessageSource
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.support.TransactionTemplate
import javax.persistence.EntityManager

/**
 * Briding service for getting spring beans within non spring managed objects
 */
interface SpringContextBridgedServices {
    var currentUserHandler: CurrentUserHandler
    var reportIntentService: ReportIntentService
    var resourceBundle: ResourceBundle
    var organizationService: OrganizationService
    var reportService: ReportService
    var pathoConfig: PathoConfig
    var mediaRepository: MediaRepository
    var physicianService: PhysicianService
    var listItemRepository: ListItemRepository
    var userService: UserService
    var favouriteListRepository: FavouriteListRepository
    var centralHandler: CentralHandler
    var messageSource: MessageSource
    var taskRepository: TaskRepository
    var worklistHandler: WorklistHandler
    var sampleService: SampleService
    var entityManager: EntityManager
    var patientRepository : PatientRepository
    var transactionTemplate: TransactionTemplate
    var groupService: GroupService
    var groupRepository : GroupRepository
    var diagnosisService: DiagnosisService
    var pdfService: PDFService
    var pdfRepository: PDFRepository
    var patientService: PatientService
    var taskService: TaskService
    var organizationRepository: OrganizationRepository
    var personRepository: PersonRepository
    var accountingDataRepository: AccountingDataRepository
    var favouriteListService: FavouriteListService
    var associatedContactRepository: AssociatedContactRepository
    var bioBankRepository: BioBankRepository
    var logRepository: LogRepository
    var faxService: FaxService
    var stainingPrototypeRepository: StainingPrototypeRepository
    var slideRepository: SlideRepository
    var slideService: SlideService
    var userRepository: UserRepository
    var physicianRepository: PhysicianRepository
    var councilService: CouncilService
    var councilRepository: CouncilRepository
    var printDocumentRepository: PrintDocumentRepository
    var materialPresetRepository: MaterialPresetRepository
    var diagnosisPresetRepository: DiagnosisPresetRepository
    var ldapRepository: LDAPRepository
    var diagnosisPresetService: DiagnosisPresetService
    var materialPresetService: MaterialPresetService
    var stainingPrototypeService: StainingPrototypeService
    var listItemService: ListItemService
    var printService: PrintService
    var taskExecutor: ThreadPoolTaskExecutor
    var bioBankService: BioBankService
    var blockService: BlockService
}