package com.patho.main.model.patient

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.Parent
import com.patho.main.model.preset.DiagnosisPreset
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
open class Diagnosis : AbstractPersistable, Parent<DiagnosisRevision> {

    @Id
    @GeneratedValue(generator = "diagnosis_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Parent of the reportIntent, sample bject
     */
    @ManyToOne
    open override var parent: DiagnosisRevision? = null

    /**
     * Name of the reportIntent.
     */
    @Column
    open var name = ""

    /**
     * Diagnosis as short string.
     */
    @Column(columnDefinition = "text")
    open var diagnosis = ""

    /**
     * True if finding is malign.
     */
    @Column
    open var malign: Boolean = false

    /**
     * ICD10 Number of this reportIntent
     */
    @Column
    open var icd10 = ""

    /**
     * Protoype used for this reportIntent.
     */
    @OneToOne
    @NotAudited
    open var diagnosisPrototype: DiagnosisPreset? = null

    /**
     * Associated sample
     */
    @OneToOne
    open var sample: Sample? = null

    constructor() {}

    open override val patient: Patient?
        @Transient
        get() = parent?.patient

    /**
     * Returns the parent task
     */
    open override val task: Task?
        @Transient
        get() = parent?.task

    open override fun toString(): String {
        return "Diagnosis: " + name + if (id != 0L) ", ID: $id" else ""
    }

}
