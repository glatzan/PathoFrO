package com.patho.main.model.patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.model.Physician;
import com.patho.main.model.Signature;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.LogAble;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.interfaces.TaskEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "diagnosisRevision_sequencegenerator", sequenceName = "diagnosisRevision_sequence")
@Getter
@Setter
public class DiagnosisRevision implements Parent<Task>, TaskEntity, LogAble, PatientRollbackAble<Task>, ID {

	@Id
	@GeneratedValue(generator = "diagnosisRevision_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	/**
	 * Name of this revision
	 */
	@Column
	private String name;

	/**
	 * Version
	 */
	@Version
	private long version;

	/**
	 * Parent of the Diagnosis
	 */
	@ManyToOne(targetEntity = Task.class)
	private Task parent;

	/**
	 * Type of the revison @see {@link DiagnosisRevisionType}
	 */
	@Enumerated(EnumType.STRING)
	private DiagnosisRevisionType type;

	/**
	 * Date of diagnosis creation.
	 */
	@Column
	private long creationDate;

	/**
	 * Date of diagnosis finalization.
	 */
	@Column
	private long completionDate;

	/**
	 * True if notification should be performed
	 */
	@Column
	private boolean notificationPending;
	
	/**
	 * Date of notification 
	 */
	@Column
	private long notificationDate;
	
	/**
	 * All diagnoses
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("sample.id ASC")
	private List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();

	/**
	 * Text containing the histological record for all samples.
	 */
	@Column(columnDefinition = "text")
	private String text = "";

	/**
	 * Selected physician to sign the report
	 */
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Signature signatureOne;

	/**
	 * Selected consultant to sign the report
	 */
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Signature signatureTwo;

	/**
	 * Date of the signature
	 */
	@Column
	private long signatureDate;

	/**
	 * Standardt consutructor
	 */
	public DiagnosisRevision() {
	}

	public DiagnosisRevision(String name, DiagnosisRevisionType type) {
		this.name = name;
		this.type = type;
	}
	
	public DiagnosisRevision(Task parent, DiagnosisRevisionType type) {
		this.parent = parent;
		this.type = type;
	}

	/********************************************************
	 * Transient
	 ********************************************************/

	@Transient
	public Diagnosis getLastRelevantDiagnosis() {
		return getDiagnoses().get(getDiagnoses().size() - 1);
	}

	/**
	 * Returns true if a diagnosis is marked as malign.
	 * 
	 * @return
	 */
	@Transient
	public boolean isMalign() {
		return diagnoses.stream().anyMatch(p -> p.isMalign());
	}

	@Transient
	public Date getSignatureDateAsDate() {
		return new Date(signatureDate);
	}

	@Transient
	public void setSignatureDateAsDate(Date signatureDateAsDate) {
		this.signatureDate = signatureDateAsDate.getTime();
	}

	/**
	 * Creates a signature object with the given physician
	 * 
	 * @param physician
	 */
	@Transient
	public void setPhysicianAsSignatureTwo(Physician physician) {
		Signature signature = new Signature(physician);
		signature.setRole(physician != null ? physician.getClinicRole() : "");
		setSignatureTwo(signature);
	}

	@Override
	public String toString() {
		return "Diagnosis-Revision: " + getName() + (getId() != 0 ? ", ID: " + getId() : "");
	}

	@Transient
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof DiagnosisRevision && ((DiagnosisRevision) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}
	
	/********************************************************
	 * Interface Parent
	 ********************************************************/
	/**
	 * �berschreibt Methode aus dem Interface StainingTreeParent
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
