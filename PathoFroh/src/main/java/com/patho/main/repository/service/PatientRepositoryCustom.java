package com.patho.main.repository.service;

import com.patho.main.model.patient.Patient;
import com.patho.main.util.search.settings.SimpleListSearchCriterion;
import com.patho.main.util.worklist.search.WorklistSearchExtended;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PatientRepositoryCustom {

    /**
     * Returns a patient with the given id
     *
     * @param id
     * @return
     */
    public Optional<Patient> findOptionalById(Long id);

    /**
     * Returns a patient with the given id
     *
     * @param id
     * @return
     */
    public Optional<Patient> findOptionalById(Long id, boolean initializeAll);

    /**
     * Returns a patient with the given id
     *
     * @param id
     * @return
     */
    public Optional<Patient> findOptionalById(Long id, boolean initializeTasks, boolean initializeFiles);

    /**
     * Searches for a patient via given piz. Piz does not need 8 digest. The missing
     * ones will be match by %.
     *
     * @param piz
     * @return
     */
    public Optional<Patient> findOptionalByPiz(String piz);

    /**
     * Searches for a patient via given piz.Piz does not need 8 digest. The missing
     * ones will be match by %.
     *
     * @param piz
     * @param loadTasks
     * @param loadFiles
     * @param irgnoreArchived
     * @return
     */
    public Optional<Patient> findOptionalByPiz(String piz, boolean loadTasks, boolean loadFiles,
                                               boolean irgnoreArchived);

    /**
     * Returns a list of patients with the given ids
     *
     * @param ids
     * @return
     */
    public List<Patient> findAllByIds(List<Long> ids);

    /**
     * Returns a list of patients with the given ids
     *
     * @param ids
     * @param initializeTasks
     * @param initializeFiles
     * @param irgnoreArchived
     * @return
     */
    public List<Patient> findAllByIds(List<Long> ids, boolean initializeTasks, boolean initializeFiles,
                                      boolean irgnoreArchived);

    /**
     * List of patients with the given PIZ. Piz does not need 8 digest. The missing
     * ones will be match by %.
     *
     * @param piz
     * @return
     */
    public List<Patient> findByPiz(String piz);

    /**
     * List of patients with the given PIZ. Piz does not need 8 digest. The missing
     * ones will be match by %.
     *
     * @param piz
     * @param initializeTasks
     * @param initializeFiles
     * @param irgnoreArchived
     * @return
     */
    public List<Patient> findByPiz(String piz, boolean initializeTasks, boolean initializeFiles,
                                   boolean irgnoreArchived);

    /**
     * Retuns a list of patients with complex search criteria
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Patient> findByDateAndCriterion(SimpleListSearchCriterion simpleListSearchCriterion, Instant startDate, Instant endDate);

    /**
     * Returns a list of patients with complex search criteria
     *
     * @param startDate
     * @param endDate
     * @param initializeTasks
     * @param initializeFiles
     * @param irgnoreArchived
     * @return
     */
    public List<Patient> findByDateAndCriterion(SimpleListSearchCriterion simpleListSearchCriterion, Instant startDate, Instant endDate,
                                                boolean initializeTasks, boolean initializeFiles, boolean irgnoreArchived);

    /**
     * Returns a list of patients with the given name, firstname and birthday
     *
     * @param name
     * @param firstname
     * @param birthday
     * @return
     */
    public List<Patient> findByNameAndFirstnameAndBirthday(String name, String firstname, Date birthday);

    /**
     * Returns a list of patients with the given name, firstname and birthday
     *
     * @param name
     * @param firstname
     * @param birthday
     * @return
     */
    public List<Patient> findByNameAndFirstnameAndBirthday(String name, String firstname, Date birthday,
                                                           boolean initializeTasks, boolean initializeFiles, boolean irgnoreArchived);

    /**
     * Returns a list of Patients with complex search criteria
     *
     * @param worklistSearchExtended
     * @return
     */
    public List<Patient> findComplex(WorklistSearchExtended worklistSearchExtended);

    /**
     * Returns a list of Patients with complex search criteria
     *
     * @param worklistSearchExtended
     * @return
     */
    public List<Patient> findComplex(WorklistSearchExtended worklistSearchExtended, boolean loadTasks,
                                     boolean loadFiles, boolean irgnoreArchived);

    /**
     * Generic search method
     *
     * @param criteria
     * @param root
     * @param predicates
     * @param loadTasks
     * @param loadFiles
     * @param irgnoreArchived
     * @return
     */
    public List<Patient> find(CriteriaQuery<Patient> criteria, Root<Patient> root, List<Predicate> predicates,
                              boolean loadTasks, boolean loadFiles, boolean irgnoreArchived);

    /**
     * Returns a list of patients which task are listed in the favorite list.
     *
     * @param id
     * @param loadTasks
     * @return
     */
    public List<Patient> findAllByFavouriteList(long id, boolean loadTasks);

    /**
     * Returns a list of patients which task are listed in the favorite list.
     *
     * @param ids
     * @param loadTasks
     * @return
     */
    public List<Patient> findAllByFavouriteLists(List<Long> ids, boolean loadTasks);
}
