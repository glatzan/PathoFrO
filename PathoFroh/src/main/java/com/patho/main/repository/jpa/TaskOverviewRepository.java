package com.patho.main.repository.jpa;

import com.patho.main.model.dto.TaskOverview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskOverviewRepository extends BaseRepository<TaskOverview, Long> {
    public List<TaskOverview> findAllWithAssociatedPerson(@Param("personid") long personID);

    public List<TaskOverview> findAllWithAssociatedPerson(@Param("personid") long personID, Pageable oageable);
}
