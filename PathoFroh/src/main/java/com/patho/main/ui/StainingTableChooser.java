package com.patho.main.ui;

import java.util.ArrayList;
import java.util.List;

import com.patho.main.model.interfaces.IdManuallyAltered;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class StainingTableChooser<T extends IdManuallyAltered & PatientRollbackAble<?>> {

	private boolean choosen;
	private boolean even;

	private boolean idChanged;

	private T entity;

	private List<StainingTableChooser<?>> children;

	public StainingTableChooser(T entity, boolean even) {
		this.entity = entity;
		this.even = even;
		children = new ArrayList<StainingTableChooser<?>>();
	}

	public void setChildren(List<StainingTableChooser<?>> children) {
		this.children = children;
	}

	public void addChild(StainingTableChooser<?> child) {
		getChildren().add(child);
	}

	public boolean isSampleType() {
		return entity instanceof Sample;
	}

	public boolean isBlockType() {
		return entity instanceof Block;
	}

	public boolean isStainingType() {
		return entity instanceof Slide;
	}

	public void setIDText(String text) {

		// prevent null errors
		if (text == null)
			text = "";

		log.debug("Updating text to " + text);

		if (isSampleType()) {
			if (!text.equals(((Sample) entity).getSampleID())) {
				setIdChanged(true);
				log.debug("Text chagned");
			}

			((Sample) entity).setSampleID(text);
			return;
		}

		if (isBlockType()) {
			if (!text.equals(((Block) entity).getBlockID())) {
				setIdChanged(true);
				log.debug("Text chagned");
			}
			((Block) entity).setBlockID(text);
			return;
		}

		if (isStainingType()) {
			if (!text.equals(((Slide) entity).getSlideID())) {
				setIdChanged(true);
				log.debug("Text chagned");
			}
			((Slide) entity).setSlideID(text);
			return;
		}
	}

	public String getIDText() {
		if (isSampleType())
			return ((Sample) entity).getSampleID();
		if (isBlockType())
			return ((Block) entity).getBlockID();
		if (isStainingType())
			return ((Slide) entity).getSlideID();

		return "";
	}

	/**
	 * Creates linear list of all slides of the given task. The StainingTableChosser
	 * is used as holder class in order to offer an option to select the slides by
	 * clicking on a checkbox. Archived elements will not be shown if showArchived
	 * is false.
	 * 
	 * @param showArchived
	 */
	public static ArrayList<StainingTableChooser<?>> factory(Task task, boolean showArchived) {
		log.debug("Generating new List for receiptlog");

		ArrayList<StainingTableChooser<?>> result = new ArrayList<StainingTableChooser<?>>();

		boolean even = false;

		for (Sample sample : task.getSamples()) {
			// skips archived tasks

			StainingTableChooser<Sample> sampleChooser = new StainingTableChooser<Sample>(sample, even);
			result.add(sampleChooser);

			for (Block block : sample.getBlocks()) {
				// skips archived blocks

				StainingTableChooser<Block> blockChooser = new StainingTableChooser<Block>(block, even);
				result.add(blockChooser);
				sampleChooser.addChild(blockChooser);

				for (Slide staining : block.getSlides()) {
					// skips archived sliedes

					StainingTableChooser<Slide> stainingChooser = new StainingTableChooser<Slide>(staining, even);
					result.add(stainingChooser);
					blockChooser.addChild(stainingChooser);
				}
			}

			even = !even;

		}
		return result;
	}
}
