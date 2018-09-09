package com.patho.main.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.log.LogInfo;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import com.patho.main.model.util.log.LogListener;
import com.patho.main.util.helper.SecurityContextHolderUtil;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

	@Override
	public <S extends T> S save(S arg0);

	public <S extends T> S save(S arg0, String log);

	public <S extends T> S save(S arg0, String log, Patient p) ;

	public <S extends T> S save(S arg0, String log, HistoUser user) ;

	public <S extends T> S save(S arg0, String log, HistoUser user, Patient p) ;

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> arg0);

	public <S extends T> List<S> saveAll(Iterable<S> arg0, String log);

	public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, Patient p);
	
	public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, HistoUser user);

	public <S extends T> List<S> saveAll(Iterable<S> arg0, String log, HistoUser user, Patient p);
	
	
	public <S extends T> void delete(S arg0, String log);
	
	public <S extends T> void delete(S arg0, String log, Patient p);

	public <S extends T> void delete(S arg0, String log, HistoUser user);
	
	public <S extends T> void delete(S arg0, String log, HistoUser user, Patient p);
	
	public <S extends T> S lock(S arg0);
}
