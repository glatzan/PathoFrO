package com.patho.main.action.dialog.settings.material;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.StainingPrototype;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.selectors.StainingPrototypeHolder;
import com.patho.main.util.dialog.event.ReloadEvent;
import com.patho.main.util.dialog.event.SlideSelectEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

@Getter
@Setter
public class MaterialEditDialog extends AbstractDialog {
    private boolean newMaterial;

    private MaterialPreset materialPreset;

    public MaterialEditDialog initAndPrepareBean() {
        return initAndPrepareBean(new MaterialPreset());
    }

    public MaterialEditDialog initAndPrepareBean(MaterialPreset material) {
        System.out.println(material);
        if (initBean(material))
            prepareDialog();
        return this;
    }

    public boolean initBean(MaterialPreset material) {
        setNewMaterial(material.getId() == 0);
        setMaterialPreset(material);
        return super.initBean(task, Dialog.SETTINGS_MATERIAL_EDIT);
    }

    public void saveAndHide() {
        SpringContextBridge.services().getMaterialPresetService().addOrUpdate(getMaterialPreset());
        hideDialog(new ReloadEvent());
    }

    public void removeStaining(StainingPrototype stainingPrototype) {
        materialPreset.getStainingPrototypes().remove(stainingPrototype);
    }

    /**
     * On dialog return, reload data
     */
    public void onDefaultDialogReturn(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
        } else if (event.getObject() != null && event.getObject() instanceof SlideSelectEvent) {
            SlideSelectEvent result = (SlideSelectEvent) event.getObject();
            for (StainingPrototypeHolder prototype : result.getObj()) {
                for (int i = 0; i < prototype.getCount(); i++) {
                    materialPreset.getStainingPrototypes().add(prototype.getPrototype());
                }
            }
        }
    }
}
