package com.patho.main.dao;

import org.hibernate.Hibernate;

import com.patho.main.model.StainingPrototype;

public class SettingsDAO extends AbstractDAO {

	public StainingPrototype initializeStainingPrototype(StainingPrototype stainingPrototype, boolean initialize) {
		stainingPrototype = reattach(stainingPrototype);

		if (initialize) {
			Hibernate.initialize(stainingPrototype.getBatchDetails());
		}

		return stainingPrototype;
	}

	
	public StainingPrototype getStainingPrototype(long id, boolean initialize) {

		StainingPrototype stainingPrototype = get(StainingPrototype.class, id);

		if (initialize) {
			Hibernate.initialize(stainingPrototype.getBatchDetails());
		}

		return stainingPrototype;
	}

}
