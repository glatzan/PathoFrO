package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge

/**
 * Exception  is thrown if the notification phase should be ended and not all notifications are completed.
 */
class NotificationNotCompletedException : DialogException("Notification not completed",
        SpringContextBridge.services().resourceBundle["exceptions.notificationNotCompleted.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.notificationNotCompleted.text"] ?: "")