package com.patho.main.action.dialog;

import com.patho.main.action.dialog.AbstractTabDialog.AbstractTab;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public abstract class AbstractTabChangeEventHandler {

    /**
     * Dialog to open on event
     */
    protected AbstractDialog eventDialog;

    /**
     * Target Tab
     */
    protected AbstractTab targetTab;

    /**
     * if true the tab change event will be fired
     */
    protected boolean fireTabChangeEvent;

    /**
     * For all tabs in this array a tab change event can be fired
     */
    protected AbstractTab[] onChangeEventTabs;

    /**
     * If true
     */
    protected boolean fireForEveryTabChange;

    public AbstractTabChangeEventHandler(AbstractDialog eventDialog, AbstractTab... onChangeEventTabs) {
        this(eventDialog, true, onChangeEventTabs);
    }

    public AbstractTabChangeEventHandler(AbstractDialog eventDialog, boolean fireForEveryTabChange,
                                         AbstractTab... onChangeEventTabs) {
        this.eventDialog = eventDialog;
        this.onChangeEventTabs = onChangeEventTabs;
        this.fireForEveryTabChange = fireForEveryTabChange;
    }

    public boolean isFireEvent(AbstractTab targetTab, AbstractTab currentTab) {
        if (Arrays.stream(onChangeEventTabs).anyMatch(p -> p == currentTab)
                && (fireTabChangeEvent || currentTab.isFireTabChangeEvent())) {
            if (isFireForEveryTabChange())
                return true;
            else {
                // only fire if target tab is not in the fire tab array
                if (Arrays.stream(onChangeEventTabs).anyMatch(p -> p == targetTab)) {
                    // copy fire tab to new taks
                    targetTab.setFireTabChangeEvent(true);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    public void fireTabChangeEvent(AbstractTab targetTab, AbstractTab currentTab) {
        // deleting fire event falg
        for (int i = 0; i < onChangeEventTabs.length; i++) {
            onChangeEventTabs[i].setFireTabChangeEvent(false);
        }
        setFireTabChangeEvent(false);

        setTargetTab(targetTab);

        if (getEventDialog() != null) {
            getEventDialog().initAndPrepareBean();
        }
    }

    public abstract void performEvent(boolean positive);

    public void setOnChangeEventTabs(AbstractTab... abstractTabs) {
        this.onChangeEventTabs = abstractTabs;
    }

}
