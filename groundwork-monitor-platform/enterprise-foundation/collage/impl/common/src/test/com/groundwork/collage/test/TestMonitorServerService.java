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
package com.groundwork.collage.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.MatchType;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.MonitorServer;

public class TestMonitorServerService extends AbstractTestCaseWithTransactionSupport
{
	private static final String NEW_MONITOR_SERVER_NAME = "NewMonitorServer";
	private static final String NEW_MONITOR_SERVER_IP = "111.111.111.111";
	private static final String NEW_MONITOR_SERVER_DESCRIPTION = "NewMonitorServer Description";
	private static final String NEW_MONITOR_SERVER_NAME2 = "NewMonitorServer2";
	private static final String NEW_MONITOR_SERVER_IP2 = "222.222.222.222";
	
	private MonitorServerService monitorService = null;
	
	public TestMonitorServerService(String x) {
		super(x);
	}
	
	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		suite = new TestSuite(TestMonitorServerService.class);

		// or a subset thereoff
		//suite.addTest(new TestMonitorServerService(""));
    
		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve business service
		monitorService = collage.getMonitorServerService();		
		assertNotNull(monitorService);
	}
	
	public void testCreateMonitorServer() throws BusinessServiceException
	{
		// Create monitor server
		MonitorServer monitorServer = monitorService.createMonitorServer();
		assertNotNull(monitorServer);
		
		monitorServer.setMonitorServerName(NEW_MONITOR_SERVER_NAME);
		monitorServer.setIp(NEW_MONITOR_SERVER_IP);
		monitorServer.setDescription(NEW_MONITOR_SERVER_DESCRIPTION);
		
		// Save monitor server
		monitorService.saveMonitorServer(monitorServer);
		
		// Get newly created monitor server
		monitorServer = monitorService.getMonitorServerByName(NEW_MONITOR_SERVER_NAME);
		assertNotNull(monitorServer);
		assertEquals(NEW_MONITOR_SERVER_IP, monitorServer.getIp());
		
		// Delete Monitor Server
		monitorService.deleteMonitorServer(monitorServer);
		
		// Make sure monitor server has been deleted
		monitorServer = monitorService.getMonitorServerByName(NEW_MONITOR_SERVER_NAME);
		assertNull(monitorServer);
		
		///////////////////////////////////////////////////////////////////////
		// Create Monitor Server (String name, String IP)
		///////////////////////////////////////////////////////////////////////
		
		monitorServer = monitorService.createMonitorServer(NEW_MONITOR_SERVER_NAME, NEW_MONITOR_SERVER_IP);
		assertNotNull(monitorServer);
				
		// Save monitor server
		monitorService.saveMonitorServer(monitorServer);
		
		// Get newly created monitor server
		monitorServer = monitorService.getMonitorServerById(monitorServer.getMonitorServerId().intValue());
		assertNotNull(monitorServer);
		assertEquals(NEW_MONITOR_SERVER_NAME, monitorServer.getMonitorServerName());
		
		// Delete monitor server by id
		int monitorServerId = monitorServer.getMonitorServerId().intValue();
		monitorService.deleteMonitorServerById(monitorServerId);
		
		// Make sure monitor server has been deleted
		monitorServer = monitorService.getMonitorServerById(monitorServerId);
		assertNull(monitorServer);	
		
		///////////////////////////////////////////////////////////////////////
		// Save Monitor Server (String name, String IP)
		///////////////////////////////////////////////////////////////////////
		
		monitorServer = monitorService.saveMonitorServer(NEW_MONITOR_SERVER_NAME, NEW_MONITOR_SERVER_IP);
		assertNotNull(monitorServer);
		
		// Get newly created monitor server
		int[] monitorIds = new int[] {monitorServer.getMonitorServerId().intValue()};
		List<MonitorServer> list = monitorService.getMonitorServersById(monitorIds, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals(NEW_MONITOR_SERVER_NAME, ((MonitorServer)list.get(0)).getMonitorServerName());
		
		// Delete monitor server by name
		monitorService.deleteMonitorServerByName(NEW_MONITOR_SERVER_NAME);
		
		// Make sure monitor server has been deleted
		monitorServer = monitorService.getMonitorServerByName(NEW_MONITOR_SERVER_NAME);
		assertNull(monitorServer);		
	}

	public void testCreateMultipleMonitorServers() throws BusinessServiceException
	{
		MonitorServer monitorServer = monitorService.createMonitorServer(NEW_MONITOR_SERVER_NAME);
		assertNotNull(monitorServer);
		
		MonitorServer monitorServer2 = monitorService.createMonitorServer(
														NEW_MONITOR_SERVER_NAME2, 
														NEW_MONITOR_SERVER_IP2);
		assertNotNull(monitorServer2);
		
		List<MonitorServer> monitorServers = new ArrayList<MonitorServer>(2);
		monitorServers.add(monitorServer);
		monitorServers.add(monitorServer2);
		
		// Persist changes
		monitorService.saveMonitorServers(monitorServers);
				
		// Get Monitor Servers By Name
		monitorServers.clear();
		
		String[] monitorServerNames = new String[] {NEW_MONITOR_SERVER_NAME, NEW_MONITOR_SERVER_NAME2};
		monitorServers = monitorService.getMonitorServersByName(monitorServerNames, null);
		assertNotNull(monitorServers);
		assertEquals(2, monitorServers.size());
		
		// Delete Monitor Servers
		monitorService.deleteMonitorServersByName(monitorServerNames);
		
		///////////////////////////////////////////////////////////////////////
		// Delete multiple monitor servers
		///////////////////////////////////////////////////////////////////////
		
		monitorServer = monitorService.createMonitorServer(NEW_MONITOR_SERVER_NAME);
		assertNotNull(monitorServer);
		
		monitorServer2 = monitorService.createMonitorServer(
														NEW_MONITOR_SERVER_NAME2, 
														NEW_MONITOR_SERVER_IP2);
		assertNotNull(monitorServer2);
		
		monitorServers = new ArrayList<MonitorServer>(2);
		monitorServers.add(monitorServer);
		monitorServers.add(monitorServer2);
		
		// Persist changes
		monitorService.saveMonitorServers(monitorServers);
				
		// Get Monitor Servers By Name
		int[] monitorIds = new int[] {monitorServer.getMonitorServerId().intValue(),
									  monitorServer2.getMonitorServerId().intValue()};
		monitorServers.clear();
		
		monitorServers = monitorService.getMonitorServersById(monitorIds, null);
		assertNotNull(monitorServers);
		assertEquals(2, monitorServers.size());
		
		// Delete Monitor Servers
		monitorService.deleteMonitorServersById(monitorIds);	
		
		// Make sure they have been deleted
		monitorServers = monitorService.getMonitorServersById(monitorIds, null);
		assertNotNull(monitorServers); // Empty Collection
		assertEquals(0, monitorServers.size());
	}

	public void testGetMonitorServers() throws BusinessServiceException
	{
		FilterCriteria filterCriteria = FilterCriteria.ilike(
											MonitorServer.HP_NAME, 
											"groundwork",
											MatchType.START);
		
		FoundationQueryList results = monitorService.getMonitorServers(filterCriteria, null, 0, 2);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(3, results.getTotalCount());		
	}
}
