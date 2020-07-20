package com.patho.main.repository.jpa.custom;

import com.patho.main.model.user.HistoUser;

import java.util.List;

public interface UserRepositroyCustom {
    public List<HistoUser> findAllIgnoreArchived();

    public List<HistoUser> findAllIgnoreArchived(boolean irgnoreArchived);
}
