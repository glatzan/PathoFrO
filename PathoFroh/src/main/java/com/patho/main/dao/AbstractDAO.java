package com.patho.main.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.log.LogInfo;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.util.log.LogListener;
import com.patho.main.util.exception.HistoDatabaseConstraintViolationException;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.SecurityContextHolderUtil;
import com.patho.main.util.hibernate.RootAware;

public abstract class AbstractDAO implements Serializable {

	private static final long serialVersionUID = 8566919900494360311L;

	protected SessionFactory sessionFactory;

	@Autowired
	protected ResourceBundle resourceBundle;

	private Session session;

	public Session getSession() {
		try {
			return sessionFactory.getCurrentSession();
		} catch (HibernateException hibernateException) {
			hibernateException.printStackTrace();
			if (session == null || !session.isOpen()) {
				session = sessionFactory.openSession();
			}
		}
		return session;
	}

	public static void printStats(Statistics stats) {
		System.out.println("Fetch Count=" + stats.getEntityFetchCount());
		System.out.println("Second Level Hit Count=" + stats.getSecondLevelCacheHitCount());
		System.out.println("Second Level Miss Count=" + stats.getSecondLevelCacheMissCount());
		System.out.println("Second Level Put Count=" + stats.getSecondLevelCachePutCount());
	}

	@SuppressWarnings("unchecked")
	public <C> C get(Class<C> clazz, Serializable serializable) {
		return (C) getSession().get(clazz, serializable);
	}

	public <C extends ID> C save(C object) throws HistoDatabaseInconsistentVersionException {
		return save(object, null, null, null);
	}

	public <C extends ID> C save(C object, String resourcesKey) throws HistoDatabaseInconsistentVersionException {
		return save(object, resourcesKey, null, null);
	}

	public <C extends ID> C save(C object, String resourcesKey, Object[] resourcesKeyInsert)
			throws HistoDatabaseInconsistentVersionException {
		return save(object, resourcesKey, resourcesKeyInsert, null);
	}

	public <C extends ID> C save(C object, String resourcesKey, Object[] resourcesKeyInsert, Patient patient)
			throws HistoDatabaseInconsistentVersionException {

		try {
			
			if (resourcesKey != null) {
				LogInfo logInfo = new LogInfo(resourceBundle.get(resourcesKey, resourcesKeyInsert), patient);
				SecurityContextHolderUtil.setObjectToSecurityContext(LogListener.LOG_KEY_INFO, logInfo);
			}
			
			getSession().saveOrUpdate(object);
			getSession().flush();
			return object;
		} catch (HibernateException hibernateException) {
			object = (C) getSession().merge(object);
			return object;
		} catch (OptimisticLockException | HibernateOptimisticLockingFailureException e) {
			getSession().getTransaction().rollback();
			throw new HistoDatabaseInconsistentVersionException(object);
		} catch (Exception e) {
			getSession().getTransaction().rollback();
			e.printStackTrace();
			throw new HistoDatabaseInconsistentVersionException(object);
		}
	}

	public <C extends ID> void saveCollection(List<C> objects, String resourcesKey) {
		saveCollection(objects, resourcesKey, null);
	}

	public <C extends ID> void saveCollection(List<C> objects, String resourcesKey, Object[] resourcesKeyInsert) {
		saveCollection(objects, resourcesKey, resourcesKeyInsert, null);
	}

	public <C extends ID> void saveCollection(List<C> objects, String resourcesKey, Object[] resourcesKeyInsert,
			Patient patient) {
		for (C object : objects) {
			save(object, resourcesKey, resourcesKeyInsert, patient);
		}
	}

	public <C extends ID> C delete(C object) {
		return delete(object, null);
	}

	public <C extends ID> C delete(C object, String resourcesKey) {
		return delete(object, resourcesKey, null);
	}

	public <C extends ID> C delete(C object, String resourcesKey, Object[] resourcesKeyInsert) {
		return delete(object, resourcesKey, resourcesKeyInsert, null);
	}

	public <C extends ID> C delete(C object, String resourcesKey, Object[] resourcesKeyInsert, Patient patient) {

		if (resourcesKey != null) {
			LogInfo logInfo = new LogInfo(resourceBundle.get(resourcesKey, resourcesKeyInsert), patient);
			SecurityContextHolderUtil.setObjectToSecurityContext(LogListener.LOG_KEY_INFO, logInfo);
		}

		try {
			getSession().delete(object);
			getSession().flush();
		} catch (HibernateException hibernateException) {
			session.delete(session.merge(object));
		} catch (javax.persistence.OptimisticLockException e) {
			getSession().getTransaction().rollback();
			throw new HistoDatabaseInconsistentVersionException(object);
		} catch (PersistenceException e) {
			getSession().getTransaction().rollback();
			throw new HistoDatabaseConstraintViolationException(object);
		} catch (Exception e) {
			getSession().getTransaction().rollback();
			e.printStackTrace();
			throw new HistoDatabaseInconsistentVersionException(object);
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public <C extends ID> C reattach(C object) throws HistoDatabaseInconsistentVersionException {
		try {
			getSession().saveOrUpdate(object);
			getSession().flush();
		} catch (javax.persistence.OptimisticLockException e) {
			getSession().getTransaction().rollback();
			getSession().beginTransaction();

			// Class<? extends HasID> klass = (Class<? extends HasID>)
			// object.getClass();
			throw new HistoDatabaseInconsistentVersionException(object);
		} catch (HibernateException hibernateException) {
			object = (C) getSession().merge(object);
			hibernateException.printStackTrace();
		}
		return object;
	}

	public void refresh(Object object) {
		getSession().refresh(object);
	}

	public void commit() {
		getSession().getTransaction().commit();
	}

	public void lockParent(RootAware<?> rootAware) {
		lock(rootAware.root());
	}

	public void lock(Object object) {
		getSession().lock(object, LockMode.OPTIMISTIC_FORCE_INCREMENT);
		getSession().flush();
	}
}
