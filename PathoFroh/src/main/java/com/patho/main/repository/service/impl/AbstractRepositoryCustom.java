package com.patho.main.repository.service.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractRepositoryCustom {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	protected EntityManager em;

	protected Session getSession() {
		return em.unwrap(Session.class);
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		return getSession().getCriteriaBuilder();
	}
}
