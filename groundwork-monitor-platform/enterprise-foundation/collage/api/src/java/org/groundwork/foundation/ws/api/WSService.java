/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.api;

import java.rmi.RemoteException;

import org.groundwork.foundation.ws.model.ServiceQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.SortCriteria;

public interface WSService extends java.rmi.Remote
{
    /**
     * 
     * @param type
     * @param value
     * @return
     * @throws WSFoundationException
     */
    public WSFoundationCollection getServices(ServiceQueryType type, String value, String applicationType,  int fromRange, int toRange, SortCriteria orderedBy) throws RemoteException, WSFoundationException;

    /**
     * Overloaded method allowing String parameters specifically used by custom reporting
     * data source.
     * 
     * @param type
     * @param value
     * @param applicationType
     * @param fromRange
     * @param toRange
     * @param sortOrder
     * @param sortField
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getServicesByString(String type, String value, String applicationType, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException;
    
    /**
     * Returns collection of services which match specified criteria.
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getServicesByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException;
    
    /**
     * Returns collection of services which match specified criteria.
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getTroubledServices(Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException;
    
    /**
     * Gets all services (service names only) for the supplied host..Does not get the complete hierarchy of inner objects.
     * @param hostName
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getServiceListByHostName(String hostName)  throws RemoteException, WSFoundationException;
   
    /**
     * Gets Lightweight service information.Does not return dynamic properties
     * @param hostName
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleServiceListByHostName(String hostName)  throws RemoteException, WSFoundationException;
    
    /**
     * Gets Lightweight service information.Does not return dynamic properties
     * @param filter
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleServiceListByCriteria(Filter filter,Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException;
}
