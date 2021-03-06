package com.patho.main.action.views

import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.SortOrder
import com.patho.main.model.Physician
import com.patho.main.model.patient.Task
import com.patho.main.model.preset.DiagnosisPreset
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.model.preset.MaterialPreset
import com.patho.main.repository.jpa.*
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.bearer.SimplePhysicianBearer
import org.primefaces.model.menu.MenuModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import javax.faces.component.html.HtmlPanelGroup

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class GenericViewData @Autowired constructor(
        private val listItemRepository: ListItemRepository,
        private val diagnosisPresetRepository: DiagnosisPresetRepository,
        private val physicianRepository: PhysicianRepository,
        private val materialPresetRepository: MaterialPresetRepository,
        private val taskRepository: TaskRepository,
        private val worklistHandler: WorklistHandler) : AbstractTaskView() {

    /**
     * H:pannelgrid for dynamic command buttons
     */
    open var taskMenuCommandButtons: HtmlPanelGroup? = null

    /**
     * MenuModel for task editing
     */
    open var taskMenuModel: MenuModel? = null

    /**
     * Contains all available case histories
     */
    open var slideCommentary: List<ListItem> = listOf()

    /**
     * List of all reportIntent presets
     */
    open var diagnosisPresets: List<DiagnosisPreset> = listOf()

    /**
     * List of physicians which have the role signature
     */
    open var physiciansToSignList: List<Physician> = listOf()

    /**
     * Transformer for physiciansToSign
     */
    open var physiciansToSignListTransformer: DefaultTransformer<Physician> = DefaultTransformer(listOf())

    /**
     * List of available materials
     */
    open var materialList: List<MaterialPreset> = mutableListOf()

    /**
     * Contains all available case histories
     */
    open var caseHistoryList: List<ListItem> = listOf()

    /**
     * Contains all available wards
     */
    open var wardList: List<ListItem> = listOf()

    /**
     * List of all surgeons
     */
    open var surgeons: List<SimplePhysicianBearer> = listOf()

    /**
     * List of all surgeons
     */
    open var privatePhysicians: List<SimplePhysicianBearer> = listOf()

    /**
     * Loads generic data for all views
     */
    override fun loadView() {
        logger.debug("Loading generic data")

        slideCommentary = listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.SLIDES, false)

        caseHistoryList = listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.CASE_HISTORY, false)

        wardList = listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.WARDS,
                false)

        diagnosisPresets = diagnosisPresetRepository.findAllByOrderByIndexInListAsc()

        physiciansToSignList = physicianRepository.findAllByRole(ContactRole.SIGNATURE, true)
        physiciansToSignListTransformer = DefaultTransformer(physiciansToSignList)

        materialList = materialPresetRepository.findAllOrderByIndexInListAsc(true, true)
    }

    /**
     * Loads generic data for all views
     */
    override fun loadView(task: Task) {
        loadView(task, true)
    }

    open fun loadView(task: Task, loadGeneric: Boolean) {
        super.loadView(task)

        if (loadGeneric)
            loadView()

        surgeons = physicianRepository.findAllByRole(
                arrayOf(ContactRole.SURGEON, ContactRole.EXTERNAL_SURGEON), true, SortOrder.PRIORITY).map { p -> SimplePhysicianBearer(p.id, p, task) }

        privatePhysicians = physicianRepository.findAllByRole(arrayOf(ContactRole.PRIVATE_PHYSICIAN),
                true, SortOrder.PRIORITY).map { p -> SimplePhysicianBearer(p.id, p, task) }
    }
}