package com.patho.main.dialog.miscellaneous

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.util.dialog.event.ConfirmEvent
import org.springframework.stereotype.Component

/**
 * Generic confirm dialog
 */
@Component()
open class ConfirmDialog : AbstractDialog_(Dialog.CONFIRM_CHANGE) {

    lateinit var headline: String

    lateinit var text: String

    open fun initAndPrepareBean(headline: String = "", body: String = ""): ConfirmDialog {
        if (initBean(headline, body))
            prepareDialog()
        return this
    }

    open fun initBean(headline: String = "", body: String = ""): Boolean {
        this.headline = if (headline.isEmpty()) "" else resourceBundle[headline]
        this.text = if (body.isEmpty()) "" else resourceBundle[body]
        return super.initBean()
    }

    open fun header(text: String): ConfirmDialog {
        return header(text, "")
    }

    open fun header(text: String, vararg args: Any): ConfirmDialog {
        headline = resourceBundle[text, args]
        return this
    }

    open fun ctext(text: String): ConfirmDialog {
        return ctext(text, "")
    }

    open fun ctext(text: String, vararg args: Any): ConfirmDialog {
        this.text = resourceBundle[text, args.map { it.toString() }]
        return this
    }

    open fun confirmAndHide() {
        return hideDialog(ConfirmEvent(true))
    }

    open fun declineAndHide() {
        return hideDialog(ConfirmEvent(false))
    }
}
