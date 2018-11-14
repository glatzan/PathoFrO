package com.patho.main.action.dialog;

import org.omnifaces.el.functions.Arrays;

import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractTabDialog<I> extends AbstractDialog<AbstractTabDialog<I>>
		implements AbstractTabChangeEvent {

	protected AbstractTab[] tabs;

	protected AbstractTab selectedTab;

	/**
	 * For all tabs in this array a tab change event can be fired
	 */
	protected AbstractTab[] onChangeEventTabs;

	/**
	 * if true the tab change event will be fired
	 */
	protected boolean fireTabChangeEvent;

	/**
	 * This tab will be selected after the event is finished
	 */
	protected AbstractTab changeToTabAfterEvent;

	/**
	 * This actionslistener will be fired on event occurrence
	 */
	protected AbstractTabChangeEvent abstractTabChangeEventHandler;

	public boolean initBean(Task task, Dialog dialog) {
		super.initBean(task, dialog);

		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initTab();
		}

		onTabChange(tabs[0], true);

		return true;
	}

	public boolean initBean(Dialog dialog) {
		return initBean(null, dialog);
	}

	public void onTabChange(AbstractTab tab) {
		onTabChange(tab, false);
	}

	public void onTabChange(AbstractTab tab, boolean ignoreTabChangeEvent) {
		if (!ignoreTabChangeEvent && Arrays.contains(onChangeEventTabs, getSelectedTab())
				&& (fireTabChangeEvent || getSelectedTab().isFireTabChangeEvent())) {
			changeToTabAfterEvent = tab;
			abstractTabChangeEventHandler.onStartTabChangeEvent();
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

	}

}
