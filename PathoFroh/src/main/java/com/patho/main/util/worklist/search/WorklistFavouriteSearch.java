package com.patho.main.util.worklist.search;

import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Patient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class WorklistFavouriteSearch extends AbstractWorklistSearch {

	private FavouriteList favouriteList;

	@Override
	public List<Patient> getPatients() {
		log.debug("Searching current worklist");

		List<Patient> patient = favouriteListDAO.getPatientFromFavouriteList(favouriteList.getId(), true);

		// setting task within favourite list as active
		patient.forEach(p -> p.getTasks().forEach(z -> {
			if (z.isListedInFavouriteList(favouriteList))
				z.setActive(true);
		}));
		
		return patient;
	}
}
