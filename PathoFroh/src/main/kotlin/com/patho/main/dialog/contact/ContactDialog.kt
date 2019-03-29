package com.patho.main.dialog.contact

import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.task.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.repository.TaskRepository
import com.patho.main.service.ReportIntentService
import com.patho.main.util.dialogReturn.ReloadEvent
import com.patho.main.util.task.TaskNotFoundException
import com.patho.main.util.ui.selector.ReportIntentSelector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ContactDialog @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val reportIntentService: ReportIntentService) :
        AbstractTaskDialog() {

    var showRole = arrayOf<ContactRole>()
    var selectAbleRoles = arrayOf<ContactRole>()
    var reportIntents = arrayOf<ReportIntentSelector>()

    override fun initBean(task: Task): Boolean {
        selectAbleRoles = ContactRole.values()
        showRole = ContactRole.values()
        reportIntents = task.contacts.map { p -> ReportIntentSelector(p) }.toTypedArray()
        return super.initBean(task, Dialog.CONTACTS)
    }

    fun update(reload: Boolean) {
        super.update()

        if (reload) {
            var optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, false, false, false, true, true)
            if (!optionalTask.isPresent)
                throw TaskNotFoundException()
        }

        reportIntents = task.contacts.map { p -> ReportIntentSelector(p) }.toTypedArray()
    }

    fun removeContact(reportIntent: ReportIntent) {
        reportIntentService.removeReportIntent(task, reportIntent)
        update(true)
    }



    override fun hideDialog() {
        super.hideDialog(ReloadEvent())
    }
}