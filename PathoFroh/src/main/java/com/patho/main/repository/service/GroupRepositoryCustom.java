package com.patho.main.repository.service;

import java.util.List;
import java.util.Optional;

import com.patho.main.model.user.HistoGroup;

public interface GroupRepositoryCustom {

	public void initializeGroupSettings(HistoGroup group);
	
	public Optional<HistoGroup> findOptionalById(Long id, boolean loadSettings);

	
	public List<HistoGroup> findAll(boolean irgnoreArchived);

	public List<HistoGroup> findAll(boolean initilizeSettings, boolean irgnoreArchived);

	public List<HistoGroup> findAllOrderByIdAsc();
	
	public List<HistoGroup> findAllOrderByIdAsc(boolean irgnoreArchived);

	public List<HistoGroup> findAllOrderByIdAsc(boolean initilizeSettings, boolean irgnoreArchived);
}
