package com.patho.main.model

import com.patho.main.model.interfaces.ID
import org.slf4j.LoggerFactory
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import javax.persistence.MappedSuperclass
import javax.persistence.Transient

/**
 * Superclass for hibernate entities
 */
@MappedSuperclass
abstract class AbstractPersistable : Serializable, ID {

    @Transient
    protected open val logger = LoggerFactory.getLogger(this.javaClass)

    override abstract var id: Long

    open override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as AbstractPersistable

        return if (this.id == null) false else this.id == other.id
    }

    open override fun hashCode(): Int {
        return javaClass.name.hashCode() + (if (id != 0L) id else 0L).toInt()
    }

    open override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"
}