package com.patho.main.action.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.util.worklist.Worklist;

import lombok.Getter;
import lombok.Setter;

@Controller
@Scope("session")
@Getter
@Setter
public class WorklistHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Containing all worklists
	 */
	private List<Worklist> worklists = new ArrayList<Worklist>();

	/**
	 * Current worklist
	 */
	private Worklist current;

	/**
	 * Returns true if the given patient is selected
	 * 
	 * @param patient
	 * @return
	 */
	public boolean isSelected(Patient patient) {
		return current.isSelected(patient);
	}

	/**
	 * Returns true if the given task is selected
	 * 
	 * @param task
	 * @return
	 */
	public boolean isSelected(Task task) {
		return current.isSelected(task);
	}

	/**
	 * Returns the selected Patient
	 * 
	 * @return
	 */
	public Patient getSelectedPatient() {
		return current.getSelectedPatient();
	}

	/**
	 * Returns the selected Task
	 * 
	 * @return
	 */
	public Task getSelectedTask() {
		return current.getSelectedTask();
	}

	/**
	 * Returns true if the worklist is the current worklist
	 * 
	 * @param worklist
	 * @return
	 */
	public boolean isSelectedWorklist(Worklist worklist) {
		return worklist == current;
	}

	/**
	 * Performs a sort an sets the worklist as the current worklist
	 * 
	 * @param worklist
	 */
	public void setSelectedWorklist(Worklist worklist) {
		worklist.sortWordklist();
		worklists.add(worklist);
		setCurrent(worklist);
	}

	/**
	 * Realoads the compete worklist
	 */
	public void reloadWorklist() {
		reloadWorklist(true);
	}

	/**
	 * Realoads the compete worklist. If updateSelectedPatient the currently selecte
	 * patient will be updated as well.
	 */
	public void reloadWorklist(boolean updateSelectedPatient) {
		current.updateWorklist(updateSelectedPatient);
	}

	// TODO implement
	public void autoReloadCurrentWorklist() {
		if (current.isAutoUpdate()) {
			logger.debug("Auto updating worklist");
			reloadWorklist(false);
		}
	}
}
