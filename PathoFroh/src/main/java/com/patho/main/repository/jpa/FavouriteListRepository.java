package com.patho.main.repository.jpa;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.repository.jpa.custom.FavouriteListRepositoryCustom;

public interface FavouriteListRepository extends BaseRepository<FavouriteList, Long>, FavouriteListRepositoryCustom {

}
