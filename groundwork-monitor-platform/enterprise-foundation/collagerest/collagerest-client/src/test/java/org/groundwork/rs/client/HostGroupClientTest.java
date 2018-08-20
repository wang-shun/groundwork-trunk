package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class HostGroupClientTest extends AbstractClientTest  {

    @Test
    public void testHostGroupsCount() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> hostGroups = client.list(DtoDepthType.Shallow, -1, -1);
        Assert.assertEquals(8, hostGroups.size());

        // wait to ensure propagation and test autocomplete
        Thread.sleep(250);
        List<DtoName> suggestions = client.autocomplete("eng-");
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("Eng-QA"),
                new DtoName("Eng-test")})));
        suggestions = client.autocomplete("zzz");
        assertNotNull(suggestions);
        assertEquals(0, suggestions.size());
        suggestions = client.autocomplete("eng-", 1);
        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("Eng-QA")})));
    }

    @Test
    public void testHostGroupLookupShallow() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hostGroup = client.lookup("Engineering");
        Assert.assertNotNull(hostGroup);
        Assert.assertEquals("Engineering", hostGroup.getName());
        Assert.assertEquals("eng", hostGroup.getAlias());
        Assert.assertEquals("NAGIOS", hostGroup.getAppType());
    }

    @Test
    public void testHostGroupLookupDeep() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hostGroup = client.lookup("Linux Servers", DtoDepthType.Deep);
        Assert.assertNotNull(hostGroup);
        Assert.assertEquals("Linux Servers", hostGroup.getName());
        Assert.assertEquals("Linux Servers", hostGroup.getAlias());
        Assert.assertEquals("NAGIOS", hostGroup.getAppType());
        Assert.assertEquals(10, hostGroup.getHosts().size());
        Assert.assertEquals(7, hostGroup.getStatistics().size());
    }

    @Test
    public void testQueryByLike() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> hostGroups = client.query("name like 'Eng%'");
        Assert.assertEquals(3, hostGroups.size());
        for (DtoHostGroup hostGroup : hostGroups) {
            Assert.assertNotNull(hostGroup.getId());
            Assert.assertTrue(hostGroup.getName().startsWith("Eng"));
        }
    }

    @Test
    public void testQueryByJoinedCollection() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> hostGroups = client.query("hosts.hostName = 'localhost'");
        Assert.assertEquals(2, hostGroups.size());
        for (DtoHostGroup hostGroup : hostGroups) {
            Assert.assertTrue(hostGroup.getName().equals("IT") || hostGroup.getName().equals("Linux Servers"));
        }
    }

    @Test
    public void testQueryByIn() throws Exception {
        if (serverDown) return;
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> hostGroups = client.query("name in ('Engineering','Support','IT','HG1')");
        Assert.assertEquals(4, hostGroups.size());
    }

