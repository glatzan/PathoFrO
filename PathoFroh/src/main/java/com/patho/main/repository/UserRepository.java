package com.patho.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.service.UserRepositroyCustom;

@Repository
public interface UserRepository extends BaseRepository<HistoUser, Long>, UserRepositroyCustom {
	
	/**
	 * Returns a user by username
	 * 
	 * @param name
	 * @return
	 */
	public Optional<HistoUser> findOptionalByPhysicianUid(String uid);
	
	/**
	 * Lists user of a group
	 * 
	 * @param id
	 * @return
	 */
	public List<HistoUser> findByGroupId(long id);


	/**
	 * Returns a list of Histousers order by ID ascending, which are not archived
	 * 
	 * @return
	 */
	public List<HistoUser> findByArchivedFalseOrderByIdAsc();
	
	public List<HistoUser> findAll();
}
