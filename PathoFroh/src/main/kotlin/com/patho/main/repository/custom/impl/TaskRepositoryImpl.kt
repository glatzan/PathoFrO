package com.patho.main.repository.custom.impl

import com.patho.main.model.Physician
import com.patho.main.model.Signature
import com.patho.main.model.Signature_
import com.patho.main.model.StainingPrototype
import com.patho.main.model.patient.*
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntent_
import com.patho.main.model.person.Person
import com.patho.main.model.person.Person_
import com.patho.main.repository.custom.TaskRepositoryCustom
import com.patho.main.repository.service.impl.AbstractRepositoryCustom
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.search.settings.ExtendedSearch
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import javax.persistence.criteria.*


@Service
open class TaskRepositoryImpl : AbstractRepositoryCustom(), TaskRepositoryCustom {

    @Transactional
    override fun findByID(task: Task, loadCouncils: Boolean, loadDiagnoses: Boolean,
                          loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task {
        return findByID(task.id, loadCouncils, loadDiagnoses, loadPDFs, loadContacts, loadParent)
    }

    @Transactional
    override fun findByID(id: Long, loadCouncils: Boolean, loadDiagnoses: Boolean,
                          loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task {
        val criteria = criteriaBuilder.createQuery(Task::class.java)
        val root = criteria.from(Task::class.java)

        val tasks = findAll(criteria, root, mutableListOf(criteriaBuilder.equal(root.get(Task_.id), id)), loadCouncils, loadDiagnoses,
                loadPDFs, loadContacts, loadParent)

        return tasks.firstOrNull() ?: throw TaskNotFoundException()
    }

    @Transactional
    override fun findByTaskID(taskID: String, loadCouncils: Boolean, loadDiagnoses: Boolean,
                              loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task {
        val criteria = criteriaBuilder.createQuery(Task::class.java)
        val root = criteria.from(Task::class.java)
        criteria.select(root)

        val tasks = findAll(criteria, root, mutableListOf(criteriaBuilder.like(root.get(Task_.taskID), taskID)), loadCouncils, loadDiagnoses,
                loadPDFs, loadContacts, loadParent)

        return tasks.firstOrNull() ?: throw TaskNotFoundException()
    }

    @Transactional
    override fun findBySlideID(taskID: String, uniqueSlideIDInBlock: Int, loadCouncils: Boolean,
                               loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): Task {
        val criteria = criteriaBuilder.createQuery(Task::class.java)
        val root = criteria.from(Task::class.java)
        criteria.select(root)

        val sampleQuery = root.join<Task, Sample>(Task_.samples.name, JoinType.LEFT)
        val blockQuery = sampleQuery.join<Sample, Block>(Sample_.blocks.name, JoinType.LEFT)
        val slideQuery = blockQuery.join<Block, Slide>(Block_.slides.name, JoinType.LEFT)

        val tasks = findAll(criteria, root,
                mutableListOf(criteriaBuilder.and(criteriaBuilder.like(root.get(Task_.taskID), taskID),
                        criteriaBuilder.equal(slideQuery.get(Slide_.uniqueIDinTask), uniqueSlideIDInBlock))),
                loadCouncils, loadDiagnoses, loadPDFs, loadContacts, loadParent)

        return tasks.firstOrNull() ?: throw TaskNotFoundException()
    }

    @Transactional
    override fun findByLastID(ofYear: LocalDate, loadCouncils: Boolean, loadDiagnoses: Boolean, loadPDFs: Boolean,
                              loadContacts: Boolean, loadParent: Boolean): Task {

        val currentYear = (ofYear.year - 2000).toString() + "%"

        // Create CriteriaQuery
        val criteria = criteriaBuilder.createQuery(Task::class.java)
        val root = criteria.from(Task::class.java)

        val subquery = criteria.subquery(Number::class.java)
        val subTaskRoot = subquery.from(Task::class.java)
        subquery.where(criteriaBuilder.like(subTaskRoot.get(Task_.taskID), currentYear))
        subquery.select(criteriaBuilder.max(subTaskRoot.get(Task_.taskID) as Expression<Number>))

        val tasks = findAll(criteria, root, mutableListOf(criteriaBuilder.equal(root.get(Task_.taskID), subquery)), loadCouncils, loadDiagnoses,
                loadPDFs, loadContacts, loadParent)

        return tasks.firstOrNull() ?: throw TaskNotFoundException()
    }

    @Transactional
    override fun findByExtendedSearchSettings(extendedSearch: ExtendedSearch, loadCouncils: Boolean,
                                              loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): List<Task> {
        val criteria = criteriaBuilder.createQuery(Task::class.java)
        val root = criteria.from(Task::class.java)
        criteria.select(root)

        val patientQuery: Join<Task, Patient> = root.join(Task_.parent.name, JoinType.LEFT)
        val personPatientQuery: Join<Patient, Person> = patientQuery.join(Patient_.person.name, JoinType.LEFT)

        val sampleQuery: Join<Task, Sample> = root.join(Task_.samples.name, JoinType.LEFT)
        val blockQuery: Join<Sample, Block> = sampleQuery.join(Sample_.blocks.name, JoinType.LEFT)
        val slideQuery: Join<Block, Slide> = blockQuery.join(Block_.slides.name, JoinType.LEFT)
        val prototypeQuery: Join<Slide, StainingPrototype> = slideQuery.join(Slide_.slidePrototype.name, JoinType.LEFT)

        val diagnosisRevisionQuery: Join<Task, DiagnosisRevision> = root.join(Task_.diagnosisRevisions.name, JoinType.LEFT)
        val diagnosesQuery: Join<DiagnosisRevision, Diagnosis> = diagnosisRevisionQuery.join(DiagnosisRevision_.diagnoses.name, JoinType.LEFT)
        val signatureOneQuery: Join<DiagnosisRevision, Signature> = diagnosisRevisionQuery.join(DiagnosisRevision_.signatureOne.name, JoinType.LEFT)
        val signatureOnePhysicianQuery: Join<Signature, Physician> = signatureOneQuery.join(Signature_.physician.name, JoinType.LEFT)
        val signatureTwoQuery: Join<DiagnosisRevision, Signature> = diagnosisRevisionQuery.join(DiagnosisRevision_.signatureTwo.name,
                JoinType.LEFT)
        val signatureTwoPhysicianQuery: Join<Signature, Physician> = signatureTwoQuery.join(Signature_.physician.name, JoinType.LEFT)

        val contactQuery: Join<ReportIntent, Task> = root.join(Task_.contacts.name, JoinType.LEFT)
        val personContactQuery: Join<Person, ReportIntent> = contactQuery.join(ReportIntent_.person.name, JoinType.LEFT)

        val predicates = mutableListOf<Predicate>()

        // patient name
        if (extendedSearch.isUsePatientName) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(personPatientQuery.get(Person_.lastName)),
                    "%${extendedSearch.patientName}%"))
            logger.debug("Search for task -> patient -> person.name ${extendedSearch.patientName}")
        }

        // surname
        if (extendedSearch.isUsePatientSurname) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(personPatientQuery.get(Person_.firstName)),
                    "%${extendedSearch.patientSurname}%"))
            logger.debug("Search for task -> patient -> person.firstname ${extendedSearch.patientSurname}")
        }

        // birthday from
        if (extendedSearch.isUsePatientBirthdayFrom && !extendedSearch.isUsePatientBirthdayTo) {
            predicates.add(criteriaBuilder.equal(personPatientQuery.get(Person_.birthday),
                    extendedSearch.patientBirthdayFrom))
            logger.debug("Search for task -> patient -> person.birthday ${extendedSearch.patientBirthdayFrom}")
        }

        // birthday from to
        if (extendedSearch.isUsePatientBirthdayFrom && extendedSearch.isUsePatientBirthdayTo) {
            predicates.add(criteriaBuilder.between(personPatientQuery.get(Person_.birthday),
                    extendedSearch.patientBirthdayFrom, extendedSearch.patientBirthdayTo))
            logger.debug("Search for task -> patient -> person.birthday ${extendedSearch.patientBirthdayFrom} to${extendedSearch.patientBirthdayTo} ")
        }

        // material
        if (extendedSearch.isUseMaterial) {

            val materials = mutableListOf<Predicate>()

            extendedSearch.material?.forEach {
                materials.add(criteriaBuilder.like(criteriaBuilder.lower(sampleQuery.get(Sample_.material)),
                        "%$it%"))
                logger.debug("Search for task -> sample -> material $it")
            }

            if (materials.size == 1)
                predicates.addAll(materials)
            else if (materials.size > 1) {
                val or = criteriaBuilder.or(*materials.toTypedArray())
                predicates.add(or)
            }
        }

        // case history
        if (extendedSearch.isUseCaseHistory) {

            val history = mutableListOf<Predicate>()

            extendedSearch.caseHistory?.forEach {
                history.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(Task_.caseHistory)),
                        "%$it%"))
                logger.debug("Search for task -> caseHistory $it")
            }

            if (history.size == 1)
                predicates.addAll(history)
            else if (history.size > 1) {
                val or = criteriaBuilder.or(*history.toTypedArray())
                predicates.add(or)
            }
        }

        return findAll(criteria, root, predicates, loadCouncils, loadDiagnoses, loadPDFs, loadContacts, loadParent)
    }

    @Transactional
    override fun findAll(criteria: CriteriaQuery<Task>, root: Root<Task>, predicates: MutableList<Predicate>,
                         loadCouncils: Boolean, loadDiagnoses: Boolean, loadPDFs: Boolean, loadContacts: Boolean, loadParent: Boolean): List<Task> {
        criteria.select(root)

        val graph = session.createEntityGraph(Task::class.java)

        if (loadCouncils)
            graph.addAttributeNodes(Task_.councils.name)

        if (loadDiagnoses)
            graph.addAttributeNodes(Task_.diagnosisRevisions.name)

        if (loadContacts)
            graph.addAttributeNodes(Task_.contacts.name)

        if (loadPDFs)
            graph.addAttributeNodes(Task_.attachedPdfs.name)

        if (loadParent) {
            val subgraph = graph.addSubGraph(Task_.parent.name, Patient::class.java)
            subgraph.addAttributeNodes(Patient_.tasks.name)
            subgraph.addAttributeNodes(Patient_.attachedPdfs.name)
        }

        criteria.where(criteriaBuilder.and(*predicates.toTypedArray()))
        criteria.distinct(true)

        val tasks = session.createQuery<Task>(criteria).setHint("javax.persistence.loadgraph", graph).resultList;

//
//        tasks.forEach {
//            Hibernate.initialize(it.parent!!.tasks)
//            Hibernate.initialize(it.parent!!.attachedPdfs)
//        }

        return tasks
    }

}