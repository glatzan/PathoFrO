package com.patho.main.repository.jpa;

import com.patho.main.model.patient.miscellaneous.Council;
import org.hibernate.Hibernate;


public interface CouncilRepository extends BaseRepository<Council, Long> {

    public default void initializeTask(Council c) {
        Hibernate.initialize(c.getTask());
    }
}
