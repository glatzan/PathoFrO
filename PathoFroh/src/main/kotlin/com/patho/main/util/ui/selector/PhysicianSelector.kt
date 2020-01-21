package com.patho.main.util.ui.selector

import com.patho.main.model.Physician

open class PhysicianSelector(physician: Physician, id: Long) : UISelector<Physician>(physician, id) {

    /**
     * Returns the item as a phyisican field
     */
    val physician
        get() = item
}