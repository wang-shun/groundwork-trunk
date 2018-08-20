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


import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.List;
import java.util.Map;

/**
 * @author rdandridge
 *
 */
public class TestStatusService extends AbstractTestCaseWithTransactionSupport 
{
	/* the following constants should reflect the state of test data */
	private StatusService statusService = null;
	private HostGroupService hostGroupService = null;
	private HostService hostService = null;
    private MetadataService metadataService = null;
    private Autocomplete statusAutocompleteService = null;


    public static final String HOSTGROUPNAME_1 = "demo-system";
	public static final String HOSTGROUPNAME_2 = "All_Infrastructure";
	public static final String HOSTNAME_1 = "nagios";
	public static final String HOSTNAME_2 = "gwrk-organizations";
	public static final String SERVICE_DESCRIPTION = "organizations";
	
	public TestStatusService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestStatusService.class);

		// or a subset thereoff
		suite.addTest(new TestStatusService("testGetServices"));
		suite.addTest(new TestStatusService("testGetServicesByHostGroupName"));
		suite.addTest(new TestStatusService("testGetServicesByHostGroupID"));
		suite.addTest(new TestStatusService("testGetServicesByHostId"));
		suite.addTest(new TestStatusService("testGetServicesByHostName"));
		suite.addTest(new TestStatusService("testGetServiceByDescriptionAndId"));
		suite.addTest(new TestStatusService("testDeleteService"));
		suite.addTest(new TestStatusService("testGetServicesByCriteria"));
        suite.addTest(new TestStatusService("testSetDynamicProperty"));
        suite.addTest(new TestStatusService("testAutocomplete"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve service status business service
		statusService = collage.getStatusService();		
		assertNotNull(statusService);
		
		hostGroupService = collage.getHostGroupService();
		assertNotNull(hostGroupService);
		
		hostService = collage.getHostService();
		assertNotNull(hostService);

        metadataService = collage.getMetadataService();
        assertNotNull(metadataService);

        statusAutocompleteService = collage.getStatusAutocompleteService();
        assertNotNull(statusAutocompleteService);
	}
	
	public void testGetServices()
	{
		FoundationQueryList services = statusService.getServices(null, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching 17 services",services.size(), 17);
	}
	
	public void testGetServicesByHostGroupName()
	{
		FoundationQueryList services = statusService.getServicesByHostGroupName(this.HOSTGROUPNAME_2, null, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching 10 services",services.size(), 10);
	}

	public void testGetServicesByHostGroupID()
	{
		HostGroup hostgroup = hostGroupService.getHostGroupByName(this.HOSTGROUPNAME_2);
		Integer hostgroupID = hostgroup.getHostGroupId();
		FoundationQueryList services = statusService.getServicesByHostGroupId(hostgroupID.intValue(), null, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching 10 services",services.size(), 10);
	}
	
	public void testGetServicesByHostId()
	{
		Host host = hostService.getHostByHostName(this.HOSTNAME_1);
		Integer hostID = host.getHostId();
		FoundationQueryList services = statusService.getServicesByHostId(hostID.intValue(), null, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching 4 services", services.size(), 4);
	}
	
	public void testGetServicesByHostName()
	{
		FoundationQueryList services = statusService.getServicesByHostName(this.HOSTNAME_1, null, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching 4 services", services.size(), 4);
	}
	
	public void testGetServiceByDescriptionAndId()
	{
		ServiceStatus service = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
		assertNotNull(service);
		assertEquals(service.getServiceDescription(), this.SERVICE_DESCRIPTION);
		
		// now try to get the same service using the service id
		Integer ssID = service.getServiceStatusId();
		assertNotNull(ssID);
		this.assertTrue(ssID.intValue() > 0);
		ServiceStatus service_dup = statusService.getServiceById(ssID.intValue());
		assertNotNull(service_dup);
		assertEquals(service, service_dup);
	}
	
	public void testDeleteService()
	{
		beginTransaction();
		
		ServiceStatus service = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
		assertNotNull(service);
		
		// now delete it
		statusService.deleteService(service);
		
		// make sure it's gone by doing a query for it
		service = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
		assertNull(service);
		
		// restore the db
		rollbackTransaction();
		
		// now make sure it IS there
		service = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
		assertNotNull(service);
	}
	
	public void testGetServicesByCriteria()
	{
		FilterCriteria filter = FilterCriteria.ne("host.hostStatus.hostMonitorStatus.name", "DOWN");
		filter.and(FilterCriteria.ne("host.hostStatus.hostMonitorStatus.name", "UNREACHABLE"));
		filter.and(FilterCriteria.ne("monitorStatus.name", "OK"));
		filter.and(FilterCriteria.ne("monitorStatus.name", "PENDING"));
	
		statusService.getServices(filter, null, 1, 1);
		FoundationQueryList services = statusService.getServices(filter, null, -1, -1);
		assertNotNull(services);
		assertEquals("Fetching services", 4, services.size());
	}

    public void testSetDynamicProperty() {
        beginTransaction();
        try {
            // define property type
            metadataService.savePropertyType("TEST_PROPERTY", "testSetDynamicProperty", PropertyType.STRING);
            // lookup service
            ServiceStatus serviceStatus = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
            assertNotNull(serviceStatus);
            assertNull(serviceStatus.getProperty("TEST_PROPERTY"));
            // set dynamic property
            serviceStatus.setProperty("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
            statusService.saveService(serviceStatus);
            // validate dynamic property
            flushAndClearSession();
            serviceStatus = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
            assertNotNull(serviceStatus);
            assertEquals("TEST_PROPERTY_VALUE", serviceStatus.getProperty("TEST_PROPERTY"));
            // remove dynamic property
            serviceStatus.setProperty("TEST_PROPERTY", null);
            assertNull(serviceStatus.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : serviceStatus.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            statusService.saveService(serviceStatus);
            // validate dynamic property
            flushAndClearSession();
            serviceStatus = statusService.getServiceByDescription(this.SERVICE_DESCRIPTION, this.HOSTNAME_2);
            assertNotNull(serviceStatus);
            assertNull(serviceStatus.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : serviceStatus.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
        } finally {
			metadataService.deletePropertyTypeByName("TEST_PROPERTY"); // clean out of metadata cache
            rollbackTransaction();
        }
    }

    public void testAutocomplete() throws Exception {
        // wait for initial load
        Thread.sleep(250);
        // test autocomplete names
        List<AutocompleteName> names = statusAutocompleteService.autocomplete(this.SERVICE_DESCRIPTION);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(this.SERVICE_DESCRIPTION, names.get(0).getName());
        // create service status
        try {
            Host host = hostService.getHostByHostName(this.HOSTNAME_2);
            assertNotNull(host);
            ServiceStatus serviceStatus = statusService.createService(this.SERVICE_DESCRIPTION+"-2", host.getApplicationType().getName(), host);
            MonitorStatus pending = metadataService.getMonitorStatusByName("PENDING");
            assertNotNull(pending);
            serviceStatus.setMonitorStatus(pending);
            serviceStatus.setLastHardState(pending);
            StateType unknown = metadataService.getStateTypeByName("UNKNOWN");
            assertNotNull(unknown);
            serviceStatus.setStateType(unknown);
            CheckType active = metadataService.getCheckTypeByName("ACTIVE");
            assertNotNull(active);
            serviceStatus.setCheckType(active);
            serviceStatus.setLastMonitorStatus("PENDING");
            statusService.saveService(serviceStatus);
            // wait for refresh and validate names
            Thread.sleep(250);
            names = statusAutocompleteService.autocomplete(this.SERVICE_DESCRIPTION);
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals(this.SERVICE_DESCRIPTION, names.get(0).getName());
            assertEquals(this.SERVICE_DESCRIPTION+"-2", names.get(1).getName());
        } finally {
            // cleanup test objects
            statusService.deleteService(this.HOSTNAME_2, this.SERVICE_DESCRIPTION + "-2");
        }
        // wait for refresh and validate names
        Thread.sleep(250);
        names = statusAutocompleteService.autocomplete(this.SERVICE_DESCRIPTION);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(this.SERVICE_DESCRIPTION, names.get(0).getName());
    }
}
	
