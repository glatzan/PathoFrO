package com.patho.main.action.handler.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Physician;
import com.patho.main.repository.DiagnosisPresetRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.ui.selectors.PhysicianSelector;
import com.patho.main.ui.transformer.DefaultTransformer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class StaticData {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisPresetRepository diagnosisPresetRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetRepository materialPresetRepository;

	/**
	 * Contains all available case histories
	 */
	private List<ListItem> slideCommentary;

	/**
	 * List of all diagnosis presets
	 */
	private List<DiagnosisPreset> diagnosisPresets;

	/**
	 * List of physicians which have the role signature
	 */
	private List<Physician> physiciansToSignList;

	/**
	 * Transfomer for physiciansToSign
	 */
	private DefaultTransformer<Physician> physiciansToSignListTransformer;

	/**
	 * List of available materials
	 */
	private List<MaterialPreset> materialList;

	/**
	 * Contains all available case histories
	 */
	private List<ListItem> caseHistoryList;

	/**
	 * selected List item form caseHistory list
	 */
	private ListItem selectedCaseHistoryItem;

	/**
	 * Contains all available wards
	 */
	private List<ListItem> wardList;

	/**
	 * List of all surgeons
	 */
	private List<Physician> surgeons;

	/**
	 * List of all private physicians
	 */
	private List<Physician> privatePhysicians;

	public void setPhysiciansToSignList(List<Physician> physicians) {
		this.physiciansToSignList = physicians;
		this.physiciansToSignListTransformer = new DefaultTransformer<Physician>(physicians);
	}

	/**
	 * Loading all data from Backend
	 */
	public void updateData() {
		// resetting selected values
		setSelectedCaseHistoryItem(null);

		setSlideCommentary(
				listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false));
		setCaseHistoryList(listItemRepository
				.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.CASE_HISTORY, false));
		setWardList(
				listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.WARDS, false));
		setDiagnosisPresets(diagnosisPresetRepository.findAllByOrderByIndexInListAsc());

		setPhysiciansToSignList(physicianRepository.findAllByRole(ContactRole.SIGNATURE, true));

		setMaterialList(materialPresetRepository.findAll(true));

		setSurgeons(
				physicianRepository.findAllByRole(new ContactRole[] { ContactRole.SURGEON }, true, SortOrder.PRIORITY));

		setPrivatePhysicians(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.PRIVATE_PHYSICIAN },
				true, SortOrder.PRIORITY));

	}
}
