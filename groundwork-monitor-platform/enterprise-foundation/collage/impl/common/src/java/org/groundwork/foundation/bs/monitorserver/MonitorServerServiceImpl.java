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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.device.DeviceServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import com.groundwork.collage.model.MonitorServer;

public class MonitorServerServiceImpl extends EntityBusinessServiceImpl	implements MonitorServerService
{
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = 
		SortCriteria.asc(MonitorServer.HP_NAME);
	
	/** Used to create a temporary placeholder for a MonitorServer's IP address based 
	    on the MonitorServer name */
	private static final String IP_PREFIX = "IP_";
	
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(DeviceServiceImpl.class);
	
	public MonitorServerServiceImpl (FoundationDAO foundationDAO)
	{		
		super(foundationDAO, MonitorServer.INTERFACE_NAME, MonitorServer.COMPONENT_NAME);
	}	

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#createMonitorServer()
	 */
	public MonitorServer createMonitorServer() throws BusinessServiceException
	{
		return (MonitorServer)create();
	}
	
	/**
	 * Returns a newly created instance of a monitor server which is NOT persisted.
	 * Note:  Since IP is required a default value will be assigned.
	 * @param name
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer createMonitorServer (String name) throws BusinessServiceException
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server name parameter.");

		return createMonitorServer(name, IP_PREFIX + name);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#createMonitorServer(java.lang.String, java.lang.String)
	 */
	public MonitorServer createMonitorServer(String name, String ip)
			throws BusinessServiceException
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server name parameter.");

		if (ip == null || ip.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy ip parameter.");

		MonitorServer monitorServer = (MonitorServer)create();
		monitorServer.setMonitorServerName(name);
		monitorServer.setIp(ip);
		
		// NOTE:  Not persisted, yet
		return monitorServer;
	}

