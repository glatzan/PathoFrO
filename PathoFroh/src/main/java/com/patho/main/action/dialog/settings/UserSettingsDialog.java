package com.patho.main.action.dialog.settings;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.AbstractTabChangeEventHandler;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PrintService;
import com.patho.main.service.UserService;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.util.dialog.event.UserReloadEvent;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.search.settings.SimpleListSearchOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configurable
@Getter
@Setter
public class UserSettingsDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserService userService;

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

		setEventHandler(new AbstractTabChangeEventHandler(new AbstractDialog(Dialog.USER_SETTINGS_SAVE) {
		}, false, generalTab, printTab) {
			public void performEvent(boolean positive) {
				getEventDialog().hideDialog();

				if (positive)
					save();
				else
					update();
				onTabChange(getTargetTab(), true);
			}
		});

		setTabs(generalTab, printTab, favouriteListTab);
	}

	public boolean initBean() {
		setUser(userService.getCurrentUser());
		update();
		return super.initBean(Dialog.USER_SETTINGS);
	}

	public void save() {
		getUser().getSettings().setLabelPrinter(getPrintTab().getLabelPrinter());
		getUser().getSettings().setPrinter(getPrintTab().getClinicPrinter());
		setUser(userRepository.save(getUser()));
		System.out.println(getUser().getSettings().getPreferredLabelPrinter());
	}

	public void update() {
		setUser(userRepository.findById(user.getId()).get());
	}

	public void saveAndHide() {
		logger.debug("Saving user Settings");
		save();
		super.hideDialog(new UserReloadEvent(getUser()));
	}

	public void hideDialog() {
		logger.debug("Resetting user Settings");
		super.hideDialog();
	}

	@Getter
	@Setter
	public class GeneralTab extends AbstractTab {

		private List<View> availableViews;

		private List<SimpleListSearchOption> availableWorklistsToLoad;

		public GeneralTab() {
			setTabName("GeneralTab");
			setName("dialog.userSettings.general");
			setViewID("generalTab");
			setCenterInclude("include/general.xhtml");
		}

		public boolean initTab() {
			setAvailableViews(
					new ArrayList<View>(userService.getCurrentUser().getSettings().getAvailableViews()));
			setAvailableWorklistsToLoad(userService.getCurrentUser().getSettings().getAvailableWorklists());
			return true;
		}
	}

	@Getter
	@Setter
	@Configurable
	public class PrinterTab extends AbstractTab {

		@Autowired
		private PrintService printService;

		private ClinicPrinter clinicPrinter;

		private LabelPrinter labelPrinter;

		public PrinterTab() {
			setTabName("PrinterTab");
			setName("dialog.userSettings.printer");
			setViewID("printerTab");
			setCenterInclude("include/printer.xhtml");
		}

		public boolean initTab() {
			ClinicPrinter printer = printService.getCurrentPrinter(userService.getCurrentUser());
			LabelPrinter labelPrinter = printService.getCurrentLabelPrinter(userService.getCurrentUser());

			setLabelPrinter(labelPrinter);
			setClinicPrinter(printer);
			return true;
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
			setDisabled(!userService.userHasPermission(HistoPermissions.FAVOURITE_LIST_USE));
			return true;
		}

		@Override
		public void updateData() {
			long test = System.currentTimeMillis();
			logger.info("start - > 0");

			List<FavouriteList> list = favouriteListRepository.findByUserAndWriteableAndReadable(user, false, false,
					false, false, false, false);

			containers = list.stream().map(p -> new FavouriteListContainer(p, user)).collect(Collectors.toList());

			logger.info("end -> " + (System.currentTimeMillis() - test));
		}

	}

}
