/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.faces.event.ActionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSCommon;
import org.groundwork.foundation.ws.api.WSHost;
import org.groundwork.foundation.ws.api.WSService;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.impl.WSServiceServiceLocator;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.ActionReturn;
import org.groundwork.foundation.ws.model.impl.ApplicationType;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.DateProperty;
import org.groundwork.foundation.ws.model.impl.DoubleProperty;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.groundwork.foundation.ws.model.impl.LongProperty;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.groundworkopensource.portal.common.NavigationHelper;
import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.context.effects.JavascriptContext;
import javax.faces.context.FacesContext;

/**
 * The Class ConsoleManager.
 */
public class ConsoleManager {

    /** The logger. */
    private static Logger logger = Logger.getLogger(ConsoleManager.class
            .getName());

    /** The search bean. */
    private SearchBean searchBean;

    /** The filter in session. */
    private FilterBean filterInSession = null;

    /** NavigationHelper. */
    private NavigationHelper navigationHelper;

    /** alarm Button Text. */
    private String alarmButtonText;

    /** mute Button Image path. */
    private String muteButtonImage = ConsoleConstants.IMAGES_MUTE_JPEG;
    
    private static final String DELIMITER_COLON = ":";

    /**
     * Constructor for ConsoleManager.
     */
    public ConsoleManager() {
        logger.debug("Enter ConsoleManager Constructor");
        filterInSession = ConsoleHelper.getFilterBean();
        navigationHelper = new NavigationHelper();
        alarmButtonText = ResourceUtils
                .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_ALARM);
        logger.debug("Exit ConsoleManager Constructor");

    }

    /**
     * Performs search on a filter.
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
                        .isEmpty(searchBean.getAgeValueTo())) || !CommonUtils
                        .isEmpty(searchBean.getSeverity()) ||  !CommonUtils
                        .isEmpty(searchBean.getOpStatus()) ||  !CommonUtils
                        .isEmpty(searchBean.getMonStatus())) {
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
            String searchTabHistoryXML = genrateTabHistoryXML();
            TabsetBean tabset = ConsoleHelper.getTabSetBean();
            Tab tab = tabset.getTabs().get(tabset.getTabIndex());
            String userId = tabset.getUserId();
            String tabid = tab.getTabId();
            int tabidIndex = Integer.parseInt(tabid
                    .substring(Tab.SEARCH_PANELID_PREFIX.length()));
            try {
                navigationHelper.updateTabHistoryRecord(userId, tabidIndex,
                        ConsoleConstants.APP_TYPE_CONSOLE, searchTabHistoryXML);
            } catch (IOException e) {
                logger
                        .warn("Failed to update tab navigation information from database for user with Id ["
                                + tabset.getUserId()
                                + "].  for the tab ["
                                + tab.getLabel() + "]");
            }
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
     * Clears the search.
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
            if (filterType != null
                    && !ConsoleConstants.EMPTY_STRING
                            .equalsIgnoreCase(filterType)) {

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
        String tabid = tab.getTabId();

        // reset database with null tab history .
        try {
            int tabidIndex = Integer.parseInt(tabid
                    .substring(Tab.SEARCH_PANELID_PREFIX.length()));
            navigationHelper.updateTabHistoryRecord(tabset.getUserId(),
                    tabidIndex, ConsoleConstants.APP_TYPE_CONSOLE, null);
        } catch (IOException e) {

            logger
                    .warn("Failed to update tab navigation information from database for user with Id ["
                            + tabset.getUserId()
                            + "].  for the tab ["
                            + tab.getLabel() + "]");
        }

        logger.debug("Exit clearSearch method");

    }

    /**
     * update tab label.
     */
    public void updateLabel() {
        logger.debug("Enter updateLabel method");
        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        Tab tab = tabset.getTabs().get(tabset.getTabIndex());
        String tabid = tab.getTabId();
        try {
            int tabidIndex = Integer.parseInt(tabid
                    .substring(Tab.SEARCH_PANELID_PREFIX.length()));
            navigationHelper.updateNodeLabelRecord(tabset.getUserId(),
                    tabidIndex, ConsoleConstants.APP_TYPE_CONSOLE, tab
                            .getLabel());
        } catch (IOException e) {

            logger
                    .warn("Failed to update tab label information from database for user with Id ["
                            + tabset.getUserId()
                            + "].  for the tab ["
                            + tab.getLabel() + "]");
        }

    }

    /**
     * Displays the events.
     * 
     * @param eventList
     *            the event list
     */
    private void displayEvents(List<EventBean> eventList) {
        logger.debug("Enter displayEvents method");
        EventBean[] events = eventList.toArray(new EventBean[eventList.size()]);

        DataTableBean eventTableBean = ConsoleHelper.getEventTableBean();

        if (eventTableBean != null) {
            eventTableBean.setEvents(events);
        }

        logger.debug("Exit displayEvents method");
    }

    /**
     * populate Events for HostGroup.
     * 
     * @param hostGroup
     *            the host group
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsByHostGroup(String hostGroup, Filter searchFilter) {
        logger.debug("Enter populateEventsByHostGroup method");
        this.setHostGroupFilter(hostGroup, searchFilter);
        this.initializePage(true);
        logger.debug("Exit populateEventsByHostGroup method");
    }

    /**
     * Sets the host group filter.
     * 
     * @param hostGroup
     *            the host group
     * @param searchFilter
     *            the search filter
     */
    public void setHostGroupFilter(String hostGroup, Filter searchFilter) {
        logger.debug("Enter populateEventsByHostGroup method");
        Filter hostGroupsFilter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
                FilterOperator.EQ, hostGroup);
        Filter openStatusFilter = new Filter(
                ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
                ConsoleConstants.OPERATION_STATUS_OPEN);
        hostGroupsFilter = Filter.AND(hostGroupsFilter, openStatusFilter);
        if (filterInSession != null) {
            filterInSession.setFilter(hostGroupsFilter);
        }
        if (searchFilter != null) {
            filterInSession.setFilter(Filter
                    .AND(hostGroupsFilter, searchFilter));
        }
    }

    /**
     * populate Events for service group.
     * 
     * @param serviceGroup
     *            the service group
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsByServiceGroup(String serviceGroup,
            Filter searchFilter) {
        logger.debug("Enter populateEventsByHostGroup method");
        // setting service group filter
        this.setServiceGroupFilter(serviceGroup, searchFilter);
        this.initializePage(true);

    }

    /**
     * Service Group Filter.
     * 
     * @param serviceGroup
     *            the service group
     * @param searchFilter
     *            the search filter
     */
    public void setServiceGroupFilter(String serviceGroup, Filter searchFilter) {
        Filter serviceGroupsFilter = new Filter(
                ConsoleConstants.PROP_NAME_CATEGORY_NAME, FilterOperator.EQ,
                serviceGroup);
        Filter openStatusFilter = new Filter(
                ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
                ConsoleConstants.OPERATION_STATUS_OPEN);
        serviceGroupsFilter = Filter.AND(serviceGroupsFilter, openStatusFilter);

        if (filterInSession != null) {
            filterInSession.setFilter(serviceGroupsFilter);
        }
        if (searchFilter != null) {
            filterInSession.setFilter(Filter.AND(serviceGroupsFilter,
                    searchFilter));
        }
    }

    /**
     * Populates event by application type.
     * 
     * @param appType
     *            the app type
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsByApplicationType(String appType,
            Filter searchFilter) {
        logger.debug("Enter populateEventsByApplicationType method");
        this.setApplicationTypeFilter(appType, searchFilter);

        this.initializePage(false);
        logger.debug("Enter populateEventsByApplicationType method");
    }

    /**
     * Application type filter.
     * 
     * @param appType
     *            the app type
     * @param searchFilter
     *            the search filter
     */
    public void setApplicationTypeFilter(String appType, Filter searchFilter) {
        Filter applicationFilter = new Filter(
                ConsoleConstants.PROP_NAME_APP_TYPE, FilterOperator.EQ, appType);

        Filter openStatusFilter = new Filter(
                ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
                ConsoleConstants.OPERATION_STATUS_OPEN);
        applicationFilter = Filter.AND(applicationFilter, openStatusFilter);

        if (filterInSession != null) {
            filterInSession.setFilter(applicationFilter);
        }
        if (searchFilter != null) {
            filterInSession.setFilter(Filter.AND(applicationFilter,
                    searchFilter));
        }
        try {

            WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();
            WSFoundationCollection col = wsCommon.getEntityTypeProperties(
                    ConsoleConstants.ENTITY_TYPE_LOGMESSAGE, appType, true);
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
                entityTypeProperties[entityTypeProperties.length-1] = nagiosServiceProp;
            } // end if
            ConsoleHelper.getEventTableBean().setDynamicColumns(
                    entityTypeProperties);
            // ConsoleHelper.getEventTableBean().constructComponent();
        } catch (Exception exc) {
            logger.error(exc.getMessage());
        }
    }

    /**
     * Populate events by operation status.
     * 
     * @param opStatus
     *            the op status
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsByOperationStatus(String opStatus,
            Filter searchFilter) {
        logger.debug("Enter populateEventsByOperationStatus method");
        this.setOperationStatusFilter(opStatus, searchFilter);
        this.initializePage(true);
        logger.debug("Exit populateEventsByOperationStatus method");
    }

    /**
     * Sets the operation status filter.
     * 
     * @param opStatus
     *            the op status
     * @param searchFilter
     *            the search filter
     */
    public void setOperationStatusFilter(String opStatus, Filter searchFilter) {
        Filter operationStatusFilter = new Filter(
                ConsoleConstants.PROP_NAME_OPERATION_STATUS, FilterOperator.EQ,
                opStatus);
        if (filterInSession != null) {
            filterInSession.setFilter(operationStatusFilter);
        }
        if (searchFilter != null) {
            filterInSession.setFilter(Filter.AND(operationStatusFilter,
                    searchFilter));
        }
    }

    /**
     * Populates all Open events.
     */
    public void populateAllOpenEvents() {
        logger.debug("Enter populateAllOpenEvents method");
        ConsoleHelper.getConsoleBean().setAllEventsStyleClass(
                "highlightNavigator");
        DynamicNodeUserObject sysFilterDynaNodeUserObj = ConsoleHelper
                .getSystemFilterBean().getSelectedNodeObject();
        if (sysFilterDynaNodeUserObj != null) {
            sysFilterDynaNodeUserObj.setStyleClass(ConsoleConstants.STYLE_NONE);
        }
        DynamicNodeUserObject pubFilterDynaNodeUserObj = ConsoleHelper
                .getPublicFilterBean().getSelectedNodeObject();
        if (pubFilterDynaNodeUserObj != null) {
            pubFilterDynaNodeUserObj.setStyleClass(ConsoleConstants.STYLE_NONE);
        }
        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        if (null != tabset) {
            Tab tab = tabset.getTabs().get(tabset.getTabIndex());
            if (null != tab) {
                tab.resetSearchCriteria();
                String tabid;
                int tabidIndex = 0;
                // update navigation history
                try {
                    tabid = tab.getTabId();
                    tabidIndex = Integer.parseInt(tabid
                            .substring(Tab.SEARCH_PANELID_PREFIX.length()));

                    tabset
                            .getNavigationHelper()
                            .updateHistoryRecord(
                                    tabset.getUserId(),
                                    tabidIndex,
                                    ResourceUtils
                                            .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT),
                                    ConsoleConstants.EMPTY_STRING,
                                    ConsoleConstants.APP_TYPE_CONSOLE,
                                    null,
                                    ResourceUtils
                                            .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_TAB_DEFAULT));
                } catch (NumberFormatException e) {
                    // ignore
                    logger
                            .debug("NumberFormatException : tabId seems to be incorrect ["
                                    + tabidIndex + "]");
                } catch (IOException e) {
                    // ignore
                    logger
                            .warn("Failed to update tab navigation information from database for user with Id ["
                                    + tabset.getUserId()
                                    + "].  for the tab ["
                                    + tab.getLabel() + "]");
                } catch (Exception exception) {
                    logger
                            .warn("Failed to update tab navigation information from database for user with Id ["
                                    + tabset.getUserId()
                                    + "].  for the tab ["
                                    + tab.getLabel() + "]");
                }
            }
        }
        populateAllOpenEvents(null);
        logger.debug("Exit populateEventsByOperationStatus method");
    }

    /**
     * populate All open events.
     * 
     * @param searchFilter
     *            the search filter
     */
    public void populateAllOpenEvents(Filter searchFilter) {
        logger.debug("Enter populateAllOpenEvents method");
        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        Tab tab = tabset.getTabs().get(tabset.getTabIndex());
        // reset the select button
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

        this.setAllOpenEventsFilter(searchFilter);
        this.initializePage(true);
        logger.debug("Exit populateAllOpenEvents method");
    }

    /**
     * set All open event filter.
     * 
     * @param searchFilter
     *            the search filter
     */
    public void setAllOpenEventsFilter(Filter searchFilter) {
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
    }

    /**
     * Populate events by severity.
     * 
     * @param severity
     *            the severity
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsBySeverity(String severity, Filter searchFilter) {
        logger.debug("Enter populateEventsBySeverity method");
        Filter severityFilter = new Filter(
                ConsoleConstants.PROP_NAME_MONITOR_STATUS, FilterOperator.EQ,
                severity);
        if (filterInSession != null) {
            filterInSession.setFilter(severityFilter);
        }
        if (searchFilter != null) {
            filterInSession.setFilter(Filter.AND(severityFilter, searchFilter));
        }
        this.initializePage(true);
        logger.debug("Exit populateEventsBySeverity method");
    }

    /**
     * Populate events by for the public filters.
     * 
     * @param pubFilter
     *            the pub filter
     * @param searchFilter
     *            the search filter
     */
    public void populateEventsByCombinedFilters(FilterConfigBean pubFilter,
            Filter searchFilter) {
        logger.debug("Enter populateEventsByCombinedFilters method");
        boolean resetColumns = true;
        int pageSize = -1;
        boolean asc = true;
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
                                appType, true);
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
            StringTokenizer stkn = new StringTokenizer(pubFilter.getHostGroup(),
                    ConsoleConstants.COMMA);
            int count = stkn.countTokens(); // counter for the hostgroup.
            // Default dummy filter to start with
            Filter hgFilter = new Filter(
                    ConsoleConstants.PROP_NAME_DEVICE,
                    FilterOperator.EQ, "DUMMY");
            while (stkn.hasMoreTokens()) {
                String hostGroup = stkn.nextToken();
                Filter filter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
                        FilterOperator.EQ, hostGroup);
	        	if (combFilter != null) {	        		
	        		 if (count > 1) {
	                        // an OR filter and add it to the final
	                        // filter
	        			 hgFilter = Filter.OR(hgFilter, filter);
	                    } else {
	                        // or Just AND it
	                        combFilter = Filter.AND(combFilter, filter);
	                    } // end if	              
	            } else {	            	
	            	  if (count > 1) {
	                        combFilter = new Filter(
	                                ConsoleConstants.FILTER_REPORT_DATE,
	                                FilterOperator.GE, temp.getTime());

	                        hgFilter = Filter.OR(hgFilter, filter);
	                    } else {
			                combFilter = new Filter(ConsoleConstants.PROP_NAME_DEVICE,
			                        FilterOperator.EQ, hostGroup);
	                    }
	            } // end if
            } // end while
            if (count > 1) {
                combFilter = Filter.AND(combFilter, hgFilter);
            }
        } // end if

        if (!CommonUtils.isEmpty(pubFilter.getOpStatus())) {
            StringTokenizer stkn = new StringTokenizer(pubFilter.getOpStatus(),
                    ConsoleConstants.COMMA);
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
                    if (count > 1) {
                        // an OR filter and add it to the final
                        // filter
                        opFilter = Filter.OR(opFilter, filter);
                    } else {
                        // or Just AND it
                        combFilter = Filter.AND(combFilter, filter);
                    } // end if
                } else {
                    if (count > 1) {
                        combFilter = new Filter(
                                ConsoleConstants.FILTER_REPORT_DATE,
                                FilterOperator.GE, temp.getTime());

                        opFilter = Filter.OR(opFilter, filter);
                    } else {
                        combFilter = new Filter(
                                ConsoleConstants.PROP_NAME_OPERATION_STATUS,
                                FilterOperator.EQ, opStatus);
                    }
                } // end if
            } // end while
            if (count > 1) {
                combFilter = Filter.AND(combFilter, opFilter);
            }
        } // end if
        if (!CommonUtils.isEmpty(pubFilter.getMonitorStatus())) {
            StringTokenizer stkn = new StringTokenizer(pubFilter
                    .getMonitorStatus(), ConsoleConstants.COMMA);
            int count = stkn.countTokens();
            int eqCount = 0; // count for equals to
            while (stkn.hasMoreTokens()) {
                String monitorStatusPair = stkn.nextToken();
                StringTokenizer stknOp = new StringTokenizer(monitorStatusPair,
                       ConsoleManager.DELIMITER_COLON);
                while (stknOp.hasMoreTokens()) {
                    String monitorStatus = stknOp.nextToken();
                    String oper = stknOp.nextToken();
                    Filter filter = null;
                    if (oper != null && oper.equals("EQ")) {
                        filter = new Filter(
                                ConsoleConstants.PROP_NAME_MONITOR_STATUS,
                                FilterOperator.EQ, monitorStatus);
                        eqCount++;
                    } else {
                        filter = new Filter(
                                ConsoleConstants.PROP_NAME_MONITOR_STATUS,
                                FilterOperator.NE, monitorStatus);
                    }
                    if (combFilter != null) {
                    	if (count > 1 && count== eqCount)
                    		combFilter = Filter.OR(combFilter, filter);
                    	else
                    		combFilter = Filter.AND(combFilter, filter);
                    } else {
                        combFilter = filter;
                    } // end if
                }
            }
        } // end if

        if (!CommonUtils.isEmpty(pubFilter.getSeverity())) {
            StringTokenizer stkn = new StringTokenizer(pubFilter
                    .getSeverity(), ConsoleConstants.COMMA);
            int count = stkn.countTokens();
            int eqCount = 0; // count for equals to
            while (stkn.hasMoreTokens()) {
                String severityPair = stkn.nextToken();
                StringTokenizer stknSev = new StringTokenizer(severityPair,
                        ConsoleManager.DELIMITER_COLON);
                while (stknSev.hasMoreTokens()) {
                    String severity = stknSev.nextToken();
                    String oper = stknSev.nextToken();
                    Filter filter = null;
                    if (oper != null && oper.equals("EQ")) {
                        filter = new Filter(
                                ConsoleConstants.PROP_NAME_MONITOR_SEVERITY,
                                FilterOperator.EQ, severity);
                        eqCount++;
                    } else {
                        filter = new Filter(
                                ConsoleConstants.PROP_NAME_MONITOR_SEVERITY,
                                FilterOperator.NE, severity);
                    } // end if
                    if (combFilter != null) {
                    	if (count > 1 && count== eqCount)
                            combFilter = Filter.OR(combFilter, filter);
                        	else
                        		combFilter = Filter.AND(combFilter, filter);
                    } else {
                        combFilter = filter;
                    } // end if
                } // end while
            } // end while
        } // end if

        // Count Config
        FetchConfigBean fetch = pubFilter.getFetch();
        if (fetch != null) {
            if (!CommonUtils.isEmpty(fetch.getSize())) {
                pageSize = Integer.parseInt(fetch.getSize());
            }
            if (!CommonUtils.isEmpty(fetch.getOrder())) {
                asc = (fetch.getOrder().equalsIgnoreCase(DESC) ? false : true);
            }
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

        DynaProperty dynaProperty = pubFilter.getDynaProperty();
        if (dynaProperty != null) {

            String dataType = dynaProperty.getDataType();
            String propName = dynaProperty.getPropName();
            String propValue = dynaProperty.getPropValue();
            String operator = dynaProperty.getOperator();
            logger.debug("Dynamic property :" + propName + ": " + propValue
                    + ", dataType: " + dataType + ", operator : " + operator);
            if (CommonUtils.isEmpty(dataType) || CommonUtils.isEmpty(propName)
                    || CommonUtils.isEmpty(propValue)
                    || CommonUtils.isEmpty(operator)) {
                logger.debug("Empty Dynamic property set for a public filter");
            } else {
                logger.debug("Dynamic property is valid");
                // First get the property name
                StringProperty nameProp = new StringProperty();
                nameProp.setName(ConsoleConstants.FILTER_PROP_NAME);
                nameProp.setValue(propName);
                Filter leftFilter = new Filter();
                leftFilter.setStringProperty(nameProp);
                leftFilter.setOperator(FilterOperator.EQ);
                Filter rightFilter = new Filter();
                if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_STRING)) {
                    logger
                            .debug("Processing dynamic property of string type..");
                    StringProperty valueProp = new StringProperty();
                    valueProp
                            .setName(ConsoleConstants.FILTER_PROP_VALUE_STRING);
                    valueProp.setValue(propValue);
                    rightFilter.setStringProperty(valueProp);
                    rightFilter.setOperator(FilterOperator.LIKE);
                } else if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_BOOLEAN)) {
                    BooleanProperty valueProp = new BooleanProperty();
                    valueProp
                            .setName(ConsoleConstants.FILTER_PROP_VALUE_BOOLEAN);
                    valueProp.setValue(Boolean.parseBoolean(propValue));
                    rightFilter.setBooleanProperty(valueProp);
                    rightFilter.setOperator(FilterOperator.EQ);
                } else if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_INT)) {
                    IntegerProperty valueProp = new IntegerProperty();
                    valueProp.setName(ConsoleConstants.FILTER_PROP_VALUE_INT);
                    valueProp.setValue(Integer.parseInt(propValue));
                    rightFilter.setIntegerProperty(valueProp);
                    rightFilter
                            .setOperator(FilterOperator.fromString(operator));
                } else if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_DOUBLE)) {
                    DoubleProperty valueProp = new DoubleProperty();
                    valueProp
                            .setName(ConsoleConstants.FILTER_PROP_VALUE_DOUBLE);
                    valueProp.setValue(Double.parseDouble(propValue));
                    rightFilter.setDoubleProperty(valueProp);
                    rightFilter
                            .setOperator(FilterOperator.fromString(operator));
                } else if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_LONG)) {
                    LongProperty valueProp = new LongProperty();
                    valueProp.setName(ConsoleConstants.FILTER_PROP_VALUE_LONG);
                    valueProp.setValue(Long.parseLong(propValue));
                    rightFilter.setLongProperty(valueProp);
                    rightFilter
                            .setOperator(FilterOperator.fromString(operator));
                } else if (dataType
                        .equalsIgnoreCase(ConsoleConstants.FILTER_DATA_TYPE_DATE)) {
                    DateProperty valueProp = new DateProperty();
                    valueProp.setName(ConsoleConstants.FILTER_PROP_VALUE_DATE);
                    valueProp.setValue(Date.valueOf(propValue));
                    rightFilter.setDateProperty(valueProp);
                    rightFilter
                            .setOperator(FilterOperator.fromString(operator));
                } // end if
                // Create comb filter
                if (combFilter != null) {
                    logger.debug("Combo filter not null");
                    combFilter = Filter.AND(combFilter, Filter.AND(leftFilter,
                            rightFilter));
                } else {
                    combFilter = Filter.AND(leftFilter, rightFilter);
                } // end if

            }
        } // end if

        if (searchFilter != null && combFilter != null) {
            filterInSession.setFilter(Filter.AND(combFilter, searchFilter));
        } // end if
        if (searchFilter != null) {
            filterInSession.setFilter(searchFilter);
        } // end if
        if (combFilter == null) {
            Filter tempFilter = new Filter(ConsoleConstants.FILTER_REPORT_DATE,
                    FilterOperator.GE, temp.getTime());
            if (searchFilter != null) {
                filterInSession.setFilter(Filter.AND(tempFilter, searchFilter));
            } else {
                filterInSession.setFilter(tempFilter);
            }
        } else {
            if (searchFilter != null) {
                filterInSession.setFilter(Filter.AND(combFilter, searchFilter));
            } else {
                filterInSession.setFilter(combFilter);
            }
        } // end if

        this.refreshPage(resetColumns, pageSize, asc, sortColumn);
        logger.debug("Exit populateEventsByOperationStatus method");
    }

    /**
     * Gets actions by application type.
     * 
     * @param appType
     *            the app type
     * 
     * @return the actions by application type
     */
    public Action[] getActionsByApplicationType(String appType) {
        return ActionClient.getActionsByApplicationType(appType);

    }

    /**
     * Performs action for the given message IDs and ActionID.
     * 
     * @param appType
     *            the app type
     * @param messageIds
     *            the message ids
     * @param actionID
     *            the action id
     * 
     * @return the action return
     */
    public ActionReturn performAction(int[] messageIds, int actionID,
            String appType, String userComment) {

        ActionReturn actionReturn = null;
        if (appType == null) {
            appType = ConsoleConstants.PROP_NAME_APP_TYPE_SYSTEM;
        }
        try {

            WSCommon wsCommon = ServiceLocator.commonLocator().getcommon();

            ActionPerform[] actionPerforms = new ActionPerform[1];
            StringProperty[] parameters = new StringProperty[12];
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
                    ((userComment.equals("")  || userComment== null) ? "Updated from console at" :  userComment)+ " " + DateUtils
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

            if (appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_SNMPTRAP)) {
                parameters[9] = new StringProperty(
                        ConsoleConstants.ACTION_PARAM_SERVICE,
                        PropertyUtils
                                .getProperty(ConsoleConstants.SERVICE_SNMPTRAP_LAST));
            } else {
                parameters[9] = new StringProperty(
                        ConsoleConstants.ACTION_PARAM_SERVICE,
                        PropertyUtils
                                .getProperty(ConsoleConstants.SERVICE_SYSLOG_LAST));
            }
            parameters[10] = new StringProperty(
                    ConsoleConstants.ACTION_PARAM_STATE,
                    ConsoleConstants.DEFAULT_NSCA_STATE);
            
            parameters[11] = new StringProperty(
                    ConsoleConstants.ACTION_PARAM_USER_COMMENT,
                    userComment);

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
     *            the message ids
     * 
     * @return the string
     */
    private String convertToCommaSepString(int[] messageIds) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < messageIds.length; i++) {
            buf.append(messageIds[i]);
            buf.append(ConsoleConstants.COMMA);
        }
        return buf.toString().substring(0, buf.toString().length() - 1);
    }

    /**
     * Initializes the page.
     * 
     * @param resetDynaColumns
     *            the reset dyna columns
     */
    public void initializePage(boolean resetDynaColumns) {
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
     * Initializes the page.
     * 
     * @param resetDynaColumns
     *            the reset dyna columns
     * @param pageSize
     *            the page size
     * @param asc
     *            the asc
     * @param sortColumn
     *            the sort column
     */
    private void refreshPage(boolean resetDynaColumns, int pageSize,
            boolean asc, String sortColumn) {
        DataTableBean eventTableBean = ConsoleHelper.getEventTableBean();
        eventTableBean.setLastPage(null);
        eventTableBean.setLastStartRow(-1);
        eventTableBean.setAscending(asc);
        if (CommonUtils.isEmpty(sortColumn)) {
            eventTableBean.setSortColumnName(sortColumn);
        }
        if (pageSize == -1) {
            pageSize = Integer.parseInt(PropertyUtils
                    .getProperty(ConsoleConstants.PROP_PAGE_SIZE));
        }
        List<EventBean> eventList = eventTableBean.fetchPage(0, pageSize)
                .getData();
        if (resetDynaColumns) {
            eventTableBean.setDynamicColumns(null);
        } // end if
        eventTableBean.constructComponent();
        eventTableBean.setPage(null);
        this.displayEvents(eventList);

    }

    /**
     * Refresh search bean.
     */
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
     *            the e
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
                                .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS))) {
            freezeBean.reset();
        }
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
     *            the message ids
     * 
     * @return the string
     */
    private String generateHostString(int[] messageIds) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        Tab tab = tabset.getTabs().get(tabset.getTabIndex());
        EventBean[] events = tab.getDataTableBean().getEvents();
        for (int i = 0; i < messageIds.length; i++) {
            for (int j = 0; j < events.length; j++) {
                if (messageIds[0] == events[j].getLogMessageID()) {
                    sb.append(events[j].getDevice());
                    sb.append(ConsoleConstants.COMMA);
                } // end if
            } // end if
        } // end if
        if (sb.toString() != null
                && sb.toString().endsWith(ConsoleConstants.COMMA)) {
            result = sb.toString().substring(0, sb.toString().length() - 1);
            logger.debug("Generated host String is " + result);
        } // end if
        return result;
    } // end method

    /**
     * Gets the host details for the give host.
     * 
     * @param hostName
     *            the host name
     * 
     * @return the host details
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

    /**
     * Genrate tab history xml.
     * 
     * @return Tab history in XML format
     */
    private String genrateTabHistoryXML() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;
        StringWriter stringWriter = new StringWriter();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document
                    .createElement(ConsoleConstants.EVENTS);
            document.appendChild(rootElement);
            Element searcheventsElement = document
                    .createElement(ConsoleConstants.SEARCHEVENTS);
            Element deviceEm = document.createElement(ConsoleConstants.DEVICE);
            if (searchBean.getHost() != null) {
                deviceEm.appendChild(document.createTextNode(searchBean
                        .getHost()));
            } else {
                deviceEm.appendChild(document
                        .createTextNode(ConsoleConstants.EMPTY_STRING));
            }

            searcheventsElement.appendChild(deviceEm);

            Element messageEm = document
                    .createElement(ConsoleConstants.MESSAGE);
            if (searchBean.getMessage() != null) {
                messageEm.appendChild(document.createTextNode(searchBean
                        .getMessage()));
            } else {
                messageEm.appendChild(document
                        .createTextNode(ConsoleConstants.EMPTY_STRING));
            }
            searcheventsElement.appendChild(messageEm);
            rootElement.appendChild(searcheventsElement);

            Element datetimeEm = document
                    .createElement(ConsoleConstants.DATETIME);
            datetimeEm.setAttribute(ConsoleConstants.TYPE, searchBean
                    .getAgeType());
            if (searchBean.getAgeType().equalsIgnoreCase(
                    ConsoleConstants.PRESET)) {
                datetimeEm.appendChild(document.createTextNode(searchBean
                        .getPresetValue()));
            } else {
                datetimeEm.appendChild(document.createTextNode(searchBean
                        .getAgeValueFrom()
                        + "@#%*" + searchBean.getAgeValueTo()));
            }

            rootElement.appendChild(datetimeEm);

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);

        } catch (Exception e) {
            // For now ignore the exception
            logger.debug("Failed to genrate tab history XML ");
        }
        return stringWriter.getBuffer().toString();
    }

    /**
     * Listener to toggle the SilenceAlarm button .
     * 
     * @param event
     *            the event
     */
    public void toggleSilenceAlarm(ActionEvent event) {
        logger.debug("SilenceAlarm clicked..");
        String linkText = ((HtmlCommandLink) event.getComponent()).getTitle();
        TabsetBean tabset = ConsoleHelper.getTabSetBean();
        if (linkText
                .equalsIgnoreCase(ResourceUtils
                        .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_SILENCE_ALARM))) {
            tabset.setSilenceAlarm(true);
            tabset.setAutoStart(0);
            alarmButtonText = ResourceUtils
                    .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_ALARM);
            this.muteButtonImage = ConsoleConstants.IMAGES_MUTE_JPEG;

        } else {
            tabset.setSilenceAlarm(false);
            alarmButtonText = ResourceUtils
                    .getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_SILENCE_ALARM);
            this.muteButtonImage = ConsoleConstants.IMAGES_SPEAKER_JPEG;
        }

    }
    
    /**
     * Listener for showevent tile
     */
    public void showEventTile(ActionEvent event) {
    	 logger.debug("showEventTile clicked..");
    	 PopupBean popup = ConsoleHelper.getPopupBean();
    	 popup.setShowEventTile(true);
    }

    /**
     * Sets the alarmButtonText.
     * 
     * @param alarmButtonText
     *            the alarmButtonText to set
     */
    public void setAlarmButtonText(String alarmButtonText) {
        this.alarmButtonText = alarmButtonText;
    }

    /**
     * Returns the alarmButtonText.
     * 
     * @return the alarmButtonText
     */
    public String getAlarmButtonText() {
        return alarmButtonText;
    }

    /**
     * Sets the muteButtonImage.
     * 
     * @param muteButtonImage
     *            the muteButtonImage to set
     */
    public void setMuteButtonImage(String muteButtonImage) {
        this.muteButtonImage = muteButtonImage;
    }

    /**
     * Returns the muteButtonImage.
     * 
     * @return the muteButtonImage
     */
    public String getMuteButtonImage() {
        return muteButtonImage;
    }
}
