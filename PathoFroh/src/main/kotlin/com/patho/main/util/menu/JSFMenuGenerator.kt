package com.patho.main.util.menu

import com.patho.main.action.handler.CentralHandler
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.user.HistoPermissions
import com.patho.main.service.impl.SpringContextBridge
import org.primefaces.model.menu.*
import org.slf4j.LoggerFactory
import javax.faces.component.html.HtmlPanelGroup

class JSFMenuGenerator {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun generateEditMenu(patient: Patient?, task: Task?, taskMenuCommandButtons: HtmlPanelGroup): MenuModel {
        logger.debug("Generating new MenuModel")

        val model = DefaultMenuModel()

        val resourceBundle = SpringContextBridge.services().resourceBundle
        val userService = SpringContextBridge.services().userService
        val favouriteListRepository = SpringContextBridge.services().favouriteListRepository

        if (taskMenuCommandButtons == null) {
            logger.error("No button container connected!")
            return model
        } else
        // clearing command button array
            taskMenuCommandButtons.children.clear()

        // patient menu
        val permissionPatientEdit = userService.userHasPermission(HistoPermissions.PATIENT_EDIT)

        // patient menu
        val patientSubMenu = DefaultSubMenu(resourceBundle.get("pages.header.menu.patient.headline"))
        model.addElement(patientSubMenu)

        // add patient
        var item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.add"))
        item.onclick = getOnClickCommand("addPatientButton")
        item.icon = "fa fa-user"
        item.isRendered = permissionPatientEdit
        patientSubMenu.addElement(item)

        // separator
        var seperator = DefaultSeparator()
        seperator.isRendered = permissionPatientEdit
        patientSubMenu.addElement(seperator)

        // patient overview
        item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.overview"))
        item.onclick = getOnClickCommand("showPatientOverview")
        item.icon = "fa fa-tablet"
        item.isDisabled = patient == null
        patientSubMenu.addElement(item)

        val permissionMerge = userService
                .userHasPermission(HistoPermissions.PATIENT_EDIT_MERGE)
        val permissionEdit = userService
                .userHasPermission(HistoPermissions.PATIENT_EDIT_ALTER_DATA)
        val permissionDelete = userService
                .userHasPermission(HistoPermissions.PATIENT_EDIT_DELETE)
        val permissionPDF = userService
                .userHasPermission(HistoPermissions.PATIENT_EDIT_UPLOAD_DATA)

        if (patient != null && (permissionMerge || permissionEdit || permissionDelete || permissionPDF)) {

            val administerSubMenu = DefaultSubMenu(
                    resourceBundle.get("pages.header.menu.patient.administer.headline"))
            administerSubMenu.icon = "fa fa-male"
            patientSubMenu.addElement(administerSubMenu)

            // patient edit data, disabled if not external patient
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.administer.edit"))
            item.onclick = "$('#headerForm\\\\:editPatientData').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-pencil-square-o"
            item.isDisabled = true
            item.isRendered = permissionEdit
            administerSubMenu.addElement(item)

            // remove patient if empty tasks
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.administer.remove"))
            item.onclick = "$('#headerForm\\\\:removePatient').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-trash"
            item.isDisabled = true
            item.isRendered = permissionDelete
            administerSubMenu.addElement(item)

            // patient merge
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.administer.merge"))
            item.onclick = "$('#headerForm\\\\:mergePatientData').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-medkit"
            item.isRendered = permissionMerge
            administerSubMenu.addElement(item)

            // patient upload pdf
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.patient.upload"))
            item.onclick = "$('#headerForm\\\\:uploadBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-cloud-upload"
            item.isRendered = permissionPDF
            patientSubMenu.addElement(item)

        }

