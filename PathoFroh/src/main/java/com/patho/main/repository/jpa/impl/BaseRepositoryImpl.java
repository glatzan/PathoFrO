package com.patho.main.repository.jpa.impl;

import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.log.LogInfo;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import com.patho.main.model.util.log.LogListener;
import com.patho.main.repository.jpa.BaseRepository;
import com.patho.main.util.helper.SecurityContextHolderUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager entityManager;

    protected Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public BaseRepositoryImpl(JpaEntityInformation<T, Long> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public <S extends T> S save(S arg0, String log) {
        return save(arg0, log, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> S save(S arg0, String log, Patient p) {

        HistoUser user = null;

        // getting the user who has changed the object
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof HistoUser) {
            user = (HistoUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        return save(arg0, log, user, p);
    }

    @Transactional
    @Override
    public <S extends T> S save(S arg0, String log, HistoUser user) {
        return save(arg0, log, user, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> S save(S arg0, String log, HistoUser user, Patient p) {
        SecurityContextHolderUtil.setObjectToSecurityContext(LogListener.LOG_KEY_INFO, new LogInfo(log, user, p));
        return save(arg0);
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> arg0, String log) {
        return saveAll(arg0, log, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, Patient p) {
        HistoUser user = null;

        // getting the user who has changed the object
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof HistoUser) {
            user = (HistoUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        return saveAll(arg0, log, user, p);
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, HistoUser user) {
        return saveAll(arg0, log, user, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, HistoUser user, Patient p) {
        SecurityContextHolderUtil.setObjectToSecurityContext(LogListener.LOG_KEY_INFO, new LogInfo(log, user, p));
        return saveAll(arg0);
    }

    @Transactional
    @Override
    public <S extends T> void delete(S arg0, String log) {

        delete(arg0, log, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> void delete(S arg0, String log, Patient p) {
        HistoUser user = null;

        // getting the user who has changed the object
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof HistoUser) {
            user = (HistoUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        delete(arg0, log, user, p);
    }

    @Transactional
    @Override
    public <S extends T> void delete(S arg0, String log, HistoUser user) {
        delete(arg0, log, user, arg0 instanceof Parent ? ((Parent) arg0).getPatient() : null);
    }

    @Transactional
    @Override
    public <S extends T> void delete(S arg0, String log, HistoUser user, Patient p) {
        SecurityContextHolderUtil.setObjectToSecurityContext(LogListener.LOG_KEY_INFO, new LogInfo(log, user));
        delete(arg0);
    }

    @Transactional
    @Override
    public <S extends T> S lock(S arg0) {
        logger.debug("Increment Version!");
        arg0 = save(arg0, "Version increment");
        entityManager.lock(arg0, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return arg0;
    }
}
