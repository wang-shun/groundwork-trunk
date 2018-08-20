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

package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoAuditLog;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AuditLogClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogClientTest extends AbstractClientTest {

    @Test
    public void testAuditLogClient() throws Exception {
        if (serverDown) return;
        AuditLogClient client = new AuditLogClient(getDeploymentURL());

        // test client using XML
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        doTestAuditLogClient(client);

        // test client using JSON
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        doTestAuditLogClient(client);
    }

    private void doTestAuditLogClient(AuditLogClient client) throws Exception {
        // asynchronously post client AuditLogs
        DtoAuditLogList asyncAuditLogs = new DtoAuditLogList();
        asyncAuditLogs.add(new DtoAuditLog("Console", "MODIFY", "Test modification server 1.", "admin", "server_1"));
        DtoOperationResults asyncResults = client.post(asyncAuditLogs);
        assertNotNull(asyncResults);
        assertEquals("AuditLog Async", asyncResults.getEntityType());
        assertEquals("Insert", asyncResults.getOperation());
        assertNotNull(asyncResults.getSuccessful());
        assertEquals(1, asyncResults.getSuccessful().intValue());
        assertNotNull(asyncResults.getResults());
        assertEquals(1, asyncResults.getResults().size());
        assertNotNull(asyncResults.getResults().get(0).getMessage());
        assertTrue(asyncResults.getResults().get(0).getMessage().startsWith("Job "));
        assertTrue(asyncResults.getResults().get(0).getMessage().endsWith(" submitted"));
        Thread.sleep(500);

        // synchronously post client AuditLogs
        DtoAuditLogList syncAuditLogs = new DtoAuditLogList();
        syncAuditLogs.add(new DtoAuditLog("SV", "ADD", "Test addition server 2.", "admin", "server_2", "local_processes"));
        syncAuditLogs.add(new DtoAuditLog("SV", "DELETE", "Test deletion server 2.", "admin", "server_2", "network_users"));
        DtoOperationResults syncResults = client.post(syncAuditLogs, false);
        assertNotNull(syncResults);
        assertEquals("AuditLog", syncResults.getEntityType());
        assertEquals("Insert", syncResults.getOperation());
        assertNotNull(syncResults.getSuccessful());
        assertEquals(2, syncResults.getSuccessful().intValue());
        syncAuditLogs = new DtoAuditLogList();
        DtoAuditLog dtoAuditLog = new DtoAuditLog("CloudHub", "ACTION", "Test action host group.", "admin");
        dtoAuditLog.setHostGroupName("host_group");
        syncAuditLogs.add(dtoAuditLog);
        dtoAuditLog = new DtoAuditLog("CloudHub", "ACTION", "Test action service group.", "admin");
        dtoAuditLog.setServiceGroupName("service_group");
        syncAuditLogs.add(dtoAuditLog);
        syncResults = client.post(syncAuditLogs, false);
        assertNotNull(syncResults);
        assertEquals("AuditLog", syncResults.getEntityType());
        assertEquals("Insert", syncResults.getOperation());
        assertNotNull(syncResults.getSuccessful());
        assertEquals(2, syncResults.getSuccessful().intValue());

        // list AuditLogs
        List<DtoAuditLog> auditLogs = client.list(0, 5);
        assertNotNull(auditLogs);
        assertEquals(5, auditLogs.size());
        assertEquals("Test action service group.", auditLogs.get(0).getDescription());
        assertEquals("Test action host group.", auditLogs.get(1).getDescription());
        assertEquals("Test deletion server 2.", auditLogs.get(2).getDescription());
        assertEquals("Test addition server 2.", auditLogs.get(3).getDescription());
        assertEquals("Test modification server 1.", auditLogs.get(4).getDescription());
        auditLogs = client.list(3, 1);
        assertNotNull(auditLogs);
        assertEquals(1, auditLogs.size());
        assertEquals("Test addition server 2.", auditLogs.get(0).getDescription());

        // query AuditLogs
        auditLogs = client.query("ORDER BY timestamp DESC, auditLogId DESC", 0, 5);
        assertNotNull(auditLogs);
        assertEquals(5, auditLogs.size());
        assertEquals("Test action service group.", auditLogs.get(0).getDescription());
        assertEquals("Test action host group.", auditLogs.get(1).getDescription());
        assertEquals("Test deletion server 2.", auditLogs.get(2).getDescription());
        assertEquals("Test addition server 2.", auditLogs.get(3).getDescription());
        assertEquals("Test modification server 1.", auditLogs.get(4).getDescription());
        auditLogs = client.query("ORDER BY timestamp DESC, auditLogId DESC", 3, 2);
        assertNotNull(auditLogs);
        assertEquals(2, auditLogs.size());
        assertEquals("Test addition server 2.", auditLogs.get(0).getDescription());
        assertEquals("Test modification server 1.", auditLogs.get(1).getDescription());
        auditLogs = client.query("hostName = 'server_2' ORDER BY timestamp DESC, auditLogId DESC", 0, 2);
        assertNotNull(auditLogs);
        assertEquals(2, auditLogs.size());
        assertEquals("Test deletion server 2.", auditLogs.get(0).getDescription());
        assertEquals("Test addition server 2.", auditLogs.get(1).getDescription());
        auditLogs = client.query("hostGroupName = 'host_group' ORDER BY timestamp DESC, auditLogId DESC", 0, 1);
        assertNotNull(auditLogs);
        assertEquals(1, auditLogs.size());
        assertEquals("Test action host group.", auditLogs.get(0).getDescription());
        auditLogs = client.query("serviceGroupName = 'service_group' ORDER BY timestamp DESC, auditLogId DESC", 0, 1);
        assertNotNull(auditLogs);
        assertEquals(1, auditLogs.size());
        assertEquals("Test action service group.", auditLogs.get(0).getDescription());

        // list host and service AuditLogs
        auditLogs = client.list("server_1", 0, 1);
        assertNotNull(auditLogs);
        assertEquals(1, auditLogs.size());
        assertEquals("Test modification server 1.", auditLogs.get(0).getDescription());
        auditLogs = client.list("server_2", "local_processes", 0, 1);
        assertNotNull(auditLogs);
        assertEquals(1, auditLogs.size());
        assertEquals("Test addition server 2.", auditLogs.get(0).getDescription());
    }
}
