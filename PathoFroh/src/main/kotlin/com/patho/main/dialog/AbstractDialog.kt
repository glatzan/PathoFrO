package com.patho.main.dialog

import com.patho.main.common.Dialog
import com.patho.main.config.util.ResourceBundle
import com.patho.main.util.dialogReturn.ReloadEvent
import com.patho.main.util.helper.UniqueRequestID
import org.primefaces.PrimeFaces
import org.primefaces.event.SelectEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * Dialog class
 */
abstract class AbstractDialog_(val dialog: Dialog) {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    protected lateinit var resourceBundle: ResourceBundle

    /**
     * ID for unique requests
     */
    protected var uniqueRequestID = UniqueRequestID()

    /**
     * Initializes and displays the dialog
     */
    open fun initAndPrepareBean(dialog: Dialog = this.dialog): AbstractDialog_ {
        if (initBean(dialog))
            prepareDialog()
        return this
    }

    /**
     * Initializes the dialog
     */
    open fun initBean(dialog: Dialog): Boolean {
        return initBean(dialog, false)
    }

    /**
     * Initializes the dialog
     */
    open fun initBean(dialog: Dialog = this.dialog, uniqueRequestEnabled: Boolean = false): Boolean {
        this.uniqueRequestID.isEnabled = uniqueRequestEnabled

        if (uniqueRequestEnabled)
            uniqueRequestID.nextUniqueRequestID()

        return true
    }

    /**
     * Displays the dialog
     */
    open fun prepareDialog() {
        prepareDialog(dialog)
    }

    /**
     * Displays the dialog
     */
    open fun prepareDialog(dialog: Dialog) {
        val options = HashMap<String, Any>()

        if (dialog.width != 0) {
            options["width"] = dialog.width
            options["contentWidth"] = dialog.width
        } else
            options["width"] = "auto"

        if (dialog.height != 0) {
            options["contentHeight"] = dialog.height
            options["height"] = dialog.height
        } else
            options["height"] = "auto"

        if (dialog.useOptions) {
            options["resizable"] = dialog.resizeable
            options["draggable"] = dialog.draggable
            options["modal"] = dialog.modal
        }

        options["closable"] = false

        if (dialog.header != null)
            options["headerElement"] = "dialogForm:header"

        PrimeFaces.current().dialog().openDynamic(dialog.path, options, null)

        logger.debug("Showing Dialog: $dialog")
    }

    /**
     * Updates the dialog content, without forcing a database reload
     */
    open fun update() {
        update(false)
    }

    /**
     * If true the content will be fetched from database
     */
    open fun update(update: Boolean) {
        logger.debug("Dummy reload, do nothing")
    }

    /**
     * Default return function for sub dialogs
     */
    open fun onSubDialogReturn(event: SelectEvent) {
        logger.debug("Default Dialog return function object: ${if (event.`object` != null) event.`object`.javaClass else "empty"}")
        if (event.getObject() != null && event.getObject() is ReloadEvent) {
            update(true);
        }
    }

    /**
     * Closes the dialog without a return value
     */
    open fun hideDialog() {
        return hideDialog(null)
    }

    /**
     * Closes the dialog, returning the given value
     */
    open fun hideDialog(returnValue: Any?) {
        logger.debug("Hiding Dialog: $dialog ")
        PrimeFaces.current().dialog().closeDynamic(returnValue)
    }
}