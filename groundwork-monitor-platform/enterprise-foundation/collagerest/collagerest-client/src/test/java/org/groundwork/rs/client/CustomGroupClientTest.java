/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoCustomGroupMemberUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdateList;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceGroupUpdate;
import org.groundwork.rs.dto.DtoServiceGroupUpdateList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * CustomGroupClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CustomGroupClientTest extends AbstractClientTest {

    private static final String TEST_HOST_GROUP0 = "TEST-HOST-GROUP-0";
    private static final String TEST_HOST_GROUP1 = "TEST-HOST-GROUP-1";
    private static final String TEST_SERVICE_GROUP0 = "TEST-SERVICE-GROUP-0";
    private static final String TEST_SERVICE_GROUP1 = "TEST-SERVICE-GROUP-1";
    private static final String TEST_CUSTOM_GROUP0 = "TEST-CUSTOM-GROUP-0";
    private static final String TEST_CUSTOM_GROUP1 = "TEST-CUSTOM-GROUP-1";
    private static final String TEST_CUSTOM_GROUP2 = "TEST-CUSTOM-GROUP-2";
    private static final String TEST_CUSTOM_GROUP3 = "TEST-CUSTOM-GROUP-3";
    private static final String TEST_AGENT_ID = "TEST-AGENT";
    private static final String NAGIOS_APP_TYPE = "NAGIOS";

    @Test
    public void testCustomGroupsClient() throws Exception {
        if (serverDown) return;

        // get clients
        CustomGroupClient client = new CustomGroupClient(getDeploymentURL());
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());

        // create host and service groups
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        DtoHostGroup hostGroup = new DtoHostGroup();
        hostGroup.setName(TEST_HOST_GROUP0);
        hostGroups.add(hostGroup);
        hostGroup = new DtoHostGroup();
        hostGroup.setName(TEST_HOST_GROUP1);
        hostGroups.add(hostGroup);
        DtoOperationResults results = hostGroupClient.post(hostGroups);
        assertNotNull(results);
        assertEquals(new Integer(2), results.getSuccessful());
        DtoServiceGroupUpdateList serviceGroups = new DtoServiceGroupUpdateList();
        serviceGroups.add(new DtoServiceGroupUpdate(TEST_SERVICE_GROUP0));
        results = serviceGroupClient.post(serviceGroups);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());

        // invoke test in both JSON and XML
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testCustomGroupsClient(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testCustomGroupsClient(client);

        // cleanup host and service groups
        results = hostGroupClient.delete(Arrays.asList(new String[]{TEST_HOST_GROUP0, TEST_HOST_GROUP1}));
        assertNotNull(results);
        assertEquals(new Integer(2), results.getSuccessful());
        results = serviceGroupClient.delete(TEST_SERVICE_GROUP0);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
    }

    private void testCustomGroupsClient(CustomGroupClient client) throws Exception {

        // create custom groups
        DtoCustomGroupUpdateList customGroups = new DtoCustomGroupUpdateList();
        DtoCustomGroupUpdate customGroup = new DtoCustomGroupUpdate(TEST_CUSTOM_GROUP0);
        customGroup.setDescription(TEST_CUSTOM_GROUP0);
        customGroup.setAgentId(TEST_AGENT_ID);
        customGroup.setAppType(NAGIOS_APP_TYPE);
        customGroup.addHostGroupNames(TEST_HOST_GROUP0);
        customGroups.add(customGroup);
        customGroup = new DtoCustomGroupUpdate(TEST_CUSTOM_GROUP1);
        customGroup.setAgentId(TEST_AGENT_ID);
        customGroup.addServiceGroupNames(TEST_SERVICE_GROUP0);
        customGroups.add(customGroup);
        customGroup = new DtoCustomGroupUpdate(TEST_CUSTOM_GROUP2);
        customGroup.setAppType(NAGIOS_APP_TYPE);
        customGroups.add(customGroup);
        customGroups.add(new DtoCustomGroupUpdate(TEST_CUSTOM_GROUP3));
        DtoOperationResults results = client.post(customGroups);
        assertNotNull(results);
        assertEquals(new Integer(4), results.getSuccessful());

        // lookup custom groups
        DtoCustomGroup customGroup0 = client.lookup(TEST_CUSTOM_GROUP0);
        assertNotNull(customGroup0);
        assertEquals(TEST_CUSTOM_GROUP0, customGroup0.getName());
        assertEquals(TEST_CUSTOM_GROUP0, customGroup0.getDescription());
        assertEquals(TEST_AGENT_ID, customGroup0.getAgentId());
        assertEquals(NAGIOS_APP_TYPE, customGroup0.getAppType());
        assertNotNull(customGroup0.getHostGroups());
        assertEquals(1, customGroup0.getHostGroups().size());
        assertEquals(TEST_HOST_GROUP0, customGroup0.getHostGroups().get(0).getName());
        DtoCustomGroup customGroup1 = client.lookup(TEST_CUSTOM_GROUP1);
        assertNotNull(customGroup1);
        assertEquals(TEST_CUSTOM_GROUP1, customGroup1.getName());
        assertEquals(TEST_AGENT_ID, customGroup1.getAgentId());
        assertNotNull(customGroup1.getServiceGroups());
        assertEquals(1, customGroup1.getServiceGroups().size());
        assertEquals(TEST_SERVICE_GROUP0, customGroup1.getServiceGroups().get(0).getName());
        DtoCustomGroup customGroup2 = client.lookup(TEST_CUSTOM_GROUP2);
        assertNotNull(customGroup2);
        assertEquals(TEST_CUSTOM_GROUP2, customGroup2.getName());
        assertEquals(NAGIOS_APP_TYPE, customGroup2.getAppType());
        DtoCustomGroup customGroup3 = client.lookup(TEST_CUSTOM_GROUP3);
        assertNotNull(customGroup3);
        assertEquals(TEST_CUSTOM_GROUP3, customGroup3.getName());

        // list custom groups
        List<DtoCustomGroup> listCustomGroups = client.list();
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP2, TEST_CUSTOM_GROUP3},
                listCustomGroups);
        listCustomGroups = client.list(NAGIOS_APP_TYPE, null);
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP2}, listCustomGroups);
        assertNotCustomGroups(new String[]{TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP3}, listCustomGroups);
        listCustomGroups = client.list(null, TEST_AGENT_ID);
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP1}, listCustomGroups);
        assertNotCustomGroups(new String[]{TEST_CUSTOM_GROUP2, TEST_CUSTOM_GROUP3}, listCustomGroups);
        listCustomGroups = client.list(NAGIOS_APP_TYPE, TEST_AGENT_ID);
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0}, listCustomGroups);
        assertNotCustomGroups(new String[]{TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP2, TEST_CUSTOM_GROUP3},
                listCustomGroups);

        // query custom groups
        List<DtoCustomGroup> queryCustomGroups = client.query("name like 'TEST-CUSTOM-GROUP-%'");
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP2, TEST_CUSTOM_GROUP3},
                queryCustomGroups);
        queryCustomGroups = client.query("name like 'TEST-CUSTOM-GROUP-%' order by name asc", 0, 2);
        assertCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP1}, queryCustomGroups);

        // wait to ensure propagation and test autocomplete
        Thread.sleep(250);
        List<DtoName> suggestions = client.autocomplete("test-custom-group-");
        assertNotNull(suggestions);
        assertEquals(4, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("TEST-CUSTOM-GROUP-0"),
                new DtoName("TEST-CUSTOM-GROUP-1"),
                new DtoName("TEST-CUSTOM-GROUP-2"),
                new DtoName("TEST-CUSTOM-GROUP-3")})));
        suggestions = client.autocomplete("zzz");
        assertNotNull(suggestions);
        assertEquals(0, suggestions.size());
        suggestions = client.autocomplete("test-custom-group-", 2);
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("TEST-CUSTOM-GROUP-0"),
                new DtoName("TEST-CUSTOM-GROUP-1")})));

        // add and delete custom group members
        DtoCustomGroupMemberUpdate customGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
        customGroupMemberUpdate.setName(TEST_CUSTOM_GROUP0);
        customGroupMemberUpdate.addHostGroupNames(TEST_HOST_GROUP1);
        results = client.addMembers(customGroupMemberUpdate);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        customGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
        customGroupMemberUpdate.setName(TEST_CUSTOM_GROUP0);
        customGroupMemberUpdate.addHostGroupNames(TEST_HOST_GROUP0);
        results = client.deleteMembers(customGroupMemberUpdate);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        customGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
        customGroupMemberUpdate.setName(TEST_CUSTOM_GROUP1);
        customGroupMemberUpdate.addServiceGroupNames(TEST_SERVICE_GROUP0);
        results = client.deleteMembers(customGroupMemberUpdate);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        results = client.addMembers(customGroupMemberUpdate);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        customGroup0 = client.lookup(TEST_CUSTOM_GROUP0);
        assertNotNull(customGroup0);
        assertNotNull(customGroup0.getHostGroups());
        assertEquals(1, customGroup0.getHostGroups().size());
        assertEquals(TEST_HOST_GROUP1, customGroup0.getHostGroups().get(0).getName());
        customGroup1 = client.lookup(TEST_CUSTOM_GROUP1);
        assertNotNull(customGroup1);
        assertNotNull(customGroup1.getServiceGroups());
        assertEquals(1, customGroup1.getServiceGroups().size());
        assertEquals(TEST_SERVICE_GROUP0, customGroup1.getServiceGroups().get(0).getName());

        // test warning for missing member delete
        customGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
        customGroupMemberUpdate.setName(TEST_CUSTOM_GROUP1);
        customGroupMemberUpdate.addServiceGroupNames(TEST_SERVICE_GROUP0);
        DtoOperationResults deleteResults = client.deleteMembers(customGroupMemberUpdate);
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getSuccessful());
        deleteResults = client.deleteMembers(customGroupMemberUpdate);
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
        customGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
        customGroupMemberUpdate.setName(TEST_CUSTOM_GROUP1);
        customGroupMemberUpdate.addServiceGroupNames(TEST_SERVICE_GROUP1);
        deleteResults = client.deleteMembers(customGroupMemberUpdate);
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getFailed());

        // delete custom groups
        results = client.delete(TEST_CUSTOM_GROUP0);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        results = client.delete(Arrays.asList(new String[]{TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP2}));
        assertNotNull(results);
        assertEquals(new Integer(2), results.getSuccessful());
        customGroups = new DtoCustomGroupUpdateList();
        customGroups.add(new DtoCustomGroupUpdate(TEST_CUSTOM_GROUP3));
        results = client.delete(customGroups);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        listCustomGroups = client.list();
        assertNotCustomGroups(new String[]{TEST_CUSTOM_GROUP0, TEST_CUSTOM_GROUP1, TEST_CUSTOM_GROUP2,
                TEST_CUSTOM_GROUP3}, listCustomGroups);

        // test warning for missing delete
        deleteResults = client.delete(Arrays.asList(new String[]{"NotACustomGroup"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
    }

    /**
     * Assert custom groups collection contains named custom groups.
     *
     * @param names custom group names
     * @param customGroups custom groups collection to test
     */
    private void assertCustomGroups(String [] names, List<DtoCustomGroup> customGroups) {
        assertCustomGroups(names, customGroups, false);
    }

    /**
     * Assert custom groups collection does not contain named custom
     * groups.
     *
     * @param names custom group names
     * @param customGroups custom groups collection to test
     */
    private void assertNotCustomGroups(String [] names, List<DtoCustomGroup> customGroups) {
        assertCustomGroups(names, customGroups, true);
    }

    /**
     * Assert custom groups collection contents.
     *
     * @param names custom group names
     * @param customGroups custom groups collection to test
     * @param exclude assert does not or does contain named custom groups
     */
    private void assertCustomGroups(String [] names, List<DtoCustomGroup> customGroups, boolean exclude) {
        if ((names != null) && (names.length > 0)) {
            if ((customGroups != null) && !customGroups.isEmpty()) {
                Set<String> namesSet = new HashSet<String>(Arrays.asList(names));
                for (DtoCustomGroup customGroup : customGroups) {
                    namesSet.remove(customGroup.getName());
                }
                if (exclude) {
                    assertEquals(new HashSet<String>(Arrays.asList(names)), namesSet);
                } else {
                    assertTrue(namesSet.isEmpty());
                }
            } else {
                assertTrue(exclude);
            }
        }
    }

    @Test
    public void testGroupsDeleteFromCustomGroup() {
        if (serverDown) return;

        // get clients
        CustomGroupClient client = new CustomGroupClient(getDeploymentURL());
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());

        // create host, service, and custom groups
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        DtoHostGroup hostGroup = new DtoHostGroup();
        hostGroup.setName(TEST_HOST_GROUP0);
        hostGroups.add(hostGroup);
        DtoOperationResults results = hostGroupClient.post(hostGroups);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        DtoServiceGroupUpdateList serviceGroups = new DtoServiceGroupUpdateList();
        serviceGroups.add(new DtoServiceGroupUpdate(TEST_SERVICE_GROUP0));
        results = serviceGroupClient.post(serviceGroups);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());

        // create custom group with host and service groups
        DtoCustomGroupUpdateList customGroups = new DtoCustomGroupUpdateList();
        DtoCustomGroupUpdate customGroup = new DtoCustomGroupUpdate();
        customGroup.setName("test-groups-delete-from-custom-group");
        customGroup.addHostGroupNames(TEST_HOST_GROUP0);
        customGroup.addServiceGroupNames(TEST_SERVICE_GROUP0);
        customGroups.add(customGroup);
        results = client.post(customGroups);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        DtoCustomGroup verifyCustomGroup = client.lookup("test-groups-delete-from-custom-group");
        assertNotNull(verifyCustomGroup);
        assertNotNull(verifyCustomGroup.getHostGroups());
        assertFalse(verifyCustomGroup.getHostGroups().isEmpty());
        assertNotNull(verifyCustomGroup.getServiceGroups());
        assertFalse(verifyCustomGroup.getServiceGroups().isEmpty());

        // delete host and service groups
        results = hostGroupClient.delete(TEST_HOST_GROUP0);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        hostGroup = hostGroupClient.lookup(TEST_HOST_GROUP0);
        assertNull(hostGroup);
        results = serviceGroupClient.delete(TEST_SERVICE_GROUP0);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        DtoServiceGroup serviceGroup = serviceGroupClient.lookup(TEST_SERVICE_GROUP0);
        assertNull(serviceGroup);

        // verify deleted groups deleted from custom group
        verifyCustomGroup = client.lookup("test-groups-delete-from-custom-group");
        assertNotNull(verifyCustomGroup);
        assertTrue((verifyCustomGroup.getHostGroups() == null) || verifyCustomGroup.getHostGroups().isEmpty());
        assertTrue((verifyCustomGroup.getServiceGroups() == null) || verifyCustomGroup.getServiceGroups().isEmpty());

        // cleanup custom group
        results = client.delete("test-groups-delete-from-custom-group");
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        verifyCustomGroup = client.lookup("test-groups-delete-from-custom-group");
        assertNull(verifyCustomGroup);
    }
}
