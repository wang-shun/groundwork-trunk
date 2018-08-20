/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Arrays;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.icesoft.faces.component.menubar.MenuItem;

public class MessageSelectBean {
	private EventBean[] allRows;
	private boolean selectAll;
	private String selectAllButtonText;
	public static Logger logger = Logger.getLogger(MessageSelectBean.class
			.getName());

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public MessageSelectBean() {
		selectAllButtonText = ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_SELECT_ALL);
	}

	public EventBean[] getAllRows() {
		return allRows;
	}

	public void setAllRows(EventBean[] allRows) {
		this.allRows = allRows;
	}

	public void toggleAllSelected(ActionEvent event) {
		DataTableBean dataBean = ConsoleHelper.getEventTableBean();

		if (dataBean != null) {
			allRows = dataBean.getEvents();
		} // end if
		if (allRows != null && allRows.length > 0) {
			selectAll = !selectAll;
			selectAllButtonText = selectAll ?  ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_DESELECT_ALL) :  ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_SELECT_ALL);;
			setAllSelected(selectAll);
		} // end if
	}

	private void setAllSelected(boolean val) {
		if (allRows != null) {
			for (int i = allRows.length - 1; i >= 0; i--) {
				EventBean row = (EventBean) allRows[i];
				row.setSelected(val);
			}

			DataTableBean tableBean = ConsoleHelper.getEventTableBean();
			tableBean.setSelectedRows(Arrays.asList(allRows));
			if (!val) {
				TabsetBean tabset = ConsoleHelper.getTabSetBean();
				Tab tab = tabset.getTabs().get(tabset.getTabIndex());
				tab.getActionBean().reset();
				this.reset();
			}
			TabsetBean tabset = ConsoleHelper.getTabSetBean();
			Tab tab = tabset.getTabs().get(
					tabset.getTabIndex());
			ActionBean action = tab.getActionBean();
			MenuItem menu = (MenuItem) action.getMenuModel().get(0);
			if (allRows != null && allRows.length>= 1) {
				logger.debug("Clearing menumodel");
				menu.setIcon(ConsoleConstants.MENU_ICON_ON);
				action.menuListener();
			} else {
				menu.setIcon(ConsoleConstants.MENU_ICON_OFF);
			} // end if
			tab.getFreezeBean().freeze(true);
		}

	}

	public String getSelectAllButtonText() {
		return selectAllButtonText;
	}

	public void setSelectAllButtonText(String selectAllButtonText) {
		this.selectAllButtonText = selectAllButtonText;
	}

	public void reset() {
		this.allRows = null;
		this.selectAllButtonText =  ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_SELECT_ALL);;
		selectAll = false;
		DataTableBean tableBean = ConsoleHelper.getEventTableBean();
		EventBean[] events = tableBean.getEvents();
		if (events != null) {
			for (int i = 0; i < events.length; i++) {
				EventBean row = events[i];
				row.setSelected(false);
			} // end for
		} // end if

	}
}
