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
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestHostService extends AbstractTestCaseWithTransactionSupport {
	
	/* the following constants should reflect the state of test data */

	public final static String HOST1_NAME  = "nagios";
	public final static String HOST1_PARTIAL_NAME  = "nag";
    public final static String HOST1_DESCR = "Nagios Server";
    
    public final static String GWRKHOST_PARTIAL_NAME = "gwrk";
    public final static String NO_HOST_NAME = "zzz";
    
	public final static String SERVICE1_DESCR1 = "local_procs";
	public final static String SERVICE1_DESCR2 = "Local_Procs";
	public final static String SERVICE1_DESCR3 = "lOCAL_pRoCs";
	public final static String SERVICE2_DESCR  = "network_users";
    public final static int DEVICE_ID = 1;
    public final static String DEVICE_IDENTIFICATION = "192.168.1.100";
    public final static String MONITOR_SERVER_NAME = "groundwork-monitor1";

	public final static String HOST2_NAME1 = "db-svr";
	public final static String HOST2_DESCR = "Database Server";

	public final static String HOST2_NAME2 = "DB-Svr";
	public final static String HOST2_NAME3 = "dB-svR";
	
	public final static String HOST3_NAME = "exchange";
    
    public final static String HOSTGROUP_NAME = "demo-system";
    
    public final static String APPLICATION_TYPE = "NAGIOS";

    private HostService hostService;
    private Autocomplete hostAutocompleteService;
    private HostGroupService hostGroupService;
    private MetadataService metadataService;
    private DeviceService deviceService;

	public TestHostService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");
		
		// run all tests
		//suite = new TestSuite(TestHostService.class);

		// or a subset thereoff
		suite.addTest(new TestHostService("testGetHost"));
        suite.addTest(new TestHostService("testGetHosts"));
        suite.addTest(new TestHostService("testGetHostsInHostGroup"));
		suite.addTest(new TestHostService("testGetHostsWithService"));
        suite.addTest(new TestHostService("testGetHostsWithDeviceID"));
        suite.addTest(new TestHostService("testGetHostsWithDeviceIdentification"));
        suite.addTest(new TestHostService("testGetHostsForMonitorServer"));
		suite.addTest(new TestHostService("testGetHostStatus"));
		suite.addTest(new TestHostService("testDeleteHostStatus"));
		suite.addTest(new TestHostService("testDeleteHost"));
        suite.addTest(new TestHostService("testSetDynamicProperty"));
        suite.addTest(new TestHostService("testAutocomplete"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve services
		hostService = collage.getHostService();
		assertNotNull(hostService);
        hostAutocompleteService = collage.getHostAutocompleteService();
        assertNotNull(hostAutocompleteService);
        hostGroupService = collage.getHostGroupService();
        assertNotNull(hostGroupService);
        metadataService = collage.getMetadataService();
        assertNotNull(metadataService);
        deviceService = collage.getDeviceService();
        assertNotNull(deviceService);
	}
	
	public void testGetHost()
	{
		// basic test.  get a host and make sure all the parts are there
		Host host = hostService.getHostByHostName(HOST1_NAME);
		assertNotNull(HOST1_NAME + " is not null", host);
		assertEquals("retrieving host " + HOST1_NAME, HOST1_NAME, host.getHostName());
		assertEquals("host description for " + HOST1_NAME, HOST1_DESCR, host.getDescription());

		// now get the host by id - get the id of the host retrieved in last test
		Integer host_id = host.getHostId();
		assertNotNull(host_id.toString() + " is not null", host_id);
        assertEquals("retrieving host " + host_id.toString(), host_id, host.getHostId());
        // now get the host using that id
		Host host_by_id = hostService.getHostByHostId(host_id);
		assertNotNull(host_id.toString() + " is not null", host_by_id);
        assertEquals("retrieving host " + host_id.toString(), host_id, host_by_id.getHostId());
        assertEquals("host name for " + host_id.toString(), HOST1_NAME, host_by_id.getHostName());
        assertEquals("host description for " + host_id.toString(), HOST1_DESCR, host_by_id.getDescription());
        assertEquals("host1 equal to host1_by_id", host, host_by_id);
        
		Host host1 = hostService.getHostByHostName(HOST2_NAME1);
		assertNotNull(HOST2_NAME1 + " is not null", host1);
		assertEquals("retrieving host " + HOST2_NAME1, HOST2_NAME1, host1.getHostName());
		assertEquals("host description for " + HOST2_NAME1, HOST2_DESCR, host1.getDescription());

		Host host2 = hostService.getHostByHostName(HOST2_NAME2);
		assertNotNull(HOST2_NAME2 + " is not null", host2);
		assertEquals("host description for " + HOST2_NAME2, HOST2_DESCR, host2.getDescription());

		Host host3 = hostService.getHostByHostName(HOST2_NAME3);
		assertNotNull(HOST2_NAME3 + " is not null", host3);
		assertEquals("host description for " + HOST2_NAME2, HOST2_DESCR, host3.getDescription());

		assertEquals(HOST2_NAME1 + " is equal to " + HOST2_NAME2, host1, host2);
		assertEquals(HOST2_NAME1 + " is equal to " + HOST2_NAME3, host1, host3);
		assertEquals(HOST2_NAME2 + " is equal to " + HOST2_NAME3, host2, host3);
        
		log.info("Test getHost done!");
	}
	
	public void testGetHosts()
	{
		FilterCriteria filterCriteria = FilterCriteria.eq("applicationType.name", APPLICATION_TYPE);
		FoundationQueryList hosts = hostService.getHosts(filterCriteria, /*sortCriteria*/null, /*firstResult*/-1, /*maxResults*/0);
		assertNotNull(hosts);
		assertEquals("retrieving all Hosts", 16, hosts.size());
		assertTrue("is Host class", hosts.iterator().next() instanceof Host);
		
		
	    // retrieve list of 3 hosts
	    List<String> hostList = new ArrayList<String>(3);
	    hostList.add(HOST1_NAME); hostList.add(HOST2_NAME1); hostList.add(HOST3_NAME);
		Collection<Host> hostsCollection = hostService.getHosts(hostList);
		assertNotNull(hostsCollection);
		assertEquals("retrieved 3 hosts", 3, hostsCollection.size());
		assertTrue("is Host class", hostsCollection.iterator().next() instanceof Host);
		
		hostsCollection.clear();
		hostList = null;
		try {
			hostsCollection = hostService.getHosts(hostList);
			fail("An IllegalArgumentException should have been thrown when the host list is null.");
		}
		catch (IllegalArgumentException e) 
		{
			// all ok, we expected the exception
		}
		
		hostsCollection.clear();	
		hostList = new ArrayList<String>(3);
		try {
			hostsCollection = hostService.getHosts(hostList);
			fail("An IllegalArgumentException should have been thrown when the host list is empty.");
		}
		catch (IllegalArgumentException e) 
		{
			// all ok, we expected the exception
		}
		
		hostsCollection.clear();
		hostList.add("NO_HOST_NAME");
		hostsCollection = hostService.getHosts(hostList);
		assertNotNull(hostsCollection);
		assertEquals("No hosts retrieved", 0, hostsCollection.size());
		
		log.info("Test getHosts done!");
	}

	public void testGetHostsWithService()
	{
		Collection<Host> hosts = hostService.getHostsByServiceName(SERVICE2_DESCR);
		assertNotNull(hosts);
		assertEquals("retrieving Hosts with service " + SERVICE2_DESCR, 3, hosts.size());
		assertTrue("is Host class", hosts.iterator().next() instanceof Host);
		
		log.info("Test testGetHostsWithService done!");
	}
    
    public void testGetHostsInHostGroup()
    {
        FoundationQueryList hosts = hostService.getHostsByHostGroupName(HOSTGROUP_NAME, null, null, -1, 0);
        assertNotNull(hosts);        
        assertEquals("Hosts in HostGroup", 4, hosts.size());
        
        
        HostGroup hg = hostGroupService.getHostGroupByName(HOSTGROUP_NAME);
        hosts = hostService.getHostsByHostGroupId(hg.getHostGroupId().intValue(), null, null, -1, 0);
        assertNotNull(hosts);        
        assertEquals("Hosts in HostGroup", 4, hosts.size());
        
        log.info("Test testGetHostsInHostGroup done!");
    }

    public void testGetHostsWithDeviceID()
    {
        Collection<Host> hosts = hostService.getHostsByDeviceId(DEVICE_ID);
        assertNotNull(hosts);
        assertEquals("retrieving Hosts with device id " + DEVICE_ID, 3, hosts.size());
        assertTrue("is Host class", hosts.iterator().next() instanceof Host);
        
        log.info("Test testGetHostsWithDeviceID done!");
    }
    
    public void testGetHostsWithDeviceIdentification()
    {
        Collection<Host> hosts = hostService.getHostsByDeviceIdentification(DEVICE_IDENTIFICATION);
        assertNotNull(hosts);
        assertEquals("retrieving Hosts with device description " + DEVICE_IDENTIFICATION, 3, hosts.size());
        assertTrue("is Host class", hosts.iterator().next() instanceof Host);
        
        log.info("Test testGetHostsWithDeviceIdentification done!");
    }
    
    public void testGetHostsForMonitorServer()
    {
        FoundationQueryList hosts = hostService.getHostsByMonitorServer(MONITOR_SERVER_NAME, -1, 0);
        assertNotNull(hosts);
        assertEquals("retrieving Hosts for MonitorServer " + MONITOR_SERVER_NAME, 15, hosts.size());
        assertTrue("is Host class", hosts.iterator().next() instanceof Host);
        
        log.info("Test testGetHostsForMonitorServer done!");
    }
    
    public void testHostLookup()
    {
    	
    	Collection<Host> hosts = hostService.hostLookup(HOST1_NAME);
    	assertNotNull(hosts);
    	// there should be only one in this case
    	assertEquals("Host lookup on host "+HOST1_NAME,1,hosts.size());
    	
    	hosts.clear();
    	
    	hosts = hostService.hostLookup(HOST1_PARTIAL_NAME);
    	assertNotNull(hosts);
    	// there should be only one in this case
    	assertEquals("Host lookup on host "+HOST1_PARTIAL_NAME,1,hosts.size());
    	
    	hosts.clear();

    	hosts = hostService.hostLookup(GWRKHOST_PARTIAL_NAME);
    	assertNotNull(hosts);
    	// there should be only one in this case
    	assertEquals("Host lookup on host "+GWRKHOST_PARTIAL_NAME,12,hosts.size());
    	
    	hosts.clear();
    	
    	hosts = hostService.hostLookup(NO_HOST_NAME);
    	assertNotNull(hosts);
    	assertEquals("Host lookup on host "+NO_HOST_NAME, 0, hosts.size());
    	
    	log.info("Test testHostLookup done!");
   }
    
    public void testGetHostStatus() 
	{
		// TODO: Uncomment dyna properties later
    	HostStatus status = hostService.getStatusByHostName(HOST1_NAME);
		assertNotNull("retrieved status", status);
		assertNotNull(HOST1_NAME + " last check time",   status.getLastCheckTime());
		//assertNotNull(HOST1_NAME + " last state change", status.get(Nagios.LAST_STATE_CHANGE));
		//assertNotNull(HOST1_NAME + " last notif. time",  status.get(Nagios.LAST_NOTIFICATION_TIME));

		if (log.isDebugEnabled())
		{
			log.debug(HOST1_NAME + " last check time:	 "  + status.getLastCheckTime());
			//log.debug(HOST1_NAME + " last state change: " + status.get(Nagios.LAST_STATE_CHANGE));
			//log.debug(HOST1_NAME + " last notif. time:	" + status.get(Nagios.LAST_NOTIFICATION_TIME));
		}

		status = hostService.getStatusByHostName(HOST2_NAME1);
		assertNotNull("retrieved status", status);
		assertNotNull(HOST2_NAME1 + " last check time",   status.getLastCheckTime());
		//assertNotNull(HOST2_NAME1 + " last state change", status.get(Nagios.LAST_STATE_CHANGE));
		//assertNotNull(HOST2_NAME1 + " last notif. time",  status.get(Nagios.LAST_NOTIFICATION_TIME));

		if (log.isDebugEnabled())
		{
			log.debug(HOST2_NAME1 + " last check time:	 "  + status.getLastCheckTime());
			//log.debug(HOST2_NAME1 + " last state change: " + status.get(Nagios.LAST_STATE_CHANGE));
			//log.debug(HOST2_NAME1 + " last notif. time:	" + status.get(Nagios.LAST_NOTIFICATION_TIME));
		}
		log.info("Test testGetHostStatus done!");
	}
    
	public void testDeleteHostStatus()
	{
		beginTransaction();

//		LogMessageDAO messageDAO = (LogMessageDAO)collage.getAPIObject(CollageFactory.LOG_MESSAGE_DAO);
//		assertNotNull(messageDAO);

		HostStatus hostStatus = hostService.getStatusByHostName(HOST1_NAME);
		assertNotNull("retrieved HostStatus for " + HOST1_NAME, hostStatus);

//		Collection l = messageDAO.getLogMessagesForHost(HOST1_NAME, null, null, -1, -1);
//		assertEquals("messages for HostStatus", 8, l.size());

		hostService.deleteHostStatus(HOST1_NAME);

		hostStatus = hostService.getStatusByHostName(HOST1_NAME);
		assertNull("deleted HostStatus for " + HOST1_NAME, hostStatus);
		/*Note: 
		 * Deleting HostStatuses doesn't delete the Host and therefore
		 * no deletion of any associated LogMessages
		 * Test will be done in deleteHost Unit test
		 */
		
		rollbackTransaction();

		hostStatus = hostService.getStatusByHostName(HOST1_NAME);
		assertNotNull("retrieved hostStatus " + HOST1_NAME, hostStatus);

//		l = messageDAO.getLogMessagesForHost(HOST1_NAME, null, null, -1, -1);
//		assertEquals("messages for hostStatus", 8, l.size());
		
		log.info("Test testDeleteHostStatus done!");
	}

	/* Remove LogMessage delete verification
	 * TBD: Add a specific test for LogMessage and Host deletion
	 */
	public void testDeleteHost()
	{
		beginTransaction();

//		LogMessageDAO messageDAO = (LogMessageDAO)collage.getAPIObject(CollageFactory.LOG_MESSAGE_DAO);
//		assertNotNull(messageDAO);

		Host host = hostService.getHostByHostName(HOST1_NAME);
		assertNotNull("retrieved Host " + HOST1_NAME, host);

//		Collection l = messageDAO.getLogMessagesForHost(HOST1_NAME, null, null, -1, -1);
//		assertEquals("messages for Host", 8, l.size());

		hostService.deleteHostByName(HOST1_NAME);

		host = hostService.getHostByHostName(HOST1_NAME);
		assertNull("deleted Host " + HOST1_NAME, host);

//		l = messageDAO.getLogMessagesForHost(HOST1_NAME, null, null, -1, -1);
//		assertEquals("messages for deleted Host", 0, l.size());

		rollbackTransaction();

		host = hostService.getHostByHostName(HOST1_NAME);
		assertNotNull("retrieved Host " + HOST1_NAME, host);

//		l = messageDAO.getLogMessagesForHost(HOST1_NAME, null, null, -1, -1);
//		assertEquals("messages for Host", 8, l.size());
		
		log.info("Test testDeleteHost done!");
	}

    public void testSetDynamicProperty() {
        beginTransaction();
        try {
            // define property type
            metadataService.savePropertyType("TEST_PROPERTY", "testSetDynamicProperty", PropertyType.STRING);
            // lookup host
            Host host = hostService.getHostByHostName(HOST1_NAME);
            assertNotNull(host);
            assertNotNull(host.getHostStatus());
            assertNull(host.getHostStatus().getProperty("TEST_PROPERTY"));
            // set dynamic property
            host.getHostStatus().setProperty("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
            hostService.saveHost(host);
            // validate dynamic property
            flushAndClearSession();
            host = hostService.getHostByHostName(HOST1_NAME);
            assertNotNull(host);
            assertNotNull(host.getHostStatus());
            assertEquals("TEST_PROPERTY_VALUE", host.getHostStatus().getProperty("TEST_PROPERTY"));
            // remove dynamic property
            host.getHostStatus().setProperty("TEST_PROPERTY", null);
            assertNull(host.getHostStatus().getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : host.getHostStatus().getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            hostService.saveHost(host);
            // validate dynamic property
            flushAndClearSession();
            host = hostService.getHostByHostName(HOST1_NAME);
            assertNotNull(host);
            assertNotNull(host.getHostStatus());
            assertNull(host.getHostStatus().getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : host.getHostStatus().getProperties(true).entrySet()) {
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
        List<AutocompleteName> names = hostAutocompleteService.autocomplete(HOST1_NAME);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(HOST1_NAME.toLowerCase(), names.get(0).getName());
        names = hostAutocompleteService.autocomplete(HOST1_NAME.substring(0,1).toUpperCase());
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(HOST1_NAME.toLowerCase(), names.get(0).getName());
        // test autocomplete refresh on save and delete
        names = hostAutocompleteService.autocomplete("zzz");
        assertNotNull(names);
        assertTrue(names.isEmpty());
        // create host
        Device device = deviceService.getDeviceById(DEVICE_ID);
        Host host = hostService.createHost("zzzzzz", device);
        hostService.saveHost(host);
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostAutocompleteService.autocomplete("zzz");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("zzzzzz", names.get(0).getName());
        // delete host
        hostService.deleteHostByName("zzzzzz");
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostAutocompleteService.autocomplete("zzz");
        assertNotNull(names);
        assertTrue(names.isEmpty());
        // create device
        device = deviceService.createDevice("xxxxxx", "xxxxxx");
        deviceService.saveDevice(device);
        // create host
        device = deviceService.getDeviceByIdentification("xxxxxx");
        host = hostService.createHost("xxxxxx", device);
        hostService.saveHost(host);
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostAutocompleteService.autocomplete("xxx");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("xxxxxx", names.get(0).getName());
        // delete device
        deviceService.deleteDeviceByIdentification("xxxxxx");
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostAutocompleteService.autocomplete("xxx");
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

	/**
	 * tail postgres log while running
	 */
	public void testHostCaching() {

		// use FIRST LEVEL CACHE
		beginTransaction();
		Session session = getSession();
		Integer id = 1;
		// Initial hit, generates queries in postgresql
		Host host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id); //hostService.getHostByHostId(1);
		assertNotNull(host);
		// does not generate a query, hits first level cache
		host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id);
		assertNotNull(host);
		// does not generate a query, hits first level cache
		host = (Host)session.get(com.groundwork.collage.model.impl.Host.class, id);
		assertNotNull(host);
		commitTransaction();

		// USE SECOND LEVEL CACHE
		beginTransaction();
		session = getSession();
		// does not generate a Host query, hits SECOND LEVEL cache
		// generate some SQL for PropertyType, EntityProperty and Device
		host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id);
		assertNotNull(host);
		// does not generate a query, hits second level cache
		host = (Host)session.get(com.groundwork.collage.model.impl.Host.class, id);
		assertNotNull(host);
		commitTransaction();

		// USE SECOND LEVEL CACHE plus LAZY LOAD
		beginTransaction();
		session = getSession();
		host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id);
		// generates query
		host.getServiceStatuses().size(); // lazy load
		host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id);
		// no query, in cache
		host.getServiceStatuses().size(); // lazy load
		assertNotNull(host);
		commitTransaction();

		// USE SECOND LEVEL CACHE plus LAZY LOAD
		beginTransaction();
		session = getSession();
		host = (Host)session.load(com.groundwork.collage.model.impl.Host.class, id);
		// Should be in second level cache, BUT GWOS has not 2nd-level cached this collection association
		host.getServiceStatuses().size(); // lazy load
		commitTransaction();

	}
}
