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

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.impl.NagiosStatisticProperty;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.statistics.StatisticsService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class TestStatisticsService extends AbstractTestCaseWithTransactionSupport
{
	private static final int APP_TYPE_ID_NAGIOS = 100;
	private static final String APP_TYPE_NAGIOS = "NAGIOS";
	
	StatisticsService statisticsService = null;	
	HostGroupService hostGroupService = null;
	HostService hostService = null;
	
	public TestStatisticsService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		suite = new TestSuite();
		
		// Note:  We only run one test b/c we don't want to continually
		// stop and start the statistic calculation thread
		suite.addTest(new TestStatisticsService("testStatistics"));
    
		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve business service
		statisticsService = collage.getStatisticsService();		
		assertNotNull(statisticsService);
		
		statisticsService.startStatisticsCalculation();
		
		// Give ample time to calculate statistics
		try {
			Thread.sleep(11000);
		}
		catch (Exception e)
		{
			log.equals(e);
			return;
		}
		
		hostGroupService = collage.getHostGroupService();
		assertNotNull(hostGroupService);
		
		hostService = collage.getHostService();
		assertNotNull(hostService);		
		
	}
	
	/** executed after each test */
	protected void tearDown() 
	{ 
		super.tearDown();
        statisticsService.stopStatisticsCalculation();
	}
	
	public void testStatistics () throws BusinessServiceException
	{
		// Note:  We run all tests within one test b/c we don't want
		// to start and stop the statistic calculations continually
		testGetAllHostStatistics();
		testGetAllServiceStatistics();
		testGetApplicationStatistics();
		testGetApplicationStatisticTotals();
		testGetHostStatistics();
		testGetHostStatisticTotals();
		testGetServiceStatistics();
		testGetServiceStatisticTotals();
		testGetEventStatistics();
	}

	public void testGetAllHostStatistics() throws BusinessServiceException
	{
		Collection<StateStatistics> col = statisticsService.getAllHostStatistics();
		assertNotNull(col);		
		assertEquals(14, col.size());		
	}

	public void testGetAllServiceStatistics() throws BusinessServiceException
	{
		Collection<StateStatistics> col = statisticsService.getAllServiceStatistics();
		assertNotNull(col);
		assertEquals(14, col.size());
	}

	public void testGetApplicationStatistics() throws BusinessServiceException
	{
		// First get hostgroup id
		HostGroup hg = hostGroupService.getHostGroupByName("All_Infrastructure");
		assertNotNull(hg);
		
		Collection<NagiosStatisticProperty> col = statisticsService.getApplicationStatistics(APP_TYPE_ID_NAGIOS, 
																	hg.getHostGroupId().intValue());
		assertNotNull(col);
		assertEquals(7, col.size());
		
		NagiosStatisticProperty stat = null;
		Iterator<NagiosStatisticProperty> it = col.iterator();
		while (it.hasNext())
		{
			stat = it.next();
			if (stat.getPropertyName().equalsIgnoreCase("Acknowledged"))
			{
				assertEquals(1, stat.getServiceStatisticEnabled());
				assertEquals(2, stat.getServiceStatisticDisabled());
				break;
			}
		}
						
		col = statisticsService.getApplicationStatistics(APP_TYPE_ID_NAGIOS, "All_Infrastructure");
		assertNotNull(col);
		assertEquals(7, col.size());
		
		col = statisticsService.getApplicationStatistics(APP_TYPE_NAGIOS, hg.getHostGroupId().intValue());
		assertNotNull(col);
		assertEquals(7, col.size());

		col = statisticsService.getApplicationStatistics(APP_TYPE_NAGIOS, "All_IT_Services");
		assertNotNull(col);
		
		stat = null;
		it = col.iterator();
		while (it.hasNext())
		{
			stat = it.next();
			if (stat.getPropertyName().equalsIgnoreCase("Acknowledged"))
			{
				assertEquals(0, stat.getServiceStatisticEnabled());
				assertEquals(2, stat.getServiceStatisticDisabled());
				break;
			}
		}		
	}

	public void testGetApplicationStatisticTotals() throws BusinessServiceException
	{
		///////////////////////////////////////////////////////////////////////
		// getApplicationStatisticTotals (App ID)
		///////////////////////////////////////////////////////////////////////
		Collection<NagiosStatisticProperty> col = statisticsService.getApplicationStatisticTotals(APP_TYPE_ID_NAGIOS);
		assertNotNull(col);						
		
		NagiosStatisticProperty stat = null;
		Iterator<NagiosStatisticProperty> it = col.iterator();
		while (it.hasNext())
		{
			stat = it.next();
			if (stat.getPropertyName().equalsIgnoreCase("Acknowledged"))
			{
				assertEquals(9, stat.getServiceStatisticEnabled());
				assertEquals(8, stat.getServiceStatisticDisabled());
				break;
			}
		}	
		
		///////////////////////////////////////////////////////////////////////
		// getApplicationStatisticTotals (App Name)
		///////////////////////////////////////////////////////////////////////
		col = statisticsService.getApplicationStatisticTotals(APP_TYPE_NAGIOS);
		assertNotNull(col);		
		
		it = col.iterator();
		while (it.hasNext())
		{
			stat = it.next();
			if (stat.getPropertyName().equalsIgnoreCase("Acknowledged"))
			{
				assertEquals(9, stat.getServiceStatisticEnabled());
				assertEquals(8, stat.getServiceStatisticDisabled());
				break;
			}
		}				
	}

	public void testGetHostStatistics() throws BusinessServiceException
	{
		// First get hostgroup ids
		HostGroup hg = hostGroupService.getHostGroupByName("All_Sites");
		assertNotNull(hg);
		
		HostGroup hg2 = hostGroupService.getHostGroupByName("Email");
		assertNotNull(hg2);
		
		///////////////////////////////////////////////////////////////////////
		// getHostStatisticsByHostGroupId (host group ID)
		///////////////////////////////////////////////////////////////////////
		StateStatistics stat = statisticsService.getHostStatisticsByHostGroupId(hg.getHostGroupId().intValue());
		assertNotNull(stat);
		assertEquals(1, stat.getTotalHosts());
		assertEquals(1, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UP", 1));
				
		Collection<Integer> hostGroupIds = new ArrayList<Integer>(2);
		hostGroupIds.add(hg.getHostGroupId());
		hostGroupIds.add(hg2.getHostGroupId());
		
		///////////////////////////////////////////////////////////////////////
		// getHostStatisticsByHostGroupIds (host group IDs)
		///////////////////////////////////////////////////////////////////////
		Collection<StateStatistics> col = statisticsService.getHostStatisticsByHostGroupIds(hostGroupIds);
		assertNotNull(col);
		assertEquals(2, col.size());
				
		// Get first entry in collection
		stat = col.iterator().next();
		assertEquals(1, stat.getTotalHosts());
		assertEquals(1, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UP", 1));
									
		///////////////////////////////////////////////////////////////////////
		// getHostStatisticsByHostGroupName (host group name)
		///////////////////////////////////////////////////////////////////////		
		stat = statisticsService.getHostStatisticsByHostGroupName("Email_New_York");
		assertNotNull(stat);
		assertEquals(1, stat.getTotalHosts());
		assertEquals(1, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UP", 1));
		
		Collection<String> hostGroupNames = new ArrayList<String>(2);
		hostGroupNames.add(hg.getName());
		hostGroupNames.add(hg2.getName());
		
		///////////////////////////////////////////////////////////////////////
		// getHostStatisticsByHostGroupNames (host group names)
		///////////////////////////////////////////////////////////////////////			
		col = statisticsService.getHostStatisticsByHostGroupNames(hostGroupNames);
		assertNotNull(col);
		assertEquals(2, col.size());
		
		// Get second entry in collection
		Iterator<StateStatistics> itStatistics = col.iterator();
		itStatistics.next(); stat = itStatistics.next();
		assertEquals(4, stat.getTotalHosts());
		assertEquals(4, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UP", 4));
	}
	
	public void testGetHostStatisticTotals() throws BusinessServiceException
	{
		StateStatistics stat = statisticsService.getHostStatisticTotals();
		assertNotNull(stat);	

		assertEquals(16, stat.getTotalHosts());
		assertEquals(17, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UP", 16));
	}

	public void testGetServiceStatistics() throws BusinessServiceException
	{
		// First get host id
		Host host = hostService.getHostByHostName("nagios");
		assertNotNull(host);
		
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticByHostId (host id)
		///////////////////////////////////////////////////////////////////////				
		StateStatistics stateStatistics = statisticsService.getServiceStatisticByHostId(host.getHostId().intValue());
		assertNotNull(stateStatistics);
		assertEquals(1, stateStatistics.getTotalHosts());
		assertEquals(4, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 3));
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "WARNING", 1));	
			
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticByHostName (host name)
		///////////////////////////////////////////////////////////////////////				
		stateStatistics = statisticsService.getServiceStatisticByHostName("exchange");
		assertNotNull(stateStatistics);
		assertEquals(1, stateStatistics.getTotalHosts());
		assertEquals(1, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 1));
	
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticsByHostGroupId (host group id)
		///////////////////////////////////////////////////////////////////////		
		
		// First get hostgroup ids
		HostGroup hg = hostGroupService.getHostGroupByName("All_Sites");
		assertNotNull(hg);
		
		HostGroup hg2 = hostGroupService.getHostGroupByName("Email");
		assertNotNull(hg2);
				
		stateStatistics = statisticsService.getServiceStatisticsByHostGroupId(hg.getHostGroupId().intValue());
		assertNotNull(stateStatistics);
		assertEquals(1, stateStatistics.getTotalHosts());
		assertEquals(1, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 1));
		
		Collection<Integer> hostGroupIds = new ArrayList<Integer>(2);
		hostGroupIds.add(hg.getHostGroupId());
		hostGroupIds.add(hg2.getHostGroupId());
		
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticsByHostGroupIds (host group ids)
		///////////////////////////////////////////////////////////////////////				
		Collection<StateStatistics> col = statisticsService.getServiceStatisticsByHostGroupIds(hostGroupIds);
		assertNotNull(col);
		assertEquals(2, col.size());
		
		// Get Second in collection
		Iterator<StateStatistics> itStatistics = col.iterator();
		itStatistics.next();
		stateStatistics = itStatistics.next();	
		
		assertNotNull(stateStatistics);
		assertEquals(4, stateStatistics.getTotalHosts());
		assertEquals(4, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 2));
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "UNKNOWN", 1));
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "WARNING", 0));	
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "CRITICAL", 1));		
		
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticsByHostGroupName (host group name)
		///////////////////////////////////////////////////////////////////////				
		stateStatistics = statisticsService.getServiceStatisticsByHostGroupName("Email_New_York");	
		assertNotNull(stateStatistics);
		assertEquals(1, stateStatistics.getTotalHosts());
		assertEquals(1, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 1));
		
		///////////////////////////////////////////////////////////////////////
		// getServiceStatisticsByHostGroupNames (host group names)
		///////////////////////////////////////////////////////////////////////				
		Collection<String> hostGroupNames = new ArrayList<String>(2);
		hostGroupNames.add(hg.getName());
		hostGroupNames.add(hg2.getName());
		
		col = statisticsService.getServiceStatisticsByHostGroupNames(hostGroupNames);
		assertNotNull(col);
		assertEquals(2, col.size());
		
		// Get Second in collection
		itStatistics = col.iterator();
		itStatistics.next();
		stateStatistics = itStatistics.next();	
		
		assertNotNull(stateStatistics);
		assertEquals(4, stateStatistics.getTotalHosts());
		assertEquals(4, stateStatistics.getTotalServices());
		assertNotNull(stateStatistics.getStatisticProperties());
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "OK", 2));
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "UNKNOWN", 1));
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "WARNING", 0));	
		assertTrue(assertStatisticProperty(stateStatistics.getStatisticProperties(), "CRITICAL", 1));				
	}

	public void testGetServiceStatisticTotals() throws BusinessServiceException
	{
		StateStatistics stat = statisticsService.getServiceStatisticTotals();
		assertNotNull(stat);
		assertEquals(16, stat.getTotalHosts());
		assertEquals(17, stat.getTotalServices());
		assertNotNull(stat.getStatisticProperties());
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "OK", 13));
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "UNKNOWN", 2));
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "WARNING", 1));	
		assertTrue(assertStatisticProperty(stat.getStatisticProperties(), "CRITICAL", 1));
	}
	
	public void testGetEventStatistics () throws BusinessServiceException
	{
		Collection<StatisticProperty> col = 
			statisticsService.getEventStatisticsByHostGroupName(null, "demo-system", null, null, "MonitorStatus");
		assertNotNull(col);
		assertEquals(26, col.size());
		assertTrue(assertStatisticProperty(col, "UP", 1));
		assertTrue(assertStatisticProperty(col, "OK", 5));
		assertTrue(assertStatisticProperty(col, "UNKNOWN", 4));	
		assertTrue(assertStatisticProperty(col, "PENDING", 1));		
				
		col = statisticsService.getEventStatisticsByHostName(null, "nagios", null, null, "OperationStatus");
		assertNotNull(col);
		assertEquals(5, col.size());
		assertTrue(assertStatisticProperty(col, "ACCEPTED", 4));
		assertTrue(assertStatisticProperty(col, "NOTIFIED", 4));			
	}
	
	private boolean assertStatisticProperty (Collection<StatisticProperty> properties, String propertyName, long count)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");
		
		if (properties == null)
			return false;
		
		StatisticProperty statProperty = null;
		Iterator<StatisticProperty> it = properties.iterator();
		while (it.hasNext())
		{
			statProperty = it.next();
			if (statProperty.getName().equalsIgnoreCase(propertyName))
			{
				return (count == statProperty.getCount());
			}
		}			
		
		// Property not found
		return false;
	}	
}
