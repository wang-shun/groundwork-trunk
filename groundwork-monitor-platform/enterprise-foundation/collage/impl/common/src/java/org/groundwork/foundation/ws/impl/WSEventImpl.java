/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

/* Created on: Mar 20, 2006 */

package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.EventQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.StateTransition;

/**
 * WebServiec Implementation for WSEvent interface
 * 
 * @author rogerrut
 * 
 */
public class WSEventImpl extends WebServiceImpl implements WSEvent {
    /**
     * EMPTY_STRING
     */
    private static final String EMPTY_STRING = "";
    /* Enable logging */
    protected static Log log = LogFactory.getLog(WSEventImpl.class);

    /**
     * 
     */
    public WSEventImpl() {
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSEvent#getEvents(org.groundwork.foundation.ws.model.EventQueryType,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String,
     *      org.groundwork.foundation.ws.model.impl.SortCriteria, int, int)
     */
    public WSFoundationCollection getEvents(EventQueryType eventType,
            String eventTypeValue, String applicationType, String startRange,
            String endRange, SortCriteria orderedBy, int firstResult,
            int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        WSFoundationCollection logMessages = null;

        // check first for empty event type
        if (eventType == null) {
            // when eventType or value is null, we retrieve all events, possibly
            // delimited by time.
            log.error("getEvents() - EventType is NULL");
            throw new WSFoundationException("EventType is NULL",
                    ExceptionType.WEBSERVICE);
        }

        // Check to make sure that date string passed in is valid
        if (startRange != null && startRange.length() > 0) {
            try {
                DateFormat checkDate = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss");

                // We expect date in the SQL format
                Date testDate = checkDate.parse(startRange);
                if (testDate == null) {
                    log.error("Invalid Start Date Parameter for getEvents");
                    throw new WSFoundationException(
                            "Invalid Start Date Parameter for getEvents",
                            ExceptionType.WEBSERVICE);
                }
            } catch (ParseException e) {
                log.error("Invalid Start Date Parameter for getEvents", e);
                throw new WSFoundationException(
                        "Invalid Start Date Parameter for getEvents",
                        ExceptionType.WEBSERVICE);
            }
        }

        if (endRange != null && endRange.length() > 0) {
            try {
                DateFormat checkDate = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss");

                // We expect date in the SQL format
                Date testDate = checkDate.parse(endRange);
                if (testDate == null) {
                    log.error("Invalid End Date Parameter for getEvents");
                    throw new WSFoundationException(
                            "Invalid End Date Parameter for getEvents",
                            ExceptionType.WEBSERVICE);
                }
            } catch (ParseException e) {
                log.error("Invalid End Date Parameter for getEvents", e);
                throw new WSFoundationException(
                        "Invalid End Date Parameter for getEvents",
                        ExceptionType.WEBSERVICE);
            }
        }

        try {
            // Prepare query returns a IntegerProperty Object
            if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._FOUNDATION_QUERY_PREPARE) == 0) {
                IntegerProperty result = foundationQueryPrepare(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
                stopMetricsTimer(timer);
                return new org.groundwork.foundation.ws.model.impl.WSFoundationCollection(
                        result);
            }

            // put the SortCriteria into something the DAO's can understand
            if (eventType.getValue().compareToIgnoreCase(EventQueryType._ALL) == 0) {
                logMessages = getAllEvents(applicationType, startRange,
                        endRange, orderedBy, firstResult, maxResults);
            } else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._DEVICEID) == 0)
                logMessages = getEventsByDeviceID(eventTypeValue, startRange,
                        endRange, orderedBy, firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._DEVICEIDENTIFICATION) == 0)
                logMessages = getEventsByDeviceIdentification(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._HOSTGROUPID) == 0)
                logMessages = getEventsByHostGroupID(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._HOSTGROUPNAME) == 0)
                logMessages = getEventsByHostGroupName(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._HOSTID) == 0)
                logMessages = getEventsByHostID(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._HOSTNAME) == 0)
                logMessages = getEventsByHostName(eventTypeValue,
                        applicationType, startRange, endRange, orderedBy,
                        firstResult, maxResults);
            else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._SERVICEDESCRIPTION) == 0) {
                if (eventTypeValue.contains(":")) {
                    int colonIndex = eventTypeValue.indexOf(':');
                    String serviceDescription = eventTypeValue
                            .substring(colonIndex + 1);
                    String hostName = eventTypeValue.substring(0, colonIndex);
                    logMessages = getEventsByServiceDescription(hostName,
                            serviceDescription, applicationType, startRange,
                            endRange, orderedBy, firstResult, maxResults);
                } else {
                    throw new WSFoundationException(
                            "Invalid Parameter for getEvents",
                            ExceptionType.WEBSERVICE);
                }
            } else if (eventType.getValue().compareToIgnoreCase(
                    EventQueryType._EVENTID) == 0)
                logMessages = getEventsByEventID(eventTypeValue,
                        applicationType);
            else
                throw new WSFoundationException(
                        "Invalid EventQueryType specified in getEvents",
                        ExceptionType.WEBSERVICE);

            stopMetricsTimer(timer);
            return logMessages;
        } catch (WSFoundationException wsfe) {
            log.error("Error occurred in getEvents() - ", wsfe);
            throw wsfe;
        } catch (Exception e) {
            log.error("Error occurred in getEvents() - ", e);
            throw new WSFoundationException("Error occurred in getEvents() - "
                    + e, ExceptionType.WEBSERVICE);
        }
    }

    public WSFoundationCollection getEventsForDevice(String deviceIdentification)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        return getEventsByDeviceIdentification(deviceIdentification, null,
                null, null, null, -1, -1);
    }

    /**
     * String parameter version of getEvents()
     */
    public WSFoundationCollection getEventsByString(String eventType,
            String eventTypeValue, String applicationType, String fromRange,
            String toRange, String sortField, String sortOrder,
            String firstResult, String maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        // Do conversion then delegate
        org.groundwork.foundation.ws.model.impl.EventQueryType type = org.groundwork.foundation.ws.model.impl.EventQueryType.ALL;

        if (eventType != null) {
            type = org.groundwork.foundation.ws.model.impl.EventQueryType
                    .fromValue(eventType);
        }

        SortCriteria sortCriteria = null;
        if (sortOrder != null && sortOrder.trim().length() > 0
                && sortField != null && sortField.trim().length() > 0) {
            sortCriteria = new SortCriteria(sortOrder, sortField);
        }

        int intFirstResult = -1;
        int intMaxResults = -1;

        if (firstResult != null && firstResult.length() > 0) {
            try {
                intFirstResult = Integer.parseInt(firstResult);
            } catch (Exception e) {
            } // Suppress and default to -1
        }

        if (maxResults != null && maxResults.length() > 0) {
            try {
                intMaxResults = Integer.parseInt(maxResults);
            } catch (Exception e) {
            } // Suppress and default to -1
        }

        WSFoundationCollection events = getEvents(type, eventTypeValue, applicationType, fromRange,
                toRange, sortCriteria, intFirstResult, intMaxResults);
        stopMetricsTimer(timer);
        return events;
    }

    public WSFoundationCollection getEventStatisticsByHost(
            String applicationType, String hostName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException {
        CollageTimer timer = startMetricsTimer();
        try {
            Collection<com.groundwork.collage.model.impl.StatisticProperty> stats = getStatisticsService()
                    .getEventStatisticsByHostName(applicationType, hostName,
                            fromRange, toRange, statisticType);

            return new WSFoundationCollection(getConverter().convertStatisticProperties(stats));
        } catch (CollageException e) {
            log.error("Error occurred in getEventStatisticsByHost()", e);
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    public WSFoundationCollection getEventStatisticsByHostGroup(
            String applicationType, String hostGroupName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException {
        CollageTimer timer = startMetricsTimer();
        try {
            Collection<com.groundwork.collage.model.impl.StatisticProperty> stats = getStatisticsService()
                    .getEventStatisticsByHostGroupName(applicationType,
                            hostGroupName, fromRange, toRange, statisticType);

            return new WSFoundationCollection(getConverter().convertStatisticProperties(stats));
        } catch (CollageException e) {
            log.error("Error occurred in getEventStatisticsByHostGroup()", e);
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    public WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FilterCriteria filterCriteria = getConverter().convert(filter);
            org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                    .convert(sort);

            FoundationQueryList list = getLogMessageService().getLogMessages(
                    filterCriteria, sortCriteria, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (Exception e) {
            log.error("Error occurred in getEventsByCriteria()", e);
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    public WSFoundationCollection getEventsByIds(int[] ids, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            if (ids == null || ids.length == 0)
                return new WSFoundationCollection(
                        0,
                        (org.groundwork.foundation.ws.model.impl.LogMessage[]) null);

            // Convert to array of Integer instances
            Integer[] intIds = new Integer[ids.length];
            for (int i = 0; i < ids.length; i++) {
                intIds[i] = new Integer(ids[i]);
            }

            FilterCriteria filterCriteria = FilterCriteria.in(LogMessage.HP_ID,
                    intIds);

            org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                    .convert(sort);

            FoundationQueryList list = getLogMessageService().getLogMessages(
                    filterCriteria, sortCriteria, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (Exception e) {
            log.error("Error occurred in getEventsByIds()", e);
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Gets the host state transitions for the supplied host and the date range
     * 
     * @param hostName
     * @param startDate
     * @param endDate
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        List list = getLogMessageService().getHostStateTransitions(hostName,
                startDate, endDate);

        WSFoundationCollection returnObj = new WSFoundationCollection(list
                .size(), getConverter().convertStateTransition(
                (Collection<StateTransition>) list));
        stopMetricsTimer(timer);
        return returnObj;
    }

    /**
     * Gets the service state transitions for the supplied host,service and the
     * date range
     * 
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
     * @return WSFoundationCollection(StateTransition[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getServiceStateTransitions(String hostName,
            String serviceName, String startDate, String endDate)
            throws RemoteException, WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        List list = getLogMessageService().getServiceStateTransitions(hostName,
                serviceName, startDate, endDate);
        WSFoundationCollection returnObj = new WSFoundationCollection(list
                .size(), getConverter().convertStateTransition(
                (Collection<StateTransition>) list));
        stopMetricsTimer(timer);
        return returnObj;
    }

    /**
     * Gets events for the supplied categoryName and entityName
     * 
     * @param categoryname
     * @param entityTypeName
     * @param sort
     * @param firstresult
     * @param maxresults
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventsByCategory(String categoryName,
            String entityTypeName, Filter filter, Sort sort, int firstResult,
            int maxResults) throws WSFoundationException, RemoteException {
        CollageTimer timer = startMetricsTimer();
        Category category = getCategoryService().getCategoryByName(
                categoryName, entityTypeName);
        Collection<CategoryEntity> catEntities = category.getCategoryEntities();
        Integer[] serviceIDArr = new Integer[catEntities.size()];
        if (catEntities != null) {
            Iterator<CategoryEntity> iter = catEntities.iterator();
            int i = 0;
            while (iter.hasNext()) {
                CategoryEntity entity = (CategoryEntity) iter.next();
                serviceIDArr[i] = entity.getObjectID();
                i++;
            } // end while
        } // end if
        FilterCriteria filterCriteria = FilterCriteria.in(
                "serviceStatus.serviceStatusId", serviceIDArr);
        if (filter != null)
            filterCriteria.and(getConverter().convert(filter));
        org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                .convert(sort);
        FoundationQueryList list = getLogMessageService().getLogMessages(
                filterCriteria, sortCriteria, firstResult, maxResults);
        WSFoundationCollection events = new WSFoundationCollection(list.getTotalCount(), getConverter()
                .convertLogMessage((Collection<LogMessage>) list.getResults()));
        stopMetricsTimer(timer);
        return events;
    }

    /*
     * retrieves all events for the specified device
     */
    private WSFoundationCollection getEventsByDeviceID(String id,
            String startRange, String endRange, SortCriteria orderBy,
            int firstResult, int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByDeviceId(Integer.valueOf(id), startRange,
                            endRange, null, null, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Gets all events related to a device specified by the device
     * identification.
     */
    private WSFoundationCollection getEventsByDeviceIdentification(
            String identification, String appType, String startRange,
            String endRange, SortCriteria orderedBy, int firstResult,
            int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByDeviceIdentification(identification,
                            startRange, endRange, null, null, firstResult,
                            maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all events for a hostgroup specified by the hostgroupID
     */
    private WSFoundationCollection getEventsByHostGroupID(String id,
            String appType, String startRange, String endRange,
            SortCriteria orderedBy, int firstResult, int maxResults)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByHostGroupId(Integer.parseInt(id),
                            startRange, endRange, null, null, firstResult,
                            maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all events for a hostgroup specified by the hostgroup name
     */
    private WSFoundationCollection getEventsByHostGroupName(String name,
            String appType, String startRange, String endRange,
            SortCriteria orderedBy, int firstResult, int maxResults)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByHostGroupName(name, startRange, endRange,
                            null, null, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Calls into LogMessageDAO to get all logmessages for the desired host - by
     * hostID
     */
    private WSFoundationCollection getEventsByHostID(String id, String appType,
            String startRange, String endRange, SortCriteria orderedBy,
            int firstResult, int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByHostId(Integer.parseInt(id), startRange,
                            endRange, null, null, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * get events for a host specified by hostName
     */
    private WSFoundationCollection getEventsByHostName(String name,
            String appType, String startRange, String endRange,
            SortCriteria orderedBy, int firstResult, int maxResults)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByHostName(name, startRange, endRange, null,
                            null, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get events for a service specified by serviceDescription
     */
    private WSFoundationCollection getEventsByServiceDescription(
            String hostName, String serviceDescription, String appType,
            String startRange, String endRange, SortCriteria orderedBy,
            int firstResult, int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = getLogMessageService()
                    .getLogMessagesByService(hostName, serviceDescription,
                            startRange, endRange, null, null, firstResult,
                            maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get a specific event
     */
    private WSFoundationCollection getEventsByEventID(String id, String appType)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            LogMessage logMessage = getLogMessageService().getLogMessageById(
                    Integer.parseInt(id));
            if (logMessage == null)
                return new WSFoundationCollection(
                        0,
                        new org.groundwork.foundation.ws.model.impl.LogMessage[0]);

            return new WSFoundationCollection(
                    1,
                    new org.groundwork.foundation.ws.model.impl.LogMessage[] { getConverter()
                            .convert(logMessage) });
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all events, possibly limited to a date range
     */
    private WSFoundationCollection getAllEvents(String appType,
            String startRange, String endRange, SortCriteria orderedBy,
            int firstResult, int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            // NOTE: We are ignoring SortCriteria since it was not used in
            // previous DAO implementation.
            FoundationQueryList list = null;
            if (appType == null || appType.length() == 0) {
                list = getLogMessageService().getLogMessages(startRange,
                        endRange, null, null, firstResult, maxResults);
            } else {
                list = getLogMessageService()
                        .getLogMessagesByApplicationTypeName(appType,
                                startRange, endRange, null, null, firstResult,
                                maxResults);
            }

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertLogMessage(
                            (Collection<LogMessage>) list.getResults()));
        } catch (CollageException e) {
            log.error(e);
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    private IntegerProperty foundationQueryPrepare(String query,
            String appType, String startRange, String endRange,
            SortCriteria orderedBy, int firstResult, int maxResults)
            throws WSFoundationException {
        // The LogMessage DAO handles the creation of the query
        return getLogMessageService().createPreparedQuery(query, appType,
                startRange, endRange, orderedBy, firstResult, maxResults);
    }

    /**
     * Gets events for the supplied comma separated host group or service group
     * list string.
     * 
     * @param hostGroupList
     * @param serviceGroupList
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventsByRestrictedHostGroupsAndServiceGroups(
            String hostGroupList, String serviceGroupList, Filter filter,
            Sort sort, int firstResult, int maxResults)
            throws WSFoundationException, RemoteException {
        CollageTimer timer = startMetricsTimer();
        if (hostGroupList == null || serviceGroupList == null) {
            throw new WSFoundationException(
                    "Invalid Extended host group list or service group list",
                    ExceptionType.WEBSERVICE);
        }

        FilterCriteria filterCriteria;
        if (!EMPTY_STRING.equals(hostGroupList)
                && !EMPTY_STRING.equals(serviceGroupList)) {

            List<Integer> servicesIdList = getServiceIdListByServiceGroups(serviceGroupList);

            FilterCriteria hgFilterCriteria = createInFilterCriteria(
                    "device.hosts.hostGroups.name", hostGroupList);

            // check if services id list is empty,supplied service group list
            // does not contains service. Hence get the events by Restricted
            // HostGroups list.
            if (servicesIdList.isEmpty()) {
                filterCriteria = hgFilterCriteria;
            } else {
                filterCriteria = FilterCriteria.in(
                        "serviceStatus.serviceStatusId", servicesIdList);
                filterCriteria.or(hgFilterCriteria);
            }

            if (filter != null) {
                filterCriteria.and(getConverter().convert(filter));
            }
        } else if (EMPTY_STRING.equals(hostGroupList)
                && !EMPTY_STRING.equals(serviceGroupList)) {
            // if comma separated host groups name list is empty and service
            // group list is not empty then return events by Restricted service
            // groups.
            List<Integer> servicesIdList = getServiceIdListByServiceGroups(serviceGroupList);
            if (servicesIdList.isEmpty()) {
                filterCriteria = getConverter().convert(filter);
            } else {
                filterCriteria = FilterCriteria.in(
                        "serviceStatus.serviceStatusId", servicesIdList);
                if (filter != null) {
                    filterCriteria.and(getConverter().convert(filter));
                }
            }
        } else if (!EMPTY_STRING.equals(hostGroupList)
                && EMPTY_STRING.equals(serviceGroupList)) {
            // if comma separated service groups name list is empty and host
            // group list is not empty then return events by Restricted host
            // groups.

            filterCriteria = createInFilterCriteria(
                    "device.hosts.hostGroups.name", hostGroupList);
            if (filter != null) {
                filterCriteria.and(getConverter().convert(filter));
            }
        } else {
            filterCriteria = getConverter().convert(filter);
        }
        org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                .convert(sort);
        FoundationQueryList list = getLogMessageService().getLogMessages(filterCriteria, sortCriteria, firstResult, maxResults);
        WSFoundationCollection events = new WSFoundationCollection(list.getTotalCount(), getConverter()
                .convertLogMessage((Collection<LogMessage>) list.getResults()));
        stopMetricsTimer(timer);
        return events;
    }

    /**
     * returns service id list string as per comma separated service group name
     * list.
     * 
     * @param serviceGroupList
     * @return List
     */
    private List<Integer> getServiceIdListByServiceGroups(
            String serviceGroupList) {
        CollageTimer timer = startMetricsTimer();
        List<Integer> servicesIdList = new ArrayList<Integer>();
        FilterCriteria sgFilterCriteria = createInFilterCriteria(
                Category.HP_NAME, serviceGroupList);
        FoundationQueryList serviceGroups = this.getCategoryService()
                .getCategories(sgFilterCriteria, null, -1, -1);
        org.groundwork.foundation.ws.model.impl.Category[] categoryArray = getConverter()
                .convertCategory((Collection<Category>) serviceGroups.getResults());

        if (null != categoryArray) {
            for (org.groundwork.foundation.ws.model.impl.Category category : categoryArray) {
                org.groundwork.foundation.ws.model.impl.CategoryEntity[] categoryEntities = category
                        .getCategoryEntities();
                if (categoryEntities != null) {
                    for (org.groundwork.foundation.ws.model.impl.CategoryEntity categoryEntity : categoryEntities) {
                        servicesIdList.add(categoryEntity.getObjectID());
                    }
                }
            }
        }
        stopMetricsTimer(timer);
        return servicesIdList;
    }

    /**
     * Create In filterCriteria as per property name and value
     * 
     * @param extRoleServiceGroupList
     * @return FilterCriteria
     */
    private FilterCriteria createInFilterCriteria(String propertyName,
            String value) {
        CollageTimer timer = startMetricsTimer();
        FilterCriteria filterCriteria = null;
        if (propertyName != null && value != null) {
            StringTokenizer stkn = new StringTokenizer(value, ",");
            Object[] objArray = new Object[stkn.countTokens()];
            int i = 0;
            while (stkn.hasMoreTokens()) {
                String tokenValue = stkn.nextToken();
                objArray[i] = tokenValue;
                i++;
            }
            filterCriteria = FilterCriteria.in(propertyName, objArray);
        }
        stopMetricsTimer(timer);
        return filterCriteria;
    }
}
