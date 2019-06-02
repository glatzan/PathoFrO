package com.patho.main.action.dialog.settings.favourites;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.patho.main.model.favourites.*;
import com.patho.main.service.UserService;
import com.patho.main.util.dialog.event.GroupSelectEvent;
import com.patho.main.util.dialog.event.HistoUserSelectEvent;
import com.patho.main.util.dialog.event.ReloadEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.service.FavouriteListService;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class FavouriteListEditDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

	private boolean newFavouriteList;

	private FavouriteList favouriteList;

	private List<FavouritePermissions> toDeleteList;

	private boolean adminMode;

	private FavouriteListContainer userPermission;

	private List<FavouriteList> dumpLists;

	private DefaultTransformer<FavouriteList> dumpListTransformer;

	public FavouriteListEditDialog initAndPrepareBean() {
		return initAndPrepareBean(false);
	}

	public FavouriteListEditDialog initAndPrepareBean(boolean adminMode) {
		FavouriteList favouriteList = new FavouriteList();
		favouriteList.setDefaultList(false);
		favouriteList.setUsers(new HashSet<FavouritePermissionsUser>());
		favouriteList.setGroups(new HashSet<FavouritePermissionsGroup>());
		favouriteList.setItems(new ArrayList<FavouriteListItem>());
		favouriteList.setOwner(userService.getCurrentUser());

		return initAndPrepareBean(favouriteList, adminMode, false);
	}

	public FavouriteListEditDialog initAndPrepareBean(FavouriteList favouriteList) {
		return initAndPrepareBean(favouriteList, false);
	}

	public FavouriteListEditDialog initAndPrepareBean(FavouriteList favouriteList, boolean adminMode) {
		return initAndPrepareBean(favouriteList, adminMode, true);
	}

	public FavouriteListEditDialog initAndPrepareBean(FavouriteList favouriteList, boolean adminMode,
			boolean initialize) {
		if (initBean(favouriteList, adminMode, initialize))
			prepareDialog();

		return this;
	}

	public boolean initBean(FavouriteList favouriteList, boolean adminMode, boolean initialize) {

		if (initialize)
			favouriteList = favouriteListRepository
					.findOptionalByIdAndInitialize(favouriteList.getId(), true, true, true).get();

		setFavouriteList(favouriteList);

		setNewFavouriteList(favouriteList.getId() == 0);

		setToDeleteList(new ArrayList<FavouritePermissions>());

		setAdminMode(adminMode);

		// getting lists for dumplist option, if admin mod all lists are available, if
		// user only the writeable lists are displayed
		if (adminMode) {
			dumpLists = favouriteListRepository.findAll(false, false, false, false);
		} else {
			dumpLists = favouriteListRepository.findByUserAndWriteableAndReadable(userService.getCurrentUser(),
					true, false);
		}

		setDumpListTransformer(new DefaultTransformer<FavouriteList>(dumpLists));

		setUserPermission(new FavouriteListContainer(favouriteList, userService.getCurrentUser()));
		return super.initBean(task, Dialog.SETTINGS_FAVOURITE_LIST_EDIT);
	}

	/**
	 * Method called on selecting the admin role for the user/group. Setting write
	 * and editable to true.
	 * 
	 * @param permission
	 */
	public void onSelectAdmin(FavouritePermissions permission) {
		if (permission.isAdmin()) {
			permission.setEditable(true);
			permission.setReadable(true);
		}
	}

	/**
	 * Is called on user select dialog return. Sets the new owner
	 * 
	 * @param event
	 */
	public void onReturnSelectOwner(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof HistoUserSelectEvent)
			favouriteList.setOwner(((HistoUserSelectEvent) event.getObject()).getObj());
	}

	/**
	 * Is called on user or group select dialog return. Adds an user to the
	 * permission list, sets readable to true.
	 * 
	 * @param event
	 */
	public void onPermissionSelectEvent(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof HistoUserSelectEvent) {
			favouriteListService.addUserPermission(getFavouriteList(),
					((HistoUserSelectEvent) event.getObject()).getObj(), true, false);
		} else if (event.getObject() != null && event.getObject() instanceof GroupSelectEvent) {
			favouriteListService.addGroupPermission(getFavouriteList(),
					((GroupSelectEvent) event.getObject()).getObj(), true, false);
		}
	}

	/**
	 * Removes a permission entity.
	 * 
	 * @param list
	 * @param toRemove
	 */
	public void removeEntityFromList(Set<? extends FavouritePermissions> list, FavouritePermissions toRemove) {
		if (toRemove.getId() != 0)
			toDeleteList.add(toRemove);

		list.remove(toRemove);
	}

	/**
	 * Is called on useDuplist change (boolean). If dumplist should be used an there
	 * is no default commentary, the commentary is generated.
	 */
	public void onUseDumplist() {
		if (favouriteList.getUseDumplist() && HistoUtil.isNullOrEmpty(favouriteList.getDumpCommentary())) {
			String commentary = resourceBundle.get("dialog.favouriteListEdit.dumpList.default");
			favouriteList.setDumpCommentary(commentary);
		}
	}

	/**
	 * Saves the list.
	 */
	public void saveAndHide() {
		favouriteListService.addOrUpdate(getFavouriteList(), getToDeleteList());
		super.hideDialog(new ReloadEvent());
	}

	public enum Mode {
		ADMIN, USER;
	}
}
