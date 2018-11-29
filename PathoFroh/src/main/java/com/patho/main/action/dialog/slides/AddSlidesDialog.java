package com.patho.main.action.dialog.slides;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.ListItem;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.StainingPrototypeRepository;
import com.patho.main.service.SlideService;
import com.patho.main.ui.task.TaskStatus;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.StainingPhaseUpdateEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class AddSlidesDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private StainingPrototypeRepository stainingPrototypeRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SlideService slideService;

	/**
	 * Current block for which the slides are created
	 */
	private Block block;

	/**
	 * Commentary for the slides
	 */
	private String commentary;

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
	private List<ListItem> slideCommentary;

	/**
	 * Initializes the dialog for selecting stainings.
	 */
	public AddSlidesDialog initAndPrepareBean() {
		return initAndPrepareBean((Block)null);
	}

	/**
	 * Initializes the bean and shows the dialog for creating slides
	 * 
	 * @param patient
	 */
	public AddSlidesDialog initAndPrepareBean(Block block) {
		if (initBean(block))
			prepareDialog();
		return this;
	}

	/**
	 * Initializes all field of the object
	 * 
	 * @param task
	 */
	public boolean initBean(Block block) {
		super.initBean(null, Dialog.SLIDE_CREATE);

		setBlock(block);

		setCommentary("");

		setAsCompleted(false);

		setSlideCommentary(
				listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false));

		setSelectMode(block == null);

		if (!isSelectMode())
			setRestaining(block.getTask().isListedInFavouriteList(PredefinedFavouriteList.DiagnosisList,
					PredefinedFavouriteList.ReDiagnosisList) || TaskStatus.checkIfReStainingFlag(block.getParent()));

		setContainer(new ArrayList<StainingTypeContainer>());

		// adding tabs dynamically
		for (StainingType type : StainingType.values()) {
			getContainer().add(new StainingTypeContainer(type,
					stainingPrototypeRepository.findAllByTypeOrderByPriorityCountDesc(type).stream()
							.map(p -> new StainingPrototypeHolder(p)).collect(Collectors.toList())));
		}

		return true;
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
			Task task = slideService.createSlidesXTimesAndPersist(prototpyes, block, commentary, restaining, true,
					asCompleted);
			hideDialog(new StainingPhaseUpdateEvent(task));
		}
	}

	public void selectAndHide() {
		// adding all selected prototypes to the result
		List<StainingPrototypeHolder> prototpyes = getSelectedPrototypeHolders();

		hideDialog(new SlideSelectResult(prototpyes));
	}

	/**
	 * Class for separating different staining types
	 * 
	 * @author andi
	 *
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

	/**
	 * Container for a staining prototype
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	public static class StainingPrototypeHolder {
		private int count = 1;
		private StainingPrototype prototype;

		public StainingPrototypeHolder(StainingPrototype stainingPrototype) {
			this.prototype = stainingPrototype;
		}
	}

	/**
	 * Return result, as a single object for passing via select event
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	public class SlideSelectResult implements DialogReturnEvent {
		private List<StainingPrototypeHolder> prototpyes;

		public SlideSelectResult(List<StainingPrototypeHolder> prototypes) {
			this.prototpyes = prototypes;

		}
	}
}
