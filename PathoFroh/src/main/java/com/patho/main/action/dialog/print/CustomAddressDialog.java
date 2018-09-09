package com.patho.main.action.dialog.print;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.ui.selectors.ContactSelector;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class CustomAddressDialog extends AbstractDialog {

	private ContactSelector contactContainer;

	private String customAddress;

	private boolean addressChanged;

	public void initAndPrepareBean(Task task, ContactSelector contactContainer) {
		initBean(task, contactContainer);
		prepareDialog();
	}

	public void initBean(Task task, ContactSelector contactContainer) {
		this.contactContainer = contactContainer;

		customAddress = contactContainer.getCustomAddress();

		setAddressChanged(false);

		super.initBean(task, Dialog.PRINT_ADDRESS, false);
	}

	public void copyCustomAddress() {
		if (!getCustomAddress().equals(getContactContainer().getCustomAddress()))
			setAddressChanged(true);

		getContactContainer().setCustomAddress(getCustomAddress());
	}

}
