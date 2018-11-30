package com.patho.main.action.dialog;

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

	public boolean initBean() {
		return initBean(null, getDilaog());
	}

	public boolean initBean(Dialog dialog) {
		return initBean(null, dialog);
	}

	public boolean initBean(Task task, Dialog dialog) {
		return initBean(task, dialog, null);
	}

	public boolean initBean(Dialog dialog, boolean selectFirstTab) {
		return initBean(task, dialog, true, selectFirstTab, null);
	}

	public boolean initBean(Task task, Dialog dialog, String selectedTabName) {
		return initBean(task, dialog, false, true, findTabByName(selectedTabName));
	}

	public boolean initBean(Task task, Dialog dialog, boolean reInitialize, boolean selectTab,
			AbstractTab selectedTab) {
		super.initBean(task, dialog);

		// initilizing tabs
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].isInitialized() || reInitialize)
				tabs[i].initTab();
		}

		// selecting tab
		if (selectTab) {
			if (selectedTab == null) {
				// selecting the first not disabled tab
				for (int i = 0; i < tabs.length; i++) {
					if (!tabs[i].isDisabled()) {
						onTabChange(tabs[i], true);
						break;
					}
				}
			} else {
				onTabChange(selectedTab, true);
			}
		}

		return true;
	}

	public void setTabs(AbstractTab... abstractTabs) {
		this.tabs = abstractTabs;
	}

	public void appendTab(AbstractTab... abstractTabs) {
		AbstractTab[] tmpTabs = this.tabs;
		tabs = new AbstractTab[tmpTabs.length + abstractTabs.length];
		System.arraycopy(tmpTabs, 0, tabs, 0, tmpTabs.length);
		System.arraycopy(abstractTabs, 0, tabs, tmpTabs.length, abstractTabs.length);
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

	private AbstractTab findTabByName(String name) {
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i].getTabName().equals(name))
				return tabs[i];
		}

		return null;
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
