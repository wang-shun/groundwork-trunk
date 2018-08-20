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
package org.groundwork.foundation.bs.monitorserver;

import java.util.List;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import com.groundwork.collage.model.MonitorServer;

/**
 * @author glee
 *
 */
public interface MonitorServerService extends BusinessService
{
	/**
	 * Performs a shallow retrieval (basically Name, IP and Description)
	 * of all the MonitorServers in the system which match the specified criteria.
	 * 
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public FoundationQueryList getMonitorServers(FilterCriteria filterCriteria, 
										SortCriteria sortCriteria, 
										int firstResult, 
										int maxResults) throws BusinessServiceException;
	
	/**
	 * Loads the MonitorServer with the name provided, including a shallow
	 * retrieval of all the Devices monitored by this MonitorServer.
	 * 
	 * @param name
	 */
	public MonitorServer getMonitorServerByName(String name) throws BusinessServiceException;

	/**
	 * Returns list of MonitorServer instances which match specified names.  If a monitor server
	 * does not exist it is just not returned in the list, no exception is thrown.
	 * 
	 * @param monitorServerNames
	 * @param sortCriteria
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<MonitorServer> getMonitorServersByName(String[] monitorServerNames, SortCriteria sortCriteria) throws BusinessServiceException;
	
	/**
	 * Returns the specified monitor server or null if it is not found.
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer getMonitorServerById(int id) throws BusinessServiceException;
	
	/**
	 * Returns list of MonitorServer instances which match specified names.  If a monitor server
	 * does not exist it is just not returned in the list, no exception is thrown.

	 * @param monitorServerIds
	 * @param sortCriteria
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<MonitorServer> getMonitorServersById(int[] monitorServerIds, SortCriteria sortCriteria) throws BusinessServiceException;	
	
	/**
	 * Returns a newly created instance of a monitor server which is NOT persisted.
	 * 
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer createMonitorServer () throws BusinessServiceException;

	/**
	 * Returns a newly created instance of a monitor server which is NOT persisted.
	 * Note:  Since IP is required a default value will be assigned.
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer createMonitorServer (String name) throws BusinessServiceException;	
	
	/**
	 * Returns a newly created instance of a monitor server which is NOT persisted.
	 * @param name
	 * @param IP
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer createMonitorServer (String name, String IP) throws BusinessServiceException;
	
    /**
     * Persists the specified monitor server.
     * 
     * @param monitorServer
     */
    public void saveMonitorServer(MonitorServer monitorServer) throws BusinessServiceException;
    
	/**
	 * Returns a newly created instance of a monitor server and persists it.
	 * @param name
	 * @param IP
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer saveMonitorServer (String name, String IP) throws BusinessServiceException;
	
    /**
     * Persists the specified monitorServers.
     * 
     * @param monitorServers
     */
	public void saveMonitorServers(List<MonitorServer> monitorServers) throws BusinessServiceException;	
	
	/**
	 * Removes specified monitor server
	 * 
	 * @param monitorServer
	 */
	public void deleteMonitorServer (MonitorServer monitorServer);
	
	/**
	 * Removes a MonitorServer from the system and all related entities.
	 * 
	 * @param id
	 * @throws CollageException
	 */
	public void deleteMonitorServerById(int id) throws BusinessServiceException;	
	
	/**
	 * Removes a MonitorServer from the system and all related entities.
	 * 
	 * @param name
	 * @throws CollageException
	 */
	public void deleteMonitorServerByName(String name) throws BusinessServiceException;	
	
	/**
	 * Removes specified monitor servers and all related entities.
	 * 
	 * @param monitorServers
	 * @throws CollageException
	 */
	public void deleteMonitorServers(List<MonitorServer> monitorServers) throws BusinessServiceException;
	
	/**
	 * Removes all monitor servers identifed as well as all their related entities.
	 * 
	 * @param monitorServerList String array of monitor server names to remove
	 * @throws CollageException
	 */
	public void deleteMonitorServersByName(String[] monitorServerNames) throws BusinessServiceException;
	
	/**
	 * Removes all monitor servers identifed as well as all their related entities.
	 * 
	 * @param monitorServerList int array of monitor server ids to remove
	 * @throws CollageException
	 */
	public void deleteMonitorServersById(int[] monitorServerIds) throws BusinessServiceException;	

}
