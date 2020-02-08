package com.patho.main.model.preset

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ListOrder
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.springframework.context.annotation.Lazy
import java.util.*
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class StainingPrototype() : ListOrder<MaterialPreset>, AbstractPersistable() {

    companion object{

        const val TYPE_NORMAL = 0

        const val TYPE_IMMUN = 1
    }

    @Id
    @GeneratedValue(generator = "stainingPrototype_sequencegenerator")
    @SequenceGenerator(name = "stainingPrototype_sequencegenerator", sequenceName = "stainingPrototype_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    /**
     * Name
     */
    @Column(columnDefinition = "VARCHAR")
    open var name: String = ""

    /**
     * Commentary
     */
    @Column(columnDefinition = "VARCHAR")
    open var commentary: String = ""

    /**
     * Type
     */
    @Enumerated(EnumType.STRING)
    open var type: StainingPrototypeType = StainingPrototypeType.NORMAL

    /**
     * Archive
     */
    @Column
    open var archived = false

    /**
     * On every selection of the staining, this number will be increased. The
     * staining can be ordered according to this value. So often used stainings
     * will be displayed first.
     */
    @Column
    open var priorityCount = 0

    /**
     * for sorting, manually
     */
    @Column
    override var indexInList = 0

    /**
     * Details of the batch
     */
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    open var batchDetails: MutableList<StainingPrototypeDetails> = mutableListOf()
        get() {
            if (field == null)
                field = mutableListOf<StainingPrototypeDetails>()
            return field
        }

}