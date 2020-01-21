package com.patho.main.action.dialog.settings.organization;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Organization;
import com.patho.main.model.person.Person;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.OrganizationSelectEvent;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrganizationEditDialog extends AbstractDialog {

    private Organization organization;

    private boolean newOrganization;

    /**
     * Stores persons to remove from organization when saving
     */
    private List<Person> removeFromOrganization;

    public OrganizationEditDialog initAndPrepareBean() {
        return initAndPrepareBean(new Organization(new Contact()));
    }

    public OrganizationEditDialog initAndPrepareBean(Organization organization) {
        if (initBean(organization))
            prepareDialog();
        return this;
    }

    public boolean initBean(Organization organization) {

        if (organization.getId() == 0) {
            setOrganization(organization);
            setNewOrganization(true);
        } else {
            setOrganization(SpringContextBridge.services().getOrganizationRepository().findOptionalByID(organization.getId(), true).get());
            setNewOrganization(false);
        }

        setRemoveFromOrganization(new ArrayList<Person>());

        return super.initBean(task, Dialog.SETTINGS_ORGANIZATION_EDIT);
    }

    public void selectAndHide() {
        save();
        hideDialog(new OrganizationSelectEvent(organization));
    }

    public void saveAndHide() {
        save();
        hideDialog(new ReloadEvent());
    }

    /**
     * Saves an edited physician to the database
     */
    private void save() {
        organization = SpringContextBridge.services().getOrganizationService().addOrUpdate(organization);

        for (Person person : removeFromOrganization) {
            SpringContextBridge.services().getPersonRepository().save(person, resourceBundle.get("log.person.organization.remove", person, organization));
        }
    }

    /**
     * Removes a organization temporary from the person and adds it to an array in
     * order to save the change when the user clicks on the save button.
     *
     * @param person
     * @param organization
     */
    public void removePersonFromOrganization(Person person, Organization organization) {
        SpringContextBridge.services().getOrganizationService().removePerson(organization, person, false);
        getRemoveFromOrganization().add(person);
    }

}
