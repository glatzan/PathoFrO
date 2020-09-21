package com.patho.main.dialog.slides

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.scanner.ScannedSlide
import org.springframework.stereotype.Component

@Component()
class SlideShowScannedDialog constructor() : AbstractTaskDialog(Dialog.SLIDE_SHOW_SCANNED) {

//    lateinit var scannedTask: ScannedTask

    var selectedScannedSlide: ScannedSlide? = null

    val isScannedSlideSelected
        get() = selectedScannedSlide != null

    /**
     * Initializes the dialog
     */
    override fun initBean(task: Task): Boolean {
        val result = super.initBean(task)

//        val scannedTask = scannedTaskRepository.findById(task.id).orElse(ScannedTask(task))

        return result
    }

}
