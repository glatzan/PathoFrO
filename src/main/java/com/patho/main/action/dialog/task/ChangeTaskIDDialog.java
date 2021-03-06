package com.patho.main.action.dialog.task;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.Getter;
import lombok.Setter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@Getter
@Setter
public class ChangeTaskIDDialog extends AbstractDialog {

    private String newID;

    public ChangeTaskIDDialog initAndPrepareBean(Task task) {
        if (initBean(task))
            prepareDialog();

        return this;
    }

    /**
     * Initializes the bean and calles updatePhysicianLists at the end.
     *
     * @param task
     */
    public boolean initBean(Task task) {
        setNewID(task.getTaskID());
        super.initBean(task, Dialog.TASK_CHANGE_ID);

        return true;
    }

    public void changeAndHide() {
        logger.debug("Changing task id form " + task.getId() + " to " + newID);
        hideDialog(new TaskReloadEvent(SpringContextBridge.services().getTaskService().changeTaskID(task, newID)));
    }

    public void validateTaskID(FacesContext context, UIComponent componentToValidate, Object value)
            throws ValidatorException {

        if (value.equals(task.getId())) {
            return;
        }

        switch (SpringContextBridge.services().getTaskService().validateTaskID(value.toString())) {
            case SHORT:
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        resourceBundle.get("dialog.changeTaskID.error.short")));
            case ONLY_NUMBERS:
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        resourceBundle.get("dialog.changeTaskID.error.numbers")));
            case ALREADY_PRESENT:
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        resourceBundle.get("dialog.changeTaskID.error.duplicated")));
            default:
                break;
        }
    }
}
