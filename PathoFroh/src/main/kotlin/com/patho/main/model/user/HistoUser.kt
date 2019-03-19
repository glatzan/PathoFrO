package com.patho.main.model.user

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.Physician
import com.patho.main.model.interfaces.ID
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import javax.persistence.*

/**
 *
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
open class HistoUser : AbstractPersistable, ID, UserDetails {

    @Id
    @SequenceGenerator(name = "user_sequencegenerator", sequenceName = "user_sequence")
    @GeneratedValue(generator = "user_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Column
    open var lastLogin: Instant? = null

    @Column
    open var archived: Boolean = false

    @Column
    open var localUser: Boolean = false

    @ManyToOne(fetch = FetchType.EAGER)
    @NotAudited
    open var group: HistoGroup = HistoGroup()

    @OneToOne(cascade = [CascadeType.ALL])
    @NotAudited
    open var settings = HistoSettings()

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    open var physician: Physician = Physician(Person(Contact()))

    @Transient
    open var accountNonExpired = true

    @Transient
    open var accountNonLocked = true

    @Transient
    open var credentialsNonExpired = true

    constructor()

    constructor(physician: Physician) : this(physician, HistoSettings())

    constructor(physician: Physician, histoSettings: HistoSettings) {
        this.physician = physician
        this.settings = histoSettings
    }

    /**
     * Returns the group als the auth role
     */
    @Transient
    override fun getAuthorities(): MutableList<out GrantedAuthority> {
        return mutableListOf(group)
    }

    /**
     * Returns true if histo group is not set or the disabled group is set
     */
    @Transient
    override fun isEnabled(): Boolean {
        return group.id == HistoGroup.GROUP_NOT_SET || group.id == HistoGroup.GROUP_DISABLED
    }

    /**
     * Returns the username
     */
    @Transient
    override fun getUsername(): String {
        return physician.uid
    }

    /**
     * Sets the username
     */
    @Transient
    fun setUsername(value: String) {
        physician.uid = value
    }

    @Transient
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    @Transient
    override fun getPassword(): String {
        return ""
    }

    @Transient
    override fun isAccountNonExpired(): Boolean {
        return true
    }

    @Transient
    override fun isAccountNonLocked(): Boolean {
        return true
    }
}