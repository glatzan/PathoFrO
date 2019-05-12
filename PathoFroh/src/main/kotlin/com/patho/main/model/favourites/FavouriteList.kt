package com.patho.main.model.favourites

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.patient.Task
import com.patho.main.model.user.HistoUser
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class FavouriteList() : AbstractPersistable() {

    @Id
    @SequenceGenerator(name = "favouritelist_sequencegenerator", sequenceName = "favouritelist_sequence")
    @GeneratedValue(generator = "favouritelist_sequencegenerator")
    @Column(unique = true, nullable = false)
    override open var id: Long = 0

    /**
     * Owner of the list, if null the systems owns the list
     */
    @OneToOne(fetch = FetchType.LAZY)
    open var owner: HistoUser? = null

    /**
     * Name of the list
     */
    @Column(columnDefinition = "VARCHAR")
    open var name: String = ""

    /**
     * Commentary describing the list
     */
    @Column(columnDefinition = "VARCHAR")
    open var commentary: String = ""

    /**
     * If true the list is a system list
     */
    @Column
    open var defaultList: Boolean = false

    /**
     * If true everyone can see an load this list
     */
    @Column
    open var globalView: Boolean = false

    /**
     * If true the user can manually add tasks.
     */
    @Column
    open var manuelEdit: Boolean = false

    /**
     * If true a specific icon is used
     */
    @Column
    open var useIcon: Boolean = false

    /**
     * If a click of the favourite list icon should trigger a command
     */
    @Column
    open var command: String? = null

    /**
     * Icon from primefaces
     */
    @Column
    open var icon: String = ""

    /**
     * color as hex color
     */
    @Column
    open var iconColor: String = ""

    /**
     * Info text for mouse hover
     */
    @Column
    open var infoText: String = ""

    /**
     * If more then one group has the same iconGroup (as primefaces image)
     * the will be merged under this icon in the patient list.
     */
    @Column
    open var iconGroup: String = ""

    /**
     * Listitems of the list
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "favouriteList")
    @Fetch(value = FetchMode.SUBSELECT)
    open var items: MutableList<FavouriteListItem> = mutableListOf()

    /**
     * Groups  permission settings
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "favouriteList", targetEntity = FavouritePermissionsGroup::class)
    @OrderBy("id ASC")
    open var groups: MutableSet<FavouritePermissionsGroup> = mutableSetOf()

    /**
     * User permission settings
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "favouriteList", targetEntity = FavouritePermissionsUser::class)
    @OrderBy("id ASC")
    open var users: MutableSet<FavouritePermissionsUser> = mutableSetOf()

    /**
     * TODO describe
     */
    @OneToMany(fetch = FetchType.LAZY)
    open var hideListForUser: Set<HistoUser>? = null

    /**
     * If true and an items is removed from this list, it will be copied to the dumplist
     */
    @Column
    open var useDumplist: Boolean = false

    /**
     * Dumplist
     */
    @OneToOne(fetch = FetchType.LAZY)
    open var dumpList: FavouriteList? = null

    /**
     * Commentary if the item is dumped
     */
    @Column
    open var dumpCommentary: String? = null

    /**
     * If true this list is archived
     */
    @Column
    open var archived: Boolean = false

    /**
     * If true the list will not be display within the normal add menu
     */
    @Column
    open var hideList: Boolean = false

    /**
     * If true archiving a task is not possible if the task is in this list
     */
    @Column
    open var blockTaskArchival = false

    /**
     * Checks if the given task is in this favourite list
     */
    open fun containsTask(task: Task): Boolean {
        return items.any { it.task == task }
    }
}