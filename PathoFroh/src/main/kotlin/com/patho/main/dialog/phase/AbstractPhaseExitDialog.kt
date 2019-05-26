package com.patho.main.dialog.phase

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog

abstract class AbstractPhaseExitDialog(dialog : Dialog) : AbstractTaskDialog(dialog) {
    open var removeFromFavouriteList = false
    open var exitPhase : Boolean = false
    open var removeFromWorklist : Boolean = false

}