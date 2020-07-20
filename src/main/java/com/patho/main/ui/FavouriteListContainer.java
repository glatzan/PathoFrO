package com.patho.main.ui;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.favourites.FavouritePermissionsGroup;
import com.patho.main.model.favourites.FavouritePermissionsUser;
import com.patho.main.model.user.HistoUser;

public class FavouriteListContainer {

    public static final int PERMISSION_GLOBAL = 0;
    public static final int PERMISSION_OWNER = 1;
    public static final int PERMISSION_GROUP = 2;

    private FavouriteList favouriteList;

    private boolean owner;
    private boolean global;

    private boolean userPermission;
    private boolean groupPermission;

    private boolean editable;
    private boolean readable;
    private boolean admin;

    /**
     * 0 = global 1 = owner 2 = GROUP
     */
    private int type = PERMISSION_GLOBAL;

    public FavouriteListContainer(FavouriteList favouriteList, HistoUser currentUser) {
        this.favouriteList = favouriteList;

        if (favouriteList.getOwner() != null && favouriteList.getOwner().equals(currentUser)) {
            this.editable = true;
            this.readable = true;
            this.admin = true;

            type = PERMISSION_OWNER;
        }

        // only updating if not already true
        for (FavouritePermissionsUser user : favouriteList.getUsers()) {
            if (user.getUser().equals(currentUser)) {
                this.admin = this.admin ? true : user.isAdmin();
                this.editable = this.editable ? true : user.isEditable();
                this.readable = this.readable ? true : user.isReadable();
                this.userPermission = true;

                type = type > 0 ? type : PERMISSION_GROUP;
                break;
            }
        }

        // only updating if not already true
        for (FavouritePermissionsGroup group : favouriteList.getGroups()) {
            if (group.getGroup().equals(currentUser.getGroup())) {
                this.admin = this.admin ? true : group.isAdmin();
                this.editable = this.editable ? true : group.isEditable();
                this.readable = this.readable ? true : group.isReadable();
                this.groupPermission = true;

                type = type > 0 ? type : PERMISSION_GROUP;
                break;
            }
        }
    }

    public boolean isOwnerOrAdmin() {
        return admin || owner;
    }

    public FavouriteList getFavouriteList() {
        return this.favouriteList;
    }

    public boolean isOwner() {
        return this.owner;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public boolean isUserPermission() {
        return this.userPermission;
    }

    public boolean isGroupPermission() {
        return this.groupPermission;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public boolean isReadable() {
        return this.readable;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public int getType() {
        return this.type;
    }

    public void setFavouriteList(FavouriteList favouriteList) {
        this.favouriteList = favouriteList;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public void setUserPermission(boolean userPermission) {
        this.userPermission = userPermission;
    }

    public void setGroupPermission(boolean groupPermission) {
        this.groupPermission = groupPermission;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setType(int type) {
        this.type = type;
    }
}
