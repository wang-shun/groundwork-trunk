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

/* Created on: Mar 8, 2006 */

package org.groundwork.foundation.ws.api;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.model.EventQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * @author rogerrut
 * 
 */
public interface WSEvent extends java.rmi.Remote {
    /**
     * gets events for the specified time period and event type. If type and
     * value are null, return all events for the specified time period. It is
     * valid to provide only type and value and no time. This will cause all
     * events related to the specified object type to be returned.
     * 
     * NOTE: When querying by EventQueryType SERVICEDESCRIPTION, you must
     * include the host name. To do this, prefix the servicedescription with the
     * host name followed by a ':' (colon). Example: if I want the LogMessages
     * for a Service called "local_disk" on host "nagios", I would use
     * "nagios:local_disk" for the eventTypeValue parameter.
     * 
     * @param eventType
     * @param eventTypeValue
     * @param applicationType
     * @param fromRange
     * @param toRange
     * @param orderedBy
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     * 
     */

    public WSFoundationCollection getEvents(EventQueryType eventType,
            String eventTypeValue, String applicationType, String fromRange,
            String toRange, SortCriteria orderedBy, int firstResult,
            int maxResults) throws WSFoundationException, RemoteException;

    /**
     * Overloaded getEvents method allowing String parameters specifically used
     * by custom reporting data source.
     * 
     * @param eventType
     * @param eventTypeValue
     * @param applicationType
     * @param fromRange
     * @param toRange
     * @param sortOrder
     * @param sortField
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventsByString(String eventType,
            String eventTypeValue, String applicationType, String fromRange,
            String toRange, String sortOrder, String sortField,
            String firstResult, String maxResults)
            throws WSFoundationException, RemoteException;

    /**
     * EventStatistic methods
     * 
     * @param applicationType
     * @param hostName
     * @param fromRange
     * @param toRange
     * @param statisticType
     *            One of the following: SEVERITY, OPERATION_STATUS,
     *            MONITOR_STATUS
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventStatisticsByHost(
            String applicationType, String hostName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException;

    /**
     * @param applicationType
     * @param hostGroupName
     * @param fromRange
     * @param toRange
     * @param statisticType
     *            One of the following: SEVERITY, OPERATION_STATUS,
     *            MONITOR_STATUS
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventStatisticsByHostGroup(
            String applicationType, String hostGroupName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException;

    /**
     * @param DeviceName
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    /* Method without using enums */
    public WSFoundationCollection getEventsForDevice(String DeviceName)
            throws WSFoundationException, RemoteException;

    /**
     * Returns collection of events which match specified criteria.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException;

    /***
     * Returns collection of events which match ids specified and pagination
     * criteria
     * 
     * @param ids
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getEventsByIds(int[] ids, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException;

    /**
     * Gets the host state transitions for the supplied host and the date range
     * 
     * @param hostName
     * @param startDate
     * @param endDate
     * @return WSFoundationCollection
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws RemoteException,
            WSFoundationException;

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
            throws RemoteException, WSFoundationException;

    /**
     * Gets events for the supplied categoryName and entityName
     * 
     * @param categoryName
     * 
     * @param entityTypeName
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return WSFoundationCollection
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getEventsByCategory(String categoryName,
            String entityTypeName, Filter filter, Sort sort, int firstResult,
            int maxResults) throws WSFoundationException, RemoteException;

    /**
     * Gets events for the supplied comma separated host group or service group
     * list
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
            throws WSFoundationException, RemoteException;
}
