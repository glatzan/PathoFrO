package com.patho.main.dialog.settings.material

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.preset.MaterialPreset
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.repository.jpa.MaterialPresetRepository
import com.patho.main.repository.jpa.StainingPrototypeRepository
import com.patho.main.service.ListItemService
import com.patho.main.service.MaterialPresetService
import com.patho.main.service.StainingPrototypeService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import org.primefaces.event.ReorderEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dialog for reordering the staining list
 */
@Component()
open class MaterialSortDialog @Autowired constructor(
        private val materialPresetRepository: MaterialPresetRepository,
        private val materialPresetService: MaterialPresetService) : AbstractTaskDialog(Dialog.SETTINGS_MATERIAL_SORT){

    open lateinit var materialPresets: MutableList<MaterialPreset>

    override fun initAndPrepareBean(): MaterialSortDialog {
        if (initBean())
            prepareDialog()
        return this
    }

    override fun initBean(): Boolean {
        update()
        return super.initBean()
    }

    /**
     * Reoders the current list
     */
    open fun onReorderList(event: ReorderEvent?) {
        materialPresets = materialPresetService.updateReorderedList(materialPresets).toMutableList()
        update()
    }

    /**
     * Updates the list
     */
    override fun update() {
        materialPresets = materialPresetRepository.findAllOrderByIndexInListAsc(false, true).toMutableList()
    }
}