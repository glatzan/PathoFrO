package com.patho.main.action.dialog.print;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.util.dialog.event.CustomAddressSelectEvent;
import com.patho.main.util.ui.selector.ReportIntentSelector;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomAddressDialog extends AbstractDialog {

    private ReportIntentSelector contactContainer;

    private String customAddress;

    private boolean addressChanged;

    public CustomAddressDialog initAndPrepareBean(Task task, ReportIntentSelector contactContainer) {
        if (initBean(task, contactContainer))
            prepareDialog();
        return this;
    }

    public boolean initBean(Task task, ReportIntentSelector contactContainer) {
        this.contactContainer = contactContainer;

        customAddress = contactContainer.getCustomAddress();

        setAddressChanged(false);

        return super.initBean(task, Dialog.PRINT_ADDRESS, false);
    }

    public void onContentChange() {
        setAddressChanged(!getCustomAddress().equals(getContactContainer().getCustomAddress()));
    }

    public void selectAndHide() {
        hideDialog(new CustomAddressSelectEvent(customAddress, contactContainer));
    }

}