	/**
	 * Delete specified monitor server
	 * 
	 * @param monitorServer
	 */
	public void deleteMonitorServer (MonitorServer monitorServer)
	{
		if (monitorServer == null)
			throw new IllegalArgumentException("Invalid null MonitorServer parameter.");
		
		delete(monitorServer);
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#deleteMonitorServerById(int)
	 */
	public void deleteMonitorServerById(int id) throws BusinessServiceException
	{
		delete(id);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#deleteMonitorServerByName(java.lang.String)
	 */
	public void deleteMonitorServerByName(String name)
			throws BusinessServiceException
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server name parameter.");

		MonitorServer monitorServer = getMonitorServerByName(name);
		if (monitorServer == null)
		{
			if (log.isWarnEnabled() == true)
				log.warn("Unable to delete monitor server.  Monitor server not found - " + name);
			
			return;
		}
		
		delete(monitorServer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#deleteMonitorServers(java.util.List)
	 */
	public void deleteMonitorServers(List<MonitorServer> monitorServers)
			throws BusinessServiceException
	{
		if (monitorServers == null || monitorServers.size() == 0)
		{
			if (log.isWarnEnabled() == true)
				log.warn("Null / Empty monitorServers list parameter.");
			return;
		}
		
		delete(monitorServers);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#deleteMonitorServersByName(java.lang.String[])
	 */
	public void deleteMonitorServersByName(String[] monitorServerNames)
			throws BusinessServiceException
	{
		if (monitorServerNames == null || monitorServerNames.length == 0)
		{
			if (log.isWarnEnabled() == true)
				log.warn("Null / Empty monitorServerNames parameter.");
			return;
		}
		
		// Delete list of monitor servers retrieved by name
		delete(getMonitorServersByName(monitorServerNames, null));
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#deleteMonitorServersById(int[])
	 */
	public void deleteMonitorServersById(int[] monitorServerIds)
			throws BusinessServiceException
	{
		delete(monitorServerIds);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#getMonitorServerByName(java.lang.String)
	 */
	public MonitorServer getMonitorServerByName(String name)
			throws BusinessServiceException
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server name parameter.");

		FilterCriteria filterCriteria = FilterCriteria.eq(MonitorServer.HP_NAME, name);
		
		List results = query(filterCriteria, null);
		
		if (results == null || results.size() == 0)
			return null;
		
		return (MonitorServer)results.get(0);
	}
	
	/**
	 * Returns list of MonitorServer instances which match specified names.  If a monitor server
	 * does not exist it is just not returned in the list, no exception is thrown.
	 * 
	 * @param monitorServerNames
	 * @param sortCriteria
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<MonitorServer> getMonitorServersByName(String[] monitorServerNames, SortCriteria sortCriteria) throws BusinessServiceException
	{
		if (monitorServerNames == null || monitorServerNames.length == 0)
			throw new IllegalArgumentException("Invalid null / empty monitor server name array parameter.");
		
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		// Build filter criteria
		int numNames = monitorServerNames.length;
		String name;
		FilterCriteria filterCriteria = null;
		
		for (int i = 0; i < numNames; i++)
		{
			name = monitorServerNames[i];
			
			if (filterCriteria == null)
				filterCriteria = FilterCriteria.eq(MonitorServer.HP_NAME, name);
			else
				filterCriteria.or(FilterCriteria.eq(MonitorServer.HP_NAME, name));
		}
		
		return (List<MonitorServer>)query(filterCriteria, sortCriteria);
	}
	
	/**
	 * Returns the specified monitor server or null if it is not found.
	 * 
	 * @param id
	 * @return
	 * @throws BusinessServiceException
	 */
	public MonitorServer getMonitorServerById(int id) throws BusinessServiceException
	{
		return (MonitorServer)queryById(id);
	}
	
	/**
	 * Returns list of MonitorServer instances which match specified names.  If a monitor server
	 * does not exist it is just not returned in the list, no exception is thrown.
	 * 
	 * @param monitorServerNames
	 * @param sortCriteria
	 * @return
	 * @throws BusinessServiceException
	 */
	public List<MonitorServer> getMonitorServersById(int[] monitorServerIds, SortCriteria sortCriteria) throws BusinessServiceException
	{
		if (monitorServerIds == null || monitorServerIds.length == 0)
			throw new IllegalArgumentException("Invalid null / empty monitor server id array parameter.");

		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		return (List<MonitorServer>)queryById(monitorServerIds, sortCriteria);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#getMonitorServers(org.groundwork.foundation.dao.FilterCriteria, org.groundwork.foundation.dao.SortCriteria, int, int)
	 */
	public FoundationQueryList getMonitorServers(FilterCriteria filterCriteria,
			SortCriteria sortCriteria, int firstResult, int maxResults)
			throws BusinessServiceException
	{
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		return query(filterCriteria, sortCriteria, firstResult, maxResults);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#saveMonitorServer(com.groundwork.collage.model.MonitorServer)
	 */
	public void saveMonitorServer(MonitorServer monitorServer)
			throws BusinessServiceException
	{
		if (monitorServer == null)
			throw new IllegalArgumentException("Invalid null MonitorServer parameter.");

		save(monitorServer);
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#saveMonitorServer(java.lang.String, java.lang.String)
	 */
	public MonitorServer saveMonitorServer(String name, String ip)
			throws BusinessServiceException
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server name parameter.");

		if (ip == null || ip.length() == 0)
			throw new IllegalArgumentException("Invalid null / emtpy monitor server ip parameter.");

		// See if monitor server exits
		MonitorServer server = getMonitorServerByName(name);
		if (server == null)
			server = createMonitorServer(name, ip);
		else
			server.setIp(ip);
		
		save(server);
		
		return server;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.monitorserver.MonitorServerService#saveMonitorServers(java.util.List)
	 */
	public void saveMonitorServers(List<MonitorServer> monitorServers)
			throws BusinessServiceException
	{
		save(monitorServers);
	}
}
