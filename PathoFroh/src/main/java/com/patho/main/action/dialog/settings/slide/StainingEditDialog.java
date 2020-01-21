package com.patho.main.action.dialog.settings.slide;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StainingEditDialog extends AbstractDialog {

    private boolean newStaining;

    private StainingPrototype stainingPrototype;

    private List<StainingPrototypeDetails> removeDetails;

    public StainingEditDialog initAndPrepareBean() {
        return initAndPrepareBean(new StainingPrototype());
    }

    public StainingEditDialog initAndPrepareBean(StainingPrototype staining) {
        if (initBean(staining))
            prepareDialog();
        return this;
    }

    public boolean initBean(StainingPrototype staining) {

        // reloading stating with batch details
        if (staining.getId() != 0) {
            setStainingPrototype(
                    SpringContextBridge.services().getStainingPrototypeRepository().findOptionalByIdAndInitilize(staining.getId(), true).get());
            setNewStaining(false);
        } else {
            setNewStaining(true);
            setStainingPrototype(staining);
        }

        setRemoveDetails(new ArrayList<StainingPrototypeDetails>());

        return super.initBean(task, Dialog.SETTINGS_STAINING_EDIT);
    }

    public void addBatch() {
        StainingPrototypeDetails newBatch = new StainingPrototypeDetails(stainingPrototype);
        newBatch.setDeliveryDate(new Date(System.currentTimeMillis()));
        getStainingPrototype().getBatchDetails().add(0, newBatch);
    }

    public void removeBatch(StainingPrototypeDetails stainingPrototypeDetails) {
        stainingPrototype.getBatchDetails().remove(stainingPrototypeDetails);

        // delete if user clicks save
        if (stainingPrototypeDetails.getId() != 0) {
            getRemoveDetails().add(stainingPrototypeDetails);
        }
    }

    public void cloneBatch(StainingPrototypeDetails stainingPrototypeDetails) {
        try {
            StainingPrototypeDetails clone = stainingPrototypeDetails.clone();
            clone.setId(0);
            stainingPrototype.getBatchDetails().add(0, clone);
        } catch (CloneNotSupportedException e) {
        }
    }

    public void saveAndHide() {
        save();
        hideDialog(new ReloadEvent());
    }

    private void save() {
        SpringContextBridge.services().getStainingPrototypeService().addOrUpdate(getStainingPrototype(), getRemoveDetails());
    }
}
