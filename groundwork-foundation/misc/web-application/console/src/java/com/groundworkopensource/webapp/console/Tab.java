/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package com.groundworkopensource.webapp.console;

import com.icesoft.faces.context.effects.Effect;

public class Tab {
	public static final String SEARCH_PANELID_PREFIX = "searchPanel";
	private String tabId = SEARCH_PANELID_PREFIX + "0";
	private String label;
	private String hiddenLabel;
	private String filterType;
	private boolean expandSearchBar = true;
	private boolean rendered = false;
	private SearchBean searchCriteria = new SearchBean();
	private FreezeTableBean freezeBean = new FreezeTableBean();
	private MessageSelectBean msgSelector = new MessageSelectBean();
	private DataTableBean dataTableBean = null;
	private ActionBean actionBean = new ActionBean();
	private Effect newMessageEffect = null;

	public Tab() {
		label =ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT);
	}

	public Tab(String label) {
		this.label = label;
		if (!this.label.equalsIgnoreCase(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_NEW))) {
			dataTableBean = new DataTableBean();
		} // end if

	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public boolean isExpandSearchBar() {
		return expandSearchBar;
	}

	public void setExpandSearchBar(boolean expandSearchBar) {
		this.expandSearchBar = expandSearchBar;
	}

	public String getTabId() {
		return tabId;
	}

	public void setTabId(String tabId) {
		this.tabId = tabId;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;

	}

	public SearchBean getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(SearchBean searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	public void resetSearchCriteria() {
		searchCriteria = new SearchBean();
	}

	public MessageSelectBean getMsgSelector() {
		return msgSelector;
	}

	public void setMsgSelector(MessageSelectBean msgSelector) {
		this.msgSelector = msgSelector;
	}

	public FreezeTableBean getFreezeBean() {
		return freezeBean;
	}

	public void setFreezeBean(FreezeTableBean freezeBean) {
		this.freezeBean = freezeBean;
	}

	public DataTableBean getDataTableBean() {
		return dataTableBean;
	}

	public void setDataTableBean(DataTableBean dataTableBean) {
		this.dataTableBean = dataTableBean;
	}

	public ActionBean getActionBean() {
		return actionBean;
	}

	public void setActionBean(ActionBean actionBean) {
		this.actionBean = actionBean;
	}

	public void reset() {
		searchCriteria = new SearchBean();
		freezeBean = new FreezeTableBean();
		msgSelector = new MessageSelectBean();
		dataTableBean = new DataTableBean();
		actionBean = new ActionBean();
	}

	public Effect getNewMessageEffect() {
		return newMessageEffect;
	}

	public void setNewMessageEffect(Effect newMessageEffect) {
		this.newMessageEffect = newMessageEffect;
	}

	public String getHiddenLabel() {
		return hiddenLabel;
	}

	public void setHiddenLabel(String hiddenLabel) {
		this.hiddenLabel = hiddenLabel;
	}

}
