/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.statusviewer.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.LogMessage;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortItem;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.statusviewer.bean.EventBean;
import com.groundworkopensource.portal.statusviewer.bean.StatusBean;
import com.groundworkopensource.portal.statusviewer.bean.SeverityBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.DateUtils;
import com.groundworkopensource.portal.statusviewer.common.EventMenuActionManager;
import com.groundworkopensource.portal.statusviewer.common.EventTableMappingUtil;
import com.groundworkopensource.portal.statusviewer.common.MonitorStatusUtilities;

/**
 * <p>
 * Application scope data bean for your application. Create properties here to
 * represent cached data that should be made available to all users and pages in
 * the application.
 * </p>
 * 
 * <p>
 * An instance of this class will be created for you automatically, the first
 * time your application evaluates a value binding expression or method binding
 * expression that references a managed bean using this class.
 * </p>
 */
public class EventQueryManager {

    /**
     * SUB_COMPONENT
     * 
     */
    private static final String SUB_COMPONENT = "SubComponent";
    /**
     * logger
     */
    private static Logger logger = Logger.getLogger(EventQueryManager.class
            .getName());
    /**
     * IWSFacade instance variable.
     */
    private IWSFacade foundFacade = null;
    /**
     * date time pattern
     */
    private String DATETIME_PATTERN;

