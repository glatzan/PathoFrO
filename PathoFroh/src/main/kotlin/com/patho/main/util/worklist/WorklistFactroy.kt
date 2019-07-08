package com.patho.main.util.worklist

import com.patho.main.model.patient.Task
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.search.settings.SearchSettings
import com.patho.main.util.search.settings.StaticTaskListSearch
import com.patho.main.util.worklist.Worklist

class WorklistFactroy {

    companion object {
        @JvmStatic
        fun defaultWorklist(search: SearchSettings, showActiveTasks: Boolean = false): Worklist {
            return Worklist("Default", search,
                    SpringContextBridge.services().userService.currentUser.settings.worklistHideNoneActiveTasks,
                    SpringContextBridge.services().userService.currentUser.settings.worklistSortOrder,
                    SpringContextBridge.services().userService.currentUser.settings.worklistAutoUpdate, showActiveTasks,
                    SpringContextBridge.services().userService.currentUser.settings.worklistSortOrderAsc)
        }

        @JvmStatic
        fun taskWorklist(tasks : List<Task>): Worklist {
            return Worklist("Default", StaticTaskListSearch(tasks),
                    SpringContextBridge.services().userService.currentUser.settings.worklistHideNoneActiveTasks,
                    SpringContextBridge.services().userService.currentUser.settings.worklistSortOrder,
                    SpringContextBridge.services().userService.currentUser.settings.worklistAutoUpdate, true,
                    SpringContextBridge.services().userService.currentUser.settings.worklistSortOrderAsc)
        }
    }

}