package com.patho.main.action.dialog.miscellaneous;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.dto.AccountingData;
import com.patho.main.repository.AccountingDataRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class AccountingDataDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AccountingDataRepository accountingDataRepository;

	private Date fromDate;
	private Date toDate;

	private List<AccountingData> accountingData;

	public AccountingDataDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();

		return this;
	}

	public boolean initBean() {
		super.initBean(task, Dialog.ACCOUNTING_DATA);
		return true;
	}

	public void loadAccountingDate() {
		if (fromDate == null || toDate == null) {
			setAccountingData(null);
			return;
		}
		
		setAccountingData(accountingDataRepository.findAllBetweenDates(this.fromDate, this.toDate));
	}
	
	public String getExportFileName() {
		StringBuilder builder = new StringBuilder();
		builder.append("Export");
		
		DateFormat df = new SimpleDateFormat("yyy-MM-dd");
		
		if(fromDate != null) {
			builder.append("_"+df.format(this.fromDate));
		}
		
		if(fromDate != null) {
			builder.append("_"+df.format(this.toDate));
		}
		builder.append(".xls");
		
		return builder.toString();
	}
}
