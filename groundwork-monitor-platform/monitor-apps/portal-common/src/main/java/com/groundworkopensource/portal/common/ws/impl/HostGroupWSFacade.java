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

package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSHostGroup;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IHostGroupWSFacade;

/**
 * This class provides methods to interact with "host group" foundation web
 * service.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class HostGroupWSFacade implements IHostGroupWSFacade {
    /**
     * REMOTE_EXCEPTION.
     */
    private static final String REMOTE_EXCEPTION = "RemoteException while contacting \"host group\" foundation web service in ";

    /**
     * WSFOUNDATION_EXCEPTION.
     */
    private static final String WSFOUNDATION_EXCEPTION = "WSFoundationException while getting hostgroups data in ";

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "hostgroup" web service
     * 
     * @return WSHostGroup Binding
     * @throws GWPortalException
     */
    private WSHostGroup getHostGroupBinding() throws GWPortalException {
        // get the host binding object
        try {
            WSHostGroup hostgroupBinding = WebServiceLocator.getInstance()
                    .hostGroupLocator().getwshostgroup();
            if (null != hostgroupBinding) {
                return hostgroupBinding;
            }
        } catch (ServiceException sEx) {
            LOGGER
                    .fatal("ServiceException while getting binding object for \"host group\" web service. "
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);

        }
        throw new GWPortalException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getAllHostGroups()
     */
    public final HostGroup[] getAllHostGroups() throws GWPortalException,
            WSDataUnavailableException {
        return getAllHostGroups(false);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getAllHostGroups(boolean)
     */
    public final HostGroup[] getAllHostGroups(boolean deep)
            throws GWPortalException, WSDataUnavailableException {
        WSHostGroup hostgroupBinding = getHostGroupBinding();

        try {
            // get list of host groups
            WSFoundationCollection hostGroups = hostgroupBinding.getHostGroups(
                    HostGroupQueryType.ALL, null, null, deep, -1, -1, null);
            if (hostGroups != null) {
                HostGroup[] hostGroupArr = hostGroups.getHostGroup();
                return hostGroupArr;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getAllHostGroups()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION + "getAllHostGroups()"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION + "getAllHostGroups()"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns Host Group by its Id
     * 
     * @param hostGroupId
     * @return Host Group by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public HostGroup getHostGroupsById(int hostGroupId)
            throws WSDataUnavailableException, GWPortalException {
        return getHostGroupsById(hostGroupId, true);
    }

    /**
     * Return Host group by ID
     * 
     * @param hostGroupId
     * @param deep
     * @return
     */
    public HostGroup getHostGroupsById(int hostGroupId, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        WSHostGroup hostgroupBinding = getHostGroupBinding();

        try {
            // get host groups by Id
            WSFoundationCollection hostGroups = hostgroupBinding.getHostGroups(
                    HostGroupQueryType.HOSTGROUPID,
                    String.valueOf(hostGroupId), null, deep, -1, -1, null);
            if (hostGroups != null) {
                HostGroup[] hostGroupArray = hostGroups.getHostGroup();
                if (null != hostGroupArray && hostGroupArray.length != 0
                        && null != hostGroupArray[0]) {
                    return hostGroupArray[0];
                }
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION + "getHostGroupsById"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);

        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION + "getHostGroupsById"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        // exception occurred or data not found.
        throw new WSDataUnavailableException();
    }

    /**
     * Returns Host Group by Name
     * 
     * @param hostGroupName
     * @return Host Group by Name
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public HostGroup getHostGroupsByName(String hostGroupName)
            throws WSDataUnavailableException, GWPortalException {
        WSHostGroup hostgroupBinding = getHostGroupBinding();
        try {
            // get host groups by Id
            WSFoundationCollection hostGroups = hostgroupBinding.getHostGroups(
                    HostGroupQueryType.HOSTGROUPNAME, hostGroupName, null,
                    false, -1, -1, null);
            if (hostGroups != null) {
                HostGroup[] hostGroupArray = hostGroups.getHostGroup();
                if (null != hostGroupArray && hostGroupArray.length != 0
                        && null != hostGroupArray[0]) {
                    return hostGroupArray[0];
                }
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION + "getHostGroupsByName"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new GWPortalException();

        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION + "getHostGroupsByName"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new GWPortalException();
        }
        // exception occurred or data not found.
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getHostGroupsbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int, boolean)
     */
    public HostGroup[] getHostGroupsbyCriteria(Filter filter, Sort sort,
            int firstResult, int maxResult, boolean deep)
            throws GWPortalException, WSDataUnavailableException {
        WSHostGroup hostgroupBinding = getHostGroupBinding();
        try {
            // get list of host groups

            WSFoundationCollection hostGroups = hostgroupBinding
                    .getHostGroupsByCriteria(filter, sort, firstResult,
                            maxResult, deep);
            if (hostGroups != null) {
                HostGroup[] hostGroupArr = hostGroups.getHostGroup();
                return hostGroupArr;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getHostGroupsbyCriteria()");

        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION + "getHostGroupsbyCriteria()",
                    fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION + "getHostGroupsbyCriteria()"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * 
     * return number of host group satisfy filter condition otherwise -1
     * 
     * @param filter
     * @return int
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public int getEntireNetworkStatisticsbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        WSHostGroup hostgroupBinding = getHostGroupBinding();
        try {
            // get list of host groups
            WSFoundationCollection hostGroups = hostgroupBinding
                    .getHostGroupsByCriteria(filter, null, -1, -1, false);
            if (hostGroups != null) {
                int count = hostGroups.getTotalCount();
                return count;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getEntireNetworkStatisticsbyCriteria()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION
                    + "getEntireNetworkStatisticsbyCriteria()"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION
                    + "getEntireNetworkStatisticsbyCriteria()"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }
}
