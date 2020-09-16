package com.patho.main.util.task

import com.patho.main.common.ContactRole
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent

class TaskTools {
    companion object {
        @JvmStatic
        fun getPrimaryContact(task: Task, vararg contactRole: ContactRole): ReportIntent? {
            for (associatedContact in task.contacts) {
                for (i in contactRole.indices) {
                    if (associatedContact.role == contactRole[i])
                        return associatedContact
                }
            }
            return null
        }

        @JvmStatic
        fun getPrimaryContactFromString(task: Task, vararg contactRole: String): ReportIntent? {
            return getPrimaryContact(task, *contactRole.map { p -> ContactRole.valueOf(p) }.toTypedArray())
        }
    }
}
