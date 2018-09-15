package com.patho.main.action.handler.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.patient.Task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public abstract class AbstractTaskView {

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;

	private GlobalEditViewHandler globalEditViewHandler;

	public AbstractTaskView(GlobalEditViewHandler globalEditViewHandler) {
		this.globalEditViewHandler = globalEditViewHandler;
	}

	public Task getTask() {
		return globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask();
	}

	public void setTask(Task task) {
		globalEditViewHandler.getWorklistData().getWorklist().setSelectedTask(task);
	}

	public abstract void loadView();

}
