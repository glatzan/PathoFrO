package com.patho.main.model.scanner

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.patient.Task
import com.patho.main.model.preset.StainingPrototypeDetails
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.springframework.context.annotation.Lazy
import java.io.Serializable
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class ScannedTask : AbstractPersistable{

    @Id
    override var id: Long = 0

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    open var task: Task = Task()

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    open var slides: MutableList<ScannedSlide> = mutableListOf()

    constructor()

    constructor(task : Task){
        this.task = task
    }
}