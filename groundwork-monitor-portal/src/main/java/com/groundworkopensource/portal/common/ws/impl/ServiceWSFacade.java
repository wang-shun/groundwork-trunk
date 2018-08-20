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
import org.groundwork.foundation.ws.api.WSService;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.ServiceQueryType;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSServiceFacade;

/**
 * This class provides methods to interact with "service" foundation web
 * service.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class ServiceWSFacade implements IWSServiceFacade {

    /**
     * REMOTE_EXCEPTION_MESSAGE
     */
    private static final String REMOTE_EXCEPTION_MESSAGE = "RemoteException while contacting \"service\" foundation web service in ";

    /**
     * Constant for "WSFoundationException while getting service data".
     */
    private static final String WSFOUNDATION_EXCEPTION_MESSAGE = "WSFoundationException while getting service data in ";

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "service" web service
     * 
     * @return WSService Binding
     * @throws GWPortalException
     */
    private WSService getServiceBinding() throws GWPortalException {
        // get the host binding object
        try {
            WSService serviceBinding = WebServiceLocator.getInstance()
                    .serviceLocator().getwsservice();
            if (null != serviceBinding) {
                return serviceBinding;
            }
        } catch (ServiceException e) {
            LOGGER
                    .fatal(
                            "ServiceException while getting binding object for \"service\" web service",
                            e);

        }
        throw new GWPortalException();
    }

    /**
     * Returns list of all services
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServices()
     */
    public final ServiceStatus[] getServices() throws GWPortalException,
            WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();

        try {
            WSFoundationCollection services = serviceBinding.getServices(
                    ServiceQueryType.ALL, null, null, -1, -1, null);
            if (services != null) {
                ServiceStatus[] serviceStatusArr = services.getServiceStatus();
                return serviceStatusArr;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServices()");

        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE + "getServices()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServices()", rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getTroubledServices()
     */
    public final ServiceStatus[] getTroubledServices()
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of troubled services
            WSFoundationCollection troubledServices = serviceBinding
                    .getTroubledServices(null, -1, -1);
            if (troubledServices != null) {
                ServiceStatus[] serviceStatusArr = troubledServices
                        .getServiceStatus();
                return serviceStatusArr;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getTroubledServices()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getTroubledServices()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getTroubledServices()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesByHostName(java.lang.String)
     */
    public ServiceStatus[] getServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services for a host
            WSFoundationCollection services = serviceBinding
                    .getServiceListByHostName(hostName);

            if (services != null) {
                ServiceStatus[] serviceStatusArray = services
                        .getServiceStatus();
                return serviceStatusArray;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServicesByHostName()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesByHostName()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesByHostName()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * This method retrieves array of simple services for the host.
     * 
     * @param hostName
     * @return SimpleServiceStatus array
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesByHostName(java.lang.String)
     */

    public SimpleServiceStatus[] getSimpleServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of simpleServices for a host
            WSFoundationCollection serviceList = serviceBinding
                    .getSimpleServiceListByHostName(hostName);

            if (serviceList != null) {
                SimpleServiceStatus[] simpleServices = serviceList
                        .getSimpleService();
                if ((simpleServices != null) && (simpleServices.length != 0)) {
                    return simpleServices;
                }
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting simpleServices in getSimpleServicesByHostName() for host : "
                            + hostName);
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesByHostName()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesByHostName()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesByHostId(int)
     */
    public ServiceStatus[] getServicesByHostId(int hostId)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding.getServices(
                    ServiceQueryType.HOSTID, Integer.toString(hostId), null,
                    -1, -1, null);
            if (services != null) {
                ServiceStatus[] serviceStatusArray = services
                        .getServiceStatus();
                return serviceStatusArray;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServicesByHostId()");

        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesByHostId()", fEx);
            LOGGER.error("cant find services for host: " + hostId);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesByHostId()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * get the service by criteria.
     * 
     * @param filter
     * @return list of ServiceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final ServiceStatus[] getServicesbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding
                    .getServicesByCriteria(filter, null, -1, -1);
            if (services != null) {
                ServiceStatus[] serviceStatusArray = services
                        .getServiceStatus();
                return serviceStatusArray;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServicesbyCriteria()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesbyCriteria()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesbyCriteria()",
                    rEx);
        }

        throw new WSDataUnavailableException();
    }

    /**
     * get the simple service by criteria.
     * 
     * @param filter
     * @return list of simpleServiceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final SimpleServiceStatus[] getSimpleServicesbyCriteria(
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding
                    .getSimpleServiceListByCriteria(filter, sort, firstResult,
                            maxResults);
            if (services != null) {
                return services.getSimpleService();
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServicesbyCriteria()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesbyCriteria()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesbyCriteria()",
                    rEx);
        }

        throw new WSDataUnavailableException();
    }

    /**
     * returns services by criteria.
     * 
     * @param filter
     * @param sort
     * @param startIndex
     * @param pagesize
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getServicesbyCriteria(Filter filter,
            Sort sort, int startIndex, int pagesize)
            throws WSDataUnavailableException, GWPortalException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection wsCollection = serviceBinding
                    .getServicesByCriteria(filter, sort, startIndex, startIndex
                            + pagesize);
            return wsCollection;

        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesbyCriteria()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesbyCriteria()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * Returns ServiceStatus by its Id
     * 
     * @param serviceId
     * @return ServiceStatus by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public ServiceStatus getServicesById(int serviceId)
            throws WSDataUnavailableException, GWPortalException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding.getServices(
                    ServiceQueryType.SERVICESTATUSID, Integer
                            .toString(serviceId), null, -1, -1, null);
            if (services != null) {
                ServiceStatus[] serviceStatusArray = services
                        .getServiceStatus();
                if (null != serviceStatusArray
                        && serviceStatusArray.length != 0
                        && null != serviceStatusArray[0]) {
                    return serviceStatusArray[0];
                }
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services in getServicesById()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE + "getServicesById()",
                    fEx);
            LOGGER.error("cant find services for host: " + serviceId);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesById()", rEx);
        }
        // exception occurred or data not found.
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServiceByHostAndServiceName(java.lang.String,
     *      java.lang.String)
     */
    public ServiceStatus getServiceByHostAndServiceName(String hostName,
            String serviceName) throws WSDataUnavailableException,
            GWPortalException {
        WSService serviceBinding = getServiceBinding();
        try {
            // left filter for service name
            Filter left = new Filter(FilterConstants.SERVICE_DESC,
                    FilterOperator.EQ, serviceName);
            // right filter for host name
            Filter right = new Filter(FilterConstants.HOST_HOSTNAME,
                    FilterOperator.EQ, hostName);
            // AND the 2 filters
            Filter filter = Filter.AND(left, right);

            // get this service
            WSFoundationCollection wsCollection = serviceBinding
                    .getServicesByCriteria(filter, null, -1, -1);
            if (wsCollection != null) {
                ServiceStatus[] serviceStatusArray = wsCollection
                        .getServiceStatus();
                if (null != serviceStatusArray
                        && serviceStatusArray.length != 0
                        && null != serviceStatusArray[0]) {
                    return serviceStatusArray[0];
                }
            }
            LOGGER
                    .debug("WSFoundationCollection is null while getting services in getServiceByHostAndServiceName()");
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE + "getServicesById()",
                    fEx);
            LOGGER.error("cant find service [" + serviceName + "] for host ["
                    + hostName + "]");
            throw new GWPortalException();

        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + "getServiceByHostAndServiceName()", rEx);
            throw new GWPortalException();
        }
        // exception occurred or data not found.
        throw new WSDataUnavailableException();
    }

    /**
     * Returns list of Services under a Servicegroup. takes servicegroupid as a
     * parameter.
     * 
     * @param servicegroupid
     * @return List of Services under a servicegroup
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public ServiceStatus[] getServicesByServiceGroupId(int serviceGroupId)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding.getServices(
                    ServiceQueryType.SERVICEGROUPID, Integer
                            .toString(serviceGroupId), null, -1, -1, null);
            if (services != null) {
                ServiceStatus[] serviceStatusArray = services
                        .getServiceStatus();
                return serviceStatusArray;
            }
            LOGGER
                    .info("WSFoundationCollection is null while getting services by criteria in getServicesByHostId()");

        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesByHostId()", fEx);
            LOGGER.error("cant find services for host: " + serviceGroupId);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesByHostId()",
                    rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getSimpleServiceCollectionbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getSimpleServiceCollectionbyCriteria(
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws GWPortalException, WSDataUnavailableException {
        WSService serviceBinding = getServiceBinding();
        try {
            // get list of services
            WSFoundationCollection services = serviceBinding
                    .getSimpleServiceListByCriteria(filter, sort, firstResult,
                            maxResults);
            return services;
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + "getServicesbyCriteria()", fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE + "getServicesbyCriteria()",
                    rEx);
        }

        throw new WSDataUnavailableException();
    }
}
