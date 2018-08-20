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

import com.groundwork.collage.model.AuditLog;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.auditlog.AuditLogService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.ArrayList;
import java.util.List;

/**
 * TestAuditLogService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestAuditLogService extends AbstractTestCaseWithTransactionSupport {

    /** AuditLog service */
    private AuditLogService auditLogService;

    /**
     * Test constructor.
     *
     * @param test test to execute
     */
    public TestAuditLogService(String test) {
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
        TestSuite suite = new TestSuite(TestAuditLogService.class);

        // or a subset thereof
        //TestSuite suite = new TestSuite();
        //suite.addTest(new TestMonitorServerService("testAuditLogService"));

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

        // initialize service
        auditLogService = collage.getAuditLogService();
        assertNotNull(auditLogService);
    }

    /**
     * AuditLog service test method.
     */
    public void testAuditLogService() throws Exception {
        // create, save, and read AuditLog instances
        AuditLog auditLog = auditLogService.createAuditLog();
        assertNotNull(auditLog);
        auditLog.setSubsystem("Console");
        auditLog.setAction(AuditLog.Action.MODIFY);
        auditLog.setDescription("Test modification server 1.");
        auditLog.setUsername("admin");
        auditLog.setHostName("server_1");
        auditLogService.saveAuditLog(auditLog);
        assertNotNull(auditLog.getAuditLogId());
        assertNotNull(auditLog.getTimestamp());
        assertNotNull(auditLogService.getAuditLogById(auditLog.getAuditLogId()));
        Thread.sleep(500);
        List<AuditLog> auditLogs = new ArrayList<AuditLog>();
        auditLogs.add(auditLogService.createAuditLog());
        auditLogs.get(0).setSubsystem("SV");
        auditLogs.get(0).setAction(AuditLog.Action.ADD);
        auditLogs.get(0).setDescription("Test addition server 2.");
        auditLogs.get(0).setUsername("admin");
        auditLogs.get(0).setHostName("server_2");
        auditLogs.get(0).setServiceDescription("local_processes");
        Thread.sleep(500);
        auditLogs.add(auditLogService.createAuditLog());
        auditLogs.get(1).setSubsystem("SV");
        auditLogs.get(1).setAction(AuditLog.Action.DELETE);
        auditLogs.get(1).setDescription("Test deletion server 2.");
        auditLogs.get(1).setUsername("admin");
        auditLogs.get(1).setHostName("server_2");
        auditLogs.get(1).setServiceDescription("network_users");
        auditLogService.saveAuditLogs(auditLogs);
        assertNotNull(auditLogs.get(0).getAuditLogId());
        assertNotNull(auditLogs.get(0).getTimestamp());
        assertTrue(auditLogs.get(0).getTimestamp().after(auditLog.getTimestamp()));
        assertNotNull(auditLogService.getAuditLogById(auditLogs.get(0).getAuditLogId()));
        assertNotNull(auditLogs.get(1).getAuditLogId());
        assertNotNull(auditLogs.get(1).getTimestamp());
        assertTrue(auditLogs.get(1).getTimestamp().after(auditLogs.get(0).getTimestamp()));
        assertNotNull(auditLogService.getAuditLogById(auditLogs.get(1).getAuditLogId()));

        // mutating operations throw exceptions
        auditLog.setSubsystem("CloudHub");
        try {
            auditLogService.saveAuditLog(auditLog);
            fail("Update exception expected");
        } catch (BusinessServiceException bse) {
            assertTrue(bse.getMessage().contains("cannot") && bse.getMessage().contains("update"));
        }
        auditLogs.get(0).setSubsystem("CloudHub");
        try {
            auditLogService.saveAuditLogs(auditLogs);
            fail("Update exception expected");
        } catch (BusinessServiceException bse) {
            assertTrue(bse.getMessage().contains("cannot") && bse.getMessage().contains("update"));
        }

        Thread.sleep(100);
        auditLog = auditLogService.createAuditLog();
        assertNotNull(auditLog);
        auditLog.setSubsystem("SV");
        auditLog.setAction(AuditLog.Action.ACTION);
        auditLog.setDescription("Test action host group.");
        auditLog.setUsername("admin");
        auditLog.setHostGroupName("host_group");
        auditLogService.saveAuditLog(auditLog);
        assertNotNull(auditLog.getAuditLogId());
        assertNotNull(auditLog.getTimestamp());
        assertNotNull(auditLogService.getAuditLogById(auditLog.getAuditLogId()));
        Thread.sleep(100);
        auditLog = auditLogService.createAuditLog();
        assertNotNull(auditLog);
        auditLog.setSubsystem("CONSOLE");
        auditLog.setAction(AuditLog.Action.ACTION);
        auditLog.setDescription("Test action service group.");
        auditLog.setUsername("admin");
        auditLog.setServiceGroupName("service_group");
        auditLogService.saveAuditLog(auditLog);
        assertNotNull(auditLog.getAuditLogId());
        assertNotNull(auditLog.getTimestamp());
        assertNotNull(auditLogService.getAuditLogById(auditLog.getAuditLogId()));
        Thread.sleep(100);

        // query AuditLog instances
        FoundationQueryList results = auditLogService.getAuditLogs(null, null, 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(5, results.getTotalCount());
        assertEquals(5, results.size());
        assertEquals("Test action service group.", ((AuditLog)results.get(0)).getDescription());
        assertEquals("Test action host group.", ((AuditLog)results.get(1)).getDescription());
        assertEquals("Test deletion server 2.", ((AuditLog)results.get(2)).getDescription());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(3)).getDescription());
        assertEquals("Test modification server 1.", ((AuditLog)results.get(4)).getDescription());
        results = auditLogService.getAuditLogs(null, null, 1, 1);
        assertNotNull(results);
        assertEquals(5, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test action host group.", ((AuditLog)results.get(0)).getDescription());
        results = auditLogService.queryAuditLogs("SELECT A FROM AuditLog A ORDER BY timestamp DESC, auditLogId DESC", "SELECT count(*) FROM AuditLog", 0, Integer.MAX_VALUE);
        assertEquals(5, results.getTotalCount());
        assertEquals(5, results.size());
        assertEquals("Test action service group.", ((AuditLog)results.get(0)).getDescription());
        assertEquals("Test action host group.", ((AuditLog)results.get(1)).getDescription());
        assertEquals("Test deletion server 2.", ((AuditLog)results.get(2)).getDescription());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(3)).getDescription());
        assertEquals("Test modification server 1.", ((AuditLog)results.get(4)).getDescription());
        results = auditLogService.queryAuditLogs("FROM AuditLog ORDER BY timestamp DESC, auditLogId DESC", "SELECT count(*) FROM AuditLog", 3, 2);
        assertEquals(5, results.getTotalCount());
        assertEquals(2, results.size());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(0)).getDescription());
        assertEquals("Test modification server 1.", ((AuditLog)results.get(1)).getDescription());
        results = auditLogService.queryAuditLogs("FROM AuditLog WHERE hostName = 'server_2' ORDER BY timestamp DESC, auditLogId DESC", "SELECT count(*) FROM AuditLog WHERE hostName = 'server_2'", 0, Integer.MAX_VALUE);
        assertEquals(2, results.getTotalCount());
        assertEquals(2, results.size());
        assertEquals("Test deletion server 2.", ((AuditLog)results.get(0)).getDescription());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(1)).getDescription());
        results = auditLogService.getHostAuditLogs(null, 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(1, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test modification server 1.", ((AuditLog)results.get(0)).getDescription());
        results = auditLogService.getHostAuditLogs("server_1", 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(1, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test modification server 1.", ((AuditLog)results.get(0)).getDescription());
        results = auditLogService.getServiceAuditLogs("server_2", null, 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(2, results.getTotalCount());
        assertEquals(2, results.size());
        assertEquals("Test deletion server 2.", ((AuditLog)results.get(0)).getDescription());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(1)).getDescription());
        results = auditLogService.getServiceAuditLogs("server_2", "local_processes", 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(1, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test addition server 2.", ((AuditLog)results.get(0)).getDescription());
        results = auditLogService.getHostGroupAuditLogs("host_group", 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(1, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test action host group.", ((AuditLog)results.get(0)).getDescription());
        results = auditLogService.getServiceGroupAuditLogs("service_group", 0, Integer.MAX_VALUE);
        assertNotNull(results);
        assertEquals(1, results.getTotalCount());
        assertEquals(1, results.size());
        assertEquals("Test action service group.", ((AuditLog)results.get(0)).getDescription());
    }
}
