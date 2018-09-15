package com.patho.main.action.handler.views;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.Physician;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.ui.selectors.PhysicianSelector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class GenericView extends AbstractTaskView {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	/**
	 * List of all surgeons
	 */
	private List<PhysicianSelector> surgeons;

	/**
	 * List of all private physicians
	 */
	private List<PhysicianSelector> privatePhysicians;

	private PhysicianSelector selectedSurgeon;

	private PhysicianSelector selectedPrivatePhysician;

	public GenericView(GlobalEditViewHandler globalEditViewHandler) {
		super(globalEditViewHandler);
		// TODO Auto-generated constructor stub
	}

	public void loadView() {
		logger.debug("Loading contatcs");
		setSurgeons(PhysicianSelector.factory(getTask(), getGlobalEditViewHandler().getStaticData().getSurgeons()));
		setPrivatePhysicians(PhysicianSelector.factory(getTask(),
				getGlobalEditViewHandler().getStaticData().getPrivatePhysicians()));
	}
}