    /**
     * <p>
     * Construct a new application data bean instance.
     * </p>
     */
    public EventQueryManager() {
        foundFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
        try {
            DATETIME_PATTERN = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER,
                    Constant.STATUS_VIEWER_DATETIME_PATTERN);
        } catch (Exception e) {
            // Ignore exception
            DATETIME_PATTERN = Constant.EVENT_DATETIME_PATTERN;
        }
    }

    /**
     * Queries events by filter
     * 
     * @param filter
     * @param startIndex
     * @param sort
     * @param pageSize
     * @return List
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public List<EventBean> queryForEventsByFilter(Filter filter,
            int startIndex, Sort sort, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        return this.queryForEventsByFilter(filter, null, startIndex, sort,
                pageSize);
    }

    /**
     * Queries events by filter
     * 
     * @param filter
     * @param entityTypeProperties
     * @param startIndex
     * @param sort
     * @param pageSize
     * @return List
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public List<EventBean> queryForEventsByFilter(Filter filter,
            EntityTypeProperty[] entityTypeProperties, int startIndex,
            Sort sort, int pageSize) throws GWPortalException,
            WSDataUnavailableException {
        Vector<EventBean> events = new Vector<EventBean>();
        WSFoundationCollection wsfoundationCollection = null;

        String sortColumnName = sort.getSortItem(0).getPropertyName();
        SortItem sortItem = new SortItem();
        sortItem.setPropertyName(EventTableMappingUtil
                .findORMapping(sortColumnName));
        sortItem.setSortAscending(sort.getSortItem(0).isSortAscending());
        sort.setSortItem(0, sortItem);
        // logger.debug("Sorting by " + sortColumnName + "----"
        // + sort.getSortItem(0).isSortAscending());
        wsfoundationCollection = foundFacade.getEventsByCriteria(filter, sort,
                startIndex, pageSize);
        if (wsfoundationCollection != null) {
            LogMessage[] logMsgs = wsfoundationCollection.getLogMessage();
            if (logMsgs == null) {
                return events;
            }

            this.loadEvents(logMsgs, events, entityTypeProperties,
                    wsfoundationCollection.getTotalCount());
        }

        return events;
    }

    /**
     * Loads the events
     * 
     * @param logMsgs
     * @param events
     * @param entityTypeProperties
     */
    private void loadEvents(LogMessage[] logMsgs, Vector<EventBean> events,
            EntityTypeProperty[] entityTypeProperties, int totalCount) {
        logger.debug("No of messages fetched=" + logMsgs.length);
        for (int i = 0; i < logMsgs.length; i++) {
            EventBean event = new EventBean();
            event.setTotalCount(totalCount);
            event.setLogMessageID(logMsgs[i].getLogMessageID());
            event.setDevice(logMsgs[i].getDevice().getName());
            event.setMsgCount(logMsgs[i].getMessageCount());
            try {
                event.setReportDate(DateUtils.format(
                        logMsgs[i].getReportDate(), DATETIME_PATTERN));
            } catch (Exception e) {
                event.setReportDate(DateUtils.format(
                        logMsgs[i].getReportDate(),
                        Constant.EVENT_DATETIME_PATTERN));
            }

            SeverityBean sevBean = new SeverityBean();
            String severity = MonitorStatusUtilities
            .getCamelCaseStatus(logMsgs[i].getSeverity().getName());
            sevBean.setValue(severity);
            sevBean.setStyleClass("severity_" + severity);
            event.setSeverity(sevBean);
            StatusBean statBean = new StatusBean();
            String status = logMsgs[i].getMonitorStatus().getName();
            status = MonitorStatusUtilities.getCamelCaseStatus(status);
            statBean.setValue(status);
            if (status.toLowerCase().contains(Constant.EVENT_WARNING)) {
                statBean.setStyleClass(Constant.HIGHLIT_YELLOW);
            } else if (status.toLowerCase().contains(Constant.CRITICAL)
                    || status.toLowerCase().contains(Constant.UNREACHABLE)
                    || status.toLowerCase().contains(Constant.DOWN)) {
                statBean.setStyleClass(Constant.HIGHLIT_RED);
            } else if (status.toLowerCase().contains(Constant.UNKNOWN)
                    || status.toLowerCase().contains(Constant.PENDING)) {
                statBean.setStyleClass(Constant.HIGHLIT_GRAY);
            } else if (status.toLowerCase().contains(Constant.OK)
                    || status.toLowerCase().contains(Constant.UP)) {
                statBean.setStyleClass(Constant.HIGHLIT_GREEN);
            }
            event.setStatusBean(statBean);
            event.setApplicationType(logMsgs[i].getApplicationName());
            event.setTextMessage(StringEscapeUtils.unescapeXml(logMsgs[i]
                    .getTextMessage()));
            try {
                event.setLastInsertDate(DateUtils.format(logMsgs[i]
                        .getLastInsertDate(), DATETIME_PATTERN));
                event.setFirstInsertDate(DateUtils.format(logMsgs[i]
                        .getFirstInsertDate(), DATETIME_PATTERN));
            } catch (Exception e) {
                event.setLastInsertDate(DateUtils.format(logMsgs[i]
                        .getLastInsertDate(), Constant.EVENT_DATETIME_PATTERN));
                event
                        .setFirstInsertDate(DateUtils.format(logMsgs[i]
                                .getFirstInsertDate(),
                                Constant.EVENT_DATETIME_PATTERN));
            }

            if (entityTypeProperties != null) {
                Map<String, Object> map = new HashMap<String, Object>();

                for (int j = 0; j < entityTypeProperties.length; j++) {
                    String propertyName = entityTypeProperties[j].getName();
                    // logger.info(propertyName);
                    Object propertyValue = logMsgs[i].getPropertyTypeBinding()
                            .getPropertyValue(propertyName);
                    if (propertyName != null
                            && !propertyName
                                    .equalsIgnoreCase(Constant.DYNAMIC_COLUMN_SERVICE)) {
                        map.put(propertyName, propertyValue);
                    }
                    // For Nagios service
                    if (propertyName != null
                            && propertyName.equalsIgnoreCase(SUB_COMPONENT)) {
                        if (propertyValue != null
                                && propertyValue.toString().indexOf(
                                        Constant.COLON) != -1) {
                            StringTokenizer stkn = new StringTokenizer(
                                    propertyValue.toString(), Constant.COLON);
                            // IMP do not remove even though unused
                            stkn.nextToken();
                            String serviceValue = stkn.nextToken();
                            map.put(Constant.DYNAMIC_COLUMN_SERVICE,
                                    serviceValue);
                        } // end if
                    } // end if
                } // end for

                event.setDynamicProperty(map);
            } // end if

            events.add(event);

            // clean up the objects
            statBean = null;
            event = null;
        } // end for
    } // end loadEvents method

    /**
     * Gets actions by application type
     * 
     * @param appType
     * @return Action[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Action[] getActionsByApplicationType(String appType)
            throws WSDataUnavailableException, GWPortalException {
        return EventMenuActionManager.getActionsByApplicationType(appType);

    }

}
