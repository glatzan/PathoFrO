package com.patho.main.ui.selectors;

import com.patho.main.model.StainingPrototype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * Container for a staining prototype
 * 
 * @author andi
 *
 */
@Getter
@Setter
public class StainingPrototypeHolder extends StainingPrototype {

	private int count = 1;

	@Delegate
	private StainingPrototype prototype;

	public StainingPrototypeHolder(StainingPrototype stainingPrototype) {
		this.prototype = stainingPrototype;
	}
}