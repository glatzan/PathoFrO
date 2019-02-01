package com.patho.main.model.patient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.ArchivAble;
import com.patho.main.model.interfaces.CreationDate;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.LogAble;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.PatientRollbackAble;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "patient_sequencegenerator", sequenceName = "patient_sequence")
@Getter
@Setter
public class Patient
		implements Parent<Patient>, CreationDate, LogAble, ArchivAble, PatientRollbackAble<Patient>, DataList, ID {

	@Id
	@GeneratedValue(generator = "patient_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	/**
	 * PIZ
	 */
	@Column
	private String piz = "";

	/**
	 * Insurance of the patient
	 */
	@Column
	private String insurance = "";

	/**
	 * Date of adding to the database
	 */
	@Column
	private long creationDate = 0;

	/**
	 * Person data
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@NotAudited
	private Person person;

	/**
	 * Task for this patient
	 */
	@OneToMany(mappedBy = "parent")
	@LazyCollection(LazyCollectionOption.TRUE)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("taskid DESC")
	private Set<Task> tasks = new HashSet<Task>();

	/**
	 * True if patient was added as an external patient.
	 */
	@Column
	private boolean externalPatient = false;

	/**
	 * Pdf attached to this patient, this might be an informed consent
	 */
	@OneToMany()
	@LazyCollection(LazyCollectionOption.TRUE)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("audit.createdOn DESC")
	private Set<PDFContainer> attachedPdfs = new HashSet<PDFContainer>();

	/**
	 * If true the patient is archived. Thus he won't be displayed.
	 */
	@Column
	private boolean archived = false;

	/**
	 * True if saved in database, false if only in clinic backend
	 */
	@Transient
	private boolean inDatabase = true;

	/**
	 * Standard constructor
	 */
	public Patient() {
	}

	public Patient(long id) {
		this.id = id;
	}

	public Patient(Person person) {
		this.person = person;
	}

	@Override
	@Transient
	public String toString() {
		return "Patient: " + getPerson().getFullName() + ", " + getPiz() + (getId() != 0 ? " , ID: " + getId() : "");
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Patient && ((Patient) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}

	@Transient
	public List<Task> getActiveTasks() {
		return getActiveTasks(false);
	}

	public List<Task> getTasksOfPatient(boolean activeOnly) {
		return getTasks() != null ? getTasks().stream().filter(p -> (activeOnly && p.isActive()) || !activeOnly)
				.collect(Collectors.toList()) : null;
	}

	/**
	 * Returns a list with all currently active tasks of a Patient
	 * 
	 * @return
	 */
	@Transient
	public List<Task> getActiveTasks(boolean activeOnly) {
		return getTasks() != null
				? getTasks().stream().filter(p -> p.isActiveOrActionPending(activeOnly)).collect(Collectors.toList())
				: new ArrayList<Task>();
	}

	@Transient
	public boolean hasActiveTasks() {
		return hasActiveTasks(false);
	}

	/**
	 * Returns true if at least one task is marked as active
	 * 
	 * @param patient
	 * @return
	 */
	@Transient
	public boolean hasActiveTasks(boolean activeOnly) {
		return getTasks() != null ? getTasks().stream().anyMatch(p -> p.isActiveOrActionPending(activeOnly)) : false;
	}

	/**
	 * Returns a list with tasks which are not active
	 * 
	 * @return
	 */
	@Transient
	public List<Task> getNoneActiveTasks() {
		return getTasks() != null
				? getTasks().stream().filter(p -> !p.isActiveOrActionPending()).collect(Collectors.toList())
				: null;
	}

	/**
	 * Returns true if at least one task is not marked as active
	 * 
	 * @param patient
	 * @return
	 */
	@Transient
	public boolean hasNoneActiveTasks() {
		return getTasks() != null ? getTasks().stream().anyMatch(p -> !p.isActiveOrActionPending()) : false;
	}

	@Transient
	@Override
	public Patient getPatient() {
		return this;
	}

	@Override
	@Transient
	public Task getTask() {
		return null;
	}

	@Transient
	@Override
	public String getTextIdentifier() {
		return null;
	}

	@Transient
	@Override
	public Dialog getArchiveDialog() {
		return null;
	}

	@Transient
	@Override
	public Patient getParent() {
		return null;
	}

	@Override
	public void setParent(Patient parent) {
	}

	@Override
	@Transient
	public String getPublicName() {
		return getPerson().getFullName();
	}

	@Override
	@Transient
	public String getLogPath() {
		return toString();
	}

	/**
	 * File Repository Base of the corresponding patient
	 */
	@Override
	@Transient
	public File getFileRepositoryBase() {
		return new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + String.valueOf(getId()));
	}
}
