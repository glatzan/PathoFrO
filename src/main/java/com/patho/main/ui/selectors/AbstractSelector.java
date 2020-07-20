package com.patho.main.ui.selectors;

public class AbstractSelector {
    protected boolean selected;

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
