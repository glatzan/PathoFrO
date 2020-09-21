package com.patho.main.repository.miscellaneous.impl

import com.patho.main.model.Physician
import com.patho.main.model.dto.ldap.LDAPUserMapper
import com.patho.main.repository.miscellaneous.LDAPRepository
import lombok.Getter
import lombok.Setter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.ldap.support.LdapNameBuilder
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.naming.Name

@Service
@ConfigurationProperties(prefix = "patho.ldap")
@Getter
@Setter
class LDAPRepositoryImpl : LDAPRepository {
    @Autowired
    private val ldapTemplate: LdapTemplate? = null
    private val userParentDn: String? = null
    private val userDn: String? = null

    /**
     * Returns an user with the given uid. If not found an empty Optional will be
     * returned.
     *
     * @param name
     * @return
     */
    override fun findByUid(name: String): Optional<Physician> {
        return try {
            val userMapper = ldapTemplate!!.findByDn(buildUserDn(name), LDAPUserMapper::class.java)
            Optional.ofNullable(userMapper.physician)
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    /**
     * Returns a list with physicians matching the given parameters. It is seached
     * for cn (contains name, surname and title).
     *
     * @param name
     * @return
     */
    override fun findAllByName(vararg name: String): List<Physician> {
        return findAllByName(Arrays.asList(*name))
    }

    /**
     * Returns a list with physicians matching the given parameters. It is seached
     * for cn (contains name, surname and title).
     *
     * @param name
     * @return
     */
    override fun findAllByName(names: List<String>): List<Physician> {
        val query = LdapQueryBuilder.query().where("objectclass").`is`("person")
        names.forEach(Consumer { p: String -> query.and(LdapQueryBuilder.query().where("cn").like("*$p*")) })
        val result = ldapTemplate!!.find(query, LDAPUserMapper::class.java)
        return result.stream().map { p: LDAPUserMapper -> p.physician }.collect(Collectors.toList())
    }

    /**
     * Builds dn for a person
     *
     * @param person
     * @return
     */
    protected fun buildUserDn(person: String?): Name {
        return buildDn(userParentDn, userDn, person)
    }

    /**
     * Builds dn
     *
     * @param ou
     * @param dn
     * @param dnValue
     * @return
     */
    protected fun buildDn(ou: String?, dn: String?, dnValue: String?): Name {
        return LdapNameBuilder.newInstance().add("ou", ou).add(dn, dnValue).build()
    }
}
