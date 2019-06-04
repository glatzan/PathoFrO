package com.patho.main.util.ui.backend

/**
 * Primary gui entity
 */
open class GuiEntity {
    var isRendered: Boolean = true
    var isDisabled: Boolean = false

    open fun reset() {
        isRendered = true
        isDisabled = false
    }

    open fun set(rendered: Boolean, disabled: Boolean) {
        this.isRendered = rendered
        this.isDisabled = disabled
    }

    open fun onClick() {
    }
}