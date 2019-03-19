package com.patho.main.model.user

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.SelectBeforeUpdate
import org.springframework.security.core.GrantedAuthority
import java.beans.Transient
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class HistoGroup : AbstractPersistable, ID, GrantedAuthority {

    companion object {
        /**
         * ID of an empty group
         */
        const val GROUP_NOT_SET: Long = 0

        /**
         * ID of the disabled group
         */
        const val GROUP_DISABLED: Long = 1

        /**
         * ID of the guest group
         */
        const val GROUP_GUEST_ID: Long = 2
    }

    @Id
    @SequenceGenerator(name = "group_sequencegenerator", sequenceName = "group_sequence")
    @GeneratedValue(generator = "group_sequencegenerator")
    @Column(unique = true, nullable = false)
    override open var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Group name
     */
    @Column(columnDefinition = "VARCHAR")
    open var name: String = ""

    /**
     * Group Settings, will be copied to user
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], optional = false)
    open var settings: HistoSettings = HistoSettings()

    /**
     * Authrole for program rights (spring)
     */
    @Enumerated(EnumType.STRING)
    open var authRole = AuthRole.ROLE_NONEAUTH

    /**
     * Commentary
     */
    @Column(columnDefinition = "VARCHAR")
    open var commentary: String = ""

    /**
     * If true, members of this groups are deactivated (are per default hidden and can't login)
     */
    @Column
    open var userDeactivated: Boolean = false

    /**
     * Set of group permissions
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Cascade(value = [org.hibernate.annotations.CascadeType.ALL])
    open var permissions: MutableSet<HistoPermissions> = mutableSetOf()

    /**
     * If true group is archived
     */
    @Column(columnDefinition = "boolean default true")
    open var archived: Boolean = false

    @Transient
    override fun getAuthority(): String {
        return authRole.name
    }

    constructor()

    constructor(histoSettings: HistoSettings) : this(histoSettings, AuthRole.ROLE_NONEAUTH)

    constructor(histoSettings: HistoSettings, authRole: AuthRole) {
        this.authRole = authRole
        this.settings = histoSettings
    }

    enum class AuthRole {
        ROLE_NONEAUTH, ROLE_GUEST, ROLE_USER
    }
}