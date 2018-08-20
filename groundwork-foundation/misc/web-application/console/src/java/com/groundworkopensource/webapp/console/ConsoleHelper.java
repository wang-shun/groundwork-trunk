package com.groundworkopensource.webapp.console;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;

public class ConsoleHelper {
	/**
	 * Helper method to get the DataTableBean
	 * 
	 * @return
	 */
	public static DataTableBean getEventTableBean() {

		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(
				tabset.getTabIndex());
		DataTableBean eventTableBean = tab.getDataTableBean();
		return eventTableBean;
	}

	/**
	 * Helper method to get the Console manager
	 * 
	 * @return
	 */
	public static ConsoleManager getConsoleManager() {
		ConsoleManager mgr = null;
		Object consoleMgrObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_CONSOLE_MGR);
		if (consoleMgrObject instanceof ConsoleManager) {
			mgr = (ConsoleManager) consoleMgrObject;
		}
		return mgr;

	}

	/**
	 * Helper method to get the System FilterBean
	 * 
	 * @return
	 */
	public static SystemFilterTreeBean getSystemFilterBean() {
		SystemFilterTreeBean systemTree = null;
		Object systemFilterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_FILTER_TREE);
		if (systemFilterObject instanceof SystemFilterTreeBean) {
			systemTree = (SystemFilterTreeBean) systemFilterObject;
		}
		return systemTree;

	}

	/**
	 * Helper method to get the System FilterBean
	 * 
	 * @return
	 */
	public static PublicFilterTreeBean getPublicFilterBean() {
		PublicFilterTreeBean publicFilterTree = null;
		Object publicFilterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_PUBLIC_FILTER_TREE);
		if (publicFilterObject instanceof PublicFilterTreeBean) {
			publicFilterTree = (PublicFilterTreeBean) publicFilterObject;
		}
		return publicFilterTree;

	}

	/**
	 * Gets the filter bean in session
	 * 
	 * @return
	 */
	public static FilterBean getFilterBean() {
		FilterBean filterInSession = null;
		Object filterObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_FILTER);
		if (filterObject instanceof FilterBean) {
			filterInSession = (FilterBean) filterObject;
		}
		return filterInSession;
	}

	/**
	 * Helper method to get the Tabset
	 * 
	 * @return
	 */
	public static TabsetBean getTabSetBean() {
		TabsetBean tabset = null;
		Object tabsetObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_TABSET);
		if (tabsetObject instanceof TabsetBean) {
			tabset = (TabsetBean) tabsetObject;
		}
		return tabset;

	}

	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	public static ConsoleBean getConsoleBean() {
		ConsoleBean consoleBean = null;
		Object consoleObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_CONSOLE);
		if (consoleObject instanceof ConsoleBean) {
			consoleBean = (ConsoleBean) consoleObject;
		}
		return consoleBean;

	}

	
	/**
	 * Helper method to get the Popup
	 * 
	 * @return
	 */
	public static PopupBean getPopupBean() {
		PopupBean popup = null;
		Object popupObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN__POPUP);
		if (popupObject instanceof PopupBean) {
			popup = (PopupBean) popupObject;
		}
		return popup;

	}

	/**
	 * Helper method to get the Search
	 * 
	 * @return
	 */
	public static Filter createSearchFilter(SearchBean searchBean,
			FilterBean filterInSession) {
		Filter searchFilter = null;
		Filter hostFilter = null;
		if (searchBean.getHost() != null
				&& !searchBean.getHost().trim().equals(""))
			hostFilter = new Filter(ConsoleConstants.FILTER_DEVICE,
					FilterOperator.LIKE, searchBean.getHost());
		Filter messageFilter = null;
		if (searchBean.getMessage() != null
				&& !searchBean.getMessage().equals(""))
			messageFilter = new Filter(ConsoleConstants.FILTER_TEXT_MESSAGE,
					FilterOperator.LIKE, searchBean.getMessage());
		Filter dateRangeFromFilter = null;
		Filter dateRangeToFilter = null;
		if (searchBean != null && searchBean.getAgeType() != null
				&& searchBean.getAgeType().equals("preset")) {
			Calendar calFrom = null;
			if (searchBean.getPresetValue() != null) {
				if (!searchBean.getPresetValue().equalsIgnoreCase(
						SearchBean.PRESET_NONE)) {

					String presetValue = searchBean.getPresetValue();

					if (presetValue.equalsIgnoreCase(SearchBean.PRESET_LASTHR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -1);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST6HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -6);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST12HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -12);

					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST24HR)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.HOUR_OF_DAY, -24);
					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST30MINS)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.MINUTE, -30);
					} else if (presetValue
							.equalsIgnoreCase(SearchBean.PRESET_LAST10MINS)) {
						calFrom = Calendar.getInstance();
						calFrom.add(Calendar.MINUTE, -10);
					} // end if
					dateRangeFromFilter = new Filter(
							ConsoleConstants.FILTER_REPORT_DATE,
							FilterOperator.GE, calFrom.getTime());
					dateRangeToFilter = new Filter(
							ConsoleConstants.FILTER_REPORT_DATE,
							FilterOperator.LE, Calendar.getInstance().getTime());
				}

			}
		} else {
			if (searchBean.getAgeValueFrom() != null
					&& searchBean.getAgeValueTo() != null) {
				dateRangeFromFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.GE,
						searchBean.getAgeValueFrom());
				// to
				// datetime
				Date toDate = searchBean.getAgeValueTo();
				Calendar calToDate = Calendar.getInstance();
				calToDate.setTime(toDate);
				calToDate.add(Calendar.HOUR, 23);
				calToDate.add(Calendar.MINUTE, 59);
				calToDate.add(Calendar.SECOND, 59);

				dateRangeToFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.LE,
						calToDate.getTime()); // TODO:Convert

			}
		} // end if
		Map<String, Filter> searchCriteria = new HashMap<String, Filter>();
		searchCriteria.put("host", hostFilter);
		searchCriteria.put("message", messageFilter);
		searchCriteria.put("daterangefrom", dateRangeFromFilter);
		searchCriteria.put("daterangeto", dateRangeToFilter);

		Set<String> keySet = searchCriteria.keySet();
		Iterator<String> iter = keySet.iterator();
		Calendar temp = Calendar.getInstance();
		temp.add(Calendar.YEAR, -100);
		searchFilter = new Filter(ConsoleConstants.FILTER_REPORT_DATE,
				FilterOperator.GE, temp.getTime());
		if (filterInSession != null && filterInSession.getFilter() != null) {
			searchFilter = Filter
					.AND(filterInSession.getFilter(), searchFilter);
		} // end if

		while (iter.hasNext()) {
			String key = (String) iter.next();
			Filter filter = searchCriteria.get(key);
			if (filter != null) {
				searchFilter = Filter.AND(searchFilter, filter);
			} // endif
		} // end while
		return searchFilter;

	}

	/**
	 * Helper to highlight the tree text.
	 * @param nodeName
	 */
	public static void hightlightNode(String nodeName) {
		if (nodeName.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			ConsoleHelper.getSystemFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null)
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
		} else if (nodeName.equals(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			ConsoleHelper.getSystemFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null)
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
		}	else if (nodeName.equals(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			ConsoleHelper.getSystemFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null)
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
		} else if (nodeName
				.equals(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			ConsoleHelper.getSystemFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null)
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
		} else if (nodeName
				.equals(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT))) {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
			ConsoleHelper.getSystemFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
					.getPublicFilterBean().getSelectedNodeObject();
			if (pubFilterDynaNodeUserObj != null)
				pubFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
		} else {
			ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
					ConsoleConstants.STYLE_NONE);
			DynamicNodeUserObject sysFilterDynaNodeUserObj = ConsoleHelper
					.getSystemFilterBean().getSelectedNodeObject();
			if (sysFilterDynaNodeUserObj != null)
				sysFilterDynaNodeUserObj
						.setStyleClass(ConsoleConstants.STYLE_NONE);
			ConsoleHelper.getPublicFilterBean().getSelectedNodeObject()
					.setStyleClass(ConsoleConstants.STYLE_HIGHLIGHT_NAVIGATOR);
		} // end if
	} // end method

	/**
	 * Gets the public filters configuration
	 * @return
	 */
	public static PublicFiltersConfigBean getPublicFilters() {
		PublicFiltersConfigBean pubFilters = null;
		ExternalContext exContext = FacesContext.getCurrentInstance()
				.getExternalContext();
		if (exContext != null) {
			Object context = exContext.getContext();
			if (context != null) {
				ServletContext servContext = (ServletContext) exContext
						.getContext();
				pubFilters = (PublicFiltersConfigBean) servContext
						.getAttribute(ConsoleConstants.CONSOLE_ADMIN_CONFIG_PROP);
			} // end if
		} // end if
		return pubFilters;
	}
	
	
	/**
	 * Helper method to get the LocaleBean
	 * 
	 * @return
	 */
	public static LocaleBean getLocaleBean() {

		LocaleBean localeBean = null;
		Object localeObject = ManagedBeanFactory
				.getManagedBean(ConsoleConstants.MANAGED_BEAN_LOCALE);
		if (localeObject instanceof LocaleBean) {
			localeBean = (LocaleBean) localeObject;
		}
		return localeBean;
	}

}
