package com.patho.main.action.handler.views;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Physician;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.DiagnosisPresetRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.ui.selectors.PhysicianSelector;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.helper.TimeUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class DiagnosisView extends AbstractTaskView {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	public DiagnosisView(GlobalEditViewHandler globalEditViewHandler) {
		super(globalEditViewHandler);
	}

	public void loadView() {
		logger.debug("Loading diangosis data");

		for (DiagnosisRevision revision : getTask().getDiagnosisRevisions()) {
			if (revision.getCompleted()) {
				revision.setSignatureDate(LocalDate.now());

				if (revision.getSignatureOne() == null)
					revision.setSignatureOne(new Signature());

				if (revision.getSignatureTwo() == null)
					revision.setSignatureTwo(new Signature());

				if (revision.getSignatureOne().getPhysician() == null
						|| revision.getSignatureTwo().getPhysician() == null) {
					// TODO set if physician to the left, if consultant to the right
				}
			}
		}
	}
}
