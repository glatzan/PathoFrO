package com.patho.main.util.ui.backend

/**
 * Backend bean status class for primefaces checkboxes
 */
open class CheckBoxStatus(var value: Boolean = false, rendered: Boolean = true, disabled: Boolean = false, var isInfo: Boolean = false) : GuiEntity(rendered,disabled) {

    override fun reset() {
        super.reset()
        isInfo = false
    }

    fun set(value: Boolean, rendered: Boolean, disabled: Boolean, info: Boolean) {
        this.value = value
        this.isInfo = info
        super.set(rendered, disabled)
    }
}