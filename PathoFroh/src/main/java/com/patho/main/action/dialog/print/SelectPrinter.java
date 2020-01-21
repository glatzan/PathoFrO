package com.patho.main.action.dialog.print;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session")
@Setter
@Slf4j
public class SelectPrinter extends AbstractDialog {

    private HistoUser user;

    public void initAndPrepareBean(HistoUser user) {
        initBean(user);
        prepareDialog();
    }

    public void initBean(HistoUser user) {
        this.user = user;
        super.initBean(task, Dialog.PRINT_SELECT_PRINTER, false);
    }

}
