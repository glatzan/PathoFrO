package com.patho.main.util.ui.jsfcomponents

import com.patho.main.util.bearer.SimplePhysicianBearer

interface ISelectPhysicianOverlay {

    /**
     * List of all physicians
     */
    val physicians: List<SimplePhysicianBearer>

    /**
     * Selected physician
     */
    var selectedPhysician: SimplePhysicianBearer?

    /**
     * Filter String for search
     */
    var filter: String

    /**
     * Function is called on select
     */
    fun onSelect()
}