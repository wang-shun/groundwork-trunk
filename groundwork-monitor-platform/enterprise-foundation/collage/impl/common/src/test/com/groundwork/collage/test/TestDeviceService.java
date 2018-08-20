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

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.PropertyType;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.MatchType;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author glee
 *
 */
public class TestDeviceService extends AbstractTestCaseWithTransactionSupport
{
	public static final int    TOTAL_DEVICES    = 5;

	public static final int    DEVICE1_ID = 1;
	public static final String DEVICE1_IDENTIFICATION       = "192.168.1.100";
	public static final String DEVICE1_NAME     = "groundwork";
	public static final String DEVICE1_DESCR    = "Nagios Server";
	public static final String DEVICE1_LOCATION = "Bay Area";
	public static final String DEVICE1_CONTACT  = "Roger Ruttimann";
	public static final String DEVICE1_PHONE    = "510.899.7700";
	public static final int    DEVICE1_NUM_PARENTS  = 4;
	public static final int    DEVICE1_NUM_CHILDREN = 0;
	public static final List<String> PARENTS_TO_DETACH    = new ArrayList<String>(4);
	public static final List<String> PARENTS_TO_ATTACH    = new ArrayList<String>(4);
	public static final int    NUM_PARENTS_DELETED  = 3;

	public static final String DEVICE2_IDENTIFICATION      = "192.168.1.101";
	public static final String DEVICE2_NAME    = "ex-svr-1";
	public static final String DEVICE2_DESCR   = "Exchange Server";
	public static final int    DEVICE2_NUM_PARENTS  = 1;
	public static final int    DEVICE2_NUM_CHILDREN = 1;

	public static final String DEVICE3_IDENTIFICATION      = "192.168.1.102";
	public static final String DEVICE3_NAME    = "mysql-svr-1";
	public static final String DEVICE3_DESCR   = "Database backend";
	public static final int    DEVICE3_NUM_PARENTS  = 0;
	public static final int    DEVICE3_NUM_CHILDREN = 4;
	
	public static final String DEVICE4_IDENTIFICATION      = "192.168.1.103";
	public static final String DEVICE5_IDENTIFICATION      = "192.168.1.104";
	
	public static final List<String> CHILDREN_TO_DETACH   = new ArrayList<String>(4);
	public static final List<String> CHILDREN_TO_ATTACH   = new ArrayList<String>(3);
	public static final int    NUM_CHILDREN_DELETED = 4;
	
	public static final String NEW_DEVICE_IDENTIFICATION = "NewDevice";
	public static final String NEW_DEVICE_NAME = "New Device Display Name";
	public static final String NEW_DEVICE_IDENTIFICATION2 = "NewDevice2";
	public static final String NEW_DEVICE_NAME2 = "New Device2 Display Name";
	
	public static final String NO_DEVICE_IDENTIFICATION = "111.111.1.11";
	public static final String NO_DEVICE_NAME = "dummy";
	
	public static final String HOST_NAME = "nagios";
	public static final String MONITOR_SERVER3_NAME = "groundwork-monitor3";		
	public static final int    MONITOR_SERVER1_ID = 1;
		
	public static final List<String> DEVICE_IDENTIFICATION_LIST = new ArrayList<String>(2);
		
	public static final int[] 	 DEVICE_ID_INT_ARRAY = {1, 3, 4};
	
	static {
		DEVICE_IDENTIFICATION_LIST.add(DEVICE1_IDENTIFICATION);
		DEVICE_IDENTIFICATION_LIST.add(DEVICE3_IDENTIFICATION);
		
		PARENTS_TO_DETACH.add("192.168.1.104");
		PARENTS_TO_DETACH.add("BOGUS");
		PARENTS_TO_DETACH.add("192.168.1.102");
		PARENTS_TO_DETACH.add("192.168.1.101");
		
		PARENTS_TO_ATTACH.add("192.168.1.104");
		PARENTS_TO_ATTACH.add("BOGUS");
		PARENTS_TO_ATTACH.add("192.168.1.102");
		PARENTS_TO_ATTACH.add("192.168.1.101");


        CHILDREN_TO_DETACH.add(DEVICE1_IDENTIFICATION);
        CHILDREN_TO_DETACH.add(DEVICE2_IDENTIFICATION);
        CHILDREN_TO_DETACH.add("BOGUS");
        CHILDREN_TO_DETACH.add(DEVICE5_IDENTIFICATION);
		
		CHILDREN_TO_ATTACH.add(DEVICE1_IDENTIFICATION);
		CHILDREN_TO_ATTACH.add(DEVICE2_IDENTIFICATION);
		CHILDREN_TO_ATTACH.add(DEVICE5_IDENTIFICATION);		
	}
	
