package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.dto.TaskOverview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface TaskOverviewRepository extends BaseRepository<TaskOverview, Long> {
	public List<TaskOverview> findAllWithAssociatedPerson(@Param("personid") long personID);
	public List<TaskOverview> findAllWithAssociatedPerson(@Param("personid") long personID, Pageable oageable);
}
