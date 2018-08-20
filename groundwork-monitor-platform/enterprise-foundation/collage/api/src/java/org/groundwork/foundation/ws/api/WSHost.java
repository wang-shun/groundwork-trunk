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

import org.groundwork.foundation.ws.model.HostQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.SortCriteria;

public interface WSHost extends java.rmi.Remote
{
    /**
     * 
     * @param type
     * @param value
     * @return
     * @throws WSFoundationException
     */
        public WSFoundationCollection getHosts(HostQueryType type, String value, String applicationType, int fromRange, int toRange, SortCriteria orderedBy) throws WSFoundationException, RemoteException;

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
    public WSFoundationCollection getHostsByString(String type, String value, String applicationType, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException;
    
    /** Method to lookup a host by full/partial name
     * @param hostName
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection hostLookup(String hostName) throws RemoteException, WSFoundationException;
        
    /**
     * Returns collection of hosts which match specified criteria.
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostsByCriteria(Filter filter, Sort sort, int firstResult, int maxResults)  throws RemoteException, WSFoundationException;
    
    
    /**
     * Gets list of host names only.Does not get the complete hierarchy of inner objects.
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostList()  throws RemoteException, WSFoundationException;
    
    
    /**
     * Gets Lightweight Host and service information.Does not return dynamic properties
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleHosts()  throws RemoteException, WSFoundationException;
    
    /**
     * Gets Lightweight Host and service information for the given hostGroup name.Does not return dynamic properties
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleHostsByHostGroupName(String hostGroupName, boolean deep)  throws RemoteException, WSFoundationException;
    
    
    /**
     * Gets Lightweight Host and service information.Does not return dynamic properties
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleHost(String hostName,boolean deep)  throws RemoteException, WSFoundationException;
    
    /**
     * Gets Lightweight Host and service information.Does not return dynamic properties
     * @return WSFoundationCollection(String[])
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getSimpleHostByCriteria(Filter filter,  Sort sort, int firstResult, int maxResults, boolean deep)  throws RemoteException, WSFoundationException;
}
