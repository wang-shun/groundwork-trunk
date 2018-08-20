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
import org.groundwork.foundation.ws.api.WSHost;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostQueryType;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IHostWSFacade;

/**
 * This class provides methods to interact with "host" foundation web service.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class HostWSFacade implements IHostWSFacade {

    /**
     * Message for - RemoteException while contacting "host" foundation web
     * service
     */
    private static final String REMOTE_EXCEPTION_MESSAGE = "RemoteException while contacting \"host\" foundation web service, in getHostsbyCriteria()";

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "host" web service
     * 
     * @return WSHost Binding
     * @throws GWPortalException
     */
    private WSHost getHostBinding() throws GWPortalException {
        // get the host binding object
        try {
            WSHost hostBinding = WebServiceLocator.getInstance().hostLocator()
                    .gethost();
            if (null != hostBinding) {
                return hostBinding;
            }
        } catch (ServiceException sEx) {
            LOGGER
                    .fatal("ServiceException while getting binding object for \"host\" web service."
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);
        }
        throw new GWPortalException();
    }

    /**
     * Returns all hosts
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getAllHosts()
     */
    public final Host[] getAllHosts() throws WSDataUnavailableException,
            GWPortalException {
        WSHost hostBinding = getHostBinding();

        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding.getHosts(
                    HostQueryType.ALL, null, null, -1, -1, null);
            if (hosts != null) {
                Host[] hostArr = hosts.getHost();
                return hostArr;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while retrieving all hosts in getAllHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while retrieving all hosts, in getAllHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns hosts
     * 
     * @param hostQueryType
     * @param value
     * @param applicationType
     * @param startRange
     * @param endRange
     * @param sortCriteria
     * @return WSFoundationCollection
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getAllHosts()
     */
    public final WSFoundationCollection getHosts(HostQueryType hostQueryType,
            String value, String applicationType, int startRange, int endRange,
            SortCriteria sortCriteria) throws WSDataUnavailableException,
            GWPortalException {
        WSHost hostBinding = getHostBinding();

        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding.getHosts(hostQueryType,
                    value, applicationType, startRange, endRange, sortCriteria);
            if (hosts != null) {
                return hosts;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while retrieving all hosts in getAllHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while retrieving all hosts, in getAllHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns the light weight hosts and services
     * 
     * @return the list of hosts
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public SimpleHost[] getSimpleHosts() throws WSDataUnavailableException,
            GWPortalException {
        WSHost hostBinding = getHostBinding();

        try {
            // get list of hosts
            WSFoundationCollection col = hostBinding.getSimpleHosts();
            if (col != null) {
                SimpleHost[] hostArr = col.getSimpleHost();
                return hostArr;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while retrieving all hosts in getSimpleHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while retrieving all hosts, in getSimpleHosts()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns particular Host by its Name
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsByName(java.lang.String)
     */
    public final Host getHostsByName(String hostName)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding.getHosts(
                    HostQueryType.HOSTNAME, hostName, null, -1, -1, null);
            if (hosts != null) {
                Host[] hostArr = hosts.getHost();
                if (null != hostArr && hostArr.length != 0
                        && null != hostArr[0]) {
                    return hostArr[0];
                }
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts by host-name in getHostsByName()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new GWPortalException();

        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts by host-name in getHostsByName()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new GWPortalException();
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @param hostId
     * @return Host
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsById(java.lang.String)
     */
    public final Host getHostsById(final String hostId)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding.getHosts(
                    HostQueryType.HOSTID, hostId, null, -1, -1, null);
            Host[] hostArr = hosts.getHost();
            if (null != hostArr && hostArr.length != 0 && null != hostArr[0]) {
                return hostArr[0];
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error("WSFoundationException while getting hosts by host-Id"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }

        // exception occurred or data not found.
        throw new WSDataUnavailableException();
    }

    /**
     * Returns Hosts under given host group
     * 
     * @param hostGroupName
     * @param deep
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsUnderHostGroup(java.lang.String,
     *      boolean)
     */
    public final SimpleHost[] getHostsUnderHostGroup(
            final String hostGroupName, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding
                    .getSimpleHostsByHostGroupName(hostGroupName, deep);
            if (hosts != null) {
                SimpleHost[] hostArr = hosts.getSimpleHost();
                return hostArr;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts by hostgroup-name in getHostsUnderHostGroup()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts by hostgroup-name in getHostsUnderHostGroup()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns hosts under host group by its id
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsUnderHostGroupById(java.lang.String)
     */
    public final Host[] getHostsUnderHostGroupById(final String hostGroupId)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get list of hosts
            WSFoundationCollection hosts = hostBinding.getHosts(
                    HostQueryType.HOSTGROUPID, hostGroupId, null, -1, -1, null);
            if (hosts != null) {
                Host[] hostArr = hosts.getHost();
                return hostArr;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts by hostgroup-Id in getHostsUnderHostGroupById()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts by hostgroup-Id in getHostsUnderHostGroupById()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * get the Host by criteria.
     * 
     * @param filter
     * @return Host
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Host[] getHostsbyCriteria(Filter filter)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();

        try {
            WSFoundationCollection hosts = hostBinding.getHostsByCriteria(
                    filter, null, -1, -1);
            if (hosts == null) {
                throw new WSDataUnavailableException();
            }
            // if no host available then it return null Host[].
            Host[] hostArr = hosts.getHost();
            return hostArr;

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts data in getHostsbyCriteria()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts data in getHostsbyCriteria()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

    }

    /**
     * get the Simple Host by criteria.
     * 
     * @param filter
     * @param deep
     * @return Simple Host
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getSimpleHostsbyCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();

        try {
            WSFoundationCollection hosts = hostBinding.getSimpleHostByCriteria(
                    filter, sort, firstResult, maxResults, deep);
            if (hosts == null) {
                throw new WSDataUnavailableException();
            }
            // if no host available then it return null Host[].
            return hosts;

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts data in getHostsbyCriteria()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts data in getHostsbyCriteria()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

    }

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
    public WSFoundationCollection getHostsbyCriteria(Filter filter, Sort sort,
            int startIndex, int pageSize) throws WSDataUnavailableException,
            GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            WSFoundationCollection collection = hostBinding.getHostsByCriteria(
                    filter, sort, startIndex, startIndex + pageSize);
            return collection;
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts data in getHostsbyCriteria()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * return number of host satisfy filter condition otherwise -1
     * i.e.Unscheduled Or Scheduled Host Count
     * 
     * @param filter
     * @return int
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public int getUnscheduledOrScheduledHostCount(Filter filter)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get list of host groups
            WSFoundationCollection hosts = hostBinding.getHostsByCriteria(
                    filter, null, -1, -1);
            if (hosts != null) {
                int count = hosts.getTotalCount();
                return count;
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting hosts data in getUnscheduledOrScheduledHostCount()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts data in getUnscheduledOrScheduledHostCount()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();

    }

    /**
     * This method returns the SimpleHost for the hostName parameter
     * 
     * @param hostName
     * @param deep
     *            - if set to true fetches the simpleServices for the host,if
     *            false only fetches the simpeHost.
     * @return SimpleHosts
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     */
    public SimpleHost getSimpleHostByName(String hostName, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        WSHost hostBinding = getHostBinding();
        try {
            // get the simple hosts
            WSFoundationCollection hosts = hostBinding.getSimpleHost(hostName,
                    deep);
            if (hosts != null) {
                SimpleHost[] simpleHostArray = hosts.getSimpleHost();
                if ((simpleHostArray != null) && (simpleHostArray.length != 0)) {
                    return simpleHostArray[0];
                }
            }
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting simple hosts by host-name in getSimpleHostsByName() for host : "
                            + hostName
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new GWPortalException();

        } catch (RemoteException rEx) {
            LOGGER
                    .error("RemoteException while getting hosts by host-name in getSimpleHostsByName()"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new GWPortalException();
        }
        throw new WSDataUnavailableException();
    }
}
