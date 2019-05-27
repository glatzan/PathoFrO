package com.patho.main.model.patient

/**
 * Notification status for a reportIntent revision
 */
enum class NotificationStatus {
    /**
     * Not approved yet
     */
    NOT_APPROVED,
    /**
     * Notification is pending
     */
    NOTIFICATION_PENDING,
    /**
     * No Notification should be performed
     */
    NO_NOTFICATION,
    /**
     * Notification was performed
     */
    NOTIFICATION_COMPLETED
}