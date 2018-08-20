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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSService;
import org.groundwork.foundation.ws.model.ServiceQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ServiceStatus;

// TODO: Auto-generated Javadoc
/**
 * WebServiec Implementation for WSService interface.
 * 
 * @author rogerrut
 */
public class WSServiceImpl extends WebServiceImpl implements WSService {

    /* Enable logging */
    /** The log. */
    protected static Log log = LogFactory.getLog(WSServiceImpl.class);

    /**
     * Instantiates a new wS service impl.
     */
    public WSServiceImpl() {
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSService#getServices(org.groundwork
     *      .foundation.ws.model.ServiceQueryType, java.lang.String,
     *      java.lang.String, int, int,
     *      org.groundwork.foundation.ws.model.impl.SortCriteria)
     */
    public WSFoundationCollection getServices(
            ServiceQueryType serviceQueryType, String value,
            String applicationType, int fromRange, int maxResults,
            SortCriteria orderedBy) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        WSFoundationCollection services = null;

        // check first for null type and if so, return all Services
        if (serviceQueryType == null
                || (value == null && serviceQueryType != org.groundwork.foundation.ws.model.impl.ServiceQueryType.ALL)) {
            log
                    .error("ServiceQueryType cannot be null or value cannot be null unless ServiceQueryType is ALL");
            throw new WSFoundationException(
                    "ServiceQueryType cannot be null or value cannot be null unless ServiceQueryType is ALL",
                    ExceptionType.WEBSERVICE);
        }

