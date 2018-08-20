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

import org.groundwork.foundation.ws.model.HostGroupQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public interface WSHostGroup extends java.rmi.Remote
{
    
    /**
     * 
     * @param type
     * @param value
     * @param deep
     * @return
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostGroups(HostGroupQueryType type, String value, String applicationType, boolean deep, int fromRange, int toRange, SortCriteria orderedBy) throws RemoteException, WSFoundationException;
    
    /**
     * Overloaded method allowing String parameters specifically used by custom reporting
     * data source.
     * 
     * @param type
     * @param value
     * @param applicationType
     * @param deep
     * @param fromRange
     * @param toRange
     * @param sortOrder
     * @param sortField
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostGroupsByString(String type, String value, String applicationType, String deep, String fromRange, String toRange, String sortOrder, String sortField) throws RemoteException, WSFoundationException;    

    /**
     * Returns host group information wrapped in HostGroupInfo instances.
     * Each instance represents a single host within the host group.  
     * 
     * The instances are sorted by application name, host group name and then host name
     * 
     * @param type
     * @param value
     */
    public WSFoundationCollection getHostGroupInfo (String type, String value) throws RemoteException, WSFoundationException;
    
    /**
     * Returns collection of services which match specified criteria.
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @param bDeep
     * @return
     * @throws RemoteException
     * @throws WSFoundationException
     */
    public WSFoundationCollection getHostGroupsByCriteria(Filter filter, Sort sort, int firstResult, int maxResults, boolean bDeep)  throws RemoteException, WSFoundationException;    
}
