package com.patho.main.repository.miscellaneous

import com.patho.main.model.Physician
import java.util.*

interface LDAPRepository {
    /**
     * Returns an user with the given uid. If not found an empty Optional will be
     * returned.
     *
     * @param name
     * @return
     */
    fun findByUid(name: String): Optional<Physician>

    /**
     * Returns a list with physicians matching the given parameters. It is seached
     * for cn (contains name, surname and title).
     *
     * @param name
     * @return
     */
    fun findAllByName(vararg name: String): List<Physician>

    /**
     * Returns a list with physicians matching the given parameters. It is seached
     * for cn (contains name, surname and title).
     *
     * @param names
     * @return
     */
    fun findAllByName(names: List<String>): List<Physician>
}
