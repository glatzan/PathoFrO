package com.patho.main.model.user

import com.patho.main.common.View
import com.patho.main.common.WorklistSortOrder
import com.patho.main.model.AbstractPersistable
import com.patho.main.util.printer.ClinicPrinter
import com.patho.main.util.printer.LabelPrinter
import com.patho.main.util.worklist.search.WorklistSimpleSearch
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class HistoSettings() : AbstractPersistable(), Cloneable {

    @Id
    @SequenceGenerator(name = "settings_sequencegenerator", sequenceName = "settings_sequence")
    @GeneratedValue(generator = "settings_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * True if the printer should be autoselected
     */
    @Column
    open var autoSelectedPreferredPrinter: Boolean = false

    /**
     * Name of the preferred cups printer
     */
    @Column(columnDefinition = "VARCHAR")
    open var preferredPrinter: Long = 0

    /**
     * True if the label printer should be autoselected
     */
    @Column
    open var autoSelectedPreferredLabelPrinter: Boolean = false

    /**
     * The uuid of the preferred labelprinter
     */
    @Column(columnDefinition = "VARCHAR")
    open var preferredLabelPrinter: String = ""

    /**
     * Page which should be shown as default page
     */
    @Enumerated(EnumType.STRING)
    open var defaultView: View = View.LOGIN

    /**
     * Page which will be shown on startup
     */
    @Enumerated(EnumType.STRING)
    open var startView: View = View.LOGIN

    /**
     * Default worklist to load, staining, reportIntent, notification, none
     */
    @Enumerated(EnumType.STRING)
    open var worklistToLoad: WorklistSimpleSearch.SimpleSearchOption = WorklistSimpleSearch.SimpleSearchOption.EMPTY_LIST

    /**
     * Default sortorder of worklist
     */
    @Enumerated(EnumType.ORDINAL)
    open var worklistSortOrder: WorklistSortOrder = WorklistSortOrder.PIZ

    /**
     * True the sort order is ascending, false the sortorder is descending
     */
    @Column
    open var worklistSortOrderAsc: Boolean = false

    /**
     * If true none active tasks in worklist will be hidden per default
     */
    @Column
    open var worklistHideNoneActiveTasks: Boolean = false

    /**
     * True if autoupdate of the current worklist should be happening
     */
    @Column
    open var worklistAutoUpdate: Boolean = false

    /**
     * If true, a patient added viea quciksearch will be added to the worklist an
     * the create task dialog will be opend.
     */
    @Column
    open var alternatePatientAddMode: Boolean = false

    /**
     * Background-Color of all inputfields
     */
    @Column(columnDefinition = "VARCHAR", length = 7)
    open var inputFieldColor: String = "ffffff"

    /**
     * Font-Color of the inputfields
     */
    @Column(columnDefinition = "VARCHAR", length = 7)
    open var inputFieldFontColor: String? = "000000"

    /**
     * If true tasks from the task list will be added and displayed immediately . If
     * false first they will be added to the worklist, with the second click the
     * task will be shown.
     */
    @Column
    open var addTaskWithSingleClick: Boolean = false

    /**
     * List of available views
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = [org.hibernate.annotations.CascadeType.ALL])
    @OrderColumn(name = "position")
    open var availableViews: MutableList<View> = mutableListOf()

    /**
     * List of available standard worklists
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = [org.hibernate.annotations.CascadeType.ALL])
    @OrderColumn(name = "position")
    open var availableWorklists: MutableList<WorklistSimpleSearch.SimpleSearchOption> = mutableListOf()

    /**
     * List of contact Role as an array, used by gui
     */
    open var availableWorklistsAsArray
        @Transient
        get() = availableWorklists.toTypedArray()
        @Transient
        set(value) {
            availableWorklists = value.toMutableList()
        }

    /**
     * List of contact Role as an array, used by gui
     */
    open var availableViewsAsArray
        @Transient
        get() = availableViews.toTypedArray()
        @Transient
        set(value) {
            availableViews = value.toMutableList()
        }

    @Transient
    open fun setPrinter(printer: ClinicPrinter) {
        preferredPrinter = printer.id
    }

    @Transient
    open fun setLabelPrinter(printer: LabelPrinter) {
        preferredLabelPrinter = printer.id.toString()
    }

}