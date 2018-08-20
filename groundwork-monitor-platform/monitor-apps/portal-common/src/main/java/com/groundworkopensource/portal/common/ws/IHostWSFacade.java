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

package com.groundworkopensource.portal.common.ws;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostQueryType;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "host" web service
 * 
 * @author swapnil_gujrathi
 */
public interface IHostWSFacade {

    /**
     * Returns the list of all hosts by calling foundation web service API
     * 
     * @return the list of hosts
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Host[] getAllHosts() throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns the light weight hosts and services
     * 
     * @return the list of hosts
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    SimpleHost[] getSimpleHosts() throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns the host as per the passed 'host name'
     * 
     * @param hostName
     * @return the host as per the passed 'host name'
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Host getHostsByName(String hostName) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns the list of all hosts as per the passed 'host Id'
     * 
     * @param hostId
     * @return the list of hosts by passed 'host Id'
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Host getHostsById(String hostId) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns the list of all hosts under the passed 'host group Id'
     * 
     * @param hostGroupId
     * @return the list of all hosts under the passed 'host group Id'
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Host[] getHostsUnderHostGroupById(String hostGroupId)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns the list of all hosts under filter criteria
     * 
     * @param filter
     * @return the list of all hosts under filter criteria
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Host[] getHostsbyCriteria(Filter filter) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * return number of host satisfy filter condition otherwise -1
     * i.e.Unscheduled Or Scheduled Host Count
     * 
     * @param filter
     * @return int
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    int getUnscheduledOrScheduledHostCount(Filter filter)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns hosts by provided criteria
     * 
     * @param filter
     * @param sort
     * @param startIndex
     * @param pageSize
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getHostsbyCriteria(Filter filter, Sort sort,
            int startIndex, int pageSize) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns the list of all hosts under the passed 'host group name'
     * 
     * @param hostGroupName
     * @param deep
     * @return the list of all SIMPLE hosts under the passed 'host group name'
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    SimpleHost[] getHostsUnderHostGroup(String hostGroupName, boolean deep)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns the list of SimpleHost object for the hostName parameter.
     * 
     * @param hostName
     * @param deep
     * @return SimpleHost
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    SimpleHost getSimpleHostByName(String hostName, boolean deep)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * get the Simple Host by criteria.
     * 
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @param deep
     * @return Simple Hosts
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getSimpleHostsbyCriteria(Filter filter, Sort sort,
            int firstResult, int maxResults, boolean deep)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * return WSFoundationCollection for hosts
     * 
     * @param hostQueryType
     * @param value
     * @param applicationType
     * @param startRange
     * @param endRange
     * @param sortCriteria
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getHosts(HostQueryType hostQueryType, String value,
            String applicationType, int startRange, int endRange,
            SortCriteria sortCriteria) throws WSDataUnavailableException,
            GWPortalException;

}