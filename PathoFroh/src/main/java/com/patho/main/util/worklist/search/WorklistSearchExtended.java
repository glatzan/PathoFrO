package com.patho.main.util.worklist.search;

import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.common.Eye;
import com.patho.main.model.Physician;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class WorklistSearchExtended extends AbstractWorklistSearch {

	/**
	 * Name of Material
	 */
	private String material;

	/**
	 * List of physicians
	 */
	private Physician[] surgeons;

	/**
	 * List of physicians
	 */
	private Physician[] signature;

	/**
	 * Case history
	 */
	private String caseHistory;

	/**
	 * Diagnosis text
	 */
	private String diagnosisText;

	/**
	 * Diagnosis
	 */
	private String diagnosis;

	/**
	 * Malign, tri state, 0 = nothing, 1= true, 2 = false
	 */
	private String malign = "0";

	/**
	 * Eye
	 */
	private Eye eye = Eye.UNKNOWN;

	/**
	 * ward
	 */
	private String ward;

	/**
	 * List of stainings
	 */
	private List<StainingPrototype> stainings;

	@Override
	public List<Patient> getPatients() {

//		List<Task> tasks = taskDAO.getTaskByCriteria(this, true);
//
//		List<Patient> patients = patientDao.findComplex(this, true, false, true);
//
//		for (Task task : tasks) {
//
//			for (Patient patient : patients) {
//				if (task.getParent().getId() == patient.getId()) {
//					for (Task pTask : patient.getTasks()) {
//						if (pTask.getId() == task.getId())
//							pTask.setActive(true);
//					}
//				}
//			}
//		}
//
//		return patients;
		return null;
	}

}

// private String name;
// private String surename;
// private Date birthday;
// private Person.Gender gender;
//
// private String material;
// private String caseHistory;
// private String surgeon;
// private String privatePhysician;
// private String siganture;
// private Eye eye = Eye.UNKNOWN;
//
// private Date patientAdded;
// private Date patientAddedTo;
//
// private Date taskCreated;
// private Date taskCreatedTo;
//
// private Date stainingCompleted;
// private Date stainingCompletedTo;
//
// private Date diagnosisCompleted;
// private Date diagnosisCompletedTo;
//
// private Date dateOfReceipt;
// private Date dateOfReceiptTo;
//
// private Date dateOfSurgery;
// private Date dateOfSurgeryTo;
//
// private String reportIntent;
// private String category;
// private String malign;