        // task menu
        if (patient != null && userService.userHasPermission(HistoPermissions.TASK_EDIT)) {

            val taskIsNull = task == null

            if (!taskIsNull && task!!.taskStatus == null) {
                task.generateTaskStatus()
            }

            val taskIsEditable = if (taskIsNull) false else task!!.taskStatus.editable

            val taskSubMenu = DefaultSubMenu(resourceBundle.get("pages.header.menu.task.headline"))
            model.addElement(taskSubMenu)

            // new task
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.task.add"))
            item.onclick = "$('#headerForm\\\\:newTaskBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-file"
            item.isRendered = userService.userHasPermission(HistoPermissions.TASK_EDIT_NEW)
            taskSubMenu.addElement(item)

            // new sample, if task is not null
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.add"))
            item.onclick = "$('#headerForm\\\\:newSampleBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-eyedropper"
            item.isRendered = !taskIsNull
            item.isDisabled = !taskIsEditable
            taskSubMenu.addElement(item)

            // staining submenu
            if (!taskIsNull) {
                val stainingSubMenu = DefaultSubMenu(
                        resourceBundle.get("pages.header.menu.sample.slide.headline"))
                stainingSubMenu.icon = "fa fa-paint-brush"
                taskSubMenu.addElement(stainingSubMenu)

                // new slide
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.slide.add"))
                item.onclick = "$('#headerForm\\\\:stainingOverview').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-paint-brush"
                item.isDisabled = !taskIsEditable
                stainingSubMenu.addElement(item)

                // ***************************************************
                seperator = DefaultSeparator()
                seperator.isRendered = task!!.taskStatus.editable
                stainingSubMenu.addElement(seperator)

                // leave staining phase regularly
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.slide.endPhase"))
                item.onclick = "$('#headerForm\\\\:stainingPhaseExit').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-image"
                item.isRendered = task.taskStatus.listStatus.inListStaining || task.taskStatus.listStatus.inListReStaining
                item.isDisabled = !taskIsEditable
                stainingSubMenu.addElement(item)

                // Remove from staining phase
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.slide.endStayInPhase"))
                item.command = ("#{globalEditViewHandler.removeTaskFromFavouriteList(worklistHandler.selectedTask, "
                        + PredefinedFavouriteList.StayInStainingList.id + ")}")
                item.icon = "fa fa-image"
                item.isRendered = task.taskStatus.listStatus.inListStayInStaining
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                stainingSubMenu.addElement(item)

                // Add to stay in staining phase
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.slide.enterStayInPhase"))
                item.onclick = "$('#headerForm\\\\:stainingPhaseEnter').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-image"
                item.isRendered = !(task.taskStatus.listStatus.inListStaining || task.taskStatus.listStatus.inListReStaining
                        || task.taskStatus.listStatus.inListStayInStaining) && task.taskStatus.editable
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                stainingSubMenu.addElement(item)
            }

            // diagnosis menu
            if (!taskIsNull) {
                val diagnosisSubMenu = DefaultSubMenu(
                        resourceBundle.get("pages.header.menu.sample.diagnosis.headline"))
                diagnosisSubMenu.icon = "fa fa-search"
                taskSubMenu.addElement(diagnosisSubMenu)

                // new diagnosis
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.diagnosis.add"))
                item.onclick = "$('#headerForm\\\\:newDiagnosisRevision').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-pencil-square-o"
                item.isDisabled = !taskIsEditable
                diagnosisSubMenu.addElement(item)

                // council
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.diagnosis.concils"))
                item.onclick = "$('#headerForm\\\\:councilBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-comment-o"
                diagnosisSubMenu.addElement(item)

                // ***************************************************
                seperator = DefaultSeparator()
                seperator.isRendered = task!!.taskStatus.editable
                diagnosisSubMenu.addElement(seperator)

                // Leave diagnosis phase if in phase an not complete
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.diagnosis.endPhase"))
                item.onclick = "$('#headerForm\\\\:diagnosisPhaseExit').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-eye-slash"
                item.isRendered = task.taskStatus.listStatus.inListDiagnosis || task.taskStatus.listStatus.inListReDiagnosis
                item.isDisabled = !taskIsEditable
                diagnosisSubMenu.addElement(item)

                // Leave phase if stay in phase
                item = DefaultMenuItem(
                        resourceBundle.get("pages.header.menu.sample.diagnosis.endStayInPhase"))
                item.command = ("#{globalEditViewHandler.removeTaskFromFavouriteList(worklistHandler.selectedTask, "
                        + PredefinedFavouriteList.StayInDiagnosisList.id + ")}")
                item.isRendered = task.taskStatus.listStatus.inListStayInDiagnosis
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                item.icon = "fa fa-eye-slash"
                diagnosisSubMenu.addElement(item)

                // enter diagnosis pahse
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.diagnosis.enterStayInPhase"))
                item.onclick = "$('#headerForm\\\\:diagnosisPhaseEnter').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.isRendered = !(task.taskStatus.listStatus.inListDiagnosis || task.taskStatus.listStatus.inListReDiagnosis
                        || task.taskStatus.listStatus.inListStayInDiagnosis) && task.taskStatus.editable
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                item.icon = "fa fa-eye"
                diagnosisSubMenu.addElement(item)
            }

