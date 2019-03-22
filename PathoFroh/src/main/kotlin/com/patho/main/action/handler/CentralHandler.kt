package com.patho.main.action.handler

import com.patho.main.action.views.*
import com.patho.main.common.View
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.repository.PatientRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.service.UserService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.menu.JSFMenuGenerator
import com.patho.main.util.worklist.Worklist
import com.patho.main.util.worklist.search.AbstractWorklistSearch
import com.patho.main.util.worklist.search.WorklistSimpleSearch
import org.primefaces.PrimeFaces
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import javax.faces.component.html.HtmlPanelGroup

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
        private val patientRepository: PatientRepository,
        private val taskRepository: TaskRepository) : AbstractHandler() {

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
        worklistHandler.addWorklist(worklist, true)
    }

    open fun reloadView(task: Task = worklistHandler.current.selectedTask, vararg loads: Load) {
        worklistHandler.current.selectedTask = task
//        loadViews()

    }

    open fun goToNavigation() {
        goToNavigation(navigationData.currentView)
    }

    open fun goToNavigation(view: View?) {

        when (view) {
            View.WORKLIST_TASKS -> {
                changeView(View.WORKLIST_TASKS)
                loadViews(View.WORKLIST_TASKS)
            }
            View.WORKLIST_PATIENT -> {
                // show patient if selected
                if (worklistHandler.current.isPatientSelected) {
                    changeView(View.WORKLIST_PATIENT)
                    loadViews(View.WORKLIST_PATIENT)
                } else {
                    // get first patient in worklist, show him
                    val first = worklistHandler.current.firstPatient
                    if (first != null) {
                        onSelectPatient(first, true)
                    } else {
                        // change view to blank
                        changeView(View.WORKLIST_PATIENT, View.WORKLIST_NOTHING_SELECTED)
                    }
                }
            }
            View.WORKLIST_RECEIPTLOG, View.WORKLIST_DIAGNOSIS, View.WORKLIST_REPORT ->
                // if task is select change view
                if (worklistHandler.current.isTaskSelected) {
                    changeView(view)
                    onSelectTaskAndPatient(worklistHandler.current.selectedTask)
                } else if (worklistHandler.current.isPatientSelected) {
                    // no task selected but patient
                    // getting active tasks
                    val tasks = worklistHandler.current.selectedPatient
                            .getActiveTasks(worklistHandler.current.isShowActiveTasksExplicit)

                    var found = false

                    // searching for the first not finalized task
                    for (task in tasks) {
                        if (!task.finalized) {
                            changeView(view)
                            onSelectTaskAndPatient(task)
                            found = true
                            break
                        }
                    }

                    // if all tasks are finalized selecting the first task
                    if (!found) {
                        // display first task, if all task should be shown and there is a task
                        if (!worklistHandler.current.isShowActiveTasksExplicit && !worklistHandler.current.selectedPatient.tasks.isEmpty()) {
                            changeView(view)
                            onSelectTaskAndPatient(
                                    worklistHandler.current.selectedPatient.tasks.iterator().next())
                        } else {
                            changeView(view, View.WORKLIST_NOTHING_SELECTED)
                        }
                    }

                } else {
                    // nothing selected
                    val first = worklistHandler.current.firstActiveTask

                    // select the task
                    if (first != null) {
                        changeView(view)
                        onSelectTaskAndPatient(first)
                    } else {
                        // change view to blank
                        changeView(view, View.WORKLIST_NOTHING_SELECTED)
                    }
                }
            else -> changeView(View.WORKLIST_BLANK)
        }

    }

    /**
     * Changes the current view.
     */
    open fun changeView(view: View) {
        changeView(view, view)
    }

    /**
     * Changes the currentView. The current view is displayed, the displayView is used for the selecting the view
     */
    open fun changeView(currentView: View, displayView: View) {
        logger.debug("Changing view to $currentView display view $displayView")

        navigationData.currentView = currentView
        navigationData.displayView = displayView

        if (currentView.isLastSubviewAble) {
            logger.debug("Setting last default view to $currentView")
            navigationData.lastDefaultView = currentView
        }
    }

    open fun loadViews(vararg loads: Load) {
        loadViews(navigationData.currentView, *loads)
    }

    open fun loadViews(view: View?, vararg loads: Load) {
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
            if (genericViewData.taskMenuCommandButtons != null) {
                genericViewData.taskMenuModel = JSFMenuGenerator().generateEditMenu(worklistHandler.current.selectedPatient,
                        worklistHandler.current.selectedTask, genericViewData.taskMenuCommandButtons as HtmlPanelGroup)
            } else {
                logger.debug("Buttons not ready do nothing")
            }
        }

        when (view) {
            View.WORKLIST_RECEIPTLOG -> {
                logger.debug("Loading receiptlog")
                receiptLogView.loadView(worklistHandler.current.selectedTask)
            }
            View.WORKLIST_DIAGNOSIS -> {
                logger.debug("Loading diagnosis view")
                diagnosisView.loadView(worklistHandler.current.selectedTask)
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
                reportView.loadView(worklistHandler.current.selectedTask)
            }
            else -> {
                logger.debug("Unknow view!")
            }
        }
    }

    open fun onSelectPatient(patientID: Long) {
        onSelectPatient(Patient(patientID), true)
    }

    open fun onSelectPatient(patient: Patient?) {
        onSelectPatient(patient, true)
    }

    open fun onSelectPatient(patient: Patient?, reloadPatient: Boolean) {
        var mutablePatient = patient
        val startTime = System.currentTimeMillis()

        logger.info("start - > 0")

        if (patient == null) {
            logger.debug("Deselecting patient")
            worklistHandler.current.deselectPatient()
            changeView(View.WORKLIST_PATIENT, View.WORKLIST_NOTHING_SELECTED)
            return
        }

        if (reloadPatient) {
            val oPatient = patientRepository.findOptionalById(patient.id, true, true)
            if (oPatient.isPresent)
                mutablePatient = oPatient.get()
            else {
                logger.debug("Could not reload patient, abort!")
                worklistHandler.current.deselectPatient()
                changeView(View.WORKLIST_PATIENT, View.WORKLIST_NOTHING_SELECTED)
                return
            }
        }

        // replacing patient, generating task status
        worklistHandler.current.add(mutablePatient, true)

        changeView(View.WORKLIST_PATIENT)

        loadViews(Load.MENU_MODEL)

        logger.debug("Select patient ${worklistHandler.current.selectedPatient.person.getFullName()}")
        logger.info("end -> ${(System.currentTimeMillis() - startTime)}")
    }

    open fun onSelectTaskAndPatient(taskID: Long) {
        onSelectTaskAndPatient(Task(taskID), true)
    }

    open fun onSelectTaskAndPatient(task: Task) {
        onSelectTaskAndPatient(task, true)
    }

    /**
     * Selects a task and sets the patient of this task as selectedPatient
     */
    open fun onSelectTaskAndPatient(task: Task?, reloadTask: Boolean) {
        val startTime = System.currentTimeMillis()

        logger.info("start - > 0")

        if (task == null) {
            logger.debug("Deselecting task")
            changeView(View.WORKLIST_BLANK)
            return
        }

        var mutableTask = task

        if (reloadTask) {

            logger.debug("Reloading task")

            val oTask = taskRepository.findOptionalByIdAndInitialize(task.id, false, true, true, true,
                    true)

            if (oTask.isPresent) {
                mutableTask = oTask.get()
            } else {
                // task might be delete from an other user
                if (worklistHandler.current.isPatientSelected) {
                    onSelectPatient(worklistHandler.selectedPatient)
                    MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.taskNoFound")
                    PrimeFaces.current()
                            .executeScript("clickButtonFromBean('#globalCommandsForm\\\\:refreshContentBtn')")
                }
                return
            }
        }

        logger.debug("Selecting task ${mutableTask.taskID} for ${mutableTask.patient?.person?.getFullName()}, patient has ${mutableTask.patient?.tasks?.size} tasks ")

        // replacing patient, generating task status
        worklistHandler.current.add(mutableTask, true)

        // task.setActive(true);

        // change if is subview (diagnosis, receipt log or report view)
        if (!navigationData.currentView.isLastSubviewAble) {
            logger.debug("Setting subview ${navigationData.lastDefaultView}")
            changeView(navigationData.lastDefaultView)
        }

        // generating task data, taskstatus is generated previously
        loadViews(Load.MENU_MODEL)

        logger.info("Request processed in -> ${(System.currentTimeMillis() - startTime)} ")
    }

    /**
     * Deselects a task an show the worklist patient view.
     */
    open fun onDeselectTask() {
        if (worklistHandler.current.deselectTask())
            goToNavigation(View.WORKLIST_PATIENT)
    }

    enum class Load {
        RELOAD_TASK_STATUS, MENU_MODEL, RELOAD_MENU_MODEL_FAVOURITE_LISTS, RELOAD_TASK, GENERIC_DATA
    }


    open class NavigationData {

        /**
         * View options, dynamically generated depending on the users role
         */
        open var navigationPages: List<View> = listOf()

        /**
         * Selected View in the menu
         */
        open var displayView: View = View.WORKLIST_BLANK

        /**
         * Current view which is displayed
         */
        open var currentView: View = View.WORKLIST_BLANK

        /**
         * Can be Diagnosis or Receiptlog
         */
        open var lastDefaultView: View = View.WORKLIST_BLANK

        /**
         * Returns the center view if present, otherwise an empty page will be
         * returned**@return
         */
        open val centerView: String
            get() = if (displayView != null)
                displayView?.path
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