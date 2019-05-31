package com.patho.main.ui.selectors;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;

import java.util.List;

/**
 * Container for a staining prototype
 * 
 * @author andi
 *
 */
public class StainingPrototypeHolder extends StainingPrototype {

	private int count = 1;

	private StainingPrototype prototype;

	public StainingPrototypeHolder(StainingPrototype stainingPrototype) {
		this.prototype = stainingPrototype;
	}

	public int getCount() {
		return this.count;
	}

	public StainingPrototype getPrototype() {
		return this.prototype;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setPrototype(StainingPrototype prototype) {
		this.prototype = prototype;
	}

	public void setBatchDetails(List<StainingPrototypeDetails> batchDetails) {
		this.prototype.setBatchDetails(batchDetails);
	}

	public String getName() {
		return this.prototype.getName();
	}

	public void setCommentary(String commentary) {
		this.prototype.setCommentary(commentary);
	}

	public void setName(String name) {
		this.prototype.setName(name);
	}

	public void setPriorityCount(int priorityCount) {
		this.prototype.setPriorityCount(priorityCount);
	}

	public void setArchived(boolean archived) {
		this.prototype.setArchived(archived);
	}

	public List<StainingPrototypeDetails> getBatchDetails() {
		return this.prototype.getBatchDetails();
	}

	public String getCommentary() {
		return this.prototype.getCommentary();
	}

	public void setId(long id) {
		this.prototype.setId(id);
	}

	public StainingType getType() {
		return this.prototype.getType();
	}

	public long getId() {
		return this.prototype.getId();
	}

	public void setType(StainingType type) {
		this.prototype.setType(type);
	}

	public boolean isArchived() {
		return this.prototype.isArchived();
	}

	public int getPriorityCount() {
		return this.prototype.getPriorityCount();
	}
}