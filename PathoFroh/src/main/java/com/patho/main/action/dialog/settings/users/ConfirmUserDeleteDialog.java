package com.patho.main.action.dialog.settings.users;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.user.HistoUser;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class ConfirmUserDeleteDialog extends AbstractDialog {

	private HistoUser user;

	private boolean deletePhysician;

	public ConfirmUserDeleteDialog initAndPrepareBean(HistoUser user) {
		if (initBean(user))
			prepareDialog();
		return this;
	}

	public boolean initBean(HistoUser user) {
		this.user = user;
		this.deletePhysician = false;
		return super.initBean(Dialog.SETTINGS_USERS_DELETE);
	}

	public void deleteAndHide() {
		hideDialog(new ConfirmUserDeleteEvent(true, isDeletePhysician()));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class ConfirmUserDeleteEvent implements DialogReturnEvent {
		boolean delete;
		boolean deletePhysician;
	}
}
