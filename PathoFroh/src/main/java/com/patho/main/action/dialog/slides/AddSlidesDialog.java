package com.patho.main.action.dialog.slides;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.ListItem;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.StainingPrototypeRepository;
import com.patho.main.service.SlideService;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.selectors.StainingPrototypeHolder;
import com.patho.main.util.dialog.event.SlideSelectEvent;
import com.patho.main.util.dialog.event.StainingPhaseUpdateEvent;
import com.patho.main.util.task.TaskStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AddSlidesDialog extends AbstractDialog {

    /**
     * Current block for which the slides are created
     */
    private Block block;

    /**
     * Commentary for the slides
     */
    private String commentary;

    /**
     * Slide lable text
     */
    private String slideLabelText;

    /**
     * True if block is null, so only stainings can be selected
     */
    private boolean selectMode;

    /**
     * True if the slides are restainings
     */
    private boolean restaining;

    /**
     * The slides will be marked as completed
     */
    private boolean asCompleted;

    /**
     * Tab container
     */
    private List<StainingTypeContainer> container;

    /**
     * Contains all available case histories
     */
    private List<ListItem> slideLabelTexts;

    /**
     * Initializes the dialog for selecting stainings.
     */
    public AddSlidesDialog initAndPrepareBean() {
        return initAndPrepareBean((Block) null);
    }

    /**
     * Initializes the bean and shows the dialog for creating slides
     */
    public AddSlidesDialog initAndPrepareBean(Block block) {
        if (initBean(block))
            prepareDialog();
        return this;
    }

    /**
     * Initializes all field of the object
     */
    public boolean initBean(Block block) {

        setBlock(block);

        setCommentary("");

        setAsCompleted(false);

        setSlideLabelText("");

        setSlideLabelTexts(
                SpringContextBridge.services().getListItemRepository().findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false));

        setSelectMode(block == null);

        if (!isSelectMode())
            setRestaining(TaskStatus.hasFavouriteLists(block.getTask(), PredefinedFavouriteList.DiagnosisList,
                    PredefinedFavouriteList.ReDiagnosisList) || TaskStatus.checkIfReStainingFlag(block.getParent()));

        setContainer(new ArrayList<StainingTypeContainer>());

        // adding tabs dynamically
        for (StainingType type : StainingType.values()) {
            getContainer().add(new StainingTypeContainer(type,
                    SpringContextBridge.services().getStainingPrototypeRepository().findAllByTypeOrderByPriorityCountDesc(type).stream()
                            .map(p -> new StainingPrototypeHolder(p)).collect(Collectors.toList())));
        }

        return super.initBean(Dialog.SLIDE_CREATE);
    }

    /**
     * True if at least one staining is selected
     *
     * @return
     */
    public boolean isStainingSelected() {
        return container.stream()
                .anyMatch(p -> p.getSelectedPrototypes() != null && !p.getSelectedPrototypes().isEmpty());
    }

    private List<StainingPrototypeHolder> getSelectedPrototypeHolders() {
        // adding all selected prototypes to the result
        List<StainingPrototypeHolder> prototpyes = new ArrayList<StainingPrototypeHolder>();

        getContainer().stream().map(p -> p.getSelectedPrototypes()).collect(Collectors.toList())
                .forEach(p -> prototpyes.addAll(p));

        return prototpyes;
    }

    /**
     * Hides the dialog and returns the selection
     */
    public void addAndHide() {
        // adding all selected prototypes to the result
        List<StainingPrototypeHolder> prototpyes = getSelectedPrototypeHolders();

        if (!prototpyes.isEmpty()) {
            Task task = SpringContextBridge.services().getSlideService().createSlidesXTimes(prototpyes, block, slideLabelText, commentary, restaining, true,
                    asCompleted, true);
            hideDialog(new StainingPhaseUpdateEvent(task));
        }
    }

    public void selectAndHide() {
        // adding all selected prototypes to the result
        List<StainingPrototypeHolder> prototpyes = getSelectedPrototypeHolders();

        hideDialog(new SlideSelectEvent(prototpyes));
    }

    /**
     * Class for separating different staining types
     *
     * @author andi
     */
    @Getter
    @Setter
    public static class StainingTypeContainer {
        private StainingType type;
        private List<StainingPrototypeHolder> prototpyes;
        private List<StainingPrototypeHolder> selectedPrototypes;

        public StainingTypeContainer(StainingType type, List<StainingPrototypeHolder> prototypes) {
            this.type = type;
            this.prototpyes = prototypes;
            this.selectedPrototypes = new ArrayList<StainingPrototypeHolder>();
        }
    }

}
