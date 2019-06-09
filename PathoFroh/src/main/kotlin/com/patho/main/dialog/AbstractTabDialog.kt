package com.patho.main.dialog

import com.patho.main.action.dialog.AbstractTabChangeEventHandler
import com.patho.main.common.Dialog
import com.patho.main.util.dialog.TabChangeEventListener
import org.slf4j.LoggerFactory

/**
 * Dialog with tabs
 */
abstract class AbstractTabDialog_(dialog: Dialog) : AbstractDialog_(dialog) {

    open var tabs: Array<AbstractTab> = arrayOf()

    open var selectedTab: AbstractTab? = null

    open var eventHandler: AbstractTabChangeEventHandler? = null

    open fun initBean(forceInitialization: Boolean, tabName: String): Boolean {
        return initBean(forceInitialization, findTabByName(tabName))
    }

    open fun initBean(forceInitialization: Boolean,
                      selectTab: AbstractTab? = null): Boolean {

        // initializing tabs
        tabs.forEach { p -> p.initTab(forceInitialization) }

        // selecting tab
        if (selectTab != null) {
            onTabChange(selectTab, true)
        } else {
            // selects the first tab if no tab is selected
            if (selectedTab == null) {
                var firstTab = findFirstTab()
                if (firstTab != null)
                    onTabChange(firstTab, true)
            } else {
                selectedTab?.updateData()
            }
        }

        return true
    }

    /**
     * Function is called when a tab is changed. Will execute a tab change event.
     */
    open fun onTabChange(targetTab: AbstractTab) {
        onTabChange(targetTab, false)
    }

    /**
     * Function is called when a tab is changed. Will execute a tab change event.
     */
    open fun onTabChange(targetTab: AbstractTab, ignoreTabChangeEvent: Boolean) {
        val tmpTab = selectedTab
        if (tmpTab != null && !ignoreTabChangeEvent && tmpTab.eventHandler.isFireEvent(targetTab, tmpTab)) {
            tmpTab.eventHandler.executeEvent(targetTab, tmpTab)
        } else {
            logger.debug("Changing tab to ${targetTab.name}")
            selectedTab = targetTab
            targetTab.updateData()
        }
    }

    /**
     * Returns the first not disabled tab
     */
    protected fun findFirstTab(): AbstractTab? {
        return tabs.firstOrNull { p -> !p.disabled }
    }

    /**
     * Returns the tab with the given name
     */
    protected fun findTabByName(name: String): AbstractTab? {
        return tabs.firstOrNull { p -> p.tabName == name }
    }

    /**
     * Selects the next tab
     */
    open fun nextTab() {
        logger.debug("Next tab")
        var selectedTabIndex = tabs.indexOf(selectedTab)

        if (selectedTabIndex == -1 || selectedTabIndex+1 >= tabs.size)
            return

        for (n in selectedTabIndex+1 until tabs.size) {
            if (!tabs[n].disabled) {
                onTabChange(tabs[n])
                return
            }
        }
    }

    /**
     * Selects the previous tab
     */
    open fun previousTab() {
        logger.debug("Previous tab")
        var selectedTabIndex = tabs.indexOf(selectedTab)

        if (selectedTabIndex == -1 || selectedTabIndex-1 < 0)
            return

        for (n in selectedTabIndex-1 downTo 0) {
            if (!tabs[n].disabled) {
                onTabChange(tabs[n])
                return
            }
        }
    }

    /**
     * Tab of a TabDialog
     */
    abstract class AbstractTab {

        protected val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * True if initialized
         */
        open var initialized: Boolean = false

        /**
         * internal name of the tab
         */
        open var name: String = ""

        /**
         * ID of the tab
         */
        open var viewID: String = ""

        /**
         * Name of the tab which is displayed
         */
        open var tabName: String = ""

        /**
         * Center include of the tab, as link
         */
        open var centerInclude: String = ""

        /**
         * If true the tab can not be selected
         */
        open var disabled: Boolean = false

        /**
         * Parent tab
         */
        open var parentTab: AbstractTab? = null

        /**
         * Event handler for the tab
         */
        open var eventHandler: TabChangeEventListener = TabChangeEventListener()

        /**
         * Returns true if a parent is present
         */
        val isParent: Boolean
            get() = parentTab != null

        constructor()

        /**
         * Constructor with parameters
         */
        constructor(tabName: String, name: String, viewID: String, centerInclude: String) {
            this.tabName = tabName
            this.name = name
            this.viewID = viewID
            this.centerInclude = centerInclude
        }

        /**
         * Function for updating tab data
         */
        open fun updateData() {
        }

        /**
         * Initializes the tab
         */
        open fun initTab(): Boolean {
            return initTab(false)
        }

        /**
         * Initializes the tab if not already done or if forced
         */
        open fun initTab(force: Boolean = false): Boolean {
            if (!initialized || force)
                initialized = true
            return true
        }

        /**
         * Method is called if an change event should be triggered on tab change
         */
        open fun triggerEventOnChange() {
            logger.debug("Event fired")
            this.eventHandler.fireEventOnScopeLost = true
        }

    }
}