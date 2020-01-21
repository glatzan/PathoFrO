package com.patho.main.util.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.persister.entity.EntityPersister;

@Slf4j
public class RootAwareUpdateAndDeleteEventListener implements FlushEntityEventListener {

    private static final long serialVersionUID = 6713341535019749081L;

    public static final RootAwareUpdateAndDeleteEventListener INSTANCE = new RootAwareUpdateAndDeleteEventListener();

    @Override
    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
        final EntityEntry entry = event.getEntityEntry();
        final Object entity = event.getEntity();
        final boolean mightBeDirty = entry.requiresDirtyCheck(entity);

        if (mightBeDirty && entity instanceof RootAware) {
            RootAware rootAware = (RootAware) entity;
            if (updated(event)) {
                Object root = rootAware.root();
                log.info("Incrementing {" + entity + "} entity version because a {" + root
                        + "} child entity has been updated");
                incrementRootVersion(event, root);
            } else if (deleted(event)) {
                Object root = rootAware.root();
                log.info("Incrementing {" + entity + "} entity version because a {" + root
                        + "} child entity has been deleted");
                incrementRootVersion(event, root);
            }
        }

    }

    private void incrementRootVersion(FlushEntityEvent event, Object root) {
        event.getSession().lock(root, LockMode.OPTIMISTIC_FORCE_INCREMENT);
    }

    private boolean deleted(FlushEntityEvent event) {
        return event.getEntityEntry().getStatus() == Status.DELETED;
    }

    private boolean updated(FlushEntityEvent event) {
        final EntityEntry entry = event.getEntityEntry();
        final Object entity = event.getEntity();

        int[] dirtyProperties;
        EntityPersister persister = entry.getPersister();
        final Object[] values = event.getPropertyValues();
        SessionImplementor session = event.getSession();

        if (event.hasDatabaseSnapshot()) {
            dirtyProperties = persister.findModified(event.getDatabaseSnapshot(), values, entity, session);
        } else {
            dirtyProperties = persister.findDirty(values, entry.getLoadedState(), entity, session);
        }

        return dirtyProperties != null;
    }
}