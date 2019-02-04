package com.patho.main.model.patient

import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.interfaces.ID
import com.patho.main.model.interfaces.Parent
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "diagnosis_sequencegenerator", sequenceName = "diagnosis_sequence")
class Diagnosis : ID, Parent<DiagnosisRevision> {

    @Id
    @GeneratedValue(generator = "diagnosis_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Version
    var version: Long = 0

    /**
     * Parent of the diagnosis, sample bject
     */
    @ManyToOne
    override var parent: DiagnosisRevision? = null

    /**
     * Name of the diagnosis.
     */
    @Column
    var name = ""

    /**
     * Diagnosis as short string.
     */
    @Column(columnDefinition = "text")
    var diagnosis = ""

    /**
     * True if finding is malign.
     */
    @Column
    var malign: Boolean = false

    /**
     * ICD10 Number of this diagnosis
     */
    @Column
    var icd10 = ""

    /**
     * Protoype used for this diagnosis.
     */
    @OneToOne
    @NotAudited
    var diagnosisPrototype: DiagnosisPreset? = null

    /**
     * Associated sample
     */
    @OneToOne
    var sample: Sample? = null

    override val patient: Patient?
        @Transient
        get() = parent?.patient

    /**
     * Returns the parent task
     */
    override val task: Task?
        @Transient
        get() = parent?.task

    override fun toString(): String {
        return "Diagnosis: " + name + if (id != 0L) ", ID: $id" else ""
    }

}
