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

import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * HistIdentityClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostIdentityClientTest extends AbstractClientTest {

    @Test
    public void testHostIdentityClient() throws Exception {
        if (serverDown) return;
        HostIdentityClient client = new HostIdentityClient(getDeploymentURL());

        // test client using XML
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        doTestHostIdentityClient(client);

        // test client using JSON
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        doTestHostIdentityClient(client);
    }

    private void doTestHostIdentityClient(HostIdentityClient client) throws Exception {
        String hostIdentityIdOrHostName0 = null;
        String hostIdentityIdOrHostName1 = null;
        String hostIdentityIdOrHostName2 = null;
        try {
            // asynchronously post client HostIdentities
            DtoHostIdentityList asyncHostIdentities = new DtoHostIdentityList();
            asyncHostIdentities.add(new DtoHostIdentity("test-host-name-0"));
            DtoOperationResults asyncResults = client.post(asyncHostIdentities, true);
            assertNotNull(asyncResults);
            assertEquals("HostIdentity Async", asyncResults.getEntityType());
            assertEquals("Insert", asyncResults.getOperation());
            assertNotNull(asyncResults.getSuccessful());
            assertEquals(1, asyncResults.getSuccessful().intValue());
            assertNotNull(asyncResults.getResults());
            assertEquals(1, asyncResults.getResults().size());
            assertNotNull(asyncResults.getResults().get(0).getMessage());
            assertTrue(asyncResults.getResults().get(0).getMessage().startsWith("Job "));
            assertTrue(asyncResults.getResults().get(0).getMessage().endsWith(" submitted"));
            hostIdentityIdOrHostName0 = "test-host-name-0";
            Thread.sleep(500);

            // synchronously post client HostIdentities
            DtoHostIdentityList syncHostIdentities = new DtoHostIdentityList();
            syncHostIdentities.add(new DtoHostIdentity("test-host-name-1", Arrays.asList(new String[]{"test-host-name-1.0", "test-host-name-1.1"})));
            UUID hostIdentity2UUIDId = UUID.randomUUID();
            UUID hostIdentity2UUIDHostName = UUID.randomUUID();
            syncHostIdentities.add(new DtoHostIdentity(hostIdentity2UUIDId, "test-host-name-2", Arrays.asList(new String[]{hostIdentity2UUIDHostName.toString()})));
            DtoOperationResults syncResults = client.post(syncHostIdentities);
            assertNotNull(syncResults);
            assertEquals("HostIdentity", syncResults.getEntityType());
            assertEquals("Insert", syncResults.getOperation());
            assertNotNull(syncResults.getSuccessful());
            assertEquals(2, syncResults.getSuccessful().intValue());
            hostIdentityIdOrHostName1 = "test-host-name-1";
            hostIdentityIdOrHostName2 = "test-host-name-2";

            // lookup HostIdentities
            DtoHostIdentity readHostIdentity0 = client.lookup("TEST-HOST-NAME-0");
            assertNotNull(readHostIdentity0);
            assertNotNull(readHostIdentity0.getHostIdentityId());
            assertEquals("test-host-name-0", readHostIdentity0.getHostName());
            assertEquals(1, readHostIdentity0.getHostNames().size());
            assertTrue(readHostIdentity0.getHostNames().contains("test-host-name-0"));
            assertEquals(Boolean.FALSE, readHostIdentity0.getHost());
            hostIdentityIdOrHostName0 = readHostIdentity0.getHostIdentityId().toString();
            DtoHostIdentity readHostIdentity1 = client.lookup("test-host-name-1.1");
            assertNotNull(readHostIdentity1);
            assertNotNull(readHostIdentity1.getHostIdentityId());
            assertEquals("test-host-name-1", readHostIdentity1.getHostName());
            assertEquals(3, readHostIdentity1.getHostNames().size());
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1.0"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1.1"));
            assertEquals(Boolean.FALSE, readHostIdentity1.getHost());
            hostIdentityIdOrHostName1 = readHostIdentity1.getHostIdentityId().toString();
            DtoHostIdentity readHostIdentity2 = client.lookup(hostIdentity2UUIDHostName.toString());
            assertNotNull(readHostIdentity2);
            assertEquals(hostIdentity2UUIDId, readHostIdentity2.getHostIdentityId());
            assertEquals("test-host-name-2", readHostIdentity2.getHostName());
            assertEquals(2, readHostIdentity2.getHostNames().size());
            assertTrue(readHostIdentity2.getHostNames().contains("test-host-name-2"));
            assertTrue(readHostIdentity2.getHostNames().contains(hostIdentity2UUIDHostName.toString()));
            assertEquals(Boolean.FALSE, readHostIdentity2.getHost());
            hostIdentityIdOrHostName2 = readHostIdentity2.getHostIdentityId().toString();

            // query HostIdentities
            List<DtoHostIdentity> listResults = client.list(0, 2);
            assertNotNull(listResults);
            assertEquals(2, listResults.size());
            List<DtoHostIdentity> listAllResults = client.list();
            assertNotNull(listAllResults);
            assertTrue(listAllResults.size() >= 3);
            int testsFound = 0;
            for (DtoHostIdentity hostIdentity : listAllResults) {
                if (hostIdentity.getHostName().startsWith("test-host-name-")) {
                    testsFound++;
                }
            }
            assertEquals(3, testsFound);
            List<DtoHostIdentity> queryResults = client.query("hostName like 'test-host-name-%' ORDER BY hostName");
            assertNotNull(queryResults);
            assertEquals(3, queryResults.size());
            List<DtoHostIdentity> queryPageResults = client.query("hostName like 'test-host-name-%' ORDER BY hostName", 1, 1);
            assertNotNull(queryPageResults);
            assertEquals(1, queryPageResults.size());
            assertEquals("test-host-name-1", queryPageResults.get(0).getHostName());

            // wait to ensure propagation and test autocomplete
            Thread.sleep(250);
            List<DtoName> suggestions = client.autocomplete("test-");
            assertNotNull(suggestions);
            assertEquals(5, suggestions.size());
            assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                    new DtoName("test-host-name-0", "test-host-name-0"),
                    new DtoName("test-host-name-1", "test-host-name-1"),
                    new DtoName("test-host-name-1.0", "test-host-name-1"),
                    new DtoName("test-host-name-1.1", "test-host-name-1"),
                    new DtoName("test-host-name-2", "test-host-name-2")})));
            suggestions = client.autocomplete("TEST-HOST-NAME-1");
            assertNotNull(suggestions);
            assertEquals(3, suggestions.size());
            assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                    new DtoName("test-host-name-1", "test-host-name-1"),
                    new DtoName("test-host-name-1.0", "test-host-name-1"),
                    new DtoName("test-host-name-1.1", "test-host-name-1")})));
            suggestions = client.autocomplete("test-", 2);
            assertNotNull(suggestions);
            assertEquals(4, suggestions.size());
            assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                    new DtoName("test-host-name-0", "test-host-name-0"),
                    new DtoName("test-host-name-1", "test-host-name-1"),
                    new DtoName("test-host-name-1.0", "test-host-name-1"),
                    new DtoName("test-host-name-1.1", "test-host-name-1")})));

            // update HostIdentities
            DtoOperationResults clearResults = client.clear("test-host-name-1");
            assertNotNull(clearResults);
            assertEquals("HostIdentity", clearResults.getEntityType());
            assertEquals("Clear", clearResults.getOperation());
            assertNotNull(clearResults.getSuccessful());
            assertEquals(1, clearResults.getSuccessful().intValue());
            readHostIdentity1 = client.lookup("test-host-name-1");
            assertNotNull(readHostIdentity1);
            assertNotNull(readHostIdentity1.getHostIdentityId());
            assertEquals("test-host-name-1", readHostIdentity1.getHostName());
            assertEquals(1, readHostIdentity1.getHostNames().size());
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1"));
            DtoHostIdentityList updateHostIdentities = new DtoHostIdentityList();
            DtoHostIdentity updateHostIdentity1 = new DtoHostIdentity();
            updateHostIdentity1.setHostIdentityId(readHostIdentity1.getHostIdentityId());
            updateHostIdentity1.setHostName("test-host-name-1-changed");
            updateHostIdentity1.setHostNames(Arrays.asList(new String[]{"test-host-name-1-changed-alias"}));
            updateHostIdentities.add(updateHostIdentity1);
            DtoOperationResults updateResults = client.post(updateHostIdentities);
            assertNotNull(updateResults);
            assertEquals("HostIdentity", updateResults.getEntityType());
            assertEquals("Update", updateResults.getOperation());
            assertNotNull(updateResults.getSuccessful());
            assertEquals(1, updateResults.getSuccessful().intValue());
            readHostIdentity1 = client.lookup("test-host-name-1-changed");
            assertNotNull(readHostIdentity1);
            assertNotNull(readHostIdentity1.getHostIdentityId());
            assertEquals("test-host-name-1-changed", readHostIdentity1.getHostName());
            assertEquals(3, readHostIdentity1.getHostNames().size());
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1-changed"));
            assertTrue(readHostIdentity1.getHostNames().contains("test-host-name-1-changed-alias"));

            // delete HostIdentities
            DtoOperationResults deleteResults = client.delete(readHostIdentity0.getHostIdentityId().toString());
            assertNotNull(deleteResults);
            assertEquals("HostIdentity", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(1, deleteResults.getSuccessful().intValue());
            readHostIdentity0 = client.lookup(readHostIdentity0.getHostName());
            assertNull(readHostIdentity0);
            hostIdentityIdOrHostName0 = null;
            DtoHostIdentityList deleteHostIdentities = new DtoHostIdentityList();
            deleteHostIdentities.add(readHostIdentity1);
            deleteHostIdentities.add(readHostIdentity2);
            deleteResults = client.delete(deleteHostIdentities);
            assertNotNull(deleteResults);
            assertEquals("HostIdentity", deleteResults.getEntityType());
            assertEquals("Delete", deleteResults.getOperation());
            assertNotNull(deleteResults.getSuccessful());
            assertEquals(2, deleteResults.getSuccessful().intValue());
            readHostIdentity1 = client.lookup(readHostIdentity1.getHostName());
            assertNull(readHostIdentity1);
            readHostIdentity2 = client.lookup(readHostIdentity2.getHostName());
            assertNull(readHostIdentity2);
            hostIdentityIdOrHostName1 = null;
            hostIdentityIdOrHostName2 = null;

            // test warning for missing delete
            deleteResults = client.delete(Arrays.asList(new String[]{"NotAHostIdentity"}));
            assertNotNull(deleteResults);
            assertEquals(new Integer(1), deleteResults.getWarning());
        } finally {
            // cleanup test
            if (hostIdentityIdOrHostName0 != null) {
                client.delete(hostIdentityIdOrHostName0);
            }
            if (hostIdentityIdOrHostName1 != null) {
                client.delete(hostIdentityIdOrHostName1);
            }
            if (hostIdentityIdOrHostName2 != null) {
                client.delete(hostIdentityIdOrHostName2);
            }
        }
    }
}
