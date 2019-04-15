package com.patho.main.action.dialog;

import com.patho.main.action.MainHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.patient.Task;
import com.patho.main.util.dialog.UniqueRequestID;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.exception.CustomNotUniqueReqest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

@Getter
@Setter
public abstract class AbstractDialog {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected MainHandlerAction mainHandlerAction;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected ResourceBundle resourceBundle;

    protected Task task;

    protected Dialog dilaog;

    protected UniqueRequestID uniqueRequestID = new UniqueRequestID();

    public AbstractDialog() {

    }

    public AbstractDialog(Dialog dialog) {
        setDilaog(dialog);
    }

    public AbstractDialog initAndPrepareBean() {
        if (initBean())
            prepareDialog();
        return this;
    }

    public AbstractDialog initAndPrepareBean(Task dialog) {
        return null;
    }

    public AbstractDialog initAndPrepareBean(Dialog dialog) {
        initBean(null, dialog);
        prepareDialog();
        return null;
    }

    public AbstractDialog initAndPrepareBean(Task task, Dialog dialog) {
        initBean(task, dialog);
        prepareDialog();
        return null;
    }

    public boolean initBean() {
        return initBean(null, getDilaog());
    }

    public boolean initBean(Dialog dialog) {
        return initBean(null, dialog);
    }

    public boolean initBean(Task task, Dialog dialog) {
        return initBean(task, dialog, false);
    }

    public boolean initBean(Task task, Dialog dialog, boolean uniqueRequestEnabled) {
        setTask(task);
        setDilaog(dialog);
        getUniqueRequestID().setEnabled(uniqueRequestEnabled);
        if (uniqueRequestEnabled)
            getUniqueRequestID().nextUniqueRequestID();

        return true;
    }

    /**
     * Method for displaying the associated dialog.
     */

    public void prepareDialog() {
        prepareDialog(dilaog);
    }

    /**
     * Method for displaying the associated dialog.
     */
    public void prepareDialog(Dialog dialog) {
        HashMap<String, Object> options = new HashMap<String, Object>();

        if (dialog.getWidth() != 0) {
            options.put("width", dialog.getWidth());
            options.put("contentWidth", dialog.getWidth());
        } else
            options.put("width", "auto");

        if (dialog.getHeight() != 0) {
            options.put("contentHeight", dialog.getHeight());
            options.put("height", dialog.getHeight());
        } else
            options.put("height", "auto");

        if (dialog.getUseOptions()) {
            options.put("resizable", dialog.getResizeable());
            options.put("draggable", dialog.getDraggable());
            options.put("modal", dialog.getModal());
        }

        options.put("closable", false);

        if (dialog.getHeader() != null)
            options.put("headerElement", "dialogForm:header");

        PrimeFaces.current().dialog().openDynamic(dialog.getPath(), options, null);

        logger.debug("Showing Dialog: " + dialog);
    }

    public void update() {
        update(false);
    }

    public void update(boolean update) {

    }

    /**
     * Return function for sub dialogs.
     *
     * @param event
     */
    public void onSubDialogReturn(SelectEvent event) {
        logger.debug("Default Dialog return function object: {}",event.getObject() != null ? event.getObject().getClass() : "empty");
        if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
            update(true);
        }
    }

    /**
     * Method for hiding the associated dialog.
     *
     * @throws CustomNotUniqueReqest
     */
    public void hideDialog() {
        hideDialog(null);
    }

    public void hideDialog(Object returnValue) {
        logger.debug("Hiding Dialog: " + getDilaog());
        PrimeFaces.current().dialog().closeDynamic(returnValue);
    }

}
