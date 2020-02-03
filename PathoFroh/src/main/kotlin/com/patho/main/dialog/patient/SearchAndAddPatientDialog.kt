package com.patho.main.dialog.patient

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.Dialog
import com.patho.main.config.excepion.ToManyEntriesException
import com.patho.main.dialog.AbstractTabDialog_
import com.patho.main.model.patient.Patient
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.model.user.HistoPermissions
import com.patho.main.service.PatientService
import com.patho.main.service.UserService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.ui.ListChooser
import com.patho.main.util.dialog.event.PatientSelectEvent
import com.patho.main.util.dialog.event.ReloadEvent
import com.patho.main.util.exception.CustomNullPatientExcepetion
import com.patho.main.util.ui.jsfcomponents.IPatientTableDataProvider
import com.patho.main.util.ui.jsfcomponents.IPersonDataChangeListener
import org.primefaces.event.SelectEvent
import org.primefaces.json.JSONException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Component()
class SearchAndAddPatientDialog @Autowired constructor(
        private val userService: UserService,
        private val patientService: PatientService) : AbstractTabDialog_(Dialog.PATIENT_ADD) {

    val clinicSearchTab = ClinicSearchTab()
    val externalPatientTab = ExternalPatientTab()

    var isShowExternalPatientTab: Boolean = false

    var isPersistPatient: Boolean = false

    init {
        tabs = arrayOf(clinicSearchTab)
    }

    override fun initBean(): Boolean {
        logger.debug("Initializing notification dialog")
        isShowExternalPatientTab = false
        isPersistPatient = false
        clinicSearchTab.disabled = false
        return super.initBean(true, clinicSearchTab)
    }

    fun externalMode(): SearchAndAddPatientDialog {
        return externalMode(false)
    }

    fun externalMode(forceExternalMode: Boolean = false): SearchAndAddPatientDialog {
        // only enable if forced or user has the permission to add external patients
        if (forceExternalMode || userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN)) {
            tabs = arrayOf(clinicSearchTab, externalPatientTab)
            externalPatientTab.disabled = false
            externalPatientTab.initTab(true)
        }
        return this
    }

    fun persistPatient(): SearchAndAddPatientDialog {
        isPersistPatient = true
        return this
    }

    fun initializeValues(name: String, surname: String, piz: String, date: LocalDate?): SearchAndAddPatientDialog {
        clinicSearchTab.patientName = name
        clinicSearchTab.patientBirthday = date
        clinicSearchTab.patientSurname = surname
        clinicSearchTab.patientPiz = piz
        clinicSearchTab.searchForClinicPatients()
        return this
    }

    open inner class ClinicSearchTab(
            tabName: String = "ClinicSearchTab",
            name: String = "dialog.searchPatient.search",
            viewID: String = "clinicSearch",
            centerInclude: String = "_clinicSearch.xhtml"
    ) : AbstractTabDialog_.AbstractTab(tabName, name, viewID, centerInclude), IPatientTableDataProvider {

        /**
         * Piz of Patient
         */
        var patientPiz: String = ""
        /**
         * Name of patient
         */
        var patientName: String = ""
        /**
         * Surname of patient
         */
        var patientSurname: String = ""
        /**
         * Birthday of patient
         */
        var patientBirthday: LocalDate? = null

        /**
         * True if to many search results were found in the clinic backend. No results were returned.
         */
        var isToManyMatchesInClinicDatabase: Boolean = false

        /**
         * List of found patients. ListChooser is used for unique ids for primefaces datatable
         */
        override var patientList: MutableList<ListChooser<Patient>> = mutableListOf()

        /**
         * Selected patient
         */
        override var selectedPatient: ListChooser<Patient>? = null

        /**
         * True if the user can only search the local database
         */
        var isSearchLocalDatabaseOnly: Boolean = true

        /**
         * True if a patient was selected
         */
        val isPatientSelected
            get() = selectedPatient != null

        override fun initTab(): Boolean {
            return initTab("", "", "", null)
        }

        override fun initTab(force: Boolean): Boolean {
            return initTab("", "", "", null)
        }

        open fun initTab(name: String, surname: String, piz: String, date: LocalDate?): Boolean {
            patientBirthday = date
            patientName = name
            patientPiz = piz
            patientSurname = surname

            selectedPatient = null
            isToManyMatchesInClinicDatabase = false
            patientList.clear()

            isSearchLocalDatabaseOnly = !userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC)

            return true
        }

        /**
         * Search for pizes or given namen, firstname and birthday. Prefers pizes if not
         * null. Considers search only in local database if the user has not the
         * matching rights to add new clinic patients to the local database
         */
        open fun searchForClinicPatients() {
            searchForClinicPatients(patientPiz, patientName, patientSurname, patientBirthday)
        }

        /**
         * Search for pizes or given namen, firstname and birthday. Prefers pizes if not
         * null. Considers search only in local database if the user has not the
         * matching rights to add new clinic patients to the local database
         */
        open fun searchForClinicPatients(patientPiz: String, patientName: String, patientSurname: String, patientBirthday: LocalDate?) {
            logger.debug("Searching for patients")
            try {
                patientList.clear()
                isToManyMatchesInClinicDatabase = false
                val resultArr: MutableList<Patient> = ArrayList()
                if (patientPiz.isNotEmpty()) {
                    val piz = patientPiz.trim()
                    if (piz.matches(Regex("^[0-9]{8}$"))) {
                        // if full piz
                        val oPatient = patientService.findPatientByPizInDatabaseAndPDV(piz,
                                isSearchLocalDatabaseOnly)
                        if (oPatient.isPresent) resultArr.add(oPatient.get())
                    } else if (piz.replace("_".toRegex(), "").matches(Regex("^[0-9]{6,8}$"))) {
                        // 6to 7 digits of piz
                        // isSearchLocalDatabaseOnly() can be ignored because this function is only supported by local database
                        resultArr.addAll(patientService
                                .findAllPatientsByPizInDatabaseAndPDV(piz.replace("_".toRegex(), "")))
                    }
                } else if (patientName.isNotEmpty() || patientSurname.isNotEmpty() || patientBirthday != null) {
                    val toManyEntries = AtomicBoolean(false)
                    resultArr.addAll(patientService.findAllPatientsByNameSurnameBirthdayInDatabaseAndPDV(
                            patientName, patientSurname, patientBirthday, isSearchLocalDatabaseOnly,
                            toManyEntries))
                    isToManyMatchesInClinicDatabase = toManyEntries.get()
                }
                patientList.addAll(ListChooser.getListAsIDList(resultArr))
                selectedPatient = null
            } catch (e: JSONException) {
                selectedPatient = null
            } catch (e: ToManyEntriesException) {
                selectedPatient = null
            } catch (e: CustomNullPatientExcepetion) {
                selectedPatient = null
            }
        }

        /**
         * Method is overwritten from IPatientTableDataProvider and calls hideDialogAndSelectPatient
         */
        override fun onDblSelect() {
            hideDialogAndSelectPatient()
        }

        /**
         * Method  is overwritten from IPatientTableDataProvider does nothing
         */
        override fun onSelect() {
        }

        /**
         * Selects adds the current patient to the database and closes the dialog
         */
        open fun hideDialogAndSelectPatient() {
            val patient = selectedPatient?.listItem ?: return hideDialog()

            hideDialog(PatientSelectEvent(
                    if (isPersistPatient) patientService.addPatient(patient, false) else patient))
        }

        open fun onQuickSubmit() {
            logger.debug("Quicksubmit, search for result and adding result to worklist if unique result")
            searchForClinicPatients()
            // only adding if exactly one result was found
            if (patientList.size == 1) {
                logger.debug("One result found, adding to database")
                selectedPatient = patientList.first()
                hideDialogAndSelectPatient()
            } else {
                logger.debug("No result found, or result not unique, not firing quick submit")
            }
        }
    }


    open inner class ExternalPatientTab : ClinicSearchTab(
            "ExternalPatientTab",
            "dialog.searchPatient.add",
            "externalPatient",
            "_externalPatient.xhtml"), IPersonDataChangeListener {

        lateinit var patient: Patient

        /**
         * True if patient is beeing created an the input shoud be blocked
         */
        var disableInput = false

        override fun initTab(force: Boolean): Boolean {
            disabled = !userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN)
            patient = Patient(Person(Contact()))
            patient.person.gender = Person.Gender.UNKNOWN
            disableInput = false
            return super.initTab(force)
        }

        /**
         * Return function, is called after the confirm patient data dialog is closed
         */
        open fun onConfirmExternalPatientDialog(event: SelectEvent) {
            val obj = event.getObject()
            if (obj != null && obj is PatientSelectEvent) {
                println(".....")
                Thread.sleep(1000);
//                val patient = obj.obj ?: return hideDialog()
//                if (isPersistPatient) {
//                    // creates patient in pdv
//                    logger.debug("Persist Patient in database")
//
//                    this.disableInput = true
//                    this.disabled = true
//                    clinicSearchTab.disabled = true
//
//                    val piz = SpringContextBridge.services().httpRestRepository.createPatientInPDV(patient)
//
//                    if (piz != null) {
//                        patient.piz = piz
//                        patient.externalPatient = false
//                        MessageHandler.sendGrowlMessagesAsResource("growl.patient.createdPDV.headline", "growl.patient.createdPDV.text", piz)
//                        return hideDialog(PatientSelectEvent(patientService.addPatient(patient, false)))
//                    } else {
//                        MessageHandler.sendGrowlMessagesAsResource("growl.patient.errorPDV.headline", "growl.patient.errorPDV.text")
//                        return hideDialog(ReloadEvent())
//                    }
//                } else {
//                    MessageHandler.sendGrowlMessagesAsResource("growl.patient.createNotPersist.headline", "growl.patient.createNotPersist.text")
//                    return hideDialog(PatientSelectEvent(patient))
//                }
            } else if (event.`object` is ReloadEvent) {
                return hideDialog(ReloadEvent())
            } else {
                MessageHandler.sendGrowlMessagesAsResource("growl.patient.createAbort.headline", "growl.patient.createAbort.text")
            }
        }

        /**
         * Overwrites IPersonDataChangeListener, is used if person data are changed to search for existing patients
         */
        override fun onPersonDataChange() {
            selectedPatient = null
            searchForClinicPatients("", patient.person.lastName, patient.person.firstName, patient.person.birthday)
        }
    }
}