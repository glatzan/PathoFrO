package com.patho.main.action.dialog.miscellaneous;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class ConfirmDialog extends AbstractDialog {

	private String headline;
	private String text;

	public ConfirmDialog initAndPrepareBean() {
		return initAndPrepareBean("", "");
	}

	public ConfirmDialog initAndPrepareBean(String healine, String text) {
		if (initBean(healine, text))
			prepareDialog();
		return this;
	}

	public boolean initBean(String healine, String text) {
		setHeadline(healine.equals("") ? "" : resourceBundle.get(headline));
		setText(text.equals("") ? "" : resourceBundle.get(text));
		return super.initBean(Dialog.CONFIRM_CHANGE);
	}

	public ConfirmDialog header(String text) {
		return header(text, "");
	}

	public ConfirmDialog header(String text, Object... args) {
		setHeadline(resourceBundle.get(text, args));
		return this;
	}

	public ConfirmDialog ctext(String text) {
		return ctext(text, "");
	}

	public ConfirmDialog ctext(String text, Object... args) {
		setText(resourceBundle.get(text, args));
		return this;
	}

	public void confirmAndHide() {
		hideDialog(new ConfirmEvent(true));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class ConfirmEvent implements DialogReturnEvent {
		private boolean confirm;
	}

}
