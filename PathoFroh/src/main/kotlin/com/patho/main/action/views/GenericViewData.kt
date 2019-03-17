package com.patho.main.action.views

import com.patho.main.common.ContactRole
import com.patho.main.common.SortOrder
import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.ListItem
import com.patho.main.model.MaterialPreset
import com.patho.main.model.Physician
import com.patho.main.repository.DiagnosisPresetRepository
import com.patho.main.repository.ListItemRepository
import com.patho.main.repository.MaterialPresetRepository
import com.patho.main.repository.PhysicianRepository
import com.patho.main.ui.transformer.DefaultTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class GenericViewData @Autowired constructor(
        private val listItemRepository: ListItemRepository,
        private val diagnosisPresetRepository: DiagnosisPresetRepository,
        private val physicianRepository: PhysicianRepository,
        private val materialPresetRepository: MaterialPresetRepository) : AbstractView() {

    /**
     * Contains all available case histories
     */
    val slideCommentary: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * List of all diagnosis presets
     */
    val diagnosisPresets: MutableList<DiagnosisPreset> = mutableListOf<DiagnosisPreset>()

    /**
     * List of physicians which have the role signature
     */
    var physiciansToSignList: MutableList<Physician> = mutableListOf<Physician>()

    /**
     * Transfomer for physiciansToSign
     */
    var physiciansToSignListTransformer: DefaultTransformer<Physician> = DefaultTransformer<Physician>(physiciansToSignList)

    /**
     * List of available materials
     */
    val materialList: MutableList<MaterialPreset> = mutableListOf<MaterialPreset>()

    /**
     * Contains all available case histories
     */
    val caseHistoryList: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * Contains all available wards
     */
    val wardList: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * List of all surgeons
     */
    val surgeons: MutableList<Physician> = mutableListOf<Physician>()

    /**
     * List of all  physicians
     */
    val physicians: MutableList<Physician> = mutableListOf<Physician>()

    /**
     * Loading generic data for all views
     */
    override fun loadView() {
        slideCommentary.clear()
        slideCommentary.addAll(listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false))

        caseHistoryList.clear()
        caseHistoryList.addAll(listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.CASE_HISTORY, false))

        wardList.clear()
        wardList.addAll(listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.WARDS,
                false))

        diagnosisPresets.clear()
        diagnosisPresets.addAll(diagnosisPresetRepository.findAllByOrderByIndexInListAsc())


        physiciansToSignList.clear()
        physiciansToSignList.addAll(physicianRepository.findAllByRole(ContactRole.SIGNATURE, true))
        physiciansToSignListTransformer = DefaultTransformer(physiciansToSignList)

        materialList.clear()
        materialList.addAll(materialPresetRepository.findAll(true))

        surgeons.clear()
        surgeons.addAll(physicianRepository.findAllByRole(
                arrayOf(ContactRole.SURGEON, ContactRole.EXTERNAL_SURGEON), true, SortOrder.PRIORITY))

        physicians.clear()
        physicians.addAll(physicianRepository.findAllByRole(arrayOf(ContactRole.PRIVATE_PHYSICIAN),
                true, SortOrder.PRIORITY))
    }
}