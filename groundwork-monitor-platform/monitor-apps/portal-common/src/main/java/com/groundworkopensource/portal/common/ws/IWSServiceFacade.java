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
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "service" web service
 * 
 * @author swapnil_gujrathi
 */
public interface IWSServiceFacade {

    /**
     * Returns all services.
     * 
     * @return ServiceStatus array of all services
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getServices() throws GWPortalException,
            WSDataUnavailableException;

    /**
     * Returns list of troubled services.
     * 
     * @return ServiceStatus array of troubled services
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getTroubledServices() throws GWPortalException,
            WSDataUnavailableException;

    /**
     * @param filter
     * @return serviceStatus array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getServicesbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Get services by criteria
     * 
     * @param filter
     * @param sort
     * @param startIndex
     * @param pagesize
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    WSFoundationCollection getServicesbyCriteria(Filter filter, Sort sort,
            int startIndex, int pagesize) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * @param hostName
     * @return ServiceStatus[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Returns list of Services under a Host. takes HostId as a parameter.
     * 
     * @param hostId
     * @return List of Services under a Host
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getServicesByHostId(int hostId) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * Returns ServiceStatus by its Id
     * 
     * @param serviceId
     * @return ServiceStatus by its Id
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    ServiceStatus getServicesById(int serviceId)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * Retrieves particular Service when passed Service Name and Host Name to
     * which it belongs to.
     * 
     * @param hostName
     * @param serviceName
     * @return Service by passed HostName and ServiceName.
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    ServiceStatus getServiceByHostAndServiceName(String hostName,
            String serviceName) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns list of Services under a Servicegroup. takes servicegroupid as a
     * parameter.
     * 
     * @param serviceGroupId
     * 
     * @param servicegroupid
     * @return List of Services under a servicegroup
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    ServiceStatus[] getServicesByServiceGroupId(int serviceGroupId)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * This method retrieves array of simple services for the host.
     * 
     * @param hostName
     * @return SimpleServiceStatus array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    SimpleServiceStatus[] getSimpleServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * get the simple service by criteria.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return list of simpleServiceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    SimpleServiceStatus[] getSimpleServicesbyCriteria(Filter filter, Sort sort,
            int firstResult, int maxResults) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * get the simple service by criteria.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return list of simpleServiceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    WSFoundationCollection getSimpleServiceCollectionbyCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults)
            throws GWPortalException, WSDataUnavailableException;

}