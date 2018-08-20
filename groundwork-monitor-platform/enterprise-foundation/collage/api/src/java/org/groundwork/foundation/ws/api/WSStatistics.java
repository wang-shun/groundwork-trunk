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

import org.groundwork.foundation.ws.model.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

public interface WSStatistics extends java.rmi.Remote
{
    /**
     * 
     * @param type - type of query to do - from the types in <@link StatisticQueryType> definition
     * @param value
     * @param applicationType
     * @return Collection
     * @throws WSFoundationException
     */
    public WSFoundationCollection getStatistics(StatisticQueryType type, String value, String applicationType) throws WSFoundationException, RemoteException;

    /**
     * 
     * @param type - type of query to do - from the types in <@link StatisticQueryType> definition
     * @param filter - FIlter
     * @param applicationType
     * @return Collection
     * @throws WSFoundationException
     */
    public WSFoundationCollection getGroupStatistics(StatisticQueryType type, Filter filter, String groupName, String applicationType) throws WSFoundationException, RemoteException;
    
    
    /**
     * Overloaded method allowing String parameters specifically used by custom reporting
     * data source.
     * 
     * When a type of ALL is specified then instead of returning a total for all host groups
     * individual totals for each host group is returned.
     * 
     * @param type
     * @param value
     * @param applicationType
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getStatisticsByString(String type, String value, String applicationType) throws WSFoundationException, RemoteException;
    
    /**
     * 
     * @param queryType - type of query to do - from the types in <@link NagiosStatisticQueryType> definition
     * @param value
     * @return Collection 
     * @throws WSFoundationException
     */
    public WSFoundationCollection getNagiosStatistics(NagiosStatisticQueryType queryType, String value)
    throws WSFoundationException, RemoteException;

    /**
     * Overloaded method allowing String parameters specifically used by custom reporting
     * data source.
     * 
     * @param type
     * @param value
     * @param applicationType
     * @return
     * @throws WSFoundationException
     * @throws RemoteException
     */
    public WSFoundationCollection getNagiosStatisticsByString(String type, String value) throws WSFoundationException, RemoteException;
    /**
     * Method to be invoked to return a statistic: percentage of Hosts with status UP
     * @param hostGroupName
     * @return
     * @throws java.rmi.RemoteException
     * @throws WSFoundationException
     */
    public double getHostAvailabilityForHostgroup(java.lang.String hostGroupName) throws java.rmi.RemoteException, WSFoundationException;
    
    /**
     * Method to be invoked to return a statistic: percentage of Services with status OK
     * @param hostGroupName
     * @return
     * @throws java.rmi.RemoteException
     * @throws WSFoundationException
     */
    public double getServiceAvailabilityForHostgroup(java.lang.String hostGroupName) throws java.rmi.RemoteException, WSFoundationException;
    
    /**
     * Method to be invoked to return a statistic: percentage of Services with status OK
     * @param hostGroupName
     * @return
     * @throws java.rmi.RemoteException
     * @throws WSFoundationException
     */
    public double getServiceAvailabilityForServiceGroup(java.lang.String serviceGroupName) throws java.rmi.RemoteException, WSFoundationException;
}
