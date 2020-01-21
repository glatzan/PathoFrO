package com.patho.main.repository.jpa.custom.impl;

import com.patho.main.common.ContactRole;
import com.patho.main.common.Eye;
import com.patho.main.model.Physician;
import com.patho.main.model.Signature;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.favourites.FavouriteList_;
import com.patho.main.model.patient.*;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.person.Person;
import com.patho.main.model.util.audit.Audit_;
import com.patho.main.repository.jpa.custom.PatientRepositoryCustom;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.search.settings.SimpleListSearchCriterion;
import com.patho.main.util.worklist.search.WorklistSearchExtended;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityGraph;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientRepositoryImpl extends AbstractRepositoryCustom implements PatientRepositoryCustom {

    @Override
    public Optional<Patient> findOptionalByPiz(String piz) {
        return findOptionalByPiz(piz, false, false, true);
    }

    @Override
    public Optional<Patient> findOptionalByPiz(String piz, boolean loadTasks, boolean loadFiles,
                                               boolean irgnoreArchived) {
        List<Patient> patients = findByPiz(piz, loadTasks, loadFiles, irgnoreArchived);
        return Optional.ofNullable(!patients.isEmpty() ? patients.get(0) : null);
    }

    @Override
    public Optional<Patient> findOptionalById(Long id) {
        return findOptionalById(id, false, false);
    }

    @Override
    public Optional<Patient> findOptionalById(Long id, boolean initializeAll) {
        return findOptionalById(id, initializeAll, initializeAll);
    }

    @Override
    public List<Patient> findByPiz(String piz) {
        return findByPiz(piz, false, false, true);
    }

    @Override
    public List<Patient> findByPiz(String piz, boolean loadTasks, boolean loadFiles, boolean irgnoreArchived) {
        CriteriaQuery<Patient> criteria = getCriteriaBuilder().createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(getCriteriaBuilder().like(root.get(Patient_.piz), piz + "%"));

        return find(criteria, root, predicates, loadTasks, loadFiles, irgnoreArchived);
    }

    @Override
    public Optional<Patient> findOptionalById(Long id, boolean initializeTasks, boolean initializeFiles) {
        CriteriaQuery<Patient> criteria = getCriteriaBuilder().createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(getCriteriaBuilder().equal(root.get(Patient_.id), id));

        List<Patient> patients = find(criteria, root, predicates, initializeTasks, initializeFiles, false);

        return Optional.ofNullable(!patients.isEmpty() ? patients.get(0) : null);
    }

    @Override
    public List<Patient> findAllByIds(List<Long> ids) {
        return findAllByIds(ids, false, false, false);
    }

    @Override
    public List<Patient> findAllByIds(List<Long> ids, boolean initializeTasks, boolean initializeFiles,
                                      boolean irgnoreArchived) {
        CriteriaQuery<Patient> criteria = getCriteriaBuilder().createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(root.get(Patient_.id).in(ids));

        return find(criteria, root, predicates, initializeTasks, initializeFiles, irgnoreArchived);
    }

    @Override
    public List<Patient> findByDateAndCriterion(SimpleListSearchCriterion simpleListSearchCriterion, Instant startDate, Instant endDate) {
        return findByDateAndCriterion(simpleListSearchCriterion, startDate, endDate, false, false, true);
    }

    @Override
    public List<Patient> findByDateAndCriterion(SimpleListSearchCriterion simpleListSearchCriterion, Instant startDate, Instant endDate,
                                                boolean initializeTasks, boolean initializeFiles, boolean irgnoreArchived) {
        CriteriaBuilder builder = getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<Predicate>();

        CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);
        Join<Patient, Task> taskQuery = root.join(Patient_.tasks, JoinType.LEFT);

        switch (simpleListSearchCriterion) {
            case NoTasks:
                predicates.add(builder.greaterThanOrEqualTo(root.get(Patient_.audit).get(Audit_.createdOn), TimeUtil.toUnixTimeMillis(startDate)));
                predicates.add(builder.lessThanOrEqualTo(root.get(Patient_.audit).get(Audit_.createdOn), TimeUtil.toUnixTimeMillis(endDate)));
                predicates.add(builder.isEmpty(root.get(Patient_.tasks)));
                break;
            case StainingCompleted:
                predicates.add(builder.greaterThanOrEqualTo(taskQuery.get(Task_.stainingCompletionDate), startDate));
                predicates.add(builder.lessThanOrEqualTo(taskQuery.get(Task_.stainingCompletionDate), endDate));
                break;
            case DiagnosisCompleted:
                predicates.add(builder.greaterThanOrEqualTo(taskQuery.get(Task_.diagnosisCompletionDate), startDate));
                predicates.add(builder.lessThanOrEqualTo(taskQuery.get(Task_.diagnosisCompletionDate), endDate));
                break;
            case NotificationComplteted:
                predicates.add(builder.greaterThanOrEqualTo(taskQuery.get(Task_.notificationCompletionDate), startDate));
                predicates.add(builder.lessThanOrEqualTo(taskQuery.get(Task_.notificationCompletionDate), endDate));
                break;
            case TaskCompleted:
                predicates.add(builder.greaterThanOrEqualTo(taskQuery.get(Task_.finalizationDate), startDate));
                predicates.add(builder.lessThanOrEqualTo(taskQuery.get(Task_.finalizationDate), endDate));
                break;
            case TaskCreated:
                predicates.add(builder.greaterThanOrEqualTo(taskQuery.get(Task_.audit).get(Audit_.createdOn), TimeUtil.toUnixTimeMillis(startDate)));
                predicates.add(builder.lessThanOrEqualTo(taskQuery.get(Task_.audit).get(Audit_.createdOn), TimeUtil.toUnixTimeMillis(endDate)));
                break;
            default:
                break;
        }

        return find(criteria, root, predicates, initializeTasks, initializeFiles, irgnoreArchived);

    }

    @Override
    public List<Patient> findByNameAndFirstnameAndBirthday(String name, String firstname, Date birthday) {
        return findByNameAndFirstnameAndBirthday(name, firstname, birthday, false, false, true);
    }

    @Override
    public List<Patient> findByNameAndFirstnameAndBirthday(String name, String firstname, Date birthday,
                                                           boolean initializeTasks, boolean initializeFiles, boolean irgnoreArchived) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);

        Join<Patient, Person> personQuery = root.join("person", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (HistoUtil.isNotNullOrEmpty(name))
            predicates.add(builder.like(builder.lower(personQuery.get("lastName")), "%" + name.toLowerCase() + "%"));
        if (HistoUtil.isNotNullOrEmpty(firstname))
            predicates.add(
                    builder.like(builder.lower(personQuery.get("firstName")), "%" + firstname.toLowerCase() + "%"));
        if (birthday != null)
            predicates.add(builder.equal(personQuery.get("birthday"), birthday));

        return find(criteria, root, predicates, initializeTasks, initializeFiles, irgnoreArchived);
    }

    @Override
    public List<Patient> findComplex(WorklistSearchExtended worklistSearchExtended) {
        return findComplex(worklistSearchExtended, false, false, true);
    }

    @Override
    public List<Patient> findComplex(WorklistSearchExtended worklistSearchExtended, boolean loadTasks,
                                     boolean loadFiles, boolean irgnoreArchived) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);

        Join<Patient, Task> taskQuery = root.join("tasks", JoinType.LEFT);

        Join<Task, Sample> sampleQuery = taskQuery.join("samples", JoinType.LEFT);
        Join<Sample, Block> blockQuery = sampleQuery.join("blocks", JoinType.LEFT);
        Join<Block, Slide> slideQuery = blockQuery.join("slides", JoinType.LEFT);
        Join<Slide, StainingPrototype> prototypeQuery = slideQuery.join("slidePrototype", JoinType.LEFT);

        Join<Task, DiagnosisRevision> diagnosisRevisionQuery = taskQuery.join("diagnosisRevisions", JoinType.LEFT);
        Join<DiagnosisRevision, Diagnosis> diagnosesQuery = diagnosisRevisionQuery.join("diagnoses", JoinType.LEFT);
        Join<DiagnosisRevision, Signature> signatureOneQuery = diagnosisRevisionQuery.join("signatureOne",
                JoinType.LEFT);
        Join<Signature, Physician> signatureOnePhysicianQuery = signatureOneQuery.join("physician", JoinType.LEFT);
        Join<DiagnosisRevision, Signature> signatureTwoQuery = diagnosisRevisionQuery.join("signatureTwo",
                JoinType.LEFT);
        Join<Signature, Physician> signatureTwoPhysicianQuery = signatureTwoQuery.join("physician", JoinType.LEFT);

        Join<ReportIntent, Task> contactQuery = taskQuery.join("contacts", JoinType.LEFT);
        Join<Person, ReportIntent> personContactQuery = contactQuery.join("person", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // searching for material
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getMaterial())) {
            predicates.add(builder.like(builder.lower(sampleQuery.get("material")),
                    "%" + worklistSearchExtended.getMaterial().toLowerCase() + "%"));

            logger.debug("Selecting material " + worklistSearchExtended.getMaterial());
        }

        // getting surgeon
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getSurgeons())) {
            Expression<Long> exp = personContactQuery.get("id");
            predicates.add(builder.and(
                    exp.in(Arrays.asList(worklistSearchExtended.getSurgeons()).stream().map(p -> p.getPerson().getId())
                            .collect(Collectors.toList())),
                    builder.equal(contactQuery.get("role"), ContactRole.SURGEON)));
            logger.debug("Selecting surgeon");
        }

        // getting signature
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getSignature())) {
            Expression<Long> expphysicianOne = signatureOnePhysicianQuery.get("id");
            Predicate physicianOne = expphysicianOne.in(Arrays.asList(worklistSearchExtended.getSignature()).stream()
                    .map(p -> p.getId()).collect(Collectors.toList()));

            Expression<Long> expphysicianTwo = signatureTwoPhysicianQuery.get("id");
            Predicate physicianTwo = expphysicianTwo.in(Arrays.asList(worklistSearchExtended.getSignature()).stream()
                    .map(p -> p.getId()).collect(Collectors.toList()));

            predicates.add(builder.or(physicianOne, physicianTwo));

            logger.debug("Selecting signature");
        }

        // getting history
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getCaseHistory())) {
            predicates.add(builder.like(builder.lower(taskQuery.get("caseHistory")),
                    "%" + worklistSearchExtended.getCaseHistory().toLowerCase() + "%"));

            logger.debug("Selecting history");
        }

        // getting reportIntent text
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getDiagnosisText())) {
            predicates.add(builder.like(builder.lower(diagnosisRevisionQuery.get("text")),
                    "%" + worklistSearchExtended.getDiagnosisText().toLowerCase() + "%"));

            logger.debug("Selecting reportIntent text");
        }

        // getting reportIntent
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getDiagnosis())) {
            predicates.add(builder.like(builder.lower(diagnosesQuery.get("reportIntent")),
                    "%" + worklistSearchExtended.getDiagnosis().toLowerCase() + "%"));

            logger.debug("Selecting reportIntent");
        }

        // checking malign, 0 = not selected, 1 = true, 2 = false
        if (!worklistSearchExtended.getMalign().equals("0")) {
            predicates.add(builder.equal(diagnosesQuery.get("malign"),
                    worklistSearchExtended.getMalign().equals("1") ? true : false));

            logger.debug("Selecting malign");
        }

        // getting eye
        if (worklistSearchExtended.getEye() != Eye.UNKNOWN) {
            predicates.add(builder.equal(taskQuery.get("eye"), worklistSearchExtended.getEye()));

            logger.debug("Selecting eye");
        }

        // getting ward
        if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getWard())) {
            predicates.add(builder.like(builder.lower(diagnosesQuery.get("ward")),
                    "%" + worklistSearchExtended.getWard().toLowerCase() + "%"));

            logger.debug("Selecting ward");
        }

        if (worklistSearchExtended.getStainings() != null && !worklistSearchExtended.getStainings().isEmpty()) {
            Expression<Long> prototypeID = prototypeQuery.get("id");
            Predicate stainings = prototypeID.in(
                    worklistSearchExtended.getStainings().stream().map(p -> p.getId()).collect(Collectors.toList()));

            predicates.add(stainings);
        }

        return find(criteria, root, predicates, loadTasks, loadFiles, irgnoreArchived);

    }

    @Override
    public List<Patient> find(CriteriaQuery<Patient> criteria, Root<Patient> root, List<Predicate> predicates,
                              boolean loadTasks, boolean loadFiles, boolean irgnoreArchived) {
        criteria.select(root);

        EntityGraph<Patient> graph = getSession().createEntityGraph(Patient.class);

        if (loadTasks)
            graph.addAttributeNodes("tasks");

        if (loadFiles)
            graph.addAttributeNodes("attachedPdfs");

        if (irgnoreArchived)
            predicates.add(getCriteriaBuilder().equal(root.get("archived"), false));

        criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);

        return getSession().createQuery(criteria).setHint("javax.persistence.loadgraph", graph).getResultList();
    }

    public List<Patient> findAllByFavouriteList(long id, boolean loadTasks) {
        return findAllByFavouriteLists(Arrays.asList(id), loadTasks);
    }

    public List<Patient> findAllByFavouriteLists(List<Long> ids, boolean loadTasks) {
        CriteriaBuilder qb = getSession().getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<Patient> criteria = qb.createQuery(Patient.class);
        Root<Patient> root = criteria.from(Patient.class);
        criteria.select(root);

        if (loadTasks)
            root.fetch(Patient_.tasks, JoinType.LEFT);

        Join<Patient, Task> patientTaskQuery = root.join(Patient_.tasks, JoinType.LEFT);
        Join<Task, FavouriteList> taskFavouriteQuery = patientTaskQuery.join(Task_.favouriteLists, JoinType.LEFT);

        criteria.where(taskFavouriteQuery.get(FavouriteList_.id).in(ids));

        criteria.distinct(true);

        List<Patient> patients = getSession().createQuery(criteria).getResultList();
        return patients;
    }
}