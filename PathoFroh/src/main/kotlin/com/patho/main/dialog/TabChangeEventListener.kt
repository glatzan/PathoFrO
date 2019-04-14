package com.patho.main.dialog

/**
 * Tab change event listener
 */
open class TabChangeEventListener {

    /**
     * If true the event listener is enabled
     */
    var enabled: Boolean = false

    /**
     * If true the event will be fired on every tab change, if false
     * it will only be fired if the scope of the event listener is left.
     */
    var fireOnEveryTabChange: Boolean = false

    /**
     * If
     */
    var fireEventOnScopeLost: Boolean = false

    /**
     * List of tas
     */
    var eventTabs: MutableList<AbstractTabDialog_.AbstractTab> = mutableListOf()

    /**
     *  Target tab after the event was executed
     */
    var targetTab: AbstractTabDialog_.AbstractTab? = null

    /**
     * Checks if the event should be fired, and fires it if so.
     */
    open fun fireEvent(targetTab: AbstractTabDialog_.AbstractTab, sourceTab: AbstractTabDialog_.AbstractTab) {
        if (isFireEvent(targetTab, sourceTab))
            executeEvent(targetTab, sourceTab)
    }

    /**
     * fires the event listener event
     */
    open fun executeEvent(targetTab: AbstractTabDialog_.AbstractTab, sourceTab: AbstractTabDialog_.AbstractTab) {
        this.fireEventOnScopeLost = false
        this.targetTab = targetTab
    }

    /**
     * Examines if the change tab event should be fired
     */
    open fun isFireEvent(targetTab: AbstractTabDialog_.AbstractTab, sourceTab: AbstractTabDialog_.AbstractTab): Boolean {
        // if event should be fired on every tab change
        return if (fireOnEveryTabChange)
            fireEventOnScopeLost
        else {
            // if the target tab has the same handler do nothing
            if (targetTab.eventHandler == sourceTab.eventHandler) {
                false
            } else {
                // only fire event if something has changed
                fireEventOnScopeLost
            }
        }
    }
}