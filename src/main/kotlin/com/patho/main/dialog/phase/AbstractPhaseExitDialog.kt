package com.patho.main.dialog.phase

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.util.ui.backend.CheckBoxStatus

abstract class AbstractPhaseExitDialog(dialog: Dialog) : AbstractTaskDialog(dialog) {
    open var removeFromFavouriteList = CheckBoxStatus()
    open var exitPhase = CheckBoxStatus()
    open var removeFromWorklist = CheckBoxStatus()
}