package com.patho.main.util.ui.backend

/**
 * Backend bean status class for primefaces command buttons
 */
open class CommandButtonStatus(rendered: Boolean = true, disabled: Boolean = false, alternativeText: Boolean = false) : CommandLinkStatus(rendered, disabled, alternativeText) {}