            // notification submenu
            if (!taskIsNull) {
                val notificationSubMenu = DefaultSubMenu(
                        resourceBundle.get("pages.header.menu.sample.notification.headline"))
                notificationSubMenu.icon = "fa fa-volume-up"
                taskSubMenu.addElement(notificationSubMenu)

                // contacts
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.contact"))
                item.onclick = "$('#headerForm\\\\:editContactBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-envelope-o"
                notificationSubMenu.addElement(item)

                // print
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.print"))
                item.onclick = "$('#headerForm\\\\:printBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-print"
                notificationSubMenu.addElement(item)

                // ***************************************************
                seperator = DefaultSeparator()
                seperator.isRendered = task!!.taskStatus.editable
                notificationSubMenu.addElement(seperator)

                // notification perform
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.endPhase"))
                item.onclick = "$('#headerForm\\\\:notificationPerformBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-volume-up"
                item.isRendered = !task.taskStatus.finalized
                notificationSubMenu.addElement(item)

                // exit notification phase, without performing notification
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.endPhaseForced"))
                item.onclick = "$('#headerForm\\\\:notificationPhaseExit').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                item.icon = "fa fa-volume-off"
                item.isRendered = task.taskStatus.listStatus.inListNotification || task.taskStatus.listStatus.inListStayInNotification
                notificationSubMenu.addElement(item)

                // exit stay in notification phase
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.endStayInPhase"))
                item.command = ("#{globalEditViewHandler.removeTaskFromFavouriteList(worklistHandler.selectedTask, "
                        + PredefinedFavouriteList.StayInNotificationList.id + ")}")
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                item.icon = "fa fa-volume-off"
                item.isRendered = task.taskStatus.listStatus.inListStayInNotification
                notificationSubMenu.addElement(item)

                // add to notification phase
                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.sample.notification.enterStayInPhase"))
                item.command = ("#{globalEditViewHandler.addTaskToFavouriteList(worklistHandler.selectedTask, "
                        + PredefinedFavouriteList.NotificationList.id + ")}")
                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                item.update = "navigationForm:patientList contentForm headerForm"
                item.icon = "fa fa-volume-off"
                item.isRendered = !(task.taskStatus.listStatus.inListNotification || task.taskStatus.listStatus.inListStayInNotification) && task.taskStatus.editable
                notificationSubMenu.addElement(item)

            }

            // finalized
            if (!taskIsNull) {
                seperator = DefaultSeparator()
                seperator.isRendered = userService.userHasPermission(HistoPermissions.TASK_EDIT_ARCHIVE,
                        HistoPermissions.TASK_EDIT_RESTORE)
                taskSubMenu.addElement(seperator)

                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.task.archive"))
                item.onclick = "$('#headerForm\\\\:archiveTaskBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-archive"
                item.isRendered = !task!!.finalized && userService.userHasPermission(HistoPermissions.TASK_EDIT_ARCHIVE)
                taskSubMenu.addElement(item)

                item = DefaultMenuItem(resourceBundle.get("pages.header.menu.task.dearchive"))
                item.onclick = "$('#headerForm\\\\:restoreTaskBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
                item.icon = "fa fa-dropbox"
                item.isRendered = task.finalized && userService.userHasPermission(HistoPermissions.TASK_EDIT_RESTORE)
                taskSubMenu.addElement(item)
            }

            seperator = DefaultSeparator()
            seperator.isRendered = !taskIsNull
            taskSubMenu.addElement(seperator)

