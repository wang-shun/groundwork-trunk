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
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.Sort;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "host group" web service
 * 
 * @author swapnil_gujrathi
 */
public interface IHostGroupWSFacade {

    /**
     * Returns the list of host-groups by calling foundation web service API.
     * 
     * @return the list of all host-groups
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    HostGroup[] getAllHostGroups() throws GWPortalException,
            WSDataUnavailableException;

    /**
     * Returns Host Groups by Criteria.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResult
     * @param deep
     * @return Host Groups by Criteria
     * @throws WSDataUnavailableException
     */
    HostGroup[] getHostGroupsbyCriteria(Filter filter, Sort sort,
            int firstResult, int maxResult, boolean deep)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Returns the list of host-groups by calling foundation web service API.
     * THis API accepts one boolean parameter - "deep". If deep is set to true,
     * then only it will fetch details of Host Group like Host Group children.
     * 
     * @param deep
     * @return the list of all host-groups
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    HostGroup[] getAllHostGroups(boolean deep) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * 
     * return number of host group satisfy filter condition otherwise -1
     * 
     * @param filter
     * @return int
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    int getEntireNetworkStatisticsbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Returns Host Group by its Id
     * 
     * @param hostGroupId
     * @return Host Group by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    HostGroup getHostGroupsById(int hostGroupId)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns Host Group by its Id
     * 
     * @param hostGroupId
     * @param deep
     * @return Host Group by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    HostGroup getHostGroupsById(int hostGroupId, boolean deep)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Returns Host Group by Name
     * 
     * @param hostGroupName
     * @return Host Group by Name
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    HostGroup getHostGroupsByName(String hostGroupName)
            throws WSDataUnavailableException, GWPortalException;

}