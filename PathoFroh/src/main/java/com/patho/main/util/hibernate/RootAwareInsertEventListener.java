package com.patho.main.util.hibernate;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RootAwareInsertEventListener implements PersistEventListener {

	public static final RootAwareInsertEventListener INSTANCE = new RootAwareInsertEventListener();

	@Override
	public void onPersist(PersistEvent event) throws HibernateException {
		final Object entity = event.getObject();

		if (entity instanceof RootAware) {
			RootAware rootAware = (RootAware) entity;
			Object root = rootAware.root();
			event.getSession().lock(root, LockMode.OPTIMISTIC_FORCE_INCREMENT);
			log.info("Incrementing {" + entity + "} entity version because a {" + root
					+ "} child entity has been inserted");
		}
	}

	@Override
	public void onPersist(PersistEvent event, Map createdAlready) throws HibernateException {
		onPersist(event);
	}
}