            // Favorite lists
            if (task != null) {

                val items = favouriteListRepository
                        .getMenuItems(userService.currentUser, task)

                // only render of size > 0
                if (items.size > 0) {
                    val favouriteSubMenu = DefaultSubMenu(resourceBundle.get("pages.header.menu.favouriteLists.headline"))
                    favouriteSubMenu.icon = "fa fa-list-alt"
                    taskSubMenu.addElement(favouriteSubMenu)

                    val hiddenFavouriteSubMenu = DefaultSubMenu(resourceBundle.get("pages.header.menu.favouriteLists.hiddenLists"))
                    favouriteSubMenu.icon = "fa fa-list-alt"

                    for (favouriteListItem in items) {
                        item = DefaultMenuItem(favouriteListItem.getName())

                        // if the favourite lists contains the task, option to remove ist
                        if (favouriteListItem.isContainsTask()) {
                            item.icon = "fa fa-check-circle icon-green"

                            // if favourite has a dumplist, open the dialog for moving the task to the
                            // dumplist
                            if (favouriteListItem.isDumpList()) {

                                val backendButton = JSFBackendButton().initializeButton("favouriteListItemRemoveDialog.initAndPrepareBean",
                                        variables = listOf(JSFBackendButton.VariableHolder<Task>(task, "task"),
                                                JSFBackendButton.VariableHolder<Long>(favouriteListItem.id, "favListId")))

                                backendButton.addAjaxBehaviorToButton(
                                        event = "dialogReturn",
                                        signature = "centralHandler.loadViews",
                                        update = "navigationForm:patientList contentForm headerForm",
                                        onComplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll');",
                                        variables = listOf(JSFBackendButton.VariableHolder<CentralHandler.Load>(CentralHandler.Load.MENU_MODEL, "valu1"),
                                                JSFBackendButton.VariableHolder<CentralHandler.Load>(CentralHandler.Load.RELOAD_TASK_STATUS, "valu2"),
                                                JSFBackendButton.VariableHolder<CentralHandler.Load>(CentralHandler.Load.RELOAD_MENU_MODEL_FAVOURITE_LISTS, "valu3")))

                                backendButton.addToParent(taskMenuCommandButtons)

                                // onlick active the command button
                                item.onclick = ("$('#headerForm\\\\:" + backendButton.id
                                        + "').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;")

                            } else {
                                item.command = ("#{globalEditViewHandler.removeTaskFromFavouriteList(worklistHandler.selectedTask, "
                                        + favouriteListItem.getId() + ")}")
                                item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                                item.update = "navigationForm:patientList contentForm headerForm"
                            }
                        } else {
                            item.icon = "fa fa-circle-o"
                            item.command = ("#{globalEditViewHandler.addTaskToFavouriteList(worklistHandler.selectedTask, "
                                    + favouriteListItem.getId() + ")}")
                            item.oncomplete = "updateAndAutoScrollToSelectedElement('navigationForm:patientNavigationScroll')"
                            item.update = "navigationForm:patientList contentForm headerForm"
                        }

                        if (favouriteListItem.isHidden())
                            hiddenFavouriteSubMenu.addElement(item)
                        else
                            favouriteSubMenu.addElement(item)
                    }

                    // only adding hidden lists if one list is present
                    if (hiddenFavouriteSubMenu.elements.size > 0) {
                        favouriteSubMenu.addElement(DefaultSeparator())
                        favouriteSubMenu.addElement(hiddenFavouriteSubMenu)
                    }

                } else {
                    item = DefaultMenuItem(resourceBundle.get("pages.header.menu.favouriteLists.none"))
                    item.icon = "fa fa-list-alt"
                    item.isDisabled = true
                    taskSubMenu.addElement(item)
                }

            }

            // biobank
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.task.biobank"))
            item.onclick = "$('#headerForm\\\\:bioBankBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-leaf"

            item.isDisabled = !taskIsEditable
            taskSubMenu.addElement(item)

            // upload
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.task.upload"))
            item.onclick = "$('#headerForm\\\\:uploadBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
            item.icon = "fa fa-cloud-upload"

            taskSubMenu.addElement(item)

            // log
            item = DefaultMenuItem(resourceBundle.get("pages.header.menu.log"))
            item.onclick = "$('#headerForm\\\\:logBtn').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"

            model.addElement(item)
        }

        return model

    }

    private fun getOnClickCommand(id: String): String {
        return "$('#headerForm\\\\:$id').click();$('#headerForm\\\\:taskTieredMenuButton').hide();return false;"
    }
}