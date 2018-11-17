package com.patho.main.action.dialog;

import org.omnifaces.el.functions.Arrays;

import com.patho.main.action.dialog.AbstractTabDialog.AbstractTab;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractTabDialog extends AbstractDialog {

	protected AbstractTab[] tabs;

	protected AbstractTab selectedTab;

	protected AbstractTabChangeEventHandler eventHandler;

	public boolean initBean(Task task, Dialog dialog) {
		return initBean(task, dialog, null);
	}

	public boolean initBean(Task task, Dialog dialog, String selectedTabName) {
		super.initBean(task, dialog);

		AbstractTab tabToSelect = null;

		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initTab();
			if (selectedTabName != null && tabs[i].getTabName().equals(selectedTabName))
				tabToSelect = tabs[i];
		}

		if (tabToSelect == null) {
			// selecting the first not disabled tab
			for (int i = 0; i < tabs.length; i++) {
				if (!tabs[i].isDisabled()) {
					onTabChange(tabs[i], true);
					break;
				}
			}
		} else
			onTabChange(tabToSelect, true);

		return true;
	}

	public boolean initBean(Dialog dialog) {
		return initBean(null, dialog);
	}

	public void setTabs(AbstractTab... abstractTabs) {
		this.tabs = abstractTabs;
	}

	public void onTabChange(AbstractTab tab) {
		onTabChange(tab, false);
	}

	public void onTabChange(AbstractTab tab, boolean ignoreTabChangeEvent) {
		if (eventHandler != null && !ignoreTabChangeEvent && eventHandler.isFireEvent(tab, getSelectedTab())) {
			eventHandler.fireTabChangeEvent(tab, getSelectedTab());
		} else {
			logger.debug("Changing tab to " + tab.getName());
			setSelectedTab(tab);
			tab.updateData();
		}
	}

	public void nextTab() {
		logger.trace("Next tab");
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i] == selectedTab) {
				while (i++ <= tabs.length - 1) {
					if (!tabs[i].isDisabled()) {
						onTabChange(tabs[i]);
						return;
					}
				}
			}
		}
	}

	public void previousTab() {
		logger.trace("Previous step");
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i] == selectedTab) {
				while (--i >= 0) {
					if (!tabs[i].isDisabled()) {
						onTabChange(tabs[i]);
						return;
					}
				}
			}
		}
	}

	@Getter
	@Setter
	public abstract static class AbstractTab {

		/**
		 * True if initilized
		 */
		protected boolean initialized;

		protected String name;

		protected String viewID;

		protected String tabName;

		protected String centerInclude;

		protected boolean disabled;

		protected AbstractTab parentTab;

		/**
		 * If true an this tab is in the event array, the event will be fired
		 */
		protected boolean fireTabChangeEvent;

		public boolean isParent() {
			return parentTab != null;
		}

		public void updateData() {
			return;
		}

		public boolean initTab() {
			setInitialized(true);
			return true;
		}

		public void triggerEventOnChange() {
			System.out.println("trigger");
			this.fireTabChangeEvent = true;
		}

	}

}
