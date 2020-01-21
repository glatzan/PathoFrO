package com.patho.main.action.dialog.slides;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlideNamingDialog extends AbstractDialog {

    private boolean useAutoNomeclature;

    private boolean dataChanged;

    public SlideNamingDialog initAndPrepareBean(Task task) {
        if (initBean(task))
            prepareDialog();

        return this;
    }

    public boolean initBean(Task task) {
        super.initBean(task, Dialog.SLIDE_NAMING);
        setUseAutoNomeclature(task.getUseAutoNomenclature());
        setDataChanged(false);
        return true;
    }

    public void saveAndHide() {
        task.setUseAutoNomenclature(isUseAutoNomeclature());
        hideDialog(new TaskReloadEvent(SpringContextBridge.services().getTaskRepository().save(task, resourceBundle.get("log.patient.task.update"))));
        MessageHandler.sendGrowlMessagesAsResource("growl.task.saved", "growl.task.saved.text");
    }

    public void renameAndHide(boolean ignoreManuallyChangedEntities) {
        hideDialog(
                new TaskReloadEvent(SpringContextBridge.services().getTaskService().updateNamesOfTaskEntities(task, ignoreManuallyChangedEntities, true)));
        MessageHandler.sendGrowlMessagesAsResource("growl.task.updateNaming", "growl.task.updateNaming.text");
    }
}
