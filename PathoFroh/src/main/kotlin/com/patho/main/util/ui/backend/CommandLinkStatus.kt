package com.patho.main.util.ui.backend

/**
 * Backend bean status class for primefaces command link
 */
open class CommandLinkStatus(rendered: Boolean = true, disabled: Boolean = false, open var alternativeText: Boolean = false) : GuiEntity(rendered, disabled) {
    open fun set(alternativeText: Boolean, rendered: Boolean, disabled: Boolean) {
        this.alternativeText = alternativeText
        super.set(rendered, disabled)
    }
}