package com.patho.main.action.dialog.notification;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.dialog.notification.ContactSelectDialog.SelectPhysicianReturnEvent;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.PhysicianService;
import com.patho.main.service.ReportIntentService;
import com.patho.main.ui.selectors.AssociatedContactSelector;
import com.patho.main.util.dialogReturn.ReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Configurable
@Getter
@Setter
public class ContactDialog extends AbstractDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ReportIntentService reportIntentService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskRepository taskRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PhysicianService physicianService;


    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Lazy
    private DialogHandler dialogHandler;

    /**
     * List of contacts
     */
    private AssociatedContactSelector[] contacts;

    /**
     * List of all ContactRole available for selecting physicians, used by contacts
     * and settings
     */
    private ContactRole[] selectAbleContactRoles;

    /**
     * Array of roles for that physicians should be shown.
     */
    private ContactRole[] showRoles;

    public ContactDialog initAndPrepareBean(Task task) {
        if (initBean(task))
            prepareDialog();

        return this;
    }

    public boolean initBean(Task task) {
        super.initBean(task, Dialog.CONTACTS);

        setSelectAbleContactRoles(ContactRole.values());

        setShowRoles(ContactRole.values());

        update(false);

        return true;
    }

    /**
     * Reloads data of the task
     */
    public void update(boolean reload) {
        if (reload) {
            setTask(taskRepository.findOptionalByIdAndInitialize(task.getId(), false, false, false, true, true).get());
        }

        updateContactHolders();
    }

    /**
     * Generates a list for displaying contact data
     */
    public void updateContactHolders() {
        if (task.getContacts() != null) {
            List<AssociatedContactSelector> selectors = AssociatedContactSelector.factory(task);
            setContacts(selectors.toArray(new AssociatedContactSelector[selectors.size()]));
        }
    }

    /**
     * Opens the add contact dialog
     */
    public void openAddContactDialog() {
        dialogHandler.getContactSelectDialog().initAndPrepareBean(task, ContactRole.values(), ContactRole.values(),
                ContactRole.values(), ContactRole.OTHER_PHYSICIAN).setManuallySelectRole(true);
    }

    public void removeContact(Task task, ReportIntent reportIntent) {
        reportIntentService.removeReportIntent(task, reportIntent);
        update(true);
    }

    /**
     * Adds a physician as contact
     *
     * @param physician
     * @param role
     */
    @Transient
    public void addPhysicianWithRole(Physician physician, ContactRole role) {
        try {
            reportIntentService.addReportIntent(getTask(), physician.getPerson(), role, true, true);
            // increment counter
            physicianService.incrementPhysicianPriorityCounter(physician.getPerson());
        } catch (IllegalArgumentException e) {
            logger.debug("Not adding, double contact");
            MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.contact.duplicated");
        }
    }

    /**
     * On dialog return, reload data
     *
     * @param event
     */
    public void onDefaultDialogReturn(SelectEvent event) {
        if (event.getObject() != null) {
            if (event.getObject() instanceof ReloadEvent) {
                update(true);
            } else if (event.getObject() instanceof SelectPhysicianReturnEvent) {
                addPhysicianWithRole(((SelectPhysicianReturnEvent) event.getObject()).getPhysician(),
                        ((SelectPhysicianReturnEvent) event.getObject()).getRole());
                update(true);
            }
        }
    }

    @Override
    public void hideDialog() {
        super.hideDialog(new ReloadEvent());
    }


}
