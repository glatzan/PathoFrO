package com.patho.main.model.favourites;

import com.patho.main.model.user.HistoUser;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@Getter
@Setter
public class FavouritePermissionsUser extends FavouritePermissions {

    @OneToOne
    private HistoUser user;

    public FavouritePermissionsUser() {
    }

    public FavouritePermissionsUser(HistoUser user) {
        this.user = user;
    }
}
