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

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.LogMessage;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.MatchType;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class TestFoundationDAO extends AbstractTestCaseWithTransactionSupport
{
	/* Hibernate Entity Names */
	private static final String HIBERNATE_DEVICE = "com.groundwork.collage.model.impl.Device";
	private static final String HIBERNATE_LOG_MSG = "com.groundwork.collage.model.impl.LogMessage";
	private static final String HIBERNATE_HOST_STATUS = "com.groundwork.collage.model.impl.HostStatus";
	private static final String HIBERNATE_SERVICE_STATUS = "com.groundwork.collage.model.impl.ServiceStatus";

	
	private FoundationDAO foundationDAO = null;
		
	public TestFoundationDAO(String x) { super(x); }

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");
		
		// run all tests
		suite = new TestSuite(TestFoundationDAO.class);

		// or a subset thereoff
		//suite.addTest(new TestFoundationDAO("testSave"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		foundationDAO = (FoundationDAO)collage.getAPIObject(CollageFactory.FOUNDATION_DAO);
		assertNotNull(foundationDAO);
	}
	
	public void testSave()
	{
		final String newDeviceName = "FoundationDAODeviceTest";
		
		// Query All Devices
		List allDeviceList = foundationDAO.query(HIBERNATE_DEVICE, (FilterCriteria) null, (SortCriteria) null);
		assertNotNull("retrieving all devices", allDeviceList);
		assertEquals("retrieving all devices", 5, allDeviceList.size());
		
		// Change first devices display name
		Device device = (Device)allDeviceList.get(0);
		device.setDisplayName(newDeviceName);		
		foundationDAO.save(device);  		// Persist the change
		
		// Query device by display name
		FilterCriteria filterCriteria = FilterCriteria.eq("displayName", newDeviceName);
		List list = foundationDAO.query(HIBERNATE_DEVICE, filterCriteria, (SortCriteria) null);
		assertNotNull("retrieving device by display name", list);
		assertEquals("retrieving device by display name", 1, list.size());
		
		// Change Second and Third Device Display Names
		device = (Device)allDeviceList.get(1);
		device.setDisplayName(newDeviceName);	

		device = (Device)allDeviceList.get(2);
		device.setDisplayName(newDeviceName);	
		
		// Persist Changes
		foundationDAO.save(allDeviceList);
		
		// Query device by display name - there should be three devices with the same name
		list = foundationDAO.query(HIBERNATE_DEVICE, filterCriteria, (SortCriteria) null);
		assertNotNull("retrieving device by display name", list);
		assertEquals("retrieving device by display name", 3, list.size());		
	}
	
	public void testDelete ()
	{		
		beginTransaction();
				
		// Delete device by id
		foundationDAO.delete(HIBERNATE_DEVICE, 1);			
		
		// Query All Devices - There should be 4 left after the delete
		List afterDeleteList = foundationDAO.query(HIBERNATE_DEVICE, (FilterCriteria) null, (SortCriteria) null);
		assertNotNull("retrieving all devices", afterDeleteList);
		assertEquals("retrieving all devices", 4, afterDeleteList.size());
		
		// Delete next remaining devices				
		foundationDAO.delete(afterDeleteList);
		
		// Make sure there are no devices left
		List remainingList = foundationDAO.query(HIBERNATE_DEVICE, (FilterCriteria) null, (SortCriteria) null);
		assertNotNull("retrieving all devices", remainingList);
		assertEquals("retrieving all devices", 0, remainingList.size());
		
		// Add the original devices back
		rollbackTransaction();		
	}
	
	public void testQueryById ()
	{		
		// Get Device By Id		
		Device device = (Device)foundationDAO.queryById(HIBERNATE_DEVICE,1);
		assertNotNull("device id=1", device);
		
		// Get Device Ids 2 and 3
		Collection<Integer> idCol = new ArrayList<Integer>(2);		
		idCol.add(new Integer(2));
		idCol.add(new Integer(3));
		
		List deviceList = foundationDAO.queryById(HIBERNATE_DEVICE, idCol, null);
		assertNotNull("retrieving 2 devices", deviceList);
		assertEquals("retrieving 2 devices", 2, deviceList.size());
	}
	
	public void testQuery ()
	{
		//NOTE:  The log message device id field is not mapped as a property of LogMessage therefore we have to go through device
		FilterCriteria filterCriteria = FilterCriteria.eq("device.deviceId", 2);		
		SortCriteria sortCriteria = SortCriteria.asc("lastInsertDate");
				
		List list =  foundationDAO.query(HIBERNATE_LOG_MSG, filterCriteria, sortCriteria);
		assertNotNull("retrieving log messages", list);
		assertEquals("retrieving 3 log messages", 3, list.size());
		
		// Make sure the log messages are sorted properly
		LogMessage msg = (LogMessage)list.get(0);
		LogMessage msg2 = (LogMessage)list.get(1);
		LogMessage msg3 = (LogMessage)list.get(2);
		
		assertTrue(msg.getLastInsertDate().before(msg2.getLastInsertDate()));
		assertTrue(msg2.getLastInsertDate().before(msg3.getLastInsertDate()));

		// Note:  First result is zero based, so 3 will return the fourth row
		FoundationQueryList results =  foundationDAO.query(HIBERNATE_LOG_MSG, null, null, null, 3, 5);
		assertNotNull("retrieving 5 log messages", results);
		assertEquals("retrieving 5 messages starting with third message", 5, results.size());
		
		msg = (LogMessage)results.get(0);		
		//assertEquals(msg.getTextMessage(), "message_4");
	}
	
	public void testQueryCount ()
	{
		int count = foundationDAO.queryCount(HIBERNATE_DEVICE, null);
		assertEquals("retrieving 5 devices", 5, count);
		
		FilterCriteria filterCriteria = FilterCriteria.ilike("displayName", "ApP", MatchType.START);
		
		count = foundationDAO.queryCount(HIBERNATE_DEVICE, filterCriteria);
		assertEquals("retrieving 2 devices which start with APP ", 2, count);		
	}
	
	public void testQueryByDynamicProperty ()
	{
		// Query Log Messages by dynamic properties
		FilterCriteria filterCriteria = FilterCriteria.eq("propertyValues.name", "ApplicationName");		
		filterCriteria.and(FilterCriteria.eq("propertyValues.valueString", "App Name1"));		
				
		List results =
			foundationDAO.query(HIBERNATE_LOG_MSG, filterCriteria, (SortCriteria) null);
		
		assertNotNull("retrieving log messages by dynamic property", results);
		assertEquals("retrieving 1 log messages by dynamic property", 1, results.size());	
		
		// Query Devices by dynamic properties
		filterCriteria = FilterCriteria.eq("propertyValues.name", "Location");		
		filterCriteria.and(FilterCriteria.eq("propertyValues.valueString", "Bay Area"));		
		
		FilterCriteria criteria2 =  FilterCriteria.eq("propertyValues.name", "ContactNumber");		
		criteria2.and(FilterCriteria.eq("propertyValues.valueString", "510.899.7700"));		
		
		filterCriteria.or(criteria2);
				
		results = foundationDAO.query(HIBERNATE_DEVICE, filterCriteria, (SortCriteria) null);
		
		assertNotNull("retrieving devices by dynamic property", results);
		assertEquals("retrieving 2 devices by dynamic property", 1, results.size());		
		
		// Query HostStatus by dynamic properties
		Calendar cal = Calendar.getInstance();
		cal.set(2006, 10, 21, 0, 0, 0); // November 21, 2006
		
		filterCriteria = FilterCriteria.eq("propertyValues.name", "LastStateChange");		
		filterCriteria.and(FilterCriteria.lt("propertyValues.valueDate", cal.getTime()));		
				
		results = foundationDAO.query(HIBERNATE_HOST_STATUS, filterCriteria, (SortCriteria) null);
		
		assertNotNull("retrieving host status by dynamic property", results);
		assertEquals("retrieving 3 host status by dynamic property", 5, results.size());	
		
		// Query ServiceStatus by dynamic properties
		filterCriteria = FilterCriteria.eq("propertyValues.name", "30DayMovingAvg");		
		filterCriteria.and(FilterCriteria.gt("propertyValues.valueDouble", 98.0));
		
		SortCriteria sortCriteria = SortCriteria.asc("propertyValues.valueDouble");		
		
		results = foundationDAO.query(HIBERNATE_SERVICE_STATUS, filterCriteria, sortCriteria);
		
		assertNotNull("retrieving service status by dynamic property", results);
		assertEquals("retrieving 11 service status by dynamic property", 11, results.size());					
	}	
}
