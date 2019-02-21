package com.patho.main.repository.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.patho.main.model.patient.*;
import com.patho.main.model.patient.notification.ReportIntent;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.common.Eye;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.Signature;
import com.patho.main.model.StainingPrototype;
import com.patho.main.repository.service.TaskRepositoryCustom;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.worklist.search.WorklistSearchExtended;

@Service
@Transactional
public class TaskRepositoryImpl extends AbstractRepositoryCustom implements TaskRepositoryCustom {

	public Optional<Task> findOptionalByIdAndInitialize(Long id, boolean loadCouncils, boolean loadDiangoses,
			boolean loadPDFs, boolean loadContacts, boolean loadParent) {

		CriteriaBuilder qb = getCriteriaBuilder();
		CriteriaQuery<Task> criteria = qb.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);

		return find(criteria, root, Arrays.asList(qb.equal(root.get(Task_.id), id)), loadCouncils, loadDiangoses,
				loadPDFs, loadContacts, loadParent);
	}

	public Optional<Task> findOptionalByTaskId(String taskID) {
		return findOptionalByTaskId(taskID, false, false, false, false, false);
	}

	public Optional<Task> findOptionalByTaskId(String taskID, boolean loadCouncils, boolean loadDiangoses,
			boolean loadPDFs, boolean loadContacts, boolean loadParent) {
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Task> criteria = qb.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);
		criteria.select(root);

		return find(criteria, root, Arrays.asList(qb.like(root.get("taskID"), taskID)), loadCouncils, loadDiangoses,
				loadPDFs, loadContacts, loadParent);
	}

	public Optional<Task> findOptionalBySlideID(String taskID, int uniqueSlideIDInBlock, boolean loadCouncils,
			boolean loadDiangoses, boolean loadPDFs, boolean loadContacts, boolean loadParent) {
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Task> criteria = qb.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);
		criteria.select(root);

		Join<Task, Sample> sampleQuery = root.join("samples", JoinType.LEFT);
		Join<Sample, Block> blockQuery = sampleQuery.join("blocks", JoinType.LEFT);
		Join<Block, Slide> slideQuery = blockQuery.join("slides", JoinType.LEFT);

		return find(criteria, root,
				Arrays.asList(qb.and(qb.like(root.get("taskID"), taskID),
						qb.equal(slideQuery.get("uniqueIDinTask"), uniqueSlideIDInBlock))),
				loadCouncils, loadDiangoses, loadPDFs, loadContacts, loadParent);
	}

	public Optional<Task> findOptinalByLastID(Calendar ofYear, boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs,
			boolean loadContacts, boolean loadParent) {

		String currentYear = Integer.toString(TimeUtil.getYearAsInt(ofYear) - 2000) + "%";

		// Create CriteriaBuilder
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Task> criteria = qb.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);

		Subquery<Task> subquery = criteria.subquery(Task.class);
		Root<Task> subTaskRoot = subquery.from(Task.class);
		subquery.where(qb.like(subTaskRoot.get("taskID"), currentYear));
		subquery.select(qb.max((Expression) subTaskRoot.get("taskID")));

		return find(criteria, root, Arrays.asList(qb.equal(root.get("taskID"), subquery)), loadCouncils, loadDiangoses,
				loadPDFs, loadContacts, loadParent);
	}

	public List<Task> findByCriteria(WorklistSearchExtended worklistSearchExtended, boolean initParent) {
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Task> criteria = qb.createQuery(Task.class);
		Root<Task> root = criteria.from(Task.class);
		criteria.select(root);

		if (initParent)
			root.fetch("parent", JoinType.LEFT);

		Join<Task, Sample> sampleQuery = root.join("samples", JoinType.LEFT);
		Join<Sample, Block> blockQuery = sampleQuery.join("blocks", JoinType.LEFT);
		Join<Block, Slide> slideQuery = blockQuery.join("slides", JoinType.LEFT);
		Join<Slide, StainingPrototype> prototypeQuery = slideQuery.join("slidePrototype", JoinType.LEFT);

		Join<Task, DiagnosisRevision> diagnosisRevisionQuery = root.join("diagnosisRevisions", JoinType.LEFT);
		Join<DiagnosisRevision, Diagnosis> diagnosesQuery = diagnosisRevisionQuery.join("diagnoses", JoinType.LEFT);
		Join<DiagnosisRevision, Signature> signatureOneQuery = diagnosisRevisionQuery.join("signatureOne",
				JoinType.LEFT);
		Join<Signature, Physician> signatureOnePhysicianQuery = signatureOneQuery.join("physician", JoinType.LEFT);
		Join<DiagnosisRevision, Signature> signatureTwoQuery = diagnosisRevisionQuery.join("signatureTwo",
				JoinType.LEFT);
		Join<Signature, Physician> signatureTwoPhysicianQuery = signatureTwoQuery.join("physician", JoinType.LEFT);

		Join<ReportIntent, Task> contactQuery = root.join("contacts", JoinType.LEFT);
		Join<Person, ReportIntent> personContactQuery = contactQuery.join("person", JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<Predicate>();

		// searching for material
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getMaterial())) {
			predicates.add(qb.like(qb.lower(sampleQuery.get("material")),
					"%" + worklistSearchExtended.getMaterial().toLowerCase() + "%"));

			logger.debug("Selecting material " + worklistSearchExtended.getMaterial());
		}

		// getting surgeon
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getSurgeons())) {
			Expression<Long> exp = personContactQuery.get("id");
			predicates
					.add(qb.and(
							exp.in(Arrays.asList(worklistSearchExtended.getSurgeons()).stream()
									.map(p -> p.getPerson().getId()).collect(Collectors.toList())),
							qb.equal(contactQuery.get("role"), ContactRole.SURGEON)));
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

			predicates.add(qb.or(physicianOne, physicianTwo));

			logger.debug("Selecting signature");
		}

		// getting history
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getCaseHistory())) {
			predicates.add(qb.like(qb.lower(root.get("caseHistory")),
					"%" + worklistSearchExtended.getCaseHistory().toLowerCase() + "%"));

			logger.debug("Selecting history");
		}

		// getting diagnosis text
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getDiagnosisText())) {
			predicates.add(qb.like(qb.lower(diagnosisRevisionQuery.get("text")),
					"%" + worklistSearchExtended.getDiagnosisText().toLowerCase() + "%"));

			logger.debug("Selecting diagnosis text");
		}

		// getting diagnosis
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getDiagnosis())) {
			predicates.add(qb.like(qb.lower(diagnosesQuery.get("diagnosis")),
					"%" + worklistSearchExtended.getDiagnosis().toLowerCase() + "%"));

			logger.debug("Selecting diagnosis");
		}

		// checking malign, 0 = not selected, 1 = true, 2 = false
		if (!worklistSearchExtended.getMalign().equals("0")) {
			predicates.add(qb.equal(diagnosesQuery.get("malign"),
					worklistSearchExtended.getMalign().equals("1") ? true : false));

			logger.debug("Selecting malign");
		}

		// getting eye
		if (worklistSearchExtended.getEye() != Eye.UNKNOWN) {
			predicates.add(qb.equal(root.get("eye"), worklistSearchExtended.getEye()));

			logger.debug("Selecting eye");
		}

		// getting ward
		if (HistoUtil.isNotNullOrEmpty(worklistSearchExtended.getWard())) {
			predicates.add(qb.like(qb.lower(diagnosesQuery.get("ward")),
					"%" + worklistSearchExtended.getWard().toLowerCase() + "%"));

			logger.debug("Selecting ward");
		}

		if (worklistSearchExtended.getStainings() != null && !worklistSearchExtended.getStainings().isEmpty()) {
			Expression<Long> prototypeID = prototypeQuery.get("id");
			Predicate stainings = prototypeID.in(
					worklistSearchExtended.getStainings().stream().map(p -> p.getId()).collect(Collectors.toList()));

			predicates.add(stainings);
		}

		criteria.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));

		criteria.distinct(true);

		List<Task> tasks = getSession().createQuery(criteria).getResultList();

		return tasks;
	}

	public Optional<Task> find(CriteriaQuery<Task> criteria, Root<Task> root, List<Predicate> predicates,
			boolean loadCouncils, boolean loadDiangoses, boolean loadPDFs, boolean loadContacts, boolean loadParent) {
		criteria.select(root);

		if (loadCouncils)
			root.fetch(Task_.councils, JoinType.LEFT);

		if (loadDiangoses)
			root.fetch(Task_.diagnosisRevisions, JoinType.LEFT);

		if (loadContacts)
			root.fetch(Task_.contacts, JoinType.LEFT);

		if (loadPDFs)
			root.fetch(Task_.attachedPdfs, JoinType.LEFT);

		criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));
		criteria.distinct(true);

		List<Task> groups = getSession().createQuery(criteria).getResultList();

		Optional<Task> task = Optional.ofNullable(groups.size() > 0 ? groups.get(0) : null);

		if (loadParent && task.isPresent()) {
			Hibernate.initialize(task.get().getParent().getTasks());
			Hibernate.initialize(task.get().getParent().getAttachedPdfs());
		}

		return task;
	}
}
