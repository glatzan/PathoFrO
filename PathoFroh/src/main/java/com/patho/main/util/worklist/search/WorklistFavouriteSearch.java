package com.patho.main.util.worklist.search;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.PatientRepository;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class WorklistFavouriteSearch extends AbstractWorklistSearch {

	@Autowired
	private PatientRepository patientRepository;

	private FavouriteList favouriteList;

	@Override
	public List<Patient> getPatients() {
		logger.debug("Searching current worklist");

		List<Patient> patient = patientRepository.findAllByFavouriteList(favouriteList.getId(), true);

		// setting task within favourite list as active
		patient.forEach(p -> p.getTasks().forEach(z -> {
			if (z.isListedInFavouriteList(favouriteList))
				z.setActive(true);
		}));

		return patient;
	}
}
