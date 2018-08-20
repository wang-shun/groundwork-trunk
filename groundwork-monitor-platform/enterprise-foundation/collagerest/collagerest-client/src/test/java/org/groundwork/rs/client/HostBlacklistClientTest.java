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

import org.groundwork.rs.dto.DtoHostBlacklist;
import org.groundwork.rs.dto.DtoHostBlacklistList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * HostBlacklistClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostBlacklistClientTest extends AbstractClientTest {

    @Test
    public void testHostBlacklistClient() throws Exception {
        if (serverDown) return;
        HostBlacklistClient client = new HostBlacklistClient(getDeploymentURL());

        // test client using XML
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        doTestHostBlacklistClient(client);

        // test client using JSON
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        doTestHostBlacklistClient(client);
    }

    private void doTestHostBlacklistClient(HostBlacklistClient client) throws Exception {
        String hostBlacklistHostName0 = null;
        String hostBlacklistHostName1 = null;
        String hostBlacklistHostName2 = null;
        try {
            // post client HostBlacklists
            DtoHostBlacklistList hostBlacklists = new DtoHostBlacklistList();
            hostBlacklists.add(new DtoHostBlacklist("test-host-name-0"));
            hostBlacklists.add(new DtoHostBlacklist("test-host-name-1"));
            hostBlacklists.add(new DtoHostBlacklist("test-host-name-2"));
            DtoOperationResults insertResults = client.post(hostBlacklists);
            assertNotNull(insertResults);
            assertEquals("HostBlacklist", insertResults.getEntityType());
            assertEquals("Insert", insertResults.getOperation());
            assertNotNull(insertResults.getSuccessful());
            assertEquals(3, insertResults.getSuccessful().intValue());
            assertNotNull(insertResults.getResults());
            assertEquals(3, insertResults.getResults().size());
            hostBlacklistHostName0 = "test-host-name-0";
            hostBlacklistHostName1 = "test-host-name-1";
            hostBlacklistHostName2 = "test-host-name-2";

            // lookup HostBlacklists
            DtoHostBlacklist readHostBlacklist0 = client.lookup("test-host-name-0");
            assertNotNull(readHostBlacklist0);
            assertNotNull(readHostBlacklist0.getHostBlacklistId());
            assertEquals("test-host-name-0", readHostBlacklist0.getHostName());
            DtoHostBlacklist readHostBlacklist1 = client.lookup("test-host-name-1");
            assertNotNull(readHostBlacklist1);
            assertNotNull(readHostBlacklist1.getHostBlacklistId());
            assertEquals("test-host-name-1", readHostBlacklist1.getHostName());
            DtoHostBlacklist readHostBlacklist2 = client.lookup("test-host-name-2");
            assertNotNull(readHostBlacklist2);
            assertNotNull(readHostBlacklist2.getHostBlacklistId());
            assertEquals("test-host-name-2", readHostBlacklist2.getHostName());

            // query HostBlacklists
            List<DtoHostBlacklist> listResults = client.list(0, 2);
            assertNotNull(listResults);
            assertEquals(2, listResults.size());
            List<DtoHostBlacklist> listAllResults = client.list();
            assertNotNull(listAllResults);
            assertTrue(listAllResults.size() >= 3);
            int testsFound = 0;
            for (DtoHostBlacklist hostBlacklist : listAllResults) {
                if (hostBlacklist.getHostName().startsWith("test-host-name-")) {
                    testsFound++;
                }
            }
            assertEquals(3, testsFound);
            List<DtoHostBlacklist> queryResults = client.query("hostName like 'test-host-name-%' ORDER BY hostName");
            assertNotNull(queryResults);
            assertEquals(3, queryResults.size());
            List<DtoHostBlacklist> queryPageResults = client.query("hostName like 'test-host-name-%' ORDER BY hostName", 1, 1);
            assertNotNull(queryPageResults);
            assertEquals(1, queryPageResults.size());
            assertEquals("test-host-name-1", queryPageResults.get(0).getHostName());

            // match HostBlacklists
            assertTrue(client.matchHostNameAgainstHostNames("TEST-HOST-NAME-1"));
            assertFalse(client.matchHostNameAgainstHostNames("some-other-host-name"));

            // update HostBlacklists
            DtoHostBlacklistList updateHostBlacklists = new DtoHostBlacklistList();
            DtoHostBlacklist updateHostBlacklist1 = new DtoHostBlacklist();
            updateHostBlacklist1.setHostBlacklistId(readHostBlacklist1.getHostBlacklistId());
            updateHostBlacklist1.setHostName("test-host-name-1-changed");
            updateHostBlacklists.add(updateHostBlacklist1);
            DtoOperationResults updateResults = client.post(updateHostBlacklists);
            assertNotNull(updateResults);
            assertEquals("HostBlacklist", updateResults.getEntityType());
            assertEquals("Update", updateResults.getOperation());
            assertNotNull(updateResults.getSuccessful());
            assertEquals(1, updateResults.getSuccessful().intValue());
            readHostBlacklist1 = client.lookup("test-host-name-1-changed");
            assertNotNull(readHostBlacklist1);
            assertNotNull(readHostBlacklist1.getHostBlacklistId());
            assertEquals("test-host-name-1-changed", readHostBlacklist1.getHostName());

            // delete HostBlacklists
            DtoOperationResults deleteResults = client.delete(readHostBlacklist0.getHostName());
            assertNotNull(deleteResults);
            assertEquals("HostBlacklist", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(1, deleteResults.getSuccessful().intValue());
            readHostBlacklist0 = client.lookup(readHostBlacklist0.getHostName());
            assertNull(readHostBlacklist0);
            hostBlacklistHostName0 = null;
            DtoHostBlacklistList deleteHostBlacklists = new DtoHostBlacklistList();
            deleteHostBlacklists.add(readHostBlacklist1);
            deleteHostBlacklists.add(readHostBlacklist2);
            deleteResults = client.delete(deleteHostBlacklists);
            assertNotNull(deleteResults);
            assertEquals("HostBlacklist", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(2, deleteResults.getSuccessful().intValue());
            readHostBlacklist1 = client.lookup(readHostBlacklist1.getHostName());
            assertNull(readHostBlacklist1);
            readHostBlacklist2 = client.lookup(readHostBlacklist2.getHostName());
            assertNull(readHostBlacklist2);
            hostBlacklistHostName1 = null;
            hostBlacklistHostName2 = null;

            // test warning for missing delete
            deleteResults = client.delete(Arrays.asList(new String[]{"NotAHostBlacklist"}));
            assertNotNull(deleteResults);
            assertEquals(new Integer(1), deleteResults.getWarning());
        } finally {
            // cleanup test
            if (hostBlacklistHostName0 != null) {
                client.delete(hostBlacklistHostName0);
            }
            if (hostBlacklistHostName1 != null) {
                client.delete(hostBlacklistHostName1);
            }
            if (hostBlacklistHostName2 != null) {
                client.delete(hostBlacklistHostName2);
            }
        }
    }
}
