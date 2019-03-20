package com.patho.main.action.handler

import com.patho.main.action.views.*
import com.patho.main.common.View
import com.patho.main.model.patient.Task
import com.patho.main.service.UserService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.worklist.Worklist
import com.patho.main.util.worklist.search.AbstractWorklistSearch
import com.patho.main.util.worklist.search.WorklistSimpleSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class CentralHandler @Autowired constructor(
        private val worklistHandler: WorklistHandler,
        private val receiptLogView: ReceiptLogView,
        private val diagnosisView: DiagnosisView,
        private val genericViewData: GenericViewData,
        private val taskView: TaskView,
        private val reportView: ReportView,
        private val userService: UserService,
        private val worklistViewHandler: WorklistViewHandler) : AbstractHandler() {

    open val navigationData: NavigationData = NavigationData()

    override fun loadHandler() {
        logger.debug("Loading Central Handler")

        val settings = userService.currentUser.settings
        val worklistToLoad = settings.worklistToLoad
        // if a default to load was provided
        val worklist: Worklist = if (worklistToLoad != null && worklistToLoad != WorklistSimpleSearch.SimpleSearchOption.EMPTY_LIST) {
            Worklist("Default", WorklistSimpleSearch(worklistToLoad), settings)
        } else
            Worklist("Default", AbstractWorklistSearch())

        logger.debug("2. Setting navigation data")

        // setting start view
        navigationData.currentView = settings.startView
        // setting default subview
        navigationData.lastDefaultView = settings.defaultView

        logger.debug("3. Loading common data")
        genericViewData.loadView()

        logger.debug("4. Loading view data")
        navigationData.updateData()

        logger.debug("5. Setting worklist")
        worklistViewHandler.addWorklist(worklist, true)
    }

    open fun reloadView(task: Task = worklistHandler.current.selectedTask, vararg loads: Load) {
        worklistHandler.current.selectedTask = task
//        loadViews()

    }

    open fun loadViews(view: View, vararg loads: Load) {
        logger.debug("Loading Page")

        // reloads task and generates a new task info
        if (loads.contains(Load.RELOAD_TASK)) {
            worklistHandler.current.reloadSelectedPatientAndTask()
        }

        if (loads.contains(Load.GENERIC_DATA)) {
            genericViewData.loadView(worklistHandler.current.selectedTask)
        }

        if (loads.contains(Load.RELOAD_TASK_STATUS)) {
            worklistHandler.current.selectedTask.generateTaskStatus()
        }

        if (loads.contains(Load.MENU_MODEL)) {
//            MenuGenerator().generateEditMenu(worklistHandler.getCurrent().getSelectedPatient(),
//                    worklistHandler.getCurrent().getSelectedTask(), taskMenuCommandButtons)
        }

        when (view) {
            View.WORKLIST_RECEIPTLOG -> {
                logger.debug("Loading receiptlog")
                receiptLogView.loadView(worklistHandler.current.selectedTask)
            }
            View.WORKLIST_DIAGNOSIS -> {
                logger.debug("Loading diagnosis view")
                diagnosisView.loadView(worklistHandler.getCurrent().getSelectedTask())
            }
            View.WORKLIST_PATIENT -> {
                logger.debug("Loading patient view")
            }
            View.WORKLIST_TASKS -> {
                logger.debug("Loading task view")
                taskView.loadView()
            }
            View.WORKLIST_REPORT -> {
                logger.debug("Loading report view")
                reportView.loadView()
            }
            else -> {
                logger.debug("Unknow view!")
            }
        }
    }

    enum class Load {
        RELOAD_TASK_STATUS, MENU_MODEL, RELOAD_MENU_MODEL_FAVOURITE_LISTS, RELOAD_TASK, GENERIC_DATA
    }


    open class NavigationData {

        /**
         * View options, dynamically generated depending on the users role
         */
        open var navigationPages: List<View>? = null

        /**
         * Selected View in the menu
         */
        open var displayView: View? = null

        /**
         * Current view which is displayed
         */
        open var currentView: View? = null

        /**
         * Can be Diagnosis or Receiptlog
         */
        open var lastDefaultView: View? = null

        /**
         * Returns the center view if present, otherwise an empty page will be
         * returned**@return
         */
        open val centerView: String
            get() = if (displayView != null)
                displayView?.path ?: ""
            else
                View.WORKLIST_BLANK.path

        /**
         * Loading all data from Backend
         */
        open fun updateData() {
            navigationPages = SpringContextBridge.services().userService.currentUser.settings.availableViews.toList()
        }
    }
}