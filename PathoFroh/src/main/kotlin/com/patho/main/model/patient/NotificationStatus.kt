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
     * This status is used if a notification has been performed
     */
    NOTIFICATION_PERFORMED,
    /**
     * This status is used if a notification failed
     */
    NOTIFICATION_FAILED,
    /**
     * Notification was performed
     */
    NOTIFICATION_COMPLETED
}