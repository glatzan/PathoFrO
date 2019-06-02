package com.patho.main.dialog.task

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.interfaces.ID
import com.patho.main.util.dialog.event.TaskEntityDeleteEvent
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
class DeleteTaskEntityDialog : AbstractDialog_(Dialog.DELETE_ID_OBJECT) {

    lateinit var deleteEntity: ID

    var headline: String = ""

    var text: String = ""

    fun initAndPrepareBean(deleteEntity: ID) : DeleteTaskEntityDialog {
        if (initBean(deleteEntity, "", ""))
            prepareDialog()
        return this
    }

    fun initAndPrepareBean(deleteEntity: ID, headline: String = "", text: String = ""): DeleteTaskEntityDialog {
        if (initBean(deleteEntity, headline, text))
            prepareDialog()
        return this
    }

    fun initBean(deleteEntity: ID, headline: String, text: String): Boolean {
        this.headline = if (headline.isEmpty()) "" else resourceBundle.get(headline)
        this.text = if (text.isEmpty()) "" else resourceBundle.get(text)
        this.deleteEntity = deleteEntity

        return true
    }

    fun advancedHeader(text: String, vararg obj: Any): DeleteTaskEntityDialog {
        headline = resourceBundle.get(text, *obj)
        return this
    }

    fun advancedText(text: String, vararg obj: Any): DeleteTaskEntityDialog {
        this.text = resourceBundle.get(text, *obj)
        return this
    }

    fun deleteAndHide() {
        super.hideDialog(TaskEntityDeleteEvent(deleteEntity))
    }
}