package com.patho.main.action.dialog.worklist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.export.ExportTasksDialog;
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

@Component
@Scope(value = "session")
@Getter
@Setter
public class WorklistSearchDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ExportTasksDialog exportTasksDialog;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

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
	
	private SimpleSearchTab simpleSearchTab;
	private FavouriteSearchTab favouriteSearchTab;
	private ExtendedSearchTab extendedSearchTab;

	public WorklistSearchDialog() {
		setSimpleSearchTab(new SimpleSearchTab());
		setFavouriteSearchTab(new FavouriteSearchTab());
		setExtendedSearchTab(new ExtendedSearchTab());
		tabs = new AbstractTab[] { simpleSearchTab, favouriteSearchTab, extendedSearchTab };
	}

	public void initAndPrepareBean() {
		initBean();
		prepareDialog();
	}

	public boolean initBean() {
		return super.initBean(Dialog.WORKLIST_SEARCH);
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
					userHandlerAction.getCurrentUser().getSettings().isWorklistHideNoneActiveTasks(),
					userHandlerAction.getCurrentUser().getSettings().getWorklistSortOrder(),
					userHandlerAction.getCurrentUser().getSettings().isWorklistAutoUpdate(), false,
					userHandlerAction.getCurrentUser().getSettings().isWorklistSortOrderAsc());
			return worklist;
		}
		
		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistReturnEvent(getWorklist()));
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
					userHandlerAction.getCurrentUser(), false, true, true, true, true, false);

			containers = list.stream().map(p -> new FavouriteListContainer(p, userHandlerAction.getCurrentUser()))
					.collect(Collectors.toList());
		}

		public Worklist getWorklist() {
			Worklist worklist = new Worklist("Default", worklistSearch,
					userHandlerAction.getCurrentUser().getSettings().isWorklistHideNoneActiveTasks(),
					userHandlerAction.getCurrentUser().getSettings().getWorklistSortOrder(),
					userHandlerAction.getCurrentUser().getSettings().isWorklistAutoUpdate(), true,
					userHandlerAction.getCurrentUser().getSettings().isWorklistSortOrderAsc());
			return worklist;
		}
		
		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistReturnEvent(getWorklist()));
		}
	}

	@Getter
	@Setter
	public class ExtendedSearchTab extends AbstractTab {

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
			setCaseHistoryList(listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.CASE_HISTORY,false));

			// Diagnosis presets
			setDiagnosisPresets(diagnosisPresetRepository.findAllByOrderByIndexInListAsc());

			// wardlist
			setWardList(listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.WARDS,false));

			return true;
		}

		public Worklist getWorklist() {
			Worklist worklist = new Worklist("Default", worklistSearch,
					userHandlerAction.getCurrentUser().getSettings().isWorklistHideNoneActiveTasks(),
					userHandlerAction.getCurrentUser().getSettings().getWorklistSortOrder(),
					userHandlerAction.getCurrentUser().getSettings().isWorklistAutoUpdate(), true,
					userHandlerAction.getCurrentUser().getSettings().isWorklistSortOrderAsc());
			return worklist;
		}
		
		public void hideDialogAndSelectItem() {
			WorklistSearchDialog.this.hideDialog(new WorklistReturnEvent(getWorklist()));
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

				worklistSearch.getStainings().addAll(((SlideSelectResult) event.getObject()).getPrototpyes());
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
	public class WorklistReturnEvent implements DialogReturnEvent{
		private Worklist worklist;
	}
}
