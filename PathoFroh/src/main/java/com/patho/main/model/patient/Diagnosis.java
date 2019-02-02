package com.patho.main.model.patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.interfaces.GsonAble;
import com.patho.main.model.interfaces.ID;

import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.interfaces.TaskEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Diagnose NAchbefundung Revision
 * 
 * @author andi
 *
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "diagnosis_sequencegenerator", sequenceName = "diagnosis_sequence")
@Getter
@Setter
public class Diagnosis implements Parent<DiagnosisRevision>, GsonAble,  TaskEntity, PatientRollbackAble<DiagnosisRevision>, ID {

	@Id
	@GeneratedValue(generator = "diagnosis_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	/**
	 * Parent of the diagnosis, sample bject
	 */
	@ManyToOne
	private DiagnosisRevision parent;

	/**
	 * Name of the diagnosis.
	 */
	@Column
	private String name = "";

	/**
	 * Diagnosis as short string.
	 */
	@Column(columnDefinition = "text")
	private String diagnosis = "";

	/**
	 * True if finding is malign.
	 */
	@Column
	private boolean malign;

	/**
	 * ICD10 Number of this diagnosis
	 */
	@Column
	private String icd10 = "";

	/**
	 * Protoype used for this diagnosis.
	 */
	@OneToOne
	@NotAudited
	private DiagnosisPreset diagnosisPrototype;

	/**
	 * Associated sample
	 */
	@OneToOne
	private Sample sample;

	/**
	 * Standard constructor
	 */
	public Diagnosis() {
	}

	@Override
	public String toString() {
		return "Diagnosis: " + getName() + (getId() != 0 ? ", ID: " + getId() : "");
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/
	/**
	 * Returns the patient of this diagnosis. Overwrites method from
	 * StainingTreeParent.
	 */
	@Transient
	@Override
	public Patient getPatient() {
		return getParent().getPatient();
	}

	/**
	 * Returns the parent task
	 */
	@Override
	@Transient
	public Task getTask() {
		return getParent().getTask();
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/

	/**
	 * Overwriting of TaskEntity, returns id
	 */
	@Transient
	@Override
	public String toSimpleString() {
		return getName();
	}
}
