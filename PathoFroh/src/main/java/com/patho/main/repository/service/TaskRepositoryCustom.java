package com.patho.main.repository.service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.patho.main.model.patient.Task;
import com.patho.main.util.worklist.search.WorklistSearchExtended;

public interface TaskRepositoryCustom {

    public Optional<Task> findOptionalByIdAndInitialize(Task task, boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs,
                                                        boolean loadContacts, boolean loadParent);

    public Optional<Task> findOptionalByIdAndInitialize(Long id, boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs,
                                                        boolean loadContacts, boolean loadParent);

    public Optional<Task> findOptionalByTaskId(String taskID);

    public Optional<Task> findOptionalByTaskId(String taskID, boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs,
                                               boolean loadContacts, boolean loadParent);

    public Optional<Task> findOptionalBySlideID(String taskID, int uniqueSlideIDInBlock, boolean loadCouncils, boolean loadDiangoses,
                                                boolean loadPDFs, boolean loadContacts, boolean loadParent);

    public Optional<Task> findOptinalByLastID(Calendar ofYear, boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs,
                                              boolean loadContacts, boolean loadParent);

    public List<Task> findByCriteria(WorklistSearchExtended worklistSearchExtended, boolean initParent);

    public Optional<Task> find(CriteriaQuery<Task> criteria, Root<Task> root, List<Predicate> predicates, boolean loadCouncils,
                               boolean loadDiangoses, boolean loadPDFs, boolean loadContacts, boolean loadParent);

}