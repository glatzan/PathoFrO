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
     * Loads the feedback from the message file
     */
    fun setFeedback(key: String, vararg insert: String) {
        SpringContextBridge.services().resourceBundle.get(key, insert)
    }

    /**
     * Increments the progress counter
     */
    fun progress() {
        progress++;
    }
}