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

import com.groundwork.collage.model.HostBlacklist;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.hostblacklist.HostBlacklistService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collection;

/**
 * TestHostBlacklistService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestHostBlacklistService extends AbstractTestCaseWithTransactionSupport {

    /** HostBlacklist service */
    private HostBlacklistService hostBlacklistService;

    /**
     * Test constructor.
     *
     * @param test test to execute
     */
    public TestHostBlacklistService(String test) {
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
        TestSuite suite = new TestSuite(TestHostBlacklistService.class);

        // or a subset thereof
        //TestSuite suite = new TestSuite();
        //suite.addTest(new TestMonitorServerService("testHostBlacklistServiceCRUD"));
        //suite.addTest(new TestMonitorServerService("testHostBlacklistServiceUniqueness"));
        //suite.addTest(new TestMonitorServerService("testHostBlacklistServiceQuery"));

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
        hostBlacklistService = collage.getHostBlacklistService();
        assertNotNull(hostBlacklistService);
    }

    /**
     * HostBlacklist service CRUD test method.
     */
    public void testHostBlacklistServiceCRUD() throws Exception {
        HostBlacklist hostBlacklist0 = null;
        HostBlacklist hostBlacklist1 = null;
        HostBlacklist hostBlacklist2 = null;
        try {
            // basic create tests
            hostBlacklist0 = hostBlacklistService.createHostBlacklist("test-host-name-0");
            hostBlacklist1 = hostBlacklistService.createHostBlacklist("test-host-name-1");
            hostBlacklist2 = hostBlacklistService.createHostBlacklist("test-host-name-2");
            // basic save tests
            hostBlacklistService.saveHostBlacklist(hostBlacklist0);
            hostBlacklistService.saveHostBlacklists(Arrays.asList(new HostBlacklist[]{hostBlacklist1, hostBlacklist2}));
            // basic read host names tests
            Collection<String> hostNames = hostBlacklistService.getHostNames();
            assertTrue(hostNames.contains("test-host-name-0"));
            assertTrue(hostNames.contains("test-host-name-1"));
            assertTrue(hostNames.contains("test-host-name-2"));
            // basic read tests
            HostBlacklist readHostBlacklist0 = hostBlacklistService.getHostBlacklistByHostName("test-host-name-0");
            assertNotNull(readHostBlacklist0);
            assertNotNull(readHostBlacklist0.getHostBlacklistId());
            assertEquals("test-host-name-0", readHostBlacklist0.getHostName());
            HostBlacklist readHostBlacklist1 = hostBlacklistService.getHostBlacklistByHostName("test-host-name-1");
            assertNotNull(readHostBlacklist1);
            assertNotNull(readHostBlacklist1.getHostBlacklistId());
            assertEquals("test-host-name-1", readHostBlacklist1.getHostName());
            HostBlacklist readHostBlacklist2 = hostBlacklistService.getHostBlacklistByHostName("test-host-name-2");
            assertNotNull(readHostBlacklist2);
            assertNotNull(readHostBlacklist2.getHostBlacklistId());
            assertEquals("test-host-name-2", readHostBlacklist2.getHostName());
            readHostBlacklist0 = hostBlacklistService.getHostBlacklistById(readHostBlacklist0.getHostBlacklistId());
            assertNotNull(readHostBlacklist0);
            Collection<HostBlacklist> hostBlacklists = hostBlacklistService.getHostBlacklistsByHostNames(Arrays.asList(new String[]{"test-host-name-0", "test-host-name-1", "test-host-name-2"}));
            assertNotNull(hostBlacklists);
            assertEquals(3, hostBlacklists.size());
            // basic delete tests
            hostBlacklistService.deleteHostBlacklistById(hostBlacklist0.getHostBlacklistId());
            hostBlacklist0 = hostBlacklistService.getHostBlacklistById(hostBlacklist0.getHostBlacklistId());
            assertNull(hostBlacklist0);
            hostBlacklistService.deleteHostBlacklists(Arrays.asList(new HostBlacklist[]{hostBlacklist1, hostBlacklist2}));
            hostBlacklist1 = hostBlacklistService.getHostBlacklistById(hostBlacklist1.getHostBlacklistId());
            assertNull(hostBlacklist1);
            hostBlacklist2 = hostBlacklistService.getHostBlacklistById(hostBlacklist2.getHostBlacklistId());
            assertNull(hostBlacklist2);
        } finally {
            // cleanup test objects
            if (hostBlacklist0 != null) {
                hostBlacklistService.deleteHostBlacklist(hostBlacklist0);
            }
            if (hostBlacklist1 != null) {
                hostBlacklistService.deleteHostBlacklist(hostBlacklist1);
            }
            if (hostBlacklist2 != null) {
                hostBlacklistService.deleteHostBlacklist(hostBlacklist2);
            }
        }
    }

    /**
     * HostBlacklist service uniqueness test method.
     */
    public void testHostBlacklistServiceUniqueness() throws Exception {
        HostBlacklist hostBlacklist = null;
        try {
            // create unique test HostBlacklist
            hostBlacklist = hostBlacklistService.createHostBlacklist("test");
            hostBlacklistService.saveHostBlacklist(hostBlacklist);
            // disable hibernate log4j logging
            disableHibernateLogging();
            // test host name uniqueness
            HostBlacklist duplicateHostBlacklist = hostBlacklistService.createHostBlacklist("test");
            try {
                hostBlacklistService.saveHostBlacklist(duplicateHostBlacklist);
                fail("Expected duplicate host name failure");
            } catch (DataIntegrityViolationException dive) {
            }
            // enable hibernate log4j logging
            reenableHibernateLogging();
            // remove test HostBlacklist
            hostBlacklistService.deleteHostBlacklistById(hostBlacklist.getHostBlacklistId());
            hostBlacklist = hostBlacklistService.getHostBlacklistById(hostBlacklist.getHostBlacklistId());
            assertNull(hostBlacklist);
        } finally {
            // cleanup test objects
            if (hostBlacklist != null) {
                hostBlacklistService.deleteHostBlacklist(hostBlacklist);
            }
        }
    }

    /**
     * HostBlacklist service query test method.
     */
    public void testHostBlacklistServiceQuery() throws Exception {
        HostBlacklist hostBlacklist = null;
        try {
            // setup test HostBlacklist
            hostBlacklist = hostBlacklistService.createHostBlacklist("test");
            hostBlacklistService.saveHostBlacklist(hostBlacklist);
            // test queries for HostBlacklist
            FilterCriteria hostNameFilterCriteria = FilterCriteria.ieq(HostBlacklist.HP_HOST_NAME, "test");
            FoundationQueryList results = hostBlacklistService.getHostBlacklists(hostNameFilterCriteria, null, 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertTestHostBlacklist((HostBlacklist)results.get(0));
            results = hostBlacklistService.queryHostBlacklists("from HostBlacklist where "+HostBlacklist.HP_HOST_NAME+" = 'test'", "select count(*) from HostBlacklist", 0, 1);
            assertNotNull(results);
            assertEquals(1, results.getTotalCount());
            assertEquals(1, results.size());
            assertTestHostBlacklist((HostBlacklist)results.get(0));
            // match test
            assertTrue(hostBlacklistService.matchHostNameAgainstHostNames("TEST"));
            assertFalse(hostBlacklistService.matchHostNameAgainstHostNames("some-other-host-name"));
            // teardown test HostBlacklist
            boolean deleted = hostBlacklistService.deleteHostBlacklistByHostName(hostBlacklist.getHostName());
            assertTrue(deleted);
            hostBlacklist = hostBlacklistService.getHostBlacklistById(hostBlacklist.getHostBlacklistId());
            assertNull(hostBlacklist);
        } finally {
            // cleanup test objects
            if (hostBlacklist != null) {
                hostBlacklistService.deleteHostBlacklist(hostBlacklist);
            }
        }
    }

    private void assertTestHostBlacklist(HostBlacklist readHostBlacklist) {
        assertNotNull(readHostBlacklist);
        assertEquals("test", readHostBlacklist.getHostName());
    }
}
