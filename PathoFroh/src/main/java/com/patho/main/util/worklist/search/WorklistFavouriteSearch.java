package com.patho.main.util.worklist.search;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.PatientRepository;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.task.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Getter
@Setter
public class WorklistFavouriteSearch extends AbstractWorklistSearch {

    private FavouriteList favouriteList;

    @Override
    public List<Patient> getPatients() {
        logger.debug("Searching current worklist");

        List<Patient> patient = SpringContextBridge.services().getPatientRepository().findAllByFavouriteList(favouriteList.getId(), true);

        // setting task within favourite list as active
        patient.forEach(p -> p.getTasks().forEach(z -> {
            if (TaskStatus.hasFavouriteLists(z, favouriteList))
                z.setActive(true);
        }));

        return patient;
    }
}
