package com.patho.main.action.dialog.miscellaneous;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.dto.AccountingData;
import com.patho.main.service.impl.SpringContextBridge;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class AccountingDataDialog extends AbstractDialog {

    private LocalDate fromDate;
    private LocalDate toDate;

    private List<AccountingData> accountingData;

    private boolean advancedData;

    public AccountingDataDialog initAndPrepareBean() {
        if (initBean())
            prepareDialog();

        this.advancedData = false;

        return this;
    }

    public boolean initBean() {
        super.initBean(task, Dialog.ACCOUNTING_DATA);
        return true;
    }

    public void loadAccountingDate() {

        logger.debug("Loading accounting-data form {} to {}", fromDate, toDate);

        if (fromDate == null || toDate == null) {
            setAccountingData(null);
        } else {
            setAccountingData(SpringContextBridge.services().getAccountingDataRepository().findAllBetweenDates(this.fromDate, this.toDate));
            MessageHandler.sendGrowlMessagesAsResource("growl.accounting.listLoaded");
        }
    }

    public String getExportFileName() {
        StringBuilder builder = new StringBuilder();
        builder.append("Export");

        if (fromDate != null) {
            builder.append("_" + this.fromDate.format(DateTimeFormatter.ofPattern("yyy-MM-dd")));
        }

        if (fromDate != null) {
            builder.append("_" + this.toDate.format(DateTimeFormatter.ofPattern("yyy-MM-dd")));
        }
        builder.append(".xls");

        return builder.toString();
    }
}
