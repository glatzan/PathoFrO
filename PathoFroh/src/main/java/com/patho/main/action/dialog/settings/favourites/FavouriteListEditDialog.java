package com.patho.main.action.dialog.settings.favourites;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.favourites.FavouriteListItem;
import com.patho.main.model.favourites.FavouritePermissions;
import com.patho.main.model.favourites.FavouritePermissionsGroup;
import com.patho.main.model.favourites.FavouritePermissionsUser;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.FavouritePermissionsRepository;
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
public class FavouriteListEditDialog extends AbstractDialog<FavouriteListEditDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouritePermissionsRepository favouritePermissionsRepository;

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
		favouriteList.setOwner(userHandlerAction.getCurrentUser());

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
			//
			//
			//
			// !---------------------check load
			//
			//
			//
			dumpLists = favouriteListRepository.findAll(true, true, true, true);
		} else {
			dumpLists = favouriteListRepository.findByUserAndWriteableAndReadable(userHandlerAction.getCurrentUser(),
					true, false);
		}

		setDumpListTransformer(new DefaultTransformer<FavouriteList>(dumpLists));

		setUserPermission(new FavouriteListContainer(favouriteList, userHandlerAction.getCurrentUser()));

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
		if (event.getObject() != null && event.getObject() instanceof HistoUser) {
			favouriteList.setOwner((HistoUser) event.getObject());
		}
	}

	/**
	 * Is called on user select dialog return. Adds an user to the permission list,
	 * sets readable to true.
	 * 
	 * @param event
	 */
	public void onReturnSelectUser(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof HistoUser) {
			if (!favouriteList.getUsers().stream().anyMatch(p -> p.getUser().equals(event.getObject()))) {
				FavouritePermissionsUser permission = new FavouritePermissionsUser((HistoUser) event.getObject());
				favouriteList.getUsers().add(permission);
				permission.setFavouriteList(favouriteList);
				permission.setReadable(true);

				if (favouriteList.isGlobalView())
					permission.setReadable(true);
			}
		}
	}

	/**
	 * Is called on group select dialog return. Adds a group to the permission list,
	 * sets readable to true.
	 * 
	 * @param event
	 */
	public void onReturnSelectGroup(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof HistoGroup) {
			if (!favouriteList.getGroups().stream().anyMatch(p -> p.getGroup().equals(event.getObject()))) {
				FavouritePermissionsGroup permission = new FavouritePermissionsGroup((HistoGroup) event.getObject());
				favouriteList.getGroups().add(permission);
				permission.setFavouriteList(favouriteList);
				permission.setReadable(true);

				if (favouriteList.isGlobalView())
					permission.setReadable(true);
			}
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
		if (favouriteList.isUseDumplist() && HistoUtil.isNullOrEmpty(favouriteList.getDumpCommentary())) {
			String commentary = resourceBundle.get("dialog.favouriteListEdit.dumpList.default");
			favouriteList.setDumpCommentary(commentary);
		}
	}

	/**
	 * Saves the list.
	 */
	public void saveFavouriteList() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				if (!toDeleteList.isEmpty()) {
					for (FavouritePermissions favouritePermissions : toDeleteList) {
						favouritePermissionsRepository.delete(favouritePermissions);
					}
				}

				favouriteListRepository.save(getFavouriteList(),
						resourceBundle.get(getFavouriteList().getId() == 0 ? "log.settings.favouriteList.new"
								: "log.settings.favouriteList.edit", getFavouriteList()));
			}
		});
	}

	public enum Mode {
		ADMIN, USER;
	}
}
