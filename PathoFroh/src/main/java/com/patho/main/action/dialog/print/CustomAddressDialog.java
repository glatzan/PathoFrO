package com.patho.main.action.dialog.print;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.util.dialogReturn.DialogReturnEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class CustomAddressDialog extends AbstractDialog<CustomAddressDialog> {

	private ContactSelector contactContainer;

	private String customAddress;

	private boolean addressChanged;

	public CustomAddressDialog initAndPrepareBean(Task task, ContactSelector contactContainer) {
		if (initBean(task, contactContainer))
			prepareDialog();
		return this;
	}

	public boolean initBean(Task task, ContactSelector contactContainer) {
		this.contactContainer = contactContainer;

		customAddress = contactContainer.getCustomAddress();

		setAddressChanged(false);

		return super.initBean(task, Dialog.PRINT_ADDRESS, false);
	}

	public void onContentChange() {
		setAddressChanged(!getCustomAddress().equals(getContactContainer().getCustomAddress()));
	}

	public void selectAndHide() {
		hideDialog(new CustomAddressReturn(customAddress,contactContainer));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class CustomAddressReturn implements DialogReturnEvent {
		private String customAddress;
		private ContactSelector contactSelector;
	}
}
