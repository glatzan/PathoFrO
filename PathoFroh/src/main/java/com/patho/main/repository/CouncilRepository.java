package com.patho.main.repository;

import org.hibernate.Hibernate;

import com.patho.main.model.Council;

public interface CouncilRepository extends BaseRepository<Council, Long> {

	public default void initializeTask(Council c) {
		Hibernate.initialize(c.getTask());
	}
}
