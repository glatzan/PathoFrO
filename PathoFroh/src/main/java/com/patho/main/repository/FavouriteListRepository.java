package com.patho.main.repository;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.repository.service.FavouriteListRepositoryCustom;

public interface FavouriteListRepository extends BaseRepository<FavouriteList, Long>, FavouriteListRepositoryCustom {

}
