package com.patho.main.model.favourites;

import com.patho.main.model.user.HistoGroup;
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
public class FavouritePermissionsGroup extends FavouritePermissions {

    @OneToOne
    private HistoGroup group;

    public FavouritePermissionsGroup() {
    }

    public FavouritePermissionsGroup(HistoGroup group) {
        this.group = group;
    }
}
