/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * TestHostIdentityService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestHostIdentityService extends AbstractTestCaseWithTransactionSupport {

    /** HostIdentity service */
    private HostIdentityService hostIdentityService;

    /** HostIdentity autocomplete service */
    private Autocomplete hostIdentityAutocompleteService;

    /** Host service */
    private HostService hostService;

    /** Device service */
    private DeviceService deviceService;

    /**
     * Test constructor.
     *
     * @param test test to execute
     */
    public TestHostIdentityService(String test) {
        super(test);
    }

    /**
     * Declare tests to be run.
     *
     * @return test suite
     */
    public static Test suite() {
        // initialize test database once per suite
        executeScript(false, "testdata/monitor-data.sql");

        // run all tests
        TestSuite suite = new TestSuite(TestHostIdentityService.class);

        // or a subset thereof
        //TestSuite suite = new TestSuite();
        //suite.addTest(new TestMonitorServerService("testHostIdentityServiceCRUD"));
        //suite.addTest(new TestMonitorServerService("testHostIdentityServiceHostCRUD"));
        //suite.addTest(new TestMonitorServerService("testHostIdentityServiceHostName"));
        //suite.addTest(new TestMonitorServerService("testHostIdentityServiceUniqueness"));
        //suite.addTest(new TestMonitorServerService("testHostIdentityServiceQuery"));
        //suite.addTest(new TestMonitorServerService("testAutocomplete"));

        return suite;
    }

    /**
     * Setup test.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        // setup test
        super.setUp();

        // initialize services
        hostIdentityService = collage.getHostIdentityService();
        hostIdentityAutocompleteService = collage.getHostIdentityAutocompleteService();
        hostService = collage.getHostService();
        deviceService = collage.getDeviceService();
        assertNotNull(hostIdentityService);
        assertNotNull(hostIdentityAutocompleteService);
        assertNotNull(hostService);
        assertNotNull(deviceService);
    }

    /**
     * HostIdentity service CRUD test method.
     */
    public void testHostIdentityServiceCRUD() throws Exception {
        HostIdentity hostIdentity0 = null;
        HostIdentity hostIdentity1 = null;
        HostIdentity hostIdentity2 = null;
        try {
            // basic create tests
            hostIdentity0 = hostIdentityService.createHostIdentity("test-host-name-0");
            hostIdentity1 = hostIdentityService.createHostIdentity("test-host-name-1", Arrays.asList(new String[]{"test-host-name-1.0", "test-host-name-1.1"}));
            UUID hostIdentity2UUIDId = UUID.randomUUID();
            UUID hostIdentity2UUIDHostName = UUID.randomUUID();
            hostIdentity2 = hostIdentityService.createHostIdentity(hostIdentity2UUIDId, "test-host-name-2", Arrays.asList(new String[]{hostIdentity2UUIDHostName.toString()}));
            // basic save tests
            hostIdentityService.saveHostIdentity(hostIdentity0);
            hostIdentityService.saveHostIdentities(Arrays.asList(new HostIdentity[]{hostIdentity1, hostIdentity2}));
            // basic read host names tests
            Collection<String> hostNames = hostIdentityService.getHostNames();
            assertTrue(hostNames.contains("test-host-name-0"));
            assertTrue(hostNames.contains("test-host-name-1"));
            assertTrue(hostNames.contains("test-host-name-2"));
            Collection<String> allHostNames = hostIdentityService.getAllHostNames();
            assertTrue(allHostNames.contains("test-host-name-0"));
            assertTrue(allHostNames.contains("test-host-name-1"));
            assertTrue(allHostNames.contains("test-host-name-1.0"));
            assertTrue(allHostNames.contains("test-host-name-1.1"));
            assertTrue(allHostNames.contains("test-host-name-2"));
            assertTrue(allHostNames.contains(hostIdentity2UUIDHostName.toString()));
            // basic read tests
            HostIdentity readHostIdentity0 = hostIdentityService.getHostIdentityByHostName("TEST-HOST-NAME-0");
            assertNotNull(readHostIdentity0);
            assertNotNull(readHostIdentity0.getHostIdentityId());
            assertEquals("test-host-name-0", readHostIdentity0.getHostName());
            assertEquals(1, readHostIdentity0.getHostNames().size());
            assertTrue(readHostIdentity0.getHostNames().contains("test-host-name-0"));
            HostIdentity readHostIdentity1 = hostIdentityService.getHostIdentityByHostName("test-host-name-1.1");
            assertNotNull(readHostIdentity1);
            assertNotNull(readHostIdentity1.getHostIdentityId());
            assertEquals("test-host-name-1", readHostIdentity1.getHostName());
            assertEquals(3, readHostIdentity1.getHostNames().size());
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1.0"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1.1"));
            HostIdentity readHostIdentity2 = hostIdentityService.getHostIdentityByHostName(hostIdentity2UUIDHostName.toString());
            assertNotNull(readHostIdentity2);
            assertEquals(hostIdentity2UUIDId, readHostIdentity2.getHostIdentityId());
            assertEquals("test-host-name-2", readHostIdentity2.getHostName());
            assertEquals(2, readHostIdentity2.getHostNames().size());
            assertTrue(readHostIdentity2.getHostNames().contains("test-host-name-2"));
            assertTrue(readHostIdentity2.getHostNames().contains(hostIdentity2UUIDHostName.toString()));
            readHostIdentity2 = hostIdentityService.getHostIdentityById(hostIdentity2UUIDId);
            assertNotNull(readHostIdentity2);
            assertEquals(hostIdentity2UUIDId, readHostIdentity2.getHostIdentityId());
            readHostIdentity0 = hostIdentityService.getHostIdentityByIdOrHostName("test-host-name-0");
            assertNotNull(readHostIdentity0);
            assertEquals("test-host-name-0", readHostIdentity0.getHostName());
            readHostIdentity1 = hostIdentityService.getHostIdentityByIdOrHostName("test-host-name-1.0");
            assertNotNull(readHostIdentity1);
            assertEquals("test-host-name-1", readHostIdentity1.getHostName());
            readHostIdentity2 = hostIdentityService.getHostIdentityByIdOrHostName(hostIdentity2UUIDId.toString());
            assertNotNull(readHostIdentity2);
            assertEquals("test-host-name-2", readHostIdentity2.getHostName());
            readHostIdentity2 = hostIdentityService.getHostIdentityByIdOrHostName(hostIdentity2UUIDHostName.toString());
            assertNotNull(readHostIdentity2);
            assertEquals("test-host-name-2", readHostIdentity2.getHostName());
            Collection<HostIdentity> hostIdentities = hostIdentityService.getHostIdentitiesByIdOrHostNames(Arrays.asList(new String[]{"test-host-name-0", "test-host-name-1.0", hostIdentity2UUIDHostName.toString()}));
            assertNotNull(hostIdentities);
            assertEquals(3, hostIdentities.size());
            hostIdentities = hostIdentityService.getHostIdentitiesByIdOrHostNames(Arrays.asList(new String[]{hostIdentity0.getHostIdentityId().toString(), hostIdentity1.getHostIdentityId().toString()}));
            assertNotNull(hostIdentities);
            assertEquals(2, hostIdentities.size());
            hostIdentities = hostIdentityService.getHostIdentitiesByIdOrHostNamesLookup("%-HOST-%");
            assertNotNull(hostIdentities);
            assertEquals(3, hostIdentities.size());
            hostIdentities = hostIdentityService.getHostIdentitiesByIdOrHostNamesLookup(hostIdentity0.getHostIdentityId().toString());
            assertNotNull(hostIdentities);
            assertEquals(1, hostIdentities.size());
            // regex read tests
            List<HostIdentity> regexHostIdentities = new ArrayList<HostIdentity>();
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex(".*-HOST-.*", null, regexHostIdentities);
            assertEquals(3, regexHostIdentities.size());
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex(".*-1\\.1", null, regexHostIdentities);
            assertEquals(1, regexHostIdentities.size());
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex(hostIdentity2UUIDId.toString(), null, regexHostIdentities);
            assertEquals(1, regexHostIdentities.size());
            // basic delete tests
            hostIdentityService.deleteHostIdentityById(hostIdentity0.getHostIdentityId());
            hostIdentity0 = hostIdentityService.getHostIdentityById(hostIdentity0.getHostIdentityId());
            assertNull(hostIdentity0);
            hostIdentityService.deleteHostIdentities(Arrays.asList(new HostIdentity[]{hostIdentity1, hostIdentity2}));
            hostIdentity1 = hostIdentityService.getHostIdentityById(hostIdentity1.getHostIdentityId());
            assertNull(hostIdentity1);
            hostIdentity2 = hostIdentityService.getHostIdentityById(hostIdentity2.getHostIdentityId());
            assertNull(hostIdentity2);
        } finally {
            // cleanup test objects
            if (hostIdentity0 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity0);
            }
            if (hostIdentity1 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity1);
            }
            if (hostIdentity2 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity2);
            }
        }
    }

    /**
     * HostIdentity service Host CRUD test method.
     */
    public void testHostIdentityServiceHostCRUD() throws Exception {
        HostIdentity hostIdentity0 = null;
        HostIdentity hostIdentity1 = null;
        HostIdentity hostIdentity2 = null;
        try {
            // lookup Hosts
            Host nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
            Host exchangeHost = hostService.getHostByHostName("exchange");
            assertNotNull(exchangeHost);
            Host dbSvrHost = hostService.getHostByHostName("db-svr");
            assertNotNull(dbSvrHost);
            // Host create tests
            hostIdentity0 = hostIdentityService.createHostIdentity(nagoisHost);
            hostIdentityService.saveHostIdentity(hostIdentity0);
            hostIdentity1 = hostIdentityService.createHostIdentity(exchangeHost, Arrays.asList(new String[]{"exchange-1.0", "exchange-1.1"}));
            UUID hostIdentity2UUIDId = UUID.randomUUID();
            UUID hostIdentity2UUIDHostName = UUID.randomUUID();
            hostIdentity2 = hostIdentityService.createHostIdentity(hostIdentity2UUIDId, dbSvrHost, Arrays.asList(new String[]{hostIdentity2UUIDHostName.toString()}));
            // Host save tests
            hostIdentityService.saveHostIdentity(hostIdentity0);
            hostIdentityService.saveHostIdentity(hostIdentity1);
            hostIdentityService.saveHostIdentity(hostIdentity2);
            // Host read host names tests
            Collection<String> hostNames = hostIdentityService.getHostNames();
            assertTrue(hostNames.contains("nagios"));
            assertTrue(hostNames.contains("exchange"));
            assertTrue(hostNames.contains("db-svr"));
            Collection<String> allHostNames = hostIdentityService.getAllHostNames();
            assertTrue(allHostNames.contains("nagios"));
            assertTrue(allHostNames.contains("exchange"));
            assertTrue(allHostNames.contains("exchange-1.0"));
            assertTrue(allHostNames.contains("exchange-1.1"));
            assertTrue(allHostNames.contains("db-svr"));
            assertTrue(allHostNames.contains(hostIdentity2UUIDHostName.toString()));
            // Host and Service read tests
            HostIdentity readHostIdentity0 = hostIdentityService.getHostIdentityByHostName("NAGIOS");
            assertNotNull(readHostIdentity0);
            assertNotNull(readHostIdentity0.getHostIdentityId());
            assertEquals("nagios", readHostIdentity0.getHostName());
            assertEquals(1, readHostIdentity0.getHostNames().size());
            assertTrue(readHostIdentity0.getHostNames().contains("nagios"));
            assertNotNull(readHostIdentity0.getHost());
            assertEquals("nagios", readHostIdentity0.getHost().getHostName());
            HostIdentity readHostIdentity1 = hostIdentityService.getHostIdentityByHostName("exchange-1.1");
            assertNotNull(readHostIdentity1);
            assertNotNull(readHostIdentity1.getHostIdentityId());
            assertEquals("exchange", readHostIdentity1.getHostName());
            assertEquals(3, readHostIdentity1.getHostNames().size());
            assertTrue(readHostIdentity1.getHostNames().contains("exchange"));
            assertTrue(readHostIdentity1.getHostNames().contains("exchange-1.0"));
            assertTrue(readHostIdentity1.getHostNames().contains("exchange-1.1"));
            assertNotNull(readHostIdentity1.getHost());
            assertEquals("exchange", readHostIdentity1.getHost().getHostName());
            HostIdentity readHostIdentity2 = hostIdentityService.getHostIdentityByHostName(hostIdentity2UUIDHostName.toString());
            assertNotNull(readHostIdentity2);
            assertEquals(hostIdentity2UUIDId, readHostIdentity2.getHostIdentityId());
            assertEquals("db-svr", readHostIdentity2.getHostName());
            assertEquals(2, readHostIdentity2.getHostNames().size());
            assertTrue(readHostIdentity2.getHostNames().contains("db-svr"));
            assertTrue(readHostIdentity2.getHostNames().contains(hostIdentity2UUIDHostName.toString()));
            assertNotNull(readHostIdentity2.getHost());
            assertEquals("db-svr", readHostIdentity2.getHost().getHostName());
            readHostIdentity2 = hostIdentityService.getHostIdentityById(hostIdentity2UUIDId);
            assertNotNull(readHostIdentity2);
            assertEquals(hostIdentity2UUIDId, readHostIdentity2.getHostIdentityId());
            Host readHost0 = hostIdentityService.getHostByIdOrHostName("nagios");
            assertNotNull(readHost0);
            assertEquals("nagios", readHost0.getHostName());
            ServiceStatus readService0 = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName("local_disk", "nagios");
            assertNotNull(readService0);
            assertEquals("nagios", readService0.getHost().getHostName());
            ServiceStatus readService1 = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName("local_disk", "NAGIOS");
            assertNotNull(readService1);
            assertEquals("nagios", readService1.getHost().getHostName());
            Host readHost1 = hostIdentityService.getHostByIdOrHostName("exchange-1.0");
            assertNotNull(readHost1);
            assertEquals("exchange", readHost1.getHostName());
            Host readHost2 = hostIdentityService.getHostByIdOrHostName(hostIdentity2UUIDId.toString());
            assertNotNull(readHost2);
            assertEquals("db-svr", readHost2.getHostName());
            readHost2 = hostIdentityService.getHostByIdOrHostName(hostIdentity2UUIDHostName.toString());
            assertNotNull(readHost2);
            assertEquals("db-svr", readHost2.getHostName());
            Host readHost3 = hostIdentityService.getHostByIdOrHostName("app-svr-tomcat");
            assertNotNull(readHost3);
            assertEquals("app-svr-tomcat", readHost3.getHostName());
            Collection<Host> hosts = hostIdentityService.getHostsByIdOrHostNames(Arrays.asList(new String[]{"nagios", "exchange-1.0", hostIdentity2UUIDHostName.toString(), "app-svr-tomcat"}));
            assertNotNull(hosts);
            assertEquals(4, hosts.size());
            hosts = hostIdentityService.getHostsByIdOrHostNames(Arrays.asList(new String[]{hostIdentity0.getHostIdentityId().toString(), hostIdentity1.getHostIdentityId().toString()}));
            assertNotNull(hosts);
            assertEquals(2, hosts.size());
            hosts = hostIdentityService.getHostsByIdOrHostNamesLookup("%-svr%");
            assertNotNull(hosts);
            assertTrue(hosts.size() >= 2);
            hosts = hostIdentityService.getHostsByIdOrHostNamesLookup(hostIdentity0.getHostIdentityId().toString());
            assertNotNull(hosts);
            assertEquals(1, hosts.size());
            // regex read tests
            List<HostIdentity> regexHostIdentities = new ArrayList<HostIdentity>();
            List<Host> regexHosts = new ArrayList<Host>();
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex(".*-svr.*", regexHosts, regexHostIdentities);
            assertEquals(1, regexHostIdentities.size());
            assertTrue(regexHosts.size() >= 2);
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex(".*-1\\.1", regexHosts, regexHostIdentities);
            assertEquals(1, regexHostIdentities.size());
            assertTrue(regexHosts.size() >= 1);
            hostIdentityService.getHostsAndHostIdentitiesByIdOrHostNamesRegex("GWRK-.*", regexHosts, regexHostIdentities);
            assertEquals(0, regexHostIdentities.size());
            assertTrue(regexHosts.size() >= 1);
            // Host delete tests
            hostIdentityService.deleteHostIdentity(hostIdentity0);
            hostIdentity0 = hostIdentityService.getHostIdentityById(hostIdentity0.getHostIdentityId());
            assertNull(hostIdentity0);
            hostIdentityService.deleteHostIdentity(hostIdentity1);
            hostIdentity1 = hostIdentityService.getHostIdentityById(hostIdentity1.getHostIdentityId());
            assertNull(hostIdentity1);
            hostIdentityService.deleteHostIdentity(hostIdentity2);
            hostIdentity2 = hostIdentityService.getHostIdentityById(hostIdentity2.getHostIdentityId());
            assertNull(hostIdentity2);
            // lookup Hosts
            nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
            exchangeHost = hostService.getHostByHostName("exchange");
            assertNotNull(exchangeHost);
            dbSvrHost = hostService.getHostByHostName("db-svr");
            assertNotNull(dbSvrHost);
        } finally {
            // cleanup test objects
            if (hostIdentity0 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity0);
            }
            if (hostIdentity1 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity1);
            }
            if (hostIdentity2 != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity2);
            }
        }
    }

    /**
     * HostIdentity service host name manipulation test method.
     */
    public void testHostIdentityServiceHostName() throws Exception {
        HostIdentity hostIdentity = null;
        try {
            // setup test Host and HostIdentity
            Host nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
            hostIdentity = hostIdentityService.createHostIdentity(nagoisHost, Arrays.asList(new String[]{"nagios-2"}));
            hostIdentityService.saveHostIdentity(hostIdentity);
            // rename HostIdentity
            HostIdentity readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios");
            assertNotNull(readHostIdentity);
            assertEquals("nagios", readHostIdentity.getHostName());
            assertEquals(2, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
            assertNotNull(readHostIdentity.getHost());
            assertEquals("nagios", readHostIdentity.getHost().getHostName());
            assertTrue(hostIdentityService.renameHostIdentity("nagios", "nagios-rename"));
            readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios-rename");
            assertNotNull(readHostIdentity);
            assertEquals("nagios-rename", readHostIdentity.getHostName());
            assertEquals(3, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios-rename"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            assertTrue(hostIdentityService.renameHostIdentity("nagios-rename", "nagios"));
            readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios");
            assertNotNull(readHostIdentity);
            assertEquals("nagios", readHostIdentity.getHostName());
            assertEquals(3, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios-rename"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            // add host name to HostIdentity
            assertTrue(hostIdentityService.addHostNameToHostIdentity("nagios", "nagios-3"));
            readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios-3");
            assertNotNull(readHostIdentity);
            assertEquals("nagios", readHostIdentity.getHostName());
            assertEquals(4, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios-rename"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-3"));
            // remove host name from HostIdentity
            assertTrue(hostIdentityService.removeHostNameFromHostIdentity("nagios-3"));
            readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios");
            assertNotNull(readHostIdentity);
            assertEquals("nagios", readHostIdentity.getHostName());
            assertEquals(3, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios-rename"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            // remove all host names from HostIdentity
            assertTrue(hostIdentityService.removeAllHostNamesFromHostIdentity("nagios"));
            readHostIdentity = hostIdentityService.getHostIdentityByHostName("nagios");
            assertNotNull(readHostIdentity);
            assertEquals("nagios", readHostIdentity.getHostName());
            assertEquals(1, readHostIdentity.getHostNames().size());
            assertTrue(readHostIdentity.getHostNames().contains("nagios"));
            // teardown test HostIdentity and Host
            boolean deleted = hostIdentityService.deleteHostIdentityByIdOrHostName("nagios");
            assertTrue(deleted);
            hostIdentity = hostIdentityService.getHostIdentityById(hostIdentity.getHostIdentityId());
            assertNull(hostIdentity);
            nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
        } finally {
            // cleanup test objects
            if (hostIdentity != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity);
            }
            // restore Host name
            Host nagoisRenameHost = hostService.getHostByHostName("nagios-rename");
            if (nagoisRenameHost != null) {
                nagoisRenameHost.setHostName("nagios");
                hostService.saveHost(nagoisRenameHost);
            }
        }
    }

    /**
     * HostIdentity service uniqueness test method.
     */
    public void testHostIdentityServiceUniqueness() throws Exception {
        HostIdentity hostIdentity = null;
        try {
            // create unique test HostIdentity
            hostIdentity = hostIdentityService.createHostIdentity("test", Arrays.asList(new String[]{"test-2"}));
            hostIdentityService.saveHostIdentity(hostIdentity);
            // disable hibernate log4j logging
            disableHibernateLogging();
            // test host name case insensitive uniqueness
            HostIdentity duplicateHostIdentity = hostIdentityService.createHostIdentity("test");
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host name failure");
            } catch (DataIntegrityViolationException dive) {
            }
            duplicateHostIdentity = hostIdentityService.createHostIdentity("TEST");
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host name failure");
            } catch (DataIntegrityViolationException dive) {
            }
            // test host names case insensitive uniqueness
            duplicateHostIdentity = hostIdentityService.createHostIdentity("test-duplicate", Arrays.asList(new String[]{"test-2"}));
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host names failure");
            } catch (DataIntegrityViolationException dive) {
            }
            duplicateHostIdentity = hostIdentityService.createHostIdentity("test-duplicate", Arrays.asList(new String[]{"TEST-2"}));
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host names failure");
            } catch (DataIntegrityViolationException dive) {
            }
            duplicateHostIdentity = hostIdentityService.createHostIdentity("test-duplicate", Arrays.asList(new String[]{"test"}));
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host names failure");
            } catch (DataIntegrityViolationException dive) {
            }
            duplicateHostIdentity = hostIdentityService.createHostIdentity("test-duplicate", Arrays.asList(new String[]{"TEST"}));
            try {
                hostIdentityService.saveHostIdentity(duplicateHostIdentity);
                fail("Expected duplicate host names failure");
            } catch (DataIntegrityViolationException dive) {
            }
            // enable hibernate log4j logging
            reenableHibernateLogging();
            // remove test HostIdentity
            boolean deleted = hostIdentityService.deleteHostIdentityByIdOrHostName(hostIdentity.getHostIdentityId().toString());
            assertTrue(deleted);
            hostIdentity = hostIdentityService.getHostIdentityById(hostIdentity.getHostIdentityId());
            assertNull(hostIdentity);
        } finally {
            // cleanup test objects
            if (hostIdentity != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity);
            }
        }
    }

    /**
     * HostIdentity service query test method.
     */
    public void testHostIdentityServiceQuery() throws Exception {
        HostIdentity hostIdentity = null;
        try {
            // setup test Host and HostIdentity
            Host nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
            hostIdentity = hostIdentityService.createHostIdentity(nagoisHost, Arrays.asList(new String[]{"nagios-2"}));
            hostIdentityService.saveHostIdentity(hostIdentity);
            // test queries for HostIdentity
            FilterCriteria hostNameFilterCriteria = FilterCriteria.ieq(HostIdentity.HP_HOST_NAME, "NAGIOS");
            FoundationQueryList results = hostIdentityService.getHostIdentities(hostNameFilterCriteria, null, 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertNagiosHostIdentity((HostIdentity)results.get(0));
            FilterCriteria hostIdFilterCriteria = FilterCriteria.eq(HostIdentity.HP_HOST_ID, nagoisHost.getHostId());
            results = hostIdentityService.getHostIdentities(hostIdFilterCriteria, null, 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertNagiosHostIdentity((HostIdentity)results.get(0));
            results = hostIdentityService.queryHostIdentities("from HostIdentity where "+HostIdentity.HP_HOST_NAME+" = 'nagios'", "select count(*) from HostIdentity", 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertNagiosHostIdentity((HostIdentity)results.get(0));
            // teardown test HostIdentity and Host
            boolean deleted = hostIdentityService.deleteHostIdentityByIdOrHostName(hostIdentity.getHostName());
            assertTrue(deleted);
            hostIdentity = hostIdentityService.getHostIdentityById(hostIdentity.getHostIdentityId());
            assertNull(hostIdentity);
            nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
        } finally {
            // cleanup test objects
            if (hostIdentity != null) {
                hostIdentityService.deleteHostIdentity(hostIdentity);
            }
        }
    }

    private void assertNagiosHostIdentity(HostIdentity readHostIdentity) {
        assertNotNull(readHostIdentity);
        assertEquals("nagios", readHostIdentity.getHostName());
        assertEquals(2, readHostIdentity.getHostNames().size());
        assertTrue(readHostIdentity.getHostNames().contains("nagios"));
        assertTrue(readHostIdentity.getHostNames().contains("nagios-2"));
        assertNotNull(readHostIdentity.getHost());
        assertEquals("nagios", readHostIdentity.getHost().getHostName());
    }

    public void testAutocomplete() throws Exception {
        // wait for initial load
        Thread.sleep(250);
        // test autocomplete names
        List<AutocompleteName> names = hostIdentityAutocompleteService.autocomplete("nagios");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("nagios", names.get(0).getName());
        assertEquals("nagios", names.get(0).getCanonicalName());
        names = hostIdentityAutocompleteService.autocomplete("zzz");
        assertNotNull(names);
        assertTrue(names.isEmpty());
        // create host and host identity
        try {
            Host nagoisHost = hostService.getHostByHostName("nagios");
            assertNotNull(nagoisHost);
            HostIdentity hostIdentity = hostIdentityService.createHostIdentity(nagoisHost, Arrays.asList(new String[]{"nagios-2"}));
            hostIdentityService.saveHostIdentity(hostIdentity);
            Device device = deviceService.getDeviceById(TestHostService.DEVICE_ID);
            Host host = hostService.createHost("ZZZZZZ", device);
            hostService.saveHost(host);
            // wait for refresh and validate names
            Thread.sleep(250);
            names = hostIdentityAutocompleteService.autocomplete("nagios");
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals("nagios", names.get(0).getName());
            assertEquals("nagios", names.get(0).getCanonicalName());
            assertEquals("nagios-2", names.get(1).getName());
            assertEquals("nagios", names.get(1).getCanonicalName());
            names = hostIdentityAutocompleteService.autocomplete("N");
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals("nagios", names.get(0).getName());
            assertEquals("nagios", names.get(0).getCanonicalName());
            assertEquals("nagios-2", names.get(1).getName());
            assertEquals("nagios", names.get(1).getCanonicalName());
            names = hostIdentityAutocompleteService.autocomplete("zzz");
            assertNotNull(names);
            assertEquals(1, names.size());
            assertEquals("ZZZZZZ", names.get(0).getName());
            assertEquals("ZZZZZZ", names.get(0).getCanonicalName());
        } finally {
            // cleanup test objects
            hostIdentityService.deleteHostIdentityByIdOrHostName("nagios");
            hostService.deleteHostByName("ZZZZZZ");
        }
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostIdentityAutocompleteService.autocomplete("nagios");
        assertEquals(1, names.size());
        assertEquals("nagios", names.get(0).getName());
        assertEquals("nagios", names.get(0).getCanonicalName());
        names = hostIdentityAutocompleteService.autocomplete("zzz");
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }
}
