package com.patho.main.common;

/**
 * View
 *
 * @author andi
 */
public enum View {

    LOGIN("/login.xhtml"),
    GUEST("/pages/guest.xhtml"),
    SCIENTIST("/pages/scientist.xhtml"),
    WORKLIST("/pages/worklist.xhtml"),
    INCLUDE_PAGE_BLANK("/pages/worklist/blank.xhtml", WORKLIST),
    INCLUDE_PAGE_DATA_ERROR("/pages/worklist/dataError.xhtml", WORKLIST),
    INCLUDE_PAGE_NOTHING_SELECTED("/pages/worklist/notSelected.xhtml", WORKLIST),
    INCLUDE_PAGE_TASKS("/pages/worklist/taskList.xhtml", WORKLIST),
    INCLUDE_PAGE_PATIENT("/pages/worklist/patient.xhtml", WORKLIST),
    INCLUDE_PAGE_RECEIPTLOG("/pages/worklist/receiptlog.xhtml", WORKLIST, true),
    INCLUDE_PAGE_DIAGNOSIS("/pages/worklist/diagnosis.xhtml", WORKLIST, true),
    INCLUDE_PAGE_REPORT("/pages/worklist/report.xhtml", WORKLIST, true);

    public final String path;

    private final View rootView;

    /**
     * True if persistent view
     */
    private boolean lastSubviewAble;

    View(final String path) {
        this.path = path;
        this.rootView = null;
        this.lastSubviewAble = false;
    }

    View(final String path, View parentView) {
        this.path = path;
        this.rootView = parentView;
        this.lastSubviewAble = false;
    }

    View(final String path, View parentView, boolean lastSubviewAble) {
        this.path = path;
        this.rootView = parentView;
        this.lastSubviewAble = lastSubviewAble;
    }

    public String getRootPath() {
        if (rootView != null)
            return rootView.getPath();
        return getPath();
    }

    public String getPath() {
        return this.path;
    }

    public View getRootView() {
        return this.rootView;
    }

    public boolean isLastSubviewAble() {
        return this.lastSubviewAble;
    }
}
