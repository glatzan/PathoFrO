package com.patho.main.repository.jpa.custom

import com.patho.main.model.patient.Task
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.search.settings.ExtendedSearch
import java.time.LocalDate
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface TaskRepositoryCustom {

    fun findByID(task: Task, loadCouncils: Boolean, loadDiagnoses: Boolean,
                 loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task

    fun findByID(id: Long, loadCouncils: Boolean, loadDiagnoses: Boolean,
                 loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task

    @Throws(TaskNotFoundException::class)
    fun findByTaskID(taskID: String, loadCouncils: Boolean = false, loadDiagnoses: Boolean = false,
                     loadPDFs: Boolean = false, loadContacts: Boolean = false, loadParent: Boolean = false): Task

    @Throws(TaskNotFoundException::class)
    fun findByLastID(ofYear: LocalDate, loadCouncils: Boolean, loadDiagnoses: Boolean, loadPDFs: Boolean,
                     loadContacts: Boolean, loadParent: Boolean): Task

    @Throws(TaskNotFoundException::class)
    fun findBySlideID(taskID: String, uniqueSlideIDInBlock: Int, loadCouncils: Boolean,
                      loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task

    fun findByExtendedSearchSettings(extendedSearch: ExtendedSearch, loadCouncils: Boolean,
                                     loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): List<Task>

    fun findAllByID(ids: List<Long>, loadCouncils: Boolean,
                    loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): List<Task>

    fun findAll(criteria: CriteriaQuery<Task>, root: Root<Task>, predicates: MutableList<Predicate>,
                loadCouncils: Boolean, loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): List<Task>
}