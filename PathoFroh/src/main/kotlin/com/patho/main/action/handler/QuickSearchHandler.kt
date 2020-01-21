package com.patho.main.action.handler

import com.patho.main.model.patient.Patient
import com.patho.main.model.user.HistoPermissions
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.PatientService
import com.patho.main.service.UserService
import com.patho.main.util.exceptions.TaskNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Controller
import java.util.*
import javax.faces.application.FacesMessage

@Controller
@Scope("session")
class QuickSearchHandler @Autowired constructor(
        private val userService: UserService,
        private val taskRepository: TaskRepository,
        private val worklistHandler: WorklistHandler,
        private val patientService: PatientService) : AbstractHandler() {

    /**
     * Search String for quick search
     */
    var quickSearch: String? = null

    /**
     * True if search foucus is current worklist
     * TODO: Implement
     */
    var isSearchWorklist: Boolean = false

    /**
     * Launch data for opening a patient add dialog from quick search
     */
    var extendedLaunchData: ExtendedLaunchData = ExtendedLaunchData()


    override fun loadHandler() {
        quickSearch = ""
    }

    fun quickSearch() {
        quickSearch(quickSearch ?: "", userService.currentUser.settings.alternatePatientAddMode)
        quickSearch = ""
    }

    fun quickSearch(serach: String, alternateMode: Boolean) {
        logger.debug("Search for $serach, AlternateMode: $alternateMode")

        var quickSerach = serach
        // search only in selected worklist
        if (isSearchWorklist) {
            logger.debug("Search in worklist")
        } else {
            // removing spaces
            quickSerach = quickSerach.trim { it <= ' ' }

            if (quickSerach.matches("^\\d{6}$".toRegex())) { // task
                // search for task (6 digits)

                try {
                    val task = taskRepository.findByTaskID(quickSerach, false, true, true, true, true)
                    logger.debug("Task found, adding to worklist")
                    worklistHandler.addTaskToWorklist(task, true)
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.task.headline",
                            "growl.search.task.text")
                } catch (e: TaskNotFoundException) {
                    // no task was found
                    logger.debug("No task with the given id found")
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.task.notFound", "general.blank",
                            FacesMessage.SEVERITY_ERROR)
                }
            } else if (quickSerach.matches("^\\d{8}$".toRegex())) { // piz
                // searching for piz (8 digits)
                logger.debug("Search for piz: $quickSerach")

                // Searching for patient in pdv and local database
                var patient: Optional<Patient>
                try {
                    patient = patientService.findPatientByPizInDatabaseAndPDV(quickSerach,
                            !userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC), true,
                            true)
                } catch (e: Exception) {
                    patient = Optional.empty()
                }

                if (patient.isPresent) {
                    logger.debug("Found patient $patient and adding to currentworklist")

                    worklistHandler.addPatientToWorkList(patient.get(), true, true)
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.headline",
                            "growl.search.patient.text")

                    // if alternate mode the create Task dialog will be
                    // shown after the patient is added to the worklist
                    if (alternateMode) {
                        // starting task button from gui (return handler)
                        MessageHandler.executeScript("clickButtonFromBean('headerForm:newTaskBtn')")
                    }

                } else {
                    // no patient was found for piz
                    logger.debug("No Patient found with piz $quickSerach")

                    MessageHandler.sendGrowlMessagesAsResource("growl.search.patient.notFoundPiz", "general.blank",
                            FacesMessage.SEVERITY_ERROR)
                }
            } else if (quickSerach.matches("^\\d{9}$".toRegex())) { // slide id
                // searching for slide (9 digits)
                logger.debug("Search for SlideID: $quickSerach")

                val taskId = quickSerach.substring(0, 6)
                val uniqueSlideIDinTask = quickSerach.substring(6, 9)

                try {
                    val task = taskRepository.findBySlideID(taskId,
                            Integer.parseInt(uniqueSlideIDinTask), false, true, true, true, true)
                    logger.debug("Slide found")
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.slide.headline",
                            "growl.search.slide.text")
                    worklistHandler.addTaskToWorklist(task, true)
                } catch (e: TaskNotFoundException) {
                    // no slide was found
                    logger.debug("No slide with the given id found")
                    MessageHandler.sendGrowlMessagesAsResource("growl.search.slide.notFount", "general.blank",
                            FacesMessage.SEVERITY_ERROR)
                }

            } else if (quickSerach.matches("^(.+)(, )(.+)$".toRegex())) {
                logger.debug("Search for name, first name")
                // name, surename; name surename
                val arr = quickSerach.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // setting data for launching dialog via gui element (return handler
                extendedLaunchData.set(arr[0], arr[1], "", null)
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')")
            } else if (quickSerach.matches("^(.+) (.+)$".toRegex())) {
                logger.debug("Search for firstname, name")
                // name, surename; name surename
                val arr = quickSerach.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                extendedLaunchData.set(arr[1], arr[0], "", null)
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')")
            } else if (quickSerach.matches("^[\\p{Alpha}\\-]+".toRegex())) {
                logger.debug("Search for name")
                extendedLaunchData.set(quickSerach, "", "", null)
                MessageHandler.executeScript("clickButtonFromBean('navigationForm:searchPatientButtonQuickSeach')")
            } else {
                logger.debug("No search match found")
                MessageHandler.sendGrowlMessagesAsResource("growl.search.notFount", "general.blank",
                        FacesMessage.SEVERITY_ERROR)
            }
        }
    }

    /**
     * Launch data for the patient search dialog
     */
    class ExtendedLaunchData {
        var name: String? = null
        var surname: String? = null
        var piz: String? = null
        var birthday: Date? = null

        fun set(name: String?, surname: String?, piz: String?, birthday: Date?) {
            this.name = name
            this.surname = surname
            this.piz = piz
            this.birthday = birthday
        }
    }
}