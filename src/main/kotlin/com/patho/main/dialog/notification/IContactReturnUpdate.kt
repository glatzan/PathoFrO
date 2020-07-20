package com.patho.main.dialog.notification

import org.primefaces.event.SelectEvent

/**
 * Interfaces providing a function which can be called on dialog return
 */
interface IContactReturnUpdate {
    fun onContactDialogReturn(event: SelectEvent)
}