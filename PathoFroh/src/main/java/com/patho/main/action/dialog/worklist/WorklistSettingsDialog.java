package com.patho.main.action.dialog.worklist;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Configurable;

@Getter
@Setter
public class WorklistSettingsDialog extends AbstractDialog {
	@Override
	public boolean initBean() {
		return super.initBean(Dialog.WORKLIST_SETTINGS);
	}
}
