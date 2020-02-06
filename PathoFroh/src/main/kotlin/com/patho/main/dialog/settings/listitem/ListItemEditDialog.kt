package com.patho.main.dialog.settings.listitem

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.repository.jpa.StainingPrototypeRepository
import com.patho.main.service.ListItemService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.util.dialog.event.ReloadEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dialog for creating or editing list items
 */
@Component()
open class ListItemEditDialog @Autowired constructor(
        private val listItemService: ListItemService) : AbstractTaskDialog(Dialog.SETTINGS_LISTITEM_EDIT) {

    lateinit var listItem: ListItem

    val isNewListItem
        get() = listItem.id == 0L

    open fun initAndPrepareBean(listItemType: ListItemType): ListItemEditDialog {
        return initAndPrepareBean(ListItem(listItemType))
    }

    open fun initAndPrepareBean(listItem: ListItem = ListItem()): ListItemEditDialog {
        if (initBean(listItem))
            prepareDialog()
        return this
    }

    open fun initBean(listItem: ListItem): Boolean {
        this.listItem = listItem
        return super.initBean()
    }

    open fun saveAndHide() {
        save()
        hideDialog(ReloadEvent())
    }

    private fun save() {
        listItemService.addOrUpdate(listItem)
    }

}