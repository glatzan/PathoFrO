package com.patho.main.action.dialog;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.common.Dialog;

@Component
@Scope(value = "session")
//TODO move to common dialog handler for dialogs without logic
public class WorklistSettingsDialog extends AbstractDialog {

	public void initAndPrepareBeanForSorting() {
		super.initBean(null, Dialog.WORKLIST_ORDER);
		prepareDialog();
	}
		
	public void initAndPrepareBeanForSettings() {
		super.initBean(null, Dialog.WORKLIST_SETTINGS);
		prepareDialog();
	}

}
