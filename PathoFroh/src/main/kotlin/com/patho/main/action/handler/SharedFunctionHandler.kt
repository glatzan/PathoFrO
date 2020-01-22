package com.patho.main.action.handler

import com.patho.main.model.patient.Task
import com.patho.main.service.FavouriteListService
import com.patho.main.util.helper.HistoUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Controller

@Controller
@Scope("session")
open class SharedFunctionHandler @Autowired constructor(
        private val favouriteListService: FavouriteListService,
        private val worklistHandler: WorklistHandler) : AbstractHandler() {

    override fun loadHandler() {
    }

    /**
     * Adds an task to a favourite list
     */
    open fun addTaskToFavouriteList(task: Task, favouriteListID: Long) {
        var tmp = favouriteListService.addTaskToList(task.id, favouriteListID)

        // checking for success
        val list = tmp.favouriteLists.firstOrNull { it.id == favouriteListID }

        if (list != null)
            MessageHandler.sendGrowlMessagesAsResource("growl.favouriteList.added.headline",
                    "growl.favouriteList.added.textSuccess", tmp.taskID, list.name)
        else
            MessageHandler.sendGrowlErrorAsResource("growl.favouriteList.added.headline",
                    "growl.favouriteList.added.textError", tmp.taskID)

        worklistHandler.replaceTaskInWorklist(tmp)
    }

    /**
     * Removes a task from a favourite list
     */
    open fun removeTaskFromFavouriteList(task: Task, favouriteListID: Long) {
        val removeList = task.favouriteLists.firstOrNull { it.id == favouriteListID }

        // error task is not in list
        if (removeList == null) {
            MessageHandler.sendGrowlMessagesAsResource("growl.favouriteList.removed.headline",
                    "growl.favouriteList.removed.textErrorNotInList", task.taskID)
            return
        }

        var tmp = favouriteListService.removeTaskFromList(task.id, favouriteListID)

        // check for success
        val list = tmp.favouriteLists.firstOrNull { it.id == favouriteListID }

        if (list == null)
            MessageHandler.sendGrowlMessagesAsResource("growl.favouriteList.removed.headline",
                    "growl.favouriteList.removed.textSuccess", tmp.taskID, removeList.name)
        else
            MessageHandler.sendGrowlErrorAsResource("growl.favouriteList.removed.headline",
                    "growl.favouriteList.removed.textError", tmp.taskID, removeList.name)

        worklistHandler.replaceTaskInWorklist(tmp)
    }

    // TODO TEST
    open fun executeCommand(command: String, task: Task?) {
        if (HistoUtil.isNotNullOrEmpty(command)) {
//            if (command.matches("dialog:dialog.councilDialog")) {
//                MessageHandler.executeScript("clickButtonFromBean('headerForm:councilBtn')")
//            }
        }
    }
}