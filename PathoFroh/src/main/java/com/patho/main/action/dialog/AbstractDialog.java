package com.patho.main.action.dialog;

import java.util.HashMap;

import javax.faces.event.AbortProcessingException;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.patho.main.action.MainHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.patient.Task;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.helper.UniqueRequestID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
public abstract class AbstractDialog<T extends AbstractDialog<?>> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected MainHandlerAction mainHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;

	protected Task task;

	protected Dialog dilaog;

	protected UniqueRequestID uniqueRequestID = new UniqueRequestID();

	public T initAndPrepareBean(Task dialog) {
		return null;
	}

	public T initAndPrepareBean(Dialog dialog) {
		initBean(null, dialog);
		prepareDialog();
		return null;
	}

	public T initAndPrepareBean(Task task, Dialog dialog) {
		initBean(task, dialog);
		prepareDialog();
		return null;
	}

	public boolean initBean(Task task, Dialog dialog) {
		return initBean(task, dialog, false);
	}

	public boolean initBean(Task task, Dialog dialog, boolean uniqueRequestEnabled) {
		setTask(task);
		setDilaog(dialog);
		getUniqueRequestID().setEnabled(uniqueRequestEnabled);
		if (uniqueRequestEnabled)
			getUniqueRequestID().nextUniqueRequestID();

		return true;
	}

	/**
	 * Method for displaying the associated dialog.
	 */

	public void prepareDialog() {
		prepareDialog(dilaog);
	}

	/**
	 * Method for displaying the associated dialog.
	 */
	public void prepareDialog(Dialog dialog) {
		HashMap<String, Object> options = new HashMap<String, Object>();

		if (dialog.getWidth() != 0) {
			options.put("width", dialog.getWidth());
			options.put("contentWidth", dialog.getWidth());
		} else
			options.put("width", "auto");

		if (dialog.getHeight() != 0) {
			options.put("contentHeight", dialog.getHeight());
			options.put("height", dialog.getHeight());
		} else
			options.put("height", "auto");

		if (dialog.isUseOptions()) {
			options.put("resizable", dialog.isResizeable());
			options.put("draggable", dialog.isDraggable());
			options.put("modal", dialog.isModal());
		}

		options.put("closable", false);

		if (dialog.getHeader() != null)
			options.put("headerElement", "dialogForm:header");

		PrimeFaces.current().dialog().openDynamic(dialog.getPath(), options, null);

		logger.debug("Showing Dialog: " + dialog);
	}

	/**
	 * Method for hiding the associated dialog.
	 * 
	 * @throws CustomNotUniqueReqest
	 */
	public void hideDialog() {
		hideDialog(null);
	}

	public void hideDialog(Object returnValue) {
		logger.debug("Hiding Dialog: " + getDilaog());
		PrimeFaces.current().dialog().closeDynamic(returnValue);
	}

}
