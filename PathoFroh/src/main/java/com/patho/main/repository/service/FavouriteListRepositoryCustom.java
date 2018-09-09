package com.patho.main.repository.service;

import java.util.List;
import java.util.Optional;

import com.patho.main.model.dto.FavouriteListMenuItem;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoUser;

public interface FavouriteListRepositoryCustom {
	Optional<FavouriteList> findOptionalByIdAndInitialize(Long id, boolean loadList, boolean loadPermissions,
			boolean loadDumpList);

	Optional<FavouriteList> findOptionalByIdAndInitialize(Long id, boolean loadItems, boolean loadOwner,
			boolean loadPermissions, boolean loadDumpList);

	List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable);

	List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable,
			boolean loadList, boolean loadPermissions, boolean loadDumpList);

	List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable,
			boolean loadItems, boolean loadOwner, boolean loadPermissions, boolean loadDumpList);

	List<FavouriteList> findAll();

	List<FavouriteList> findAll(boolean loadDumpList);

	List<FavouriteList> findAll(boolean loadItems, boolean loadOwner, boolean loadPermissions, boolean loadDumpList);

	List<FavouriteList> findByIds(List<Long> ids);

	List<FavouriteListMenuItem> getMenuItems(HistoUser user, Task task);
}