//    @Test
//    public void testQueryByJoinedCollection2() throws Exception {
//        if (serverDown) return;
//        HostGroupClient client = new HostGroupClient(getDeploymentURL());
//        List<DtoHostGroup> hostGroups = client.query("property.Latency = 356", DtoDepthType.Deep);
//        Assert.assertEquals(1, hostGroups.size());
//    }

    @Test
    public void testCreateClearDeleteHostGroup() throws Exception {
        if (serverDown) return;
        DtoHostGroupList hostUpdates = buildHostGroupUpdate();
        DtoOperationResults results = executePost(hostUpdates);
        Assert.assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHostGroup groupA = retrieveSingleHostGroup("groupA", Response.Status.OK);
        Assert.assertNotNull(groupA);
        assertHostGroupWritten(groupA);

        DtoHostGroup groupB = retrieveSingleHostGroup("groupB", Response.Status.OK);
        Assert.assertNotNull(groupB);
        assertHostGroupWritten(groupB);

        // reset data for next test
        executeClear("groupA,groupB");
        groupA = retrieveSingleHostGroup("groupA", Response.Status.OK);
        Assert.assertNull(groupA.getHosts());
        groupB = retrieveSingleHostGroup("groupB", Response.Status.OK);
        Assert.assertNull(groupB.getHosts());

        executeDelete("groupA,groupB");

        // test its deleted
        retrieveSingleHostGroup("groupA", Response.Status.NOT_FOUND);
        retrieveSingleHostGroup("groupB", Response.Status.NOT_FOUND);

        // test warning for missing delete
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAHostGroup"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
    }

    private DtoOperationResults executePost(DtoHostGroupList updates) throws Exception {
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        return client.post(updates);
    }

    private DtoOperationResults executeDelete(String hostGroupIds) throws Exception {
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, hostGroupIds.split(","));
        return client.delete(ids);
    }

    private DtoOperationResults executeClear(String hostGroupIds) throws Exception {
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, hostGroupIds.split(","));
        return client.clear(ids);
    }

    private DtoHostGroupList buildHostGroupUpdate() throws Exception {
        DtoHostGroupList hostGroups = new DtoHostGroupList();
        DtoHostGroup hostGroupA = new DtoHostGroup();
        hostGroupA.setName("groupA");
        hostGroupA.setDescription("Group A");
        hostGroupA.setAlias("A");
        hostGroupA.setAgentId(AGENT_84);
        hostGroupA.setAppType("NAGIOS");
        hostGroupA.addHost(lookupHost("localhost"));
        hostGroupA.addHost(lookupHost("demo"));
        hostGroups.add(hostGroupA);
        DtoHostGroup hostGroupB = new DtoHostGroup();
        hostGroupB.setName("groupB");
        hostGroupB.setDescription("Group B");
        hostGroupB.setAlias("B");
        hostGroupB.setAgentId(AGENT_85);
        hostGroupB.setAppType("NAGIOS");
        hostGroupB.addHost(lookupHost("malbec"));
        hostGroupB.addHost(lookupHost("demo"));
        hostGroups.add(hostGroupB);
        return hostGroups;
    }

    private void assertHostGroupWritten(DtoHostGroup group) {
        Assert.assertNotNull(group.getName());
        if (group.getName().equals("groupA")) {
            Assert.assertEquals("A", group.getAlias());
            Assert.assertEquals("Group A", group.getDescription());
            assertEquals(AGENT_84, group.getAgentId());
            Assert.assertEquals("NAGIOS", group.getAppType());
            Assert.assertEquals(2, group.getHosts().size());
            int count = 0;
            for (DtoHost host : group.getHosts()) {
                if (host.getHostName().equals("localhost"))
                    count++;
                if (host.getHostName().equals("demo"))
                    count++;
            }
            Assert.assertEquals(2, count);
        }
        else if (group.getName().equals("groupB")) {
            Assert.assertEquals("B", group.getAlias());
            Assert.assertEquals("Group B", group.getDescription());
            assertEquals(AGENT_85, group.getAgentId());
            Assert.assertEquals("NAGIOS", group.getAppType());
            Assert.assertEquals(2, group.getHosts().size());
            int count = 0;
            for (DtoHost host : group.getHosts()) {
                if (host.getHostName().equals("malbec"))
                    count++;
                if (host.getHostName().equals("demo"))
                    count++;
            }
            Assert.assertEquals(2, count);
        }
        else {
            Assert.fail("host name " + group.getName() + " not valid");
        }
    }

    private DtoHostGroup retrieveSingleHostGroup(String groupName, Response.Status status) throws Exception {
        HostGroupClient client = new HostGroupClient(getDeploymentURL());
        DtoHostGroup group = client.lookup(groupName, DtoDepthType.Deep);
        if (status == Response.Status.NOT_FOUND) {
            Assert.assertNull(group);
            return null;
        }
        else if (status == Response.Status.OK) {
            Assert.assertNotNull(group);
        }
        return group;
    }

}
