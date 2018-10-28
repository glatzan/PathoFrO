package com.patho.main.action.dialog.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "session")
@Getter
@Setter
@Slf4j
public class UserSettingsDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserRepository userRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

	private GeneralTab generalTab;
	private PrinterTab printTab;
	private FavouriteListTab favouriteListTab;

	private HistoUser user;

	public UserSettingsDialog() {
		generalTab = new GeneralTab();
		printTab = new PrinterTab();
		favouriteListTab = new FavouriteListTab();

		tabs = new AbstractTab[] { generalTab, printTab, favouriteListTab };
	}

	public void initAndPrepareBean() {
		initBean();
		prepareDialog();
	}

	public boolean initBean() {
		setUser(userHandlerAction.getCurrentUser());
		return super.initBean(Dialog.USER_SETTINGS);
	}

	public void saveUserSettings() {
		log.debug("Saving user Settings");

		userRepository.save(getUser());
		userHandlerAction.updateSelectedPrinters();
	}

	public void resetUserSettings() {
		log.debug("Resetting user Settings");
		setUser(userRepository.findById(user.getId()).orElse(null));
	}

	@Getter
	@Setter
	public class GeneralTab extends AbstractTab {

		private List<View> availableViews;

		private List<SimpleSearchOption> availableWorklistsToLoad;

		public GeneralTab() {
			setTabName("GeneralTab");
			setName("dialog.userSettings.general");
			setViewID("generalTab");
			setCenterInclude("include/general.xhtml");
		}

		public boolean initTab() {

			setAvailableViews(
					new ArrayList<View>(userHandlerAction.getCurrentUser().getSettings().getAvailableViews()));

			setAvailableWorklistsToLoad(userHandlerAction.getCurrentUser().getSettings().getAvailableWorklists());

			return true;
		}
	}

	@Getter
	@Setter
	public class PrinterTab extends AbstractTab {

		public PrinterTab() {
			setTabName("PrinterTab");
			setName("dialog.userSettings.printer");
			setViewID("printerTab");
			setCenterInclude("include/printer.xhtml");
		}

	}

	@Getter
	@Setter
	public class FavouriteListTab extends AbstractTab {

		private List<FavouriteListContainer> containers;

		public FavouriteListTab() {
			setTabName("FavouriteListTab");
			setName("dialog.userSettings.favouriteLists");
			setViewID("favouriteTab");
			setCenterInclude("include/favouriteLists.xhtml");
		}

		public boolean initTab() {
			setDisabled(!userHandlerAction.currentUserHasPermission(HistoPermissions.FAVOURITE_LIST_USE));
			return true;
		}

		@Override
		public void updateData() {
			long test = System.currentTimeMillis();
			log.info("start - > 0");

			List<FavouriteList> list = favouriteListRepository.findByUserAndWriteableAndReadable(user, false, false,
					true, true, true, false);

			containers = list.stream().map(p -> new FavouriteListContainer(p, user)).collect(Collectors.toList());

			log.info("end -> " + (System.currentTimeMillis() - test));
		}

	}

}
