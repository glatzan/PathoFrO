package com.patho.main.util.ui.selector

import com.patho.main.model.Physician

open class PhysicianSelector(val physician: Physician, override var id: Long) : UISelector<Physician>(physician) {
}