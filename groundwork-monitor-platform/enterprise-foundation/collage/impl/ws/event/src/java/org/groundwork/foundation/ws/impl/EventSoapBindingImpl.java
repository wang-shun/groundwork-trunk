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
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.EventQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class EventSoapBindingImpl implements WSEvent {
    private static final String BEAN_WSEVENT = "WSEvent";

    /*
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSEvent#getEvents(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getEventsByString(String eventType,
            String value, String applicationType, String fromRange,
            String toRange, String sortOrder, String sortField,
            String firstResult, String maxResults)
            throws WSFoundationException, RemoteException {
        // get the WSEvent api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsByString(eventType, value, applicationType,
                    fromRange, toRange, sortOrder, sortField, firstResult,
                    maxResults);
        }
    }

    public WSFoundationCollection getEvents(EventQueryType type, String value,
            String appType, String startRange, String endRange,
            SortCriteria orderedBy, int firstResult, int maxResults)
            throws java.rmi.RemoteException, WSFoundationException {
        // get the WSEvent api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEvents(type, value, appType, startRange, endRange,
                    orderedBy, firstResult, maxResults);
        }
    }

    public WSFoundationCollection getEventsForDevice(java.lang.String deviceName)
            throws java.rmi.RemoteException, WSFoundationException {
        // get the WSEvent api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsForDevice(deviceName);
        }
    }

    public WSFoundationCollection getEventStatisticsByHost(
            String applicationType, String hostName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException {
        // get the WSEvent api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventStatisticsByHost(applicationType, hostName,
                    fromRange, toRange, statisticType);
        }
    }

    public WSFoundationCollection getEventStatisticsByHostGroup(
            String applicationType, String hostGroupName, String fromRange,
            String toRange, String statisticType) throws WSFoundationException,
            RemoteException {
        // get the WSEvent api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventStatisticsByHostGroup(applicationType,
                    hostGroupName, fromRange, toRange, statisticType);
        }
    }

    public WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsByCriteria(filter, sort, firstResult,
                    maxResults);
        }
    }

    public WSFoundationCollection getEventsByIds(int[] ids, Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsByIds(ids, sort, firstResult, maxResults);
        }
    }

    public WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws RemoteException,
            WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getHostStateTransitions(hostName, startDate, endDate);
        }
    }

    public WSFoundationCollection getServiceStateTransitions(String hostName,
            String serviceName, String startDate, String endDate)
            throws RemoteException, WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getServiceStateTransitions(hostName, serviceName,
                    startDate, endDate);
        }
    }

    public WSFoundationCollection getEventsByCategory(String categoryName,
            String entityTypeName, Filter filter, Sort sort, int firstResult,
            int maxResults) throws RemoteException, WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsByCategory(categoryName, entityTypeName,
                    filter, sort, firstResult, maxResults);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSEvent#getEventsByRestrictedHostGroupsAndServiceGroups(java.lang.String,
     *      java.lang.String, org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getEventsByRestrictedHostGroupsAndServiceGroups(
            String hostGroupList, String serviceGroupList, Filter filter,
            Sort sort, int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        // get the WSHostGroup api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSEvent event = (WSEvent) factory.getAPIObject(BEAN_WSEVENT);

        // check the event object, if getting it failed, bail out now.
        if (event == null) {
            throw new WSFoundationException(
                    "Unable to create WSEvent instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
            return event.getEventsByRestrictedHostGroupsAndServiceGroups(
                    hostGroupList, serviceGroupList, filter, sort, firstResult,
                    maxResults);
        }
    }
}
