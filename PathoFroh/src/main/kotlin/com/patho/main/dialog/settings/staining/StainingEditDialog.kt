package com.patho.main.dialog.settings.staining

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.preset.StainingPrototypeDetails
import com.patho.main.repository.jpa.StainingPrototypeRepository
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.util.dialog.event.ReloadEvent
import com.patho.main.util.exceptions.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dialog for creating or editing stainings
 */
@Component()
open class StainingEditDialog @Autowired constructor(
        private val stainingPrototypeRepository: StainingPrototypeRepository) : AbstractTaskDialog(Dialog.SETTINGS_STAINING_EDIT) {

    open lateinit var stainingPrototype: StainingPrototype

    val isNewStaining
        get() = stainingPrototype.id == 0L

    override fun initAndPrepareBean(): StainingEditDialog {
        return  initAndPrepareBean(StainingPrototype())
    }

    open fun initAndPrepareBean(prototype: StainingPrototype): StainingEditDialog {
        if (initBean(prototype))
            prepareDialog()
        return this
    }

    open fun initBean(prototype: StainingPrototype): Boolean {
        this.stainingPrototype = prototype

        if (!isNewStaining) {
            val oStainingPrototype = stainingPrototypeRepository.findOptionalByIdAndInitialize(this.stainingPrototype.id)

            if (!oStainingPrototype.isPresent)
                throw EntityNotFoundException()
            else
                this.stainingPrototype = oStainingPrototype.get()
        }

        return super.initBean()
    }

    /**
     * Create a new batch
     */
    open fun addBatch() {
        val newBatch = StainingPrototypeDetails()
        this.stainingPrototype.batchDetails.add(0, newBatch)
    }

    /**
     * Remove a batch
     */
    open fun removeBatch(stainingPrototypeDetails: StainingPrototypeDetails?) {
        this.stainingPrototype.batchDetails.remove(stainingPrototypeDetails)
    }

    /**
     * Clone a batch
     */
    open fun cloneBatch(stainingPrototypeDetails: StainingPrototypeDetails) {
        this.stainingPrototype.batchDetails.add(0, (stainingPrototypeDetails.clone() as StainingPrototypeDetails))
    }

    open fun saveAndHide() {
        save()
        hideDialog(ReloadEvent())
    }

    open fun save() {
        services().stainingPrototypeService.addOrUpdate(this.stainingPrototype)
    }

}