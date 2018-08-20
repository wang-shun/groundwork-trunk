/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import javax.faces.event.ActionEvent;

import com.icesoft.faces.context.effects.BlindDown;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Shrink;

public class ConsoleBean {
	private boolean navigationVisible = true;
	private String sideBarArrow = null;
	private Effect effect;
	private String tooltip = null;
	private String allEventsStyleClass = null;
	private String sideBarCollapseIconStyleClass = null;
	private String layoutWrapperStyleClass = null;
	private boolean searchPanelVisible = true;
	private String searchPanelImage = null;
	private String searchPanelStyle = null;

	public ConsoleBean() {
		allEventsStyleClass = ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR;
		layoutWrapperStyleClass = ConsoleConstants.STYLE_LAYOUTWRAPPER;
		sideBarCollapseIconStyleClass = ConsoleConstants.ICON_SIDEBAR_COLLAPSE;
		tooltip = ConsoleConstants.TOOLTIP_HIDE_NAVIGATION;
		sideBarArrow = ConsoleConstants.IMG_SIDEBAR_ARROW_LEFT;
		searchPanelImage = ConsoleConstants.IMG_SEARCHBAR_UP;
		searchPanelStyle = ConsoleConstants.STYLE_SHOW_SEARCHPANEL;
	}

	public String getSideBarArrow() {
		return sideBarArrow;
	}

	public void setSideBarArrow(String sideBarArrow) {
		this.sideBarArrow = sideBarArrow;
	}

	public void hideTree(ActionEvent e) {

		navigationVisible = !navigationVisible;
		if (navigationVisible) {

			sideBarArrow = ConsoleConstants.IMG_SIDEBAR_ARROW_LEFT;
			BlindDown blindDown = new BlindDown();
			this.setEffect(blindDown);
			tooltip = ConsoleConstants.TOOLTIP_HIDE_NAVIGATION;
			sideBarCollapseIconStyleClass = ConsoleConstants.ICON_SIDEBAR_COLLAPSE;
			layoutWrapperStyleClass = ConsoleConstants.STYLE_LAYOUTWRAPPER;
		} else {
			sideBarArrow = ConsoleConstants.IMG_SIDEBAR_ARROW_RIGHT;
			Shrink shrink = new Shrink();
			this.setEffect(shrink);
			tooltip = ConsoleConstants.TOOLTIP_SHOW_NAVIGATION;
			sideBarCollapseIconStyleClass = ConsoleConstants.ICON_SIDEBAR_COLLAPSE_HIDE;
			layoutWrapperStyleClass = ConsoleConstants.STYLE_LAYOUTWRAPPER_HIDE_SIDEBAR;
		} // end if

	}
	
	public void hideSearchPanel(ActionEvent e) {

		searchPanelVisible = !searchPanelVisible;
		if (searchPanelVisible) {
			searchPanelImage = ConsoleConstants.IMG_SEARCHBAR_UP;
			searchPanelStyle = ConsoleConstants.STYLE_SHOW_SEARCHPANEL;
		} else {
			searchPanelImage = ConsoleConstants.IMG_SEARCHBAR_DOWN;
			searchPanelStyle = ConsoleConstants.STYLE_HIDE_SEARCHPANEL;
		} // end if

	}

	public Effect getEffect() {
		return effect;
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getAllEventsStyleClass() {
		return allEventsStyleClass;
	}

	public void setAllEventsStyleClass(String allEventsStyleClass) {
		this.allEventsStyleClass = allEventsStyleClass;
	}

	public boolean isNavigationVisible() {
		return navigationVisible;
	}

	public void setNavigationVisible(boolean navigationVisible) {
		this.navigationVisible = navigationVisible;
	}

	public String getSideBarCollapseIconStyleClass() {
		return sideBarCollapseIconStyleClass;
	}

	public void setSideBarCollapseIconStyleClass(
			String sideBarCollapseIconStyleClass) {
		this.sideBarCollapseIconStyleClass = sideBarCollapseIconStyleClass;
	}

	public String getLayoutWrapperStyleClass() {
		return layoutWrapperStyleClass;
	}

	public void setLayoutWrapperStyleClass(String layoutWrapperStyleClass) {
		this.layoutWrapperStyleClass = layoutWrapperStyleClass;
	}

	public boolean isSearchPanelVisible() {
		return searchPanelVisible;
	}

	public void setSearchPanelVisible(boolean searchPanelVisible) {
		this.searchPanelVisible = searchPanelVisible;
	}

	public String getSearchPanelImage() {
		return searchPanelImage;
	}

	public void setSearchPanelImage(String searchPanelImage) {
		this.searchPanelImage = searchPanelImage;
	}

	public String getSearchPanelStyle() {
		return searchPanelStyle;
	}

	public void setSearchPanelStyle(String searchPanelStyle) {
		this.searchPanelStyle = searchPanelStyle;
	}

}
