package com.patho.main.action.dialog.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.AbstractTabChangeEventHandler;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.settings.diagnosis.DiagnosisPresetEditDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.common.View;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.user.HistoPermissions;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.PrintService;
import com.patho.main.ui.FavouriteListContainer;
import com.patho.main.util.dialogReturn.ReloadUserEvent;
import com.patho.main.util.printer.ClinicPrinter;
import com.patho.main.util.printer.LabelPrinter;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
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
		setUser(userHandlerAction.getCurrentUser());
		update();
		return super.initBean(Dialog.USER_SETTINGS);
	}

	public void save() {
		getUser().getSettings().setPreferedLabelPritner(Long.toString(getPrintTab().getLabelPrinter().getId()));
		getUser().getSettings().setPreferedPrinter(getPrintTab().getClinicPrinter().getId());
		setUser(userRepository.save(getUser()));
	}

	public void update() {
		setUser(userRepository.findById(user.getId()).get());
	}

	public void saveAndHide() {
		logger.debug("Saving user Settings");
		save();
		super.hideDialog(new ReloadUserEvent());
	}

	public void hideDialog() {
		logger.debug("Resetting user Settings");
		super.hideDialog(new ReloadUserEvent());
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
			ClinicPrinter printer = printService.getCurrentPrinter(userHandlerAction.getCurrentUser());
			LabelPrinter labelPrinter = printService.getCurrentLabelPrinter(userHandlerAction.getCurrentUser());

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
			setDisabled(!userHandlerAction.currentUserHasPermission(HistoPermissions.FAVOURITE_LIST_USE));
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
