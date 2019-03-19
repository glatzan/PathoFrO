package com.patho.main.action.handler

import com.patho.main.action.views.*
import com.patho.main.common.View
import com.patho.main.model.patient.Task
import com.patho.main.ui.menu.MenuGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
class CentralHandler @Autowired constructor(
        private val worklistHandler: WorklistHandler,
        private val receiptLogView: ReceiptLogView,
        private val diagnosisView: DiagnosisView,
        private val genericViewData: GenericViewData,
        private val taskView: TaskView,
        private val reportView: ReportView) : AbstractHandler() {


    fun reloadView(task: Task = worklistHandler.current.selectedTask, vararg loads: Load) {
        worklistHandler.current.selectedTask = task
        loadView()

    }


    fun loadView(view: View, vararg loads: Load) {
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
            MenuGenerator().generateEditMenu(worklistHandler.getCurrent().getSelectedPatient(),
                    worklistHandler.getCurrent().getSelectedTask(), taskMenuCommandButtons)
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
}