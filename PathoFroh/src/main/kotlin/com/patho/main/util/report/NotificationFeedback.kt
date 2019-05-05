package com.patho.main.util.report

import com.patho.main.service.impl.SpringContextBridge

/**
 * Notification feedback handler
 */
interface NotificationFeedback {

    /**
     * Status as string
     */
    var status: String

    /**
     * Current progress in percent
     */
    var progress: Int


    /**
     * Progress per step
     */
    var progressPerStep: Int

    /**
     * True if notification is completed
     */
    var completed : Boolean

    /**
     * True if notification was successful
     */
    var success : Boolean

    /**
     * Loads the feedback from the message file
     */
    fun setFeedback(key: String, vararg insert: String) {
        status = SpringContextBridge.services().resourceBundle.get(key, insert)
    }

    /**
     * Increments the progress counter
     */
    fun progress() {
        if (progress + 2 * progressPerStep > 100) {
            completed = true
            progress = 100
        }else
            progress += progressPerStep
    }

    /**
     * Calculates progress per steps
     */
    fun initializeFeedback(steps: Int) {
        status = " "
        completed = false
        progress = 0
        progressPerStep = 100 / steps
        println("Progress " +progressPerStep +" " +steps)
    }

    fun end(success : Boolean){
        this.success = success
        this.completed = true
    }
}