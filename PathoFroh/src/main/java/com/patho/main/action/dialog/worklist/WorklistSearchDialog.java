package com.patho.main.action.dialog.worklist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.patho.main.service.UserService;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.slides.AddSlidesDialog.SlideSelectResult;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.ListItem;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Physician;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.repository.DiagnosisPresetRepository;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.WorklistFavouriteSearch;
import com.patho.main.util.worklist.search.WorklistSearchExtended;
import com.patho.main.util.worklist.search.WorklistSimpleSearch;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class WorklistSearchDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	private SimpleSearchTab simpleSearchTab;
	private FavouriteSearchTab favouriteSearchTab;
	private ExtendedSearchTab extendedSearchTab;

	public WorklistSearchDialog() {
		setSimpleSearchTab(new SimpleSearchTab());
		setFavouriteSearchTab(new FavouriteSearchTab());
		setExtendedSearchTab(new ExtendedSearchTab());
		tabs = new AbstractTab[] { simpleSearchTab, favouriteSearchTab, extendedSearchTab };

		// only setting tab on first dialog display
		setSelectedTab(simpleSearchTab);
	}

	public boolean initBean() {
		return super.initBean(Dialog.WORKLIST_SEARCH, false);
	}

	@Getter
	@Setter
	public class SimpleSearchTab extends AbstractTab {

		private WorklistSimpleSearch worklistSearch;

		public SimpleSearchTab() {
			setTabName("SimpleSearchTab");
			setName("dialog.worklistsearch.simple");
			setViewID("simpleSearch");
			setCenterInclude("include/simpleSearch.xhtml");
		}

		@Override
		public boolean initTab() {
			setWorklistSearch(new WorklistSimpleSearch());
			return true;
		}

		public void onChangeWorklistSelection() {
			worklistSearch.setSearchIndex(SimpleSearchOption.CUSTOM_LIST);
		}

		public Worklist getWorklist() {
			Worklist worklist = new Worklist("Default", worklistSearch,
					userService.getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
					userService.getCurrentUser().getSettings().getWorklistSortOrder(),
					userService.getCurrentUser().getSettings().getWorklistAutoUpdate(), false,
					userService.getCurrentUser().getSettings().getWorklistSortOrderAsc());
			return worklist;
		}

		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistSearchReturnEvent(getWorklist()));
		}
	}

	@Getter
	@Setter
	public class FavouriteSearchTab extends AbstractTab {

		private WorklistFavouriteSearch worklistSearch;

		private List<FavouriteListContainer> containers;

		private FavouriteListContainer selectedContainer;

		public FavouriteSearchTab() {
			setTabName("FavouriteSearchTab");
			setName("dialog.worklistsearch.favouriteList");
			setViewID("favouriteListSearch");
			setCenterInclude("include/favouriteSearch.xhtml");
		}

		@Override
		public boolean initTab() {
			setWorklistSearch(new WorklistFavouriteSearch());
			setSelectedContainer(null);
			return true;
		}

		@Override
		public void updateData() {
			List<FavouriteList> list = favouriteListRepository.findByUserAndWriteableAndReadable(
					userService.getCurrentUser(), false, true, true, true, true, false);

			containers = list.stream().map(p -> new FavouriteListContainer(p, userService.getCurrentUser()))
					.collect(Collectors.toList());
		}

		public Worklist getWorklist() {
			Worklist worklist = new Worklist("Default", worklistSearch,
					userService.getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
					userService.getCurrentUser().getSettings().getWorklistSortOrder(),
					userService.getCurrentUser().getSettings().getWorklistAutoUpdate(), true,
					userService.getCurrentUser().getSettings().getWorklistSortOrderAsc());
			return worklist;
		}

		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistSearchReturnEvent(getWorklist()));
		}

		public void setSelectedContainer(FavouriteListContainer selectedContainer) {
			this.selectedContainer = selectedContainer;

			if (selectedContainer != null) {
				this.worklistSearch.setFavouriteList(selectedContainer.getFavouriteList());
			}
		}
	}

	@Configurable
	@Getter
	@Setter
	public class ExtendedSearchTab extends AbstractTab {

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private MaterialPresetRepository materialPresetRepository;

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

		private WorklistSearchExtended worklistSearch;

		/**
		 * List of available materials
		 */
		private List<MaterialPreset> materialList;

		/**
		 * Physician list
		 */
		private Physician[] allPhysicians;

		/**
		 * Transformer for physicians
		 */
		private DefaultTransformer<Physician> allPhysicianTransformer;

		/**
		 * Contains all available case histories
		 */
		private List<ListItem> caseHistoryList;

		/**
		 * List of all diagnosis presets
		 */
		private List<DiagnosisPreset> diagnosisPresets;

		/**
		 * Contains all available wards
		 */
		private List<ListItem> wardList;

		public ExtendedSearchTab() {
			setTabName("ExtendedSearchTab");
			setName("dialog.worklistsearch.scifi");
			setViewID("extendedSearch");
			setCenterInclude("include/extendedSearch.xhtml");
			setDisabled(true);
		}

		@Override
		public boolean initTab() {
			setWorklistSearch(new WorklistSearchExtended());

			// setting material list
			setMaterialList(materialPresetRepository.findAll(true));

			// setting physician list
			List<Physician> allPhysicians = physicianRepository.findAllByRole(ContactRole.values(), true);
			setAllPhysicians(allPhysicians.toArray(new Physician[allPhysicians.size()]));
			setAllPhysicianTransformer(new DefaultTransformer<>(getAllPhysicians()));

			// case history
			setCaseHistoryList(listItemRepository
					.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.CASE_HISTORY, false));

			// Diagnosis presets
			setDiagnosisPresets(diagnosisPresetRepository.findAllByOrderByIndexInListAsc());

			// wardlist
			setWardList(listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.WARDS,
					false));

			return true;
		}

		public Worklist getWorklist() {
			Worklist worklist = new Worklist("Default", worklistSearch,
					userService.getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
					userService.getCurrentUser().getSettings().getWorklistSortOrder(),
					userService.getCurrentUser().getSettings().getWorklistAutoUpdate(), true,
					userService.getCurrentUser().getSettings().getWorklistSortOrderAsc());
			return worklist;
		}

		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistSearchReturnEvent(getWorklist()));
		}

		public void exportWorklist() {
//			List<Task> tasks = taskDAO.getTaskByCriteria(getWorklistSearch(), true);
//			exportTasksDialog.initAndPrepareBean(tasks);
		}

		public void onSelectStainingDialogReturn(SelectEvent event) {
			logger.debug("On select staining dialog return " + event.getObject());

			if (event.getObject() != null && event.getObject() instanceof SlideSelectResult) {

				if (worklistSearch.getStainings() == null)
					worklistSearch.setStainings(new ArrayList<StainingPrototype>());

				// worklistSearch.getStainings().addAll(((SlideSelectResult)
				// event.getObject()).getPrototpyes());
			}
		}
	}

	public Worklist extendedSearch() {

		logger.debug("Calling extended search");

		return null;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public class WorklistSearchReturnEvent implements DialogReturnEvent {
		private Worklist worklist;
	}
}
