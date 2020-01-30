package com.patho.main.dialog.patient

import com.patho.main.common.Dialog
import com.patho.main.config.excepion.ToManyEntriesException
import com.patho.main.dialog.AbstractTabDialog_
import com.patho.main.model.patient.Patient
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.model.user.HistoPermissions
import com.patho.main.service.PatientService
import com.patho.main.service.UserService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.ui.ListChooser
import com.patho.main.util.dialog.event.PatientSelectEvent
import com.patho.main.util.exception.CustomNullPatientExcepetion
import org.primefaces.event.SelectEvent
import org.primefaces.json.JSONException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
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
        return super.initBean(true, clinicSearchTab)
    }

    fun externalMode(): SearchAndAddPatientDialog {
        return externalMode(false)
    }

    fun externalMode(forceExternalMode: Boolean = false): SearchAndAddPatientDialog {
        // only enable if forced or user has the permission to add external patients
        if (forceExternalMode || userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN)) {
            tabs = arrayOf(clinicSearchTab, externalPatientTab)
            externalPatientTab.initTab()
        }
        return this
    }

    fun persistPatient(): SearchAndAddPatientDialog {
        isPersistPatient = true
        return this
    }

    fun initializeValues(name: String, surname: String, piz: String, date: Date?): SearchAndAddPatientDialog {
        clinicSearchTab.patientName = name
        clinicSearchTab.patientBirthday = date
        clinicSearchTab.patientSurname = surname
        clinicSearchTab.patientPiz = piz
        clinicSearchTab.searchForClinicPatients()
        return this
    }

    open inner class ClinicSearchTab : AbstractTabDialog_.AbstractTab(
            "ClinicSearchTab",
            "dialog.searchPatient.search",
            "clinicSearch",
            "_clinicSearch.xhtml") {

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
        var patientBirthday: Date? = null

        /**
         * True if to many search results were found in the clinic backend. No results were returned.
         */
        var isToManyMatchesInClinicDatabase: Boolean = false

        /**
         * List of found patients. ListChooser is used for unique ids for primefaces datatable
         */
        val patientList: MutableList<ListChooser<Patient>> = mutableListOf()

        /**
         * Selected patient
         */
        var selectedPatientListItem: ListChooser<Patient>? = null

        /**
         * True if the user can only search the local database
         */
        var isSearchLocalDatabaseOnly: Boolean = true

        override fun initTab(): Boolean {
            return initTab("", "", "", null)
        }

        override fun initTab(force: Boolean): Boolean {
            return initTab("", "", "", null)
        }

        open fun initTab(name: String, surname: String, piz: String, date: Date?): Boolean {
            patientBirthday = date
            patientName = name
            patientPiz = piz
            patientSurname = surname

            selectedPatientListItem = null
            isToManyMatchesInClinicDatabase = false
            patientList.clear()

            isSearchLocalDatabaseOnly = !userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC)
            logger.debug("----------" + userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_CLINIC))

            return true
        }

        /**
         * Search for pizes or given namen, firstname and birthday. Prefers pizes if not
         * null. Considers search only in local database if the user has not the
         * matching rights to add new clinic patients to the local database
         */
        open fun searchForClinicPatients() {
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
                selectedPatientListItem = null
            } catch (e: JSONException) {
                selectedPatientListItem = null
            } catch (e: ToManyEntriesException) {
                selectedPatientListItem = null
            } catch (e: CustomNullPatientExcepetion) {
                selectedPatientListItem = null
            }
        }

        open fun hideDialogAndSelectPatient() {
            val patient = selectedPatientListItem?.listItem ?: return hideDialog()

            hideDialog(PatientSelectEvent(
                    if (isPersistPatient) patientService.addPatient(patient, false) else patient))
        }

        open fun onQuickSubmit() {
            logger.debug("Quicksubmit, search for result and adding result to worklist if unique result")
            searchForClinicPatients()
            // only adding if exactly one result was found
            if (patientList.size == 1) {
                logger.debug("One result found, adding to database")
                selectedPatientListItem = patientList.first()
                hideDialogAndSelectPatient()
            } else {
                logger.debug("No result found, or result not unique, not firing quick submit")
            }
        }
    }


    open inner class ExternalPatientTab : AbstractTabDialog_.AbstractTab(
            "ExternalPatientTab",
            "dialog.searchPatient.add",
            "externalPatient",
            "_externalPatient.xhtml") {

        lateinit var patient: Patient

        override fun initTab(force: Boolean): Boolean {
            disabled = !userService.userHasPermission(HistoPermissions.PATIENT_EDIT_ADD_EXTERN)
            patient = Patient(Person(Contact()))
            patient.person.gender = Person.Gender.UNKNOWN
            return super.initTab(force)
        }

        open fun onConfirmExternalPatientDialog(event: SelectEvent) {
            val obj = event.getObject()
            if (obj != null && obj is PatientSelectEvent) {
                val patient = obj.obj ?: return hideDialog()

                hideDialog(PatientSelectEvent(
                        if (isPersistPatient) patientService.addPatient(patient, false) else patient))
            }
        }
    }
}