	DeviceService deviceService = null;
	HostService hostService = null;
    MetadataService metadataService = null;

	
	public TestDeviceService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		suite = new TestSuite(TestDeviceService.class);

		// or a subset thereoff
		//suite.addTest(new TestDeviceService("testGetDevice"));
    
		return suite;
	}

	public void setUp() throws Exception
	{
    	super.setUp();
    	// Retrieve business service
		deviceService = collage.getDeviceService();		
		assertNotNull(deviceService);
		
		hostService = collage.getHostService();
		assertNotNull(hostService);

        metadataService = collage.getMetadataService();
        assertNotNull(metadataService);
	}

	
	/* the following constants should reflect the state of test data */
	
	public void testAddDevicesToMonitorServer() throws BusinessServiceException
	{
		startTime();				
		int numAdded = deviceService.addDevicesToMonitorServer(MONITOR_SERVER3_NAME,
																DEVICE_IDENTIFICATION_LIST);
		outputElapsedTime("addDevicesToMonitorServer()");
		assertEquals(2, numAdded);
	}

	public void testCreateDevice() throws BusinessServiceException
	{
		// Create and save device
		Device device = deviceService.createDevice();
		device.setIdentification(NEW_DEVICE_IDENTIFICATION);
		device.setDisplayName(NEW_DEVICE_NAME);
		deviceService.saveDevice(device);
		
		// Query New Device
		startTime();
		device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		outputElapsedTime("deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION)");
		assertNotNull(device);
		assertEquals(NEW_DEVICE_NAME, device.getDisplayName());
		
		// Delete Newly Created Device
		startTime();
		deviceService.deleteDevice(device);	
		
		// Query Deleted Device and make sure it has been deleted
		device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNull(device);
		
		// Create and save device
		device = deviceService.createDevice(NEW_DEVICE_IDENTIFICATION, NEW_DEVICE_NAME);
		deviceService.saveDevice(device);
		
		// Query New Device
		startTime();
		device = deviceService.getDeviceById(device.getDeviceId());
		outputElapsedTime("deviceService.getDeviceById(device.getDeviceId())");
		assertNotNull(device);
		assertEquals(NEW_DEVICE_NAME, device.getDisplayName());
		assertEquals(NEW_DEVICE_IDENTIFICATION, device.getIdentification());
		
		// Delete Newly Created Device
		startTime();
		deviceService.deleteDeviceById(device.getDeviceId());	
		outputElapsedTime("deviceService.deleteDeviceById(device.getDeviceId())");
		
		// Query Deleted Device and make sure it has been deleted
		device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNull(device);				
	}

	public void testDeleteDeviceByIdentification() throws BusinessServiceException
	{
		// Create and save device
		Device device = deviceService.createDevice();
		device.setIdentification(NEW_DEVICE_IDENTIFICATION);
		device.setDisplayName(NEW_DEVICE_NAME);
		deviceService.saveDevice(device);
		
		// Query New Device
		device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNotNull(device);
		assertEquals(NEW_DEVICE_NAME, device.getDisplayName());		
		
		// Delete Newly Created Device By Identification
		deviceService.deleteDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		
		// Query Deleted Device and make sure it has been deleted
		device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNull(device);		
	}

	public void testDeleteDevices() throws BusinessServiceException
	{
		// Create and save devices
		Device device1 = deviceService.createDevice(NEW_DEVICE_IDENTIFICATION, NEW_DEVICE_NAME);
		Device device2 = deviceService.createDevice(NEW_DEVICE_IDENTIFICATION2, NEW_DEVICE_NAME2);
		
		Collection<Device> col = new ArrayList<Device>(2);
		col.add(device1);
		col.add(device2);
		
		deviceService.saveDevices(col);			
		
		// Query New Devices
		device1 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNotNull(device1);
		
		device2 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION2);
		assertNotNull(device2);
		
		// Delete devices
		col.clear();
		col.add(device1);
		col.add(device2);
		
		startTime();
		deviceService.deleteDevices(col);	
		outputElapsedTime("deviceService.deleteDevices(col)");
		
		// Make sure they have been deleted
		device1 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNull(device1);
		
		device2 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION2);
		assertNull(device2);	
		
		/*********************************************************************/
		/* Delete by ids */
		/*********************************************************************/
		
		// Create and save devices
		device1 = deviceService.createDevice(NEW_DEVICE_IDENTIFICATION, NEW_DEVICE_NAME);
		device2 = deviceService.createDevice(NEW_DEVICE_IDENTIFICATION2, NEW_DEVICE_NAME2);
		
		col = new ArrayList<Device>(2);
		col.add(device1);
		col.add(device2);
		
		deviceService.saveDevices(col);			
		
		// Query New Devices
		device1 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNotNull(device1);
		
		device2 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION2);
		assertNotNull(device2);
		
		// Delete devices
		int[] deviceIds = new int[] { device1.getDeviceId().intValue(), device2.getDeviceId().intValue() };

		startTime();
		deviceService.deleteDevices(deviceIds);	
		outputElapsedTime("deviceService.deleteDevices(deviceIds)");
		
		// Make sure they have been deleted
		device1 = deviceService.getDeviceById(deviceIds[0]);
		assertNull(device1);
		
		device2 = deviceService.getDeviceById(deviceIds[1]);
		assertNull(device2);				
		
		/*********************************************************************/
		/* Delete by identifications */
		/*********************************************************************/
		
		// Create and save devices
		startTime();
		device1 = deviceService.saveDevice(NEW_DEVICE_IDENTIFICATION,
												  NEW_DEVICE_NAME, 
												  MONITOR_SERVER3_NAME);
		outputElapsedTime("deviceService.saveDevice(NEW_DEVICE_IDENTIFICATION, NEW_DEVICE_NAME, MONITOR_SERVER3_NAME)");
		
		device2 = deviceService.saveDevice(NEW_DEVICE_IDENTIFICATION2, 
												  NEW_DEVICE_NAME2,
												  MONITOR_SERVER3_NAME);
			
		// Query New Devices
		device1 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNotNull(device1);
		
		device2 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION2);
		assertNotNull(device2);
		
		// Delete devices
		String[] deviceIdents = new String[] { device1.getIdentification(), device2.getIdentification() };

		deviceService.deleteDevices(deviceIdents);	
		
		// Make sure they have been deleted
		device1 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
		assertNull(device1);
		
		device2 = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION2);
		assertNull(device2);			
	}

	public void DonottestDetachChildDevices() throws BusinessServiceException
	{
		startTime();
		int numDetached = deviceService.detachChildDevices(DEVICE3_IDENTIFICATION, CHILDREN_TO_DETACH);
		outputElapsedTime("deviceService.detachChildDevices(DEVICE3_IDENTIFICATION, CHILDREN_TO_DETACH)");
		assertEquals(3, numDetached);

		// Re-attach child devices
		/*startTime();
		int numAttached = deviceService.attachChildDevices(DEVICE3_IDENTIFICATION, CHILDREN_TO_ATTACH);
		outputElapsedTime("deviceService.attachChildDevices(DEVICE3_IDENTIFICATION, CHILDREN_TO_ATTACH)");
		assertEquals(3, numAttached);*/
	}

	public void DonottestDetachParentDevices() throws BusinessServiceException
	{
		startTime();
		int numDetached = deviceService.detachParentDevices(DEVICE1_IDENTIFICATION, PARENTS_TO_DETACH);
		outputElapsedTime("deviceService.detachParentDevices(DEVICE1_IDENTIFICATION, PARENTS_TO_DETACH)");
		assertEquals(3, numDetached);

		// Re-attach parent devices
		startTime();
		int numAttached = deviceService.attachParentDevices(DEVICE1_IDENTIFICATION, PARENTS_TO_ATTACH);
		outputElapsedTime("deviceService.attachParentDevices(DEVICE1_IDENTIFICATION, PARENTS_TO_ATTACH)");
		assertEquals(3, numAttached);		
	}

	public void testGetDeviceByHostId() throws BusinessServiceException
	{
		// Need session for getHosts() b/c they are lazy loaded
		beginTransaction();
		
		// First get a device to get a host id
		Device device = deviceService.getDeviceById(DEVICE1_ID);
		assertNotNull(device);			
	
		Set hosts = device.getHosts();
		assertNotNull(hosts);
		assertEquals(3, hosts.size());					
		
		// Get first host
		Iterator it = hosts.iterator();
		Host host = (Host)it.next();
		
		device = deviceService.getDeviceByHostId(host.getHostId());
		assertNotNull(device);
		assertEquals(DEVICE1_ID, device.getDeviceId().intValue());
		
		//rollbackTransaction();
        commitTransaction();
	}

	public void testGetDeviceByHostName() throws BusinessServiceException
	{
//		// Need session for getHosts() b/c they are lazy loaded
		beginTransaction();
		
		// First get a device to get a host name
		Device device = deviceService.getDeviceById(DEVICE1_ID);
		assertNotNull(device);
		
		Set hosts = device.getHosts();
		assertNotNull(hosts);
		assertEquals(3, hosts.size());
		
		// Get first host
		Iterator it = hosts.iterator();
		Host host = (Host)it.next();
		
		device = deviceService.getDeviceByHostName(host.getHostName());
		assertNotNull(device);
		assertEquals(DEVICE1_ID, device.getDeviceId().intValue());		
		
		//rollbackTransaction();
        commitTransaction();
	}

	public void testGetDevices() throws BusinessServiceException
	{
		// Case insensitive like
		FilterCriteria filterCriteria = FilterCriteria.ilike("description", "seRver", MatchType.ANYWHERE);
		
		// Sort by display name
		SortCriteria sortCriteria = SortCriteria.asc("displayName");
		
		// Return 2 results starting at index 1
		startTime();
		FoundationQueryList results = deviceService.getDevices(filterCriteria, sortCriteria, 1, 2);
		outputElapsedTime("deviceService.getDevices(filterCriteria, sortCriteria, 1, 2)");
		assertNotNull(results);
		assertEquals(2, results.size());
		
		// Check order of device and paging
		Device device = (Device)results.get(0);
		assertEquals(5, device.getDeviceId().intValue());

		device = (Device)results.get(1);
		assertEquals(2, device.getDeviceId().intValue());
	}

	public void testGetDevicesById() throws BusinessServiceException
	{
		SortCriteria sortCriteria = SortCriteria.asc("identification");
		
		startTime();
		List<Device> deviceList = deviceService.getDevices(DEVICE_ID_INT_ARRAY, sortCriteria);
		outputElapsedTime("deviceService.getDevices(DEVICE_ID_INT_ARRAY, sortCriteria)");
		assertNotNull(deviceList);
		assertEquals(DEVICE_ID_INT_ARRAY.length, deviceList.size());
	}

	public void testGetDevicesByIdentification() throws BusinessServiceException
	{
		SortCriteria sortCriteria = SortCriteria.asc("identification");
		
		startTime();
		List<Device> deviceList = deviceService.getDevices(DEVICE_IDENTIFICATION_LIST, sortCriteria);
		outputElapsedTime("deviceService.getDevices(DEVICE_IDENTIFICATION_STRING_ARRAY, sortCriteria)");
		assertNotNull(deviceList);
		assertEquals(DEVICE_IDENTIFICATION_LIST.size(), deviceList.size());
	}

	public void testGetDevicesByMonitorServerId(int monitorServerId, FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException
	{
		startTime();
		FoundationQueryList results = deviceService.getDevicesByMonitorServerId(MONITOR_SERVER1_ID, null, null, -1, -1);
		outputElapsedTime("deviceService.getDevicesByMonitorServerId(MONITOR_SERVER1_ID, null, null, -1, -1)");
		assertNotNull(results);
		assertEquals(4, results.size());
	}

    public void testSetDynamicProperty() {
        beginTransaction();
        try {
            // define property type
            metadataService.savePropertyType("TEST_PROPERTY", "testSetDynamicProperty", PropertyType.STRING);
            // create test device
            Device device = deviceService.createDevice();
            device.setIdentification(NEW_DEVICE_IDENTIFICATION);
            device.setDisplayName(NEW_DEVICE_NAME);
            deviceService.saveDevice(device);
            // lookup test device
            device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
            assertNotNull(device);
            assertNull(device.getProperty("TEST_PROPERTY"));
            // set dynamic property
            device.setProperty("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
            deviceService.saveDevice(device);
            // validate dynamic property
            flushAndClearSession();
            device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
            assertNotNull(device);
            assertEquals("TEST_PROPERTY_VALUE", device.getProperty("TEST_PROPERTY"));
            // remove dynamic property
            device.setProperty("TEST_PROPERTY", null);
            assertNull(device.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : device.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            deviceService.saveDevice(device);
            // validate dynamic property
            flushAndClearSession();
            device = deviceService.getDeviceByIdentification(NEW_DEVICE_IDENTIFICATION);
            assertNotNull(device);
            assertNull(device.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : device.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            // delete test device
            deviceService.deleteDevice(device);
        } finally {
			metadataService.deletePropertyTypeByName("TEST_PROPERTY"); // clean out of metadata cache
            rollbackTransaction();
        }
    }
}
