package com.patho.main.util

import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.search.settings.SearchSettings
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
    }

}