package com.patho.main.model

import org.slf4j.LoggerFactory
import org.springframework.data.util.ProxyUtils
import javax.persistence.MappedSuperclass

/**
 * Superclass for hibernate entities
 */
@MappedSuperclass
abstract class AbstractPersistable {

    protected open val logger = LoggerFactory.getLogger(this.javaClass)

    abstract var id: Long

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