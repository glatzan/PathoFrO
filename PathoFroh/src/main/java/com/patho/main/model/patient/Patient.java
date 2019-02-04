package com.patho.main.model.patient;

import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.*;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "patient_sequencegenerator", sequenceName = "patient_sequence")
public class Patient
        implements Parent<Patient>, CreationDate, ArchivAble, DataList, ID {

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

    /**
     * File Repository Base of the corresponding patient
     */
    @Override
    @Transient
    public File getFileRepositoryBase() {
        return new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + String.valueOf(getId()));
    }

    public long getId() {
        return this.id;
    }

    public long getVersion() {
        return this.version;
    }

    public String getPiz() {
        return this.piz;
    }

    public String getInsurance() {
        return this.insurance;
    }

    public long getCreationDate() {
        return this.creationDate;
    }

    public Person getPerson() {
        return this.person;
    }

    public Set<Task> getTasks() {
        return this.tasks;
    }

    public boolean isExternalPatient() {
        return this.externalPatient;
    }

    public Set<PDFContainer> getAttachedPdfs() {
        return this.attachedPdfs;
    }

    public boolean isArchived() {
        return this.archived;
    }

    public boolean isInDatabase() {
        return this.inDatabase;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setPiz(String piz) {
        this.piz = piz;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public void setExternalPatient(boolean externalPatient) {
        this.externalPatient = externalPatient;
    }

    public void setAttachedPdfs(Set<PDFContainer> attachedPdfs) {
        this.attachedPdfs = attachedPdfs;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setInDatabase(boolean inDatabase) {
        this.inDatabase = inDatabase;
    }
}
