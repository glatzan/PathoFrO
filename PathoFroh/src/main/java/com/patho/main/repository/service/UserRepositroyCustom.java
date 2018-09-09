package com.patho.main.repository.service;

import java.util.List;

import com.patho.main.model.user.HistoUser;

public interface UserRepositroyCustom {
	public List<HistoUser> findAllIgnoreArchived();
	public List<HistoUser> findAllIgnoreArchived(boolean irgnoreArchived);
}
