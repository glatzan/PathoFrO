package com.patho.main.util.ui.backend

/**
 * Backend bean status class for primefaces command buttons
 */
open class CommandButtonStatus(rendered: Boolean = true, disabled: Boolean = false, open var alternativeButtonText: Boolean = false) : GuiEntity(rendered, disabled) {

    fun set(alternativeButtonText: Boolean, rendered: Boolean, disabled: Boolean) {
        this.alternativeButtonText = alternativeButtonText
        super.set(rendered, disabled)
    }
}