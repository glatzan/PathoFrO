package com.patho.main.dialog

import com.patho.main.action.dialog.AbstractTabChangeEventHandler
import com.patho.main.common.Dialog
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
    open fun onTabChange(targetTab: AbstractTab, ignoreTabChangeEvent: Boolean = false) {
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
        logger.trace("Next tab")
        var selectedTabIndex = tabs.indexOf(selectedTab)

        if (selectedTabIndex == -1)
            return

        for (n in selectedTabIndex until tabs.size) {
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
        logger.trace("Previous tab")
        var selectedTabIndex = tabs.indexOf(selectedTab)

        if (selectedTabIndex == -1)
            return

        for (n in selectedTabIndex downTo 0) {
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

        val isParent: Boolean
            get() = parentTab != null

        open fun updateData() {
            return
        }

        /**
         * Initializes the tab if not already done or if forced
         */
        open fun initTab(force: Boolean): Boolean {
            if (!initialized || force)
                initialized = true
            return true
        }

        open fun triggerEventOnChange() {
            logger.debug("Event fired")
            this.eventHandler.fireEventOnScopeLost = true
        }

    }
}