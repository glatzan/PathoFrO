package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.preset.ListItem

/**
 * Interfaces for case history select overlay component (caseHistorySelectOverlay.xhtml)
 */
interface ICaseHistorySelectOverlay {

    /**
     * List of all ListItems
     */
    var selectedItem: ListItem?

    /**
     * Selected ListItem
     */
    val items: List<ListItem>

    /**
     * Filter string for search
     */
    var filter: String

    /**
     * Function is called on select
     */
    fun onSelect() {}

    /**
     * Function is called on show
     */
    fun onShow() {}
}