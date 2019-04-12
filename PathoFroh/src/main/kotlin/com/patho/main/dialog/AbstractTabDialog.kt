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

    fun initBean(forceInitialization: Boolean,
                 selectTab: AbstractTab? = null): Boolean {

        // initializing tabs
        tabs.forEach { p -> p.initTab(forceInitialization) }

        // selecting tab
        if (selectTab != null)
        else


            if (selectTab) {
                if (selectedTab == null) {
                    // selecting the first not disabled tab
                    for (i in tabs.indices) {
                        if (!tabs[i].isDisabled()) {
                            onTabChange(tabs[i], true)
                            break
                        }
                    }
                } else {
                    onTabChange(selectedTab, true)
                }
            }

        return true
    }

    fun onTabChange(tab: AbstractTab, ignoreTabChangeEvent: Boolean = false) {
        if (eventHandler != null && !ignoreTabChangeEvent && eventHandler.isFireEvent(tab, getSelectedTab())) {
            eventHandler.fireTabChangeEvent(tab, getSelectedTab())
        } else {
            logger.debug("Changing tab to " + tab.name)
            selectedTab = tab
            tab.updateData()
        }
    }

    private fun findTabByName(name: String): AbstractTab? {
        return tabs.firstOrNull { p -> p.tabName == name }
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

        open var name: String = ""

        open var viewID: String = ""

        open var tabName: String = ""

        open var centerInclude: String = ""

        open var disabled: Boolean = false

        open var parentTab: AbstractTab? = null

        /**
         * If true an this tab is in the event array, the event will be fired
         */
        open var fireTabChangeEvent: Boolean = false

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
            this.fireTabChangeEvent = true
        }

    }
}