        try {
            if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.ALL
                    .equals(serviceQueryType))
                services = getServices(applicationType, fromRange, maxResults);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.HOSTGROUPID
                    .equals(serviceQueryType))
                services = getServicesByHostGroupId(value);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.HOSTGROUPNAME
                    .equals(serviceQueryType))
                services = getServicesByHostGroupName(value);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.SERVICEDESCRIPTION
                    .equals(serviceQueryType))
                services = getServicesForServiceDescription(value, fromRange,
                        maxResults);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.SERVICESTATUSID
                    .equals(serviceQueryType))
                services = getServiceByServiceStatusID(value);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.HOSTNAME
                    .equals(serviceQueryType))
                services = getServicesForHostName(value);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.HOSTID
                    .equals(serviceQueryType))
                services = getServicesForHostId(value);
            else if (org.groundwork.foundation.ws.model.impl.ServiceQueryType.SERVICEGROUPID
                    .equals(serviceQueryType))
                services = getServicesForServiceGroupId(value);
            else
                throw new WSFoundationException(
                        "Invalid ServiceQueryType specified in getServices",
                        ExceptionType.WEBSERVICE);

            return services;
        } catch (WSFoundationException wsfe) {
            log.error("Exception occurred in getServices()", wsfe);
            throw wsfe;
        } catch (Exception e) {
            log.error("Exception occurred in getServices()", e);
            throw new WSFoundationException(
                    "Exception occurred in getServices() - " + e,
                    ExceptionType.WEBSERVICE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * String parameter version of getServices().
     * 
     * @param type
     *            the type
     * @param value
     *            the value
     * @param applicationType
     *            the application type
     * @param fromRange
     *            the from range
     * @param toRange
     *            the to range
     * @param sortOrder
     *            the sort order
     * @param sortField
     *            the sort field
     * 
     * @return the services by string
     * 
     * @throws RemoteException
     *             the remote exception
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    public WSFoundationCollection getServicesByString(String type,
            String value, String applicationType, String fromRange,
            String toRange, String sortOrder, String sortField)
            throws RemoteException, WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        // Do parameter conversion then delegate
        org.groundwork.foundation.ws.model.impl.ServiceQueryType queryType = org.groundwork.foundation.ws.model.impl.ServiceQueryType.ALL;

        if (type != null) {
            queryType = org.groundwork.foundation.ws.model.impl.ServiceQueryType
                    .fromValue(type);
        }

        int intFromRange = 0;
        int intToRange = 0;

        if (fromRange != null && fromRange.length() > 0) {
            try {
                intFromRange = Integer.parseInt(fromRange);
            } catch (Exception e) {
            } // Suppress and just use default value
        }

        if (toRange != null && toRange.length() > 0) {
            try {
                intToRange = Integer.parseInt(toRange);
            } catch (Exception e) {
            } // Suppress and just use default value
        }

        SortCriteria sortCriteria = null;
        if (sortOrder != null && sortOrder.trim().length() > 0
                && sortField != null && sortField.trim().length() > 0) {
            sortCriteria = new SortCriteria(sortOrder, sortField);
        }

        WSFoundationCollection services = getServices(queryType, value, applicationType, intFromRange, intToRange, sortCriteria);
        stopMetricsTimer(timer);
        return services;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.groundwork.foundation.ws.api.WSService#getServicesByCriteria(org.
     *      groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getServicesByCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FilterCriteria filterCriteria = getConverter().convert(filter);
            org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                    .convert(sort);

            FoundationQueryList list = getStatusService().getServices(
                    filterCriteria, sortCriteria, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (Exception e) {
            log.error("Exception occurred in getServicesByCriteria()", e);
            throw new WSFoundationException(
                    "Exception occurred in getServicesByCriteria()" + e,
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.groundwork.foundation.ws.api.WSService#getTroubledServices(org.groundwork
     * .foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getTroubledServices(Sort sort,
            int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {

            FilterCriteria filter = FilterCriteria.ne(
                    "host.hostStatus.hostMonitorStatus.name", "DOWN");
            filter.and(FilterCriteria.ne(
                    "host.hostStatus.hostMonitorStatus.name", "UNREACHABLE"));
            filter.and(FilterCriteria.ne("monitorStatus.name", "OK"));
            filter.and(FilterCriteria.ne("monitorStatus.name", "PENDING"));

            org.groundwork.foundation.dao.SortCriteria sortCriteria = getConverter()
                    .convert(sort);

            FoundationQueryList list = getStatusService().getServices(filter,
                    sortCriteria, firstResult, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (Exception e) {
            log.error("Exception occurred in getTroubledServices()", e);
            throw new WSFoundationException(
                    "Exception occurred in getTroubledServices()" + e,
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Gets all services (service names only) for the supplied host..Does not
     * get the complete hierarchy of inner objects.
     * 
     * @param hostName
     *            the host name
     * 
     * @return WSFoundationCollection(String[])
     * 
     * @throws RemoteException
     *             the remote exception
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    public WSFoundationCollection getServiceListByHostName(String hostName)
            throws RemoteException, WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        WSFoundationCollection col = this.getServicesForHostName(hostName);
        stopMetricsTimer(timer);
        return col;
    }

    /*
     * Get all services
     */
    /**
     * Gets the services.
     * 
     * @param appType
     *            the app type
     * @param startRange
     *            the start range
     * @param maxResults
     *            the max results
     * 
     * @return the services
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServices(String appType, int startRange,
            int maxResults) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FilterCriteria filterCriteria = null;

            if (appType != null && appType.length() > 0) {
                filterCriteria = FilterCriteria.eq(
                        StatusService.PROP_APP_TYPE_NAME, appType);
            }

            FoundationQueryList list = getStatusService().getServices(
                    filterCriteria, null, startRange, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all Services that belong to a particular hostgroup
     */
    /**
     * Gets the services by host group name.
     * 
     * @param hgName
     *            the hg name
     * 
     * @return the services by host group name
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesByHostGroupName(String hgName)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FoundationQueryList list = getStatusService()
                    .getServicesByHostGroupName(hgName, null, null, -1, -1);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all Services that belong to a particular hostgroup
     */
    /**
     * Gets the services by host group id.
     * 
     * @param hgID
     *            the hg id
     * 
     * @return the services by host group id
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesByHostGroupId(String hgID)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FoundationQueryList list = getStatusService()
                    .getServicesByHostGroupId(Integer.valueOf(hgID).intValue(),
                            null, null, -1, -1);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all Services that are identified by a particular service description
     */
    /**
     * Gets the services for service description.
     * 
     * @param serviceDescription
     *            the service description
     * @param startRange
     *            the start range
     * @param maxResults
     *            the max results
     * 
     * @return the services for service description
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesForServiceDescription(
            String serviceDescription, int startRange, int maxResults)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FilterCriteria filterCriteria = null;

            if (serviceDescription != null && serviceDescription.length() > 0) {
                filterCriteria = FilterCriteria.eq(
                        StatusService.PROP_SERVICEDESCRIPTION,
                        serviceDescription);
            }

            FoundationQueryList list = getStatusService().getServices(
                    filterCriteria, null, startRange, maxResults);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get a specific service by servicestatus id
     */
    /**
     * Gets the service by service status id.
     * 
     * @param serviceID
     *            the service id
     * 
     * @return the service by service status id
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServiceByServiceStatusID(String serviceID)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            ServiceStatus serviceStatus = getStatusService().getServiceById(
                    Integer.valueOf(serviceID).intValue());
            if (serviceStatus == null)
                return new WSFoundationCollection(
                        0,
                        new org.groundwork.foundation.ws.model.impl.ServiceStatus[0]);

            return new WSFoundationCollection(
                    1,
                    new org.groundwork.foundation.ws.model.impl.ServiceStatus[] { getConverter()
                            .convert(serviceStatus) });
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /*
     * Get all Services for a particular host
     */
    /**
     * Gets the services for host name.
     * 
     * @param hostName
     *            the host name
     * 
     * @return the services for host name
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesForHostName(String hostName)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FoundationQueryList list = getStatusService()
                    .getServicesByHostName(hostName, null, null, -1, -1);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Get all Services for a particular host
     */
    /**
     * Gets the services for host id.
     * 
     * @param hostId
     *            the host id
     * 
     * @return the services for host id
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesForHostId(String hostId)
            throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            FoundationQueryList list = getStatusService().getServicesByHostId(
                    Integer.valueOf(hostId).intValue(), null, null, -1, -1);

            return new WSFoundationCollection(list.getTotalCount(),
                    getConverter().convertServiceStatus(
                            (Collection<ServiceStatus>) list.getResults()));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Gets the services for service group id.
     * 
     * @param serviceGroupId
     *            the service group id
     * 
     * @return the services for service group id
     * 
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    private WSFoundationCollection getServicesForServiceGroupId(
            String serviceGroupId) throws WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        try {
            List<ServiceStatus> list = getStatusService().getServicesByCategoryId(
                            Integer.valueOf(serviceGroupId).intValue());

            return new WSFoundationCollection(list.size(),
                    getConverter().convertServiceStatus(list));
        } catch (CollageException e) {
            throw new WSFoundationException(e.getMessage(),
                    ExceptionType.DATABASE);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Gets Lightweight service information.Does not return dynamic properties
     * 
     * @param hostName
     *            the host name
     * 
     * @return WSFoundationCollection(String[])
     * 
     * @throws RemoteException
     *             the remote exception
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    public WSFoundationCollection getSimpleServiceListByHostName(String hostName)
            throws RemoteException, WSFoundationException {

        CollageTimer timer = startMetricsTimer();
        FoundationQueryList list = getStatusService().getServicesByHostName(
                hostName, null, null, -1, -1);
        List<ServiceStatus> serviceStatuses = list.getResults();
        if (serviceStatuses == null || serviceStatuses.size() <= 0) {
            return new WSFoundationCollection(0, new SimpleServiceStatus[1]);
        } // end if
        SimpleServiceStatus[] simpleServices = this
                .getSimpleServiceStatusFromServiceStatus(serviceStatuses);
        WSFoundationCollection services = new WSFoundationCollection(simpleServices.length, simpleServices);
        stopMetricsTimer(timer);
        return services;
    }

    /**
     * Gets Lightweight service information.Does not return dynamic properties
     * 
     * @param filter
     *            the filter
     * @param sort
     *            the sort
     * @param firstResult
     *            the first result
     * @param maxResults
     *            the max results
     * 
     * @return WSFoundationCollection(String[])
     * 
     * @throws RemoteException
     *             the remote exception
     * @throws WSFoundationException
     *             the WS foundation exception
     */
    public WSFoundationCollection getSimpleServiceListByCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults) throws RemoteException,
            WSFoundationException {
        CollageTimer timer = startMetricsTimer();
        if (filter == null)
            throw new WSFoundationException("Invalid filter",
                    ExceptionType.WEBSERVICE);
        FilterCriteria filterCriteria = getConverter().convert(filter);
        org.groundwork.foundation.dao.SortCriteria sortCriteria = null;
        if (sort != null) {
            sortCriteria = getConverter().convert(sort);
        } // end if
        FoundationQueryList list = getStatusService().getServices(
                filterCriteria, sortCriteria, firstResult, maxResults);
        List<ServiceStatus> serviceStatuses = list.getResults();
        if (serviceStatuses == null || serviceStatuses.size() <= 0) {
            return new WSFoundationCollection(0, new SimpleServiceStatus[1]);
        } // end if
        SimpleServiceStatus[] simpleServices = this
                .getSimpleServiceStatusFromServiceStatus(serviceStatuses);
        WSFoundationCollection services = new WSFoundationCollection(list.getTotalCount(), simpleServices);
        stopMetricsTimer(timer);
        return services;
    }

    /**
     * Creates and returns an array of Simple Service Statuses from a list of
     * Service Status objects.
     * 
     * @param serviceStatuses
     *            the service statuses
     * 
     * @return SimpleServiceStatus[]
     */
    private SimpleServiceStatus[] getSimpleServiceStatusFromServiceStatus(
            List<ServiceStatus> serviceStatuses) {
        CollageTimer timer = startMetricsTimer();
        int simpleServiceSize = serviceStatuses.size();
        SimpleServiceStatus[] simpleServices = new SimpleServiceStatus[simpleServiceSize];
        if (serviceStatuses != null) {
            int j = 0;
            for (ServiceStatus serviceStatus : serviceStatuses) {
                SimpleServiceStatus simpleServiceStatus = new SimpleServiceStatus();
                simpleServiceStatus.setServiceStatusID(serviceStatus
                        .getServiceStatusId().intValue());
                simpleServiceStatus.setDescription(serviceStatus
                        .getServiceDescription());
                simpleServiceStatus.setMonitorStatus(serviceStatus
                        .getMonitorStatus().getName());
                simpleServiceStatus.setLastCheckTime(serviceStatus
                        .getLastCheckTime());
                simpleServiceStatus.setHostName(serviceStatus.getHost()
                        .getHostName());
                simpleServiceStatus.setHostId(serviceStatus.getHost()
                        .getHostId().intValue());
                simpleServiceStatus.setNextCheckTime(serviceStatus
                        .getNextCheckTime());
                boolean acknowledged = false;
                if (serviceStatus != null) {
                    Object acknowledgedObj = serviceStatus
                            .getProperty("isProblemAcknowledged");
                    if (acknowledgedObj != null)
                        acknowledged = ((Boolean) acknowledgedObj)
                                .booleanValue();
                } // end if
                simpleServiceStatus.setAcknowledged(acknowledged);
                Date lastServiceStateChange = null;
                if (serviceStatus != null) {
                    Object lastServiceStateChangeObj = serviceStatus
                            .getProperty("LastStateChange");
                    if (lastServiceStateChangeObj != null)
                        lastServiceStateChange = (Date) lastServiceStateChangeObj;
                } // end if

                simpleServiceStatus.setLastStateChange(lastServiceStateChange);
                String lastPluginOutput = null;
                if (serviceStatus != null) {
                    Object lastPluginOuputObj = serviceStatus
                            .getProperty("LastPluginOutput");
                    if (lastPluginOuputObj != null)
                        lastPluginOutput = (String) lastPluginOuputObj;
                } // end if
                simpleServiceStatus.setLastPlugInOutput(lastPluginOutput);
                simpleServices[j] = simpleServiceStatus;
                j++;
            } // end for
        } // end if
        stopMetricsTimer(timer);
        return simpleServices;
    }
}
