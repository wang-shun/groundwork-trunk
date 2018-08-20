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

import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCategory;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSHost;
import org.groundwork.foundation.ws.api.WSService;
import org.groundwork.foundation.ws.impl.WSCommonServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.impl.WSServiceServiceLocator;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.ApplicationType;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public class ConsoleManager {

	public static Logger logger = Logger.getLogger(ConsoleManager.class
			.getName());

	private SearchBean searchBean;
	private FilterBean filterInSession = null;

	/**
	 * Constructor for ConsoleManager
	 */
	public ConsoleManager() {
		logger.debug("Enter ConsoleManager Constructor");
		filterInSession = ConsoleHelper.getFilterBean();
		logger.debug("Exit ConsoleManager Constructor");
	}

	/**
	 * Performs search on a filter
	 */
	public void performSearch() {
		logger.debug("Enter performSearch method");
		this.refreshSearchBean();
		if (!CommonUtils.isEmpty(searchBean.getHost())
				|| !CommonUtils.isEmpty(searchBean.getMessage())
				|| (searchBean.getPresetValue() != null && !searchBean
						.getPresetValue().equalsIgnoreCase(
								SearchBean.PRESET_NONE))
				|| (!CommonUtils.isEmpty(searchBean.getAgeValueFrom()) && !CommonUtils
						.isEmpty(searchBean.getAgeValueTo()))) {
			if (!CommonUtils.isEmpty(searchBean.getAgeValueFrom())
					&& !CommonUtils.isEmpty(searchBean.getAgeValueTo())) {
				if (searchBean.getAgeValueTo().before(
						searchBean.getAgeValueFrom())) {
					PopupBean popup = ConsoleHelper.getPopupBean();
					popup.setShowModalPanel(true);
					popup.setShowDraggablePanel(false);
					popup.setTitle("Error");
					popup
							.setMessage(ResourceUtils
									.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_ERROR_INVALID_DATERANGE));
					return;
				} // end if

			} // end if
			Filter searchFilter = ConsoleHelper.createSearchFilter(searchBean,
					filterInSession);
			FilterBean filterBean = ConsoleHelper.getFilterBean();
			filterBean.setFilter(searchFilter);
			this.initializePage(false);
		} else {
			PopupBean popup = ConsoleHelper.getPopupBean();
			popup.setShowModalPanel(true);
			popup.setShowDraggablePanel(false);
			popup.setTitle("Error");
			popup
					.setMessage(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_ERROR_INVALID_SEARCH));
		} // end if
		logger.debug("Exit performSearch method");

	}

	/**
	 * Clears the search
	 */
	public void clearSearch() {
		logger.debug("Enter clearSearch method");
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		SearchBean searchBean = tab.getSearchCriteria();
		searchBean.reset();
		String tabLabel = tab.getHiddenLabel();
		if (tabLabel != null) {
			int filterNameIndex = tabLabel.indexOf("=");
			String filter = tabLabel.substring(filterNameIndex + 1, tabLabel
					.length());
			String filterType = tab.getFilterType();
			logger.debug("Filter=" + filter + "--FilterType=" + filterType);
			if (filterType != null) {

				if (filterType
						.equals(ConsoleConstants.SYSTEM_FILTER_HOST_GROUPS)) {
					populateEventsByHostGroup(filter, null);
				} else if (filterType
						.equals(ConsoleConstants.SYSTEM_FILTER_SERVICE_GROUPS)) {
					populateEventsByServiceGroup(filter, null);
				} else if (filterType
						.equals(ConsoleConstants.SYSTEM_FILTER_APPLICATIONS)) {
					populateEventsByApplicationType(filter, null);
				} else if (filterType
						.equals(ConsoleConstants.SYSTEM_FILTER_OPERATION_STATUS)) {
					populateEventsByOperationStatus(filter, null);
				} else {
					PublicFiltersConfigBean configBean = ConsoleHelper
							.getPublicFilters();
					Vector<FilterConfigBean> filters = configBean
							.getFilterConfigs();
					for (int i = 0; i < filters.size(); i++) {
						FilterConfigBean filterConfig = filters.get(i);
						if (filter.equalsIgnoreCase(filterConfig.getLabel())) {
							populateEventsByCombinedFilters(filterConfig, null);
							break;
						} // end if
					} // end for
				}
			} else {
				populateEventsByOperationStatus(
						ConsoleConstants.OPERATION_STATUS_OPEN, null);
			} // end if
		} else {
			populateEventsByOperationStatus(
					ConsoleConstants.OPERATION_STATUS_OPEN, null);
		}

		logger.debug("Exit clearSearch method");

	}

	/**
	 * Displays the events
	 * 
	 * @param eventList
	 */
	private void displayEvents(List<EventBean> eventList) {
		logger.debug("Enter displayEvents method");
		EventBean[] events = (EventBean[]) eventList
				.toArray(new EventBean[eventList.size()]);

		DataTableBean eventTableBean = ConsoleHelper.getEventTableBean();

		if (eventTableBean != null)
			eventTableBean.setEvents(events);

		logger.debug("Exit displayEvents method");
	}

	/**
	 * 
	 * @param hostGroup
	 */
	public void populateEventsByHostGroup(String hostGroup, Filter searchFilter) {
		logger.debug("Enter populateEventsByHostGroup method");
		Filter hostGroupsFilter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
				FilterOperator.EQ, hostGroup);
		if (filterInSession != null)
			filterInSession.setFilter(hostGroupsFilter);
		if (searchFilter != null)
			filterInSession.setFilter(Filter
					.AND(hostGroupsFilter, searchFilter));
		this.initializePage(true);
		logger.debug("Exit populateEventsByHostGroup method");
	}

	/**
	 * 
	 * @param Service
	 *            Group
	 */
	public void populateEventsByServiceGroup(String serviceGroup,
			Filter searchFilter) {
		logger.debug("Enter populateEventsByHostGroup method");
		Filter serviceGroupsFilter = new Filter(
				ConsoleConstants.PROP_NAME_CATEGORY_NAME, FilterOperator.EQ,
				serviceGroup);
		if (filterInSession != null)
			filterInSession.setFilter(serviceGroupsFilter);
		if (searchFilter != null)
			filterInSession.setFilter(Filter.AND(serviceGroupsFilter,
					searchFilter));
		this.initializePage(true);

	}

	/**
	 * Populates event by application type
	 * 
	 * @param appType
	 */
	public void populateEventsByApplicationType(String appType,
			Filter searchFilter) {
		logger.debug("Enter populateEventsByApplicationType method");

		Filter applicationFilter = new Filter(
				ConsoleConstants.PROP_NAME_APP_TYPE, FilterOperator.EQ, appType);
		if (filterInSession != null)
			filterInSession.setFilter(applicationFilter);
		if (searchFilter != null)
			filterInSession.setFilter(Filter.AND(applicationFilter,
					searchFilter));
		try {

			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
			WSFoundationCollection col = wsCommon.getEntityTypeProperties(
					ConsoleConstants.ENTITY_TYPE_LOGMESSAGE, appType, false);
			EntityTypeProperty[] entityTypeProperties = col
					.getEntityTypeProperty();
			// Set the dynamic columns
			if (appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS)) {
				EntityTypeProperty nagiosServiceProp = new EntityTypeProperty();
				nagiosServiceProp
						.setName(ConsoleConstants.NAGIOS_SERVICE_COLUMN);
				ApplicationType nagiosAppType = new ApplicationType();
				nagiosAppType.setName(ConsoleConstants.APP_TYPE_NAGIOS);
				nagiosServiceProp.setApplicationType(nagiosAppType);
				entityTypeProperties[entityTypeProperties.length - 1] = nagiosServiceProp;
			} // end if
			ConsoleHelper.getEventTableBean().setDynamicColumns(
					entityTypeProperties);

		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
		this.initializePage(false);
		logger.debug("Enter populateEventsByApplicationType method");
	}

	/**
	 * Populate events by operation status
	 */
	public void populateEventsByOperationStatus(String opStatus,
			Filter searchFilter) {
		logger.debug("Enter populateEventsByOperationStatus method");
		Filter operationStatusFilter = new Filter(
				ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
				opStatus);
		if (filterInSession != null)
			filterInSession.setFilter(operationStatusFilter);
		if (searchFilter != null)
			filterInSession.setFilter(Filter.AND(operationStatusFilter,
					searchFilter));
		this.initializePage(true);
		logger.debug("Exit populateEventsByOperationStatus method");
	}

	/**
	 * Populates all Open events
	 */
	public void populateAllOpenEvents() {
		logger.debug("Enter populateAllOpenEvents method");
		ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
				"highlightNavigator");
		DynamicNodeUserObject sysFilterDynaNodeUserObj = ConsoleHelper
				.getSystemFilterBean().getSelectedNodeObject();
		if (sysFilterDynaNodeUserObj != null)
			sysFilterDynaNodeUserObj.setStyleClass(ConsoleConstants.STYLE_NONE);
		DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
				.getPublicFilterBean().getSelectedNodeObject();
		if (pubFilterDynaNodeUserObj != null)
			pubFilterDynaNodeUserObj.setStyleClass(ConsoleConstants.STYLE_NONE);

		populateAllOpenEvents(null);
		logger.debug("Exit populateEventsByOperationStatus method");
	}

	public void populateAllOpenEvents(Filter searchFilter) {
		logger.debug("Enter populateAllOpenEvents method");
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex()); // reset the
		// select button
		MessageSelectBean msgSelectBean = tab.getMsgSelector();
		msgSelectBean.reset();
		tab.getActionBean().reset();
		if (searchFilter == null) {
			tab
					.setLabel(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
			tab
					.setHiddenLabel(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
		}
		tab.setRendered(true);
		tab.setFilterType(null);
		Filter operationStatusFilter = new Filter(
				ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
				ConsoleConstants.OPERATION_STATUS_OPEN);
		if (filterInSession != null) {
			if (searchFilter != null) {
				filterInSession.setFilter(Filter.AND(searchFilter,
						operationStatusFilter));
			} else {
				filterInSession.setFilter(operationStatusFilter);
			} // end if
		} // end if

		this.initializePage(true);
		logger.debug("Exit populateAllOpenEvents method");
	}

	/**
	 * 
	 * @param severity
	 */
	public void populateEventsBySeverity(String severity, Filter searchFilter) {
		logger.debug("Enter populateEventsBySeverity method");
		Filter severityFilter = new Filter(
				ConsoleConstants.PROP_NAME_MONITOR_STATUS, FilterOperator.EQ,
				severity);
		if (filterInSession != null)
			filterInSession.setFilter(severityFilter);
		if (searchFilter != null)
			filterInSession.setFilter(Filter.AND(severityFilter, searchFilter));
		this.initializePage(true);
		logger.debug("Exit populateEventsBySeverity method");
	}

	/**
	 * Populate events by for the public filters
	 */
	public void populateEventsByCombinedFilters(FilterConfigBean pubFilter,
			Filter searchFilter) {
		logger.debug("Enter populateEventsByCombinedFilters method");
		boolean resetColumns = true;
		int pageSize = -1;
		boolean asc = true;
		String DELIMITER = ",";
		String DESC = "DESC"; // Descending order
		String MIN = "MIN"; // Minutes
		String sortColumn = ConsoleConstants.FILTER_REPORT_DATE;
		Calendar temp = Calendar.getInstance();
		temp.add(Calendar.YEAR, -100);
		Filter combFilter = null;
		if (!CommonUtils.isEmpty(pubFilter.getAppType())) {
			if (combFilter != null) {
				Filter filter = new Filter(ConsoleConstants.PROP_NAME_APP_TYPE,
						FilterOperator.EQ, pubFilter.getAppType());
				combFilter = Filter.AND(combFilter, filter);
			} else {
				combFilter = new Filter(ConsoleConstants.PROP_NAME_APP_TYPE,
						FilterOperator.EQ, pubFilter.getAppType());
			} // end if

			// Application type is set in the config file, then populate the
			// dynamic columns
			String appType = pubFilter.getAppType();
			WSFoundationCollection col = null;
			try {
				WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
				col = wsCommon
						.getEntityTypeProperties(
								ConsoleConstants.ENTITY_TYPE_LOGMESSAGE,
								appType, false);
			} catch (Exception exc) {
				logger.error(exc.getMessage());
			} // end try catch
			EntityTypeProperty[] entityTypeProperties = col
					.getEntityTypeProperty();
			// Set the dynamic columns
			if (appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS)) {
				EntityTypeProperty nagiosServiceProp = new EntityTypeProperty();
				nagiosServiceProp
						.setName(ConsoleConstants.NAGIOS_SERVICE_COLUMN);
				ApplicationType nagiosAppType = new ApplicationType();
				nagiosAppType.setName(ConsoleConstants.APP_TYPE_NAGIOS);
				nagiosServiceProp.setApplicationType(nagiosAppType);
				entityTypeProperties[entityTypeProperties.length - 1] = nagiosServiceProp;
			} // end if

			ConsoleHelper.getEventTableBean().setDynamicColumns(
					entityTypeProperties);
			resetColumns = false;
		} // end if

		// Add the hostgroup filter if available
		if (!CommonUtils.isEmpty(pubFilter.getHostGroup())) {
			if (combFilter != null) {
				Filter filter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
						FilterOperator.EQ, pubFilter.getHostGroup());
				combFilter = Filter.AND(combFilter, filter);
			} else {
				combFilter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
						FilterOperator.EQ, pubFilter.getHostGroup());
			} // end if
		} // end if

		if (!CommonUtils.isEmpty(pubFilter.getOpStatus())) {
			StringTokenizer stkn = new StringTokenizer(pubFilter.getOpStatus(),
					DELIMITER);
			int count = stkn.countTokens(); // counter for the operation status.
			// Default dummy filter to start with
			Filter opFilter = new Filter(
					ConsoleConstants.PROP_NAME_OPERATION_STATUS,
					FilterOperator.EQ, "DUMMY");
			while (stkn.hasMoreTokens()) {
				String opStatus = stkn.nextToken();
				Filter filter = new Filter(
						ConsoleConstants.PROP_NAME_OPERATION_STATUS,
						FilterOperator.EQ, opStatus);
				if (combFilter != null) {
					if (count > 1) // if multiple opStatus defined, then create
						// an OR filter and add it to the final
						// filter
						opFilter = Filter.OR(opFilter, filter);
					else // or Just AND it
					{
						combFilter = Filter.AND(combFilter, filter);
					} // end if
				} else {
					if (count > 1) {
						combFilter = new Filter(
								ConsoleConstants.FILTER_REPORT_DATE,
								FilterOperator.GE, temp.getTime());

						opFilter = Filter.OR(opFilter, filter);
					} else
						combFilter = new Filter(
								ConsoleConstants.PROP_NAME_OPERATION_STATUS,
								FilterOperator.EQ, opStatus);
				} // end if
			} // end if
			if (count > 1)
				combFilter = Filter.AND(combFilter, opFilter);
		} // end if
		if (!CommonUtils.isEmpty(pubFilter.getMonitorStatus())) {
			if (combFilter != null) {
				Filter filter = new Filter(
						ConsoleConstants.PROP_NAME_MONITOR_STATUS,
						FilterOperator.EQ, pubFilter.getMonitorStatus());
				combFilter = Filter.AND(combFilter, filter);
			} else {
				combFilter = new Filter(
						ConsoleConstants.PROP_NAME_MONITOR_STATUS,
						FilterOperator.EQ, pubFilter.getMonitorStatus());
			} // end if
		} // end if

		if (!CommonUtils.isEmpty(pubFilter.getSeverity())) {
			if (combFilter != null) {
				Filter filter = new Filter(
						ConsoleConstants.PROP_NAME_MONITOR_SEVERITY,
						FilterOperator.EQ, pubFilter.getSeverity());
				combFilter = Filter.AND(combFilter, filter);
			} else {
				combFilter = new Filter(
						ConsoleConstants.PROP_NAME_MONITOR_SEVERITY,
						FilterOperator.EQ, pubFilter.getSeverity());
			} // end if
		} // end if

		// Count Config
		FetchConfigBean fetch = pubFilter.getFetch();
		if (fetch != null) {
			if (!CommonUtils.isEmpty(fetch.getSize()))
				pageSize = Integer.parseInt(fetch.getSize());
			if (!CommonUtils.isEmpty(fetch.getOrder()))
				asc = (fetch.getOrder().equalsIgnoreCase(DESC) ? false : true);
		} // end if

		// Time Config
		TimeConfigBean time = pubFilter.getTime();
		if (time != null) {
			int unit = -1;
			if (!CommonUtils.isEmpty(time.getUnit())) {
				unit = Integer.parseInt(time.getUnit());
			}
			String measurement = time.getMeasurement();

			if (!CommonUtils.isEmpty(measurement)) {
				Calendar calFrom = Calendar.getInstance();
				calFrom.add(measurement.equalsIgnoreCase(MIN) ? Calendar.MINUTE
						: Calendar.SECOND, unit);
				Filter dateRangeFromFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.GE,
						calFrom.getTime());
				Filter dateRangeToFilter = new Filter(
						ConsoleConstants.FILTER_REPORT_DATE, FilterOperator.LE,
						Calendar.getInstance().getTime());
				if (combFilter != null) {

					combFilter = Filter.AND(combFilter, Filter.AND(
							dateRangeFromFilter, dateRangeToFilter));
				} else {
					combFilter = Filter.AND(dateRangeFromFilter,
							dateRangeToFilter);
				} // end if

			} // end if
		} // en dif

		if (searchFilter != null && combFilter != null) {
			filterInSession.setFilter(Filter.AND(combFilter, searchFilter));
		} // end if
		if (searchFilter != null) {
			filterInSession.setFilter(searchFilter);
		} // end if
		if (combFilter == null) {
			Filter tempFilter = new Filter(ConsoleConstants.FILTER_REPORT_DATE,
					FilterOperator.GE, temp.getTime());
			if (searchFilter != null)
				filterInSession.setFilter(Filter.AND(tempFilter, searchFilter));
			else
				filterInSession.setFilter(tempFilter);
		} else {
			if (searchFilter != null)
				filterInSession.setFilter(Filter.AND(combFilter, searchFilter));
			else
				filterInSession.setFilter(combFilter);
		} // end if

		this.refreshPage(resetColumns, pageSize, asc, sortColumn);
		logger.debug("Exit populateEventsByOperationStatus method");
	}

	/**
	 * Gets actions by application type
	 * 
	 * @param appType
	 * @return
	 */
	public Action[] getActionsByApplicationType(String appType) {
		return ActionClient.getActionsByApplicationType(appType);

	}

	/**
	 * Performs action for the given message IDs and ActionID
	 * 
	 * @param appType
	 * @return
	 */
	public ActionReturn performAction(int[] messageIds, int actionID,
			String appType) {

		ActionReturn actionReturn = null;
		if (appType == null)
			appType = ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM;
		try {

			WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();

			ActionPerform[] actionPerforms = new ActionPerform[1];
			StringProperty[] parameters = new StringProperty[11];
			parameters[0] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_LOG_MESS_IDS, this
							.convertToCommaSepString(messageIds));
			String userName = (String) ManagedBeanFactory
					.getManagedBean(ConsoleConstants.SESSION_LOGIN_USER);
			logger.debug("Logged in user is" + userName);
			parameters[1] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_USER_NAME, userName);
			parameters[2] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_SEND_NOTIFY,
					PropertyUtils
							.getProperty(ConsoleConstants.PROP_NAGIOS_SEND_NOTIFY));
			parameters[3] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_PERSIST_COMMENT,
					PropertyUtils
							.getProperty(ConsoleConstants.PROP_NAGIOS_PERSIST_COMMENT));
			parameters[4] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_COMMENT,
					ConsoleConstants.ACTION_PARAM_VALUE_COMMENT_PREFIX
							+ DateUtils
									.format(
											Calendar.getInstance().getTime(),
											PropertyUtils
													.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));

			parameters[5] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_NSCA_HOST,
					ConsoleConstants.DEFAULT_NSCA_HOST);
			parameters[6] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_USER, userName);
			parameters[7] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_NSCA_COMMENT,
					ConsoleConstants.SUBMIT_PASSIVE_RESET_COMMENT + userName);
			parameters[8] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_HOST, this
							.generateHostString(messageIds));

			if (appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_SNMPTRAP))

				parameters[9] = new StringProperty(
						ConsoleConstants.ACTION_PARAM_SERVICE,
						PropertyUtils
								.getProperty(ConsoleConstants.SERVICE_SNMPTRAP_LAST));
			else
				parameters[9] = new StringProperty(
						ConsoleConstants.ACTION_PARAM_SERVICE,
						PropertyUtils
								.getProperty(ConsoleConstants.SERVICE_SYSLOG_LAST));
			parameters[10] = new StringProperty(
					ConsoleConstants.ACTION_PARAM_STATE,
					ConsoleConstants.DEFAULT_NSCA_STATE);

			actionPerforms[0] = new ActionPerform(actionID, parameters);

			// Perform the actions - An ActionReturn instance will be returned
			// for each action performed.
			WSFoundationCollection col = wsCommon
					.performActions(actionPerforms);

			actionReturn = col.getActionReturn()[0];

		} catch (Exception exc) {
			logger.error(exc.getMessage());

		}
		return actionReturn;

	}

	/**
	 * Converts array of message ids to comma seperated string.
	 * 
	 * @param messageIds
	 * @return
	 */
	private String convertToCommaSepString(int[] messageIds) {
		StringBuffer buf = new StringBuffer();
		String DELIM = ",";
		for (int i = 0; i < messageIds.length; i++) {
			buf.append(messageIds[i]);
			buf.append(DELIM);
		}
		return buf.toString().substring(0, buf.toString().length() - 1);
	}

	/**
	 * Initializes the page
	 * 
	 * @param resetDynaColumns
	 */
	private void initializePage(boolean resetDynaColumns) {
		DataTableBean eventTableBean = ConsoleHelper.getEventTableBean();
		eventTableBean.setLastPage(null);
		eventTableBean.setLastStartRow(-1);
		List<EventBean> eventList = eventTableBean.fetchPage(
				0,
				Integer.parseInt(PropertyUtils
						.getProperty(ConsoleConstants.PROP_PAGE_SIZE)))
				.getData();
		if (resetDynaColumns) {
			eventTableBean.setDynamicColumns(null);
		} // end if
		eventTableBean.constructComponent();
		eventTableBean.setPage(null);
		this.displayEvents(eventList);

	}

	/**
	 * Initializes the page
	 * 
	 * @param resetDynaColumns
	 */
	private void refreshPage(boolean resetDynaColumns, int pageSize,
			boolean asc, String sortColumn) {
		DataTableBean eventTableBean = ConsoleHelper.getEventTableBean();
		eventTableBean.setLastPage(null);
		eventTableBean.setLastStartRow(-1);
		eventTableBean.setAscending(asc);
		if (CommonUtils.isEmpty(sortColumn))
			eventTableBean.setSortColumnName(sortColumn);
		if (pageSize == -1)
			pageSize = Integer.parseInt(PropertyUtils
					.getProperty(ConsoleConstants.PROP_PAGE_SIZE));
		List<EventBean> eventList = eventTableBean.fetchPage(0, pageSize)
				.getData();
		if (resetDynaColumns) {
			eventTableBean.setDynamicColumns(null);
		} // end if
		eventTableBean.constructComponent();
		eventTableBean.setPage(null);
		this.displayEvents(eventList);

	}

	private void refreshSearchBean() {
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		/*
		 * Tab tab = tabset.getTabs().get( tabset.getTabIndex());
		 */
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		searchBean = tab.getSearchCriteria();
	}

	/**
	 * Listener to toggle the select all button if rows selected and then
	 * paginated.
	 * 
	 * @param e
	 */
	public void paginatorClicked(ActionEvent e) {
		logger.debug("Paginator clicked..");
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());

		FreezeTableBean freezeBean = tab.getFreezeBean();
		if (freezeBean
				.getFreezeButtonText()
				.equalsIgnoreCase(
						ResourceUtils
								.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS)))
			freezeBean.reset();
		MessageSelectBean msgSelectBean = tab.getMsgSelector();
		tab.getActionBean().reset();
		if (msgSelectBean.getAllRows() != null
				&& msgSelectBean.getAllRows().length > 0) {
			PopupBean popup = ConsoleHelper.getPopupBean();
			popup.setShowModalPanel(true);
			popup.setShowDraggablePanel(false);
			popup.setTitle("Warning");
			popup
					.setMessage(ResourceUtils
							.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_ERROR_PAGINATION));
			msgSelectBean.reset();
		} // end if
	}

	/**
	 * Generates the host String to pass to the external script.
	 * 
	 * @param messageIds
	 * @return
	 */
	private String generateHostString(int[] messageIds) {
		String result = null;
		String DELIMITER = ",";
		StringBuffer sb = new StringBuffer();
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		Tab tab = tabset.getTabs().get(tabset.getTabIndex());
		EventBean[] events = tab.getDataTableBean().getEvents();
		for (int i = 0; i < messageIds.length; i++) {
			for (int j = 0; j < events.length; j++) {
				if (messageIds[0] == events[j].getLogMessageID()) {
					sb.append(events[j].getDevice());
					sb.append(DELIMITER);
				} // end if
			} // end if
		} // end if
		if (sb.toString() != null && sb.toString().endsWith(DELIMITER)) {
			result = sb.toString().substring(0, sb.toString().length() - 1);
			logger.debug("Generated host String is " + result);
		} // end if
		return result;
	} // end method

	/**
	 * Gets the host details for the give host.
	 * 
	 * @param hostName
	 * @return
	 */
	public HostDetailBean getHostDetails(String hostName) {
		HostDetailBean hostDetail = new HostDetailBean();
		WSHostServiceLocator hostLocator = ServiceLocator.hostLocator();
		WSServiceServiceLocator serviceLocator = ServiceLocator
				.serviceLocator();
		try {

			WSService wsService = serviceLocator.getwsservice();

			WSHost wsHost = hostLocator.gethost();
			Filter hostFilter = new Filter(ConsoleConstants.FILTER_DEVICE,
					FilterOperator.EQ, hostName);
			// Sort sort = new Sort(true, "device.identification");
			WSFoundationCollection col = wsHost.getHostsByCriteria(hostFilter,
					null, -1, -1);
			Host[] hosts = col.getHost();
			if (hosts != null) {
				MonitorStatus monStatus = hosts[0].getMonitorStatus();
				String hostStatus = monStatus.getName();
				hostDetail.setHostName(hostName);
				hostDetail.setStatus(hostStatus);
				hostDetail.setLastCheckTime(DateUtils.format(hosts[0]
						.getLastCheckTime(), PropertyUtils
						.getProperty(DateUtils.CONSOLE_DATETIME_PATTERN)));
				// Get the list of services for the host

				Filter serviceFilter = new Filter("host.hostName",
						FilterOperator.EQ, hosts[0].getName());
				WSFoundationCollection serCol = wsService
						.getServicesByCriteria(serviceFilter, null, -1, -1);
				ServiceStatus[] services = serCol.getServiceStatus();
				hostDetail.setServiceStatus(services);
			}
		} catch (Exception exc) {
			logger.error(exc.getMessage());
			System.err.println(exc.getMessage());
		} // end try/catch

		return hostDetail;
	}

}
