package com.patho.main.model.preset

import com.patho.main.common.ContactRole
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ListOrder
import org.hibernate.annotations.*
import org.hibernate.annotations.CascadeType
import javax.persistence.*
import javax.persistence.Entity

@Entity
@SelectBeforeUpdate(true)
open class DiagnosisPreset : ListOrder<DiagnosisPreset>, AbstractPersistable {

    @Id
    @GeneratedValue(generator = "diagnosisPreset_sequencegenerator")
    @SequenceGenerator(name = "diagnosisPreset_sequencegenerator", sequenceName = "diagnosisPreset_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    /**
     * Category of the diagnosis
     */
    @Column(columnDefinition = "VARCHAR")
    open var category: String = ""

    /**
     * icd10
     */
    @Column(columnDefinition = "VARCHAR")
    open var icd10: String = ""

    /**
     * malign
     */
    @Column(columnDefinition = "VARCHAR")
    open var malign = false

    /**
     * diagnosis as string
     */
    @Column(columnDefinition = "text")
    open var diagnosis: String = ""

    /**
     * long diagnosis text
     */
    @Column(columnDefinition = "text")
    open var extendedDiagnosisText: String = ""

    /**
     * commentary
     */
    @Column(columnDefinition = "text")
    open var commentary: String = ""

    /**
     * for sorting
     */
    @Column
    override var indexInList = 0

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = [CascadeType.ALL])
    open var diagnosisReportAsLetter: MutableSet<ContactRole> = mutableSetOf()

    @Column
    open var archived = false

    @Transient
    open val diagnosisReportAsLetterAsArray = diagnosisReportAsLetter.toTypedArray()

    constructor()

    constructor(diagnosisPreset: DiagnosisPreset) {
        this.id = diagnosisPreset.id
    }
}