package com.patho.main.dialog.settings.staining

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.repository.jpa.StainingPrototypeRepository
import com.patho.main.service.ListItemService
import com.patho.main.service.StainingPrototypeService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import org.primefaces.event.ReorderEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dialog for reordering the staining list
 */
@Component()
open class StainingSortDialog @Autowired constructor(
        private val stainingPrototypeRepository: StainingPrototypeRepository,
        private val stainingPrototypeService: StainingPrototypeService) : AbstractTaskDialog(Dialog.SETTINGS_STAINING_SORT){

    open lateinit var stainingPrototypeList: MutableList<StainingPrototype>

    override fun initAndPrepareBean(): StainingSortDialog {
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
        stainingPrototypeList = stainingPrototypeService.updateReorderedList(stainingPrototypeList).toMutableList()
        update()
    }

    /**
     * Updates the list
     */
    override fun update() {
        stainingPrototypeList = stainingPrototypeRepository.findAllByOrderByIndexInListAsc().toMutableList()
    }
}