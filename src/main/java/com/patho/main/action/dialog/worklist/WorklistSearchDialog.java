package com.patho.main.action.dialog.worklist;

import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.*;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.preset.*;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialog.event.SlideSelectEvent;
import com.patho.main.util.dialog.event.WorklistSelectEvent;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.WorklistFavouriteSearch;
import com.patho.main.util.worklist.search.WorklistSearchExtended;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WorklistSearchDialog extends AbstractTabDialog {

    private SimpleSearchTab simpleSearchTab;
    private FavouriteSearchTab favouriteSearchTab;
    private ExtendedSearchTab extendedSearchTab;

    public WorklistSearchDialog() {
        setSimpleSearchTab(new SimpleSearchTab());
        setFavouriteSearchTab(new FavouriteSearchTab());
        setExtendedSearchTab(new ExtendedSearchTab());
        tabs = new AbstractTab[]{simpleSearchTab, favouriteSearchTab, extendedSearchTab};

        // only setting tab on first dialog display
        setSelectedTab(simpleSearchTab);
    }

    public boolean initBean() {
        return super.initBean(Dialog.WORKLIST_SEARCH, false);
    }

    @Getter
    @Setter
    public class SimpleSearchTab extends AbstractTab {


        public SimpleSearchTab() {
            setTabName("SimpleSearchTab");
            setName("dialog.worklistsearch.simple.tabName");
            setViewID("simpleSearch");
            setCenterInclude("include/simpleSearch.xhtml");
        }

        @Override
        public boolean initTab() {
            return true;
        }

        public void onChangeWorklistSelection() {

        }

        public Worklist getWorklist() {
            Worklist worklist = new Worklist("Default", null,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrder(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistAutoUpdate(), false,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrderAsc());
            return worklist;
        }

        public void hideDialogAndSelectItem() {
            WorklistSearchDialog.this.hideDialog(new WorklistSelectEvent(getWorklist()));
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
            setName("dialog.worklistsearch.favouriteList.tabName");
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
            List<FavouriteList> list = SpringContextBridge.services().getFavouriteListRepository().findByUserAndWriteableAndReadable(
                    SpringContextBridge.services().getUserService().getCurrentUser(), false, true, true, true, true, false);

            containers = list.stream().map(p -> new FavouriteListContainer(p, SpringContextBridge.services().getUserService().getCurrentUser()))
                    .collect(Collectors.toList());
        }

        public Worklist getWorklist() {
            Worklist worklist = new Worklist("Default", null,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrder(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistAutoUpdate(), true,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrderAsc());
            return worklist;
        }

        public void hideDialogAndSelectItem() {
            WorklistSearchDialog.this.hideDialog(new WorklistSelectEvent(getWorklist()));
        }

        public void setSelectedContainer(FavouriteListContainer selectedContainer) {
            this.selectedContainer = selectedContainer;

            if (selectedContainer != null) {
                this.worklistSearch.setFavouriteList(selectedContainer.getFavouriteList());
            }
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
         * List of all reportIntent presets
         */
        private List<DiagnosisPreset> diagnosisPresets;

        /**
         * Contains all available wards
         */
        private List<ListItem> wardList;

        public ExtendedSearchTab() {
            setTabName("ExtendedSearchTab");
            setName("dialog.worklistsearch.scifi.tabName");
            setViewID("extendedSearch");
            setCenterInclude("include/extendedSearch.xhtml");
            setDisabled(true);
        }

        @Override
        public boolean initTab() {
            setWorklistSearch(new WorklistSearchExtended());

            // setting material list
            setMaterialList(SpringContextBridge.services().getMaterialPresetRepository().findAll(true));

            // setting physician list
            List<Physician> allPhysicians = SpringContextBridge.services().getPhysicianRepository().findAllByRole(ContactRole.values(), true);
            setAllPhysicians(allPhysicians.toArray(new Physician[allPhysicians.size()]));
            setAllPhysicianTransformer(new DefaultTransformer<>(getAllPhysicians()));

            // case history
            setCaseHistoryList(SpringContextBridge.services().getListItemRepository()
                    .findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.CASE_HISTORY, false));

            // Diagnosis presets
            setDiagnosisPresets(SpringContextBridge.services().getDiagnosisPresetRepository().findAllByOrderByIndexInListAsc());

            // wardlist
            setWardList(SpringContextBridge.services().getListItemRepository().findByListTypeAndArchivedOrderByIndexInListAsc(ListItemType.WARDS,
                    false));

            return true;
        }

        public Worklist getWorklist() {
            Worklist worklist = new Worklist("Default", null,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistHideNoneActiveTasks(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrder(),
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistAutoUpdate(), true,
                    SpringContextBridge.services().getUserService().getCurrentUser().getSettings().getWorklistSortOrderAsc());
            return worklist;
        }

        public void hideDialogAndSelectItem() {
            WorklistSearchDialog.this.hideDialog(new WorklistSelectEvent(getWorklist()));
        }

        public void exportWorklist() {
//			List<Task> tasks = taskDAO.getTaskByCriteria(getWorklistSearch(), true);
//			exportTasksDialog.initAndPrepareBean(tasks);
        }

        public void onSelectStainingDialogReturn(SelectEvent event) {
            logger.debug("On select staining dialog return " + event.getObject());

            if (event.getObject() != null && event.getObject() instanceof SlideSelectEvent) {

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
}
