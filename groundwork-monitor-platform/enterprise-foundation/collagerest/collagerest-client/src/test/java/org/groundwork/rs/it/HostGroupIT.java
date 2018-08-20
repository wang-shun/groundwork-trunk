package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.commons.lang3.StringUtils;
import org.groundwork.rs.client.CommentClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.*;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.filter.NotFilter.not;

public class HostGroupIT extends AbstractIntegrationTest {

    public static final String QUERY_BULK_HOSTGROUPS = "name like '" + BULK_HG_PREFIX + "%'";

    // test without host collections, depth = simple
    @Test
    public void testCrudWithoutHosts() {
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 10);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context);
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // assert by query
        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Simple);
        assertThat(dtoHostGroups).hasSize(hostGroups.size());
        // assert HostGroups added is same as host's collection of host groups
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Simple);

        // update
        DtoHostGroup update = context.lookupResult(context.formatNameKey(1));
        assertThat(update).isNotNull();
        // update the values in the context directly
        update.setDescription("MODIFIED FOR UPDATE");
        update.setAlias(update.getAlias() + " - modified");
        // TODO: Foundation does not support changing appTypes on HGs
        //update.setAppType("DOCK");
        update.setAlias("new-alias");
        DtoHostGroupList updates = new DtoHostGroupList();
        updates.add(update);
        // update database ...
        results = hgc.post(updates);
        assertThat(results.getCount()).isEqualTo(1);
        assertThat(results.getSuccessful()).isEqualTo(1);

        // assert the updates from server
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS);
        assertThat(dtoHostGroups.size()).as("bulk host group count of %d", context.getCount()).isEqualTo(context.getCount());
        // assert we now have IDs
        assertThat(dtoHostGroups).filteredOn("id", not(null)).hasSize(context.getCount());
        // assert updates
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Simple);

        // delete
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Simple);
        assertThat(dtoHostGroups).hasSize(0);

    }

    // test with host collections, depth = shallow, verify all hosts added to host groups are correctly added
    @Test
    public void testCrudWithHosts() {
        // create hosts
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 100);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 10);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // assert the updates from server
        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS);
        assertThat(dtoHostGroups.size()).as("bulk host group count of %d", context.getCount()).isEqualTo(context.getCount());
        // assert we now have IDs
        assertThat(dtoHostGroups).filteredOn("id", not(null)).hasSize(context.getCount());
        // assert updates and assert host group -> hosts associations
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);

        // add to host group membership
        IntegrationTestContext<DtoHost> hostsContext2 = createHosts(hc, 101, 2);
        DtoHostGroup updatedHostGroup = hgc.lookup(context.formatNameKey(1));
        assertThat(updatedHostGroup).isNotNull();
        for (DtoHost h : hostsContext2.getEntities()) {
            updatedHostGroup.addHost(h);
        }
        DtoHostGroupList updates = new DtoHostGroupList();
        updates.add(updatedHostGroup);
        results = hgc.post(updates);
        assertThat(results.getSuccessful()).isEqualTo(1);

        updatedHostGroup = hgc.lookup(context.formatNameKey(1));
        assertThat(updatedHostGroup.getHosts()).hasSize(12);
        assertThat(updatedHostGroup.getHosts()).extracting("hostName").contains(hostsContext.formatNameKey(101), hostsContext.formatNameKey(102));

        // test passing in 1 host in collection, update with existing hg name with hosts
        hostsContext2 = createHosts(hc, 103, 1);
        DtoHostGroup minimal = new DtoHostGroup();
        minimal.setName(context.formatNameKey(1));
        minimal.setAgentId(AGENT_ID);
        minimal.setAppType("VEMA");
        minimal.addHost(hostsContext2.lookupResult(hostsContext2.formatNameKey(103)));
        updates = new DtoHostGroupList();
        updates.add(minimal);
        results = hgc.post(updates);
        assertThat(results.getSuccessful()).isEqualTo(1);

        updatedHostGroup = hgc.lookup(context.formatNameKey(1));
        assertThat(updatedHostGroup.getHosts()).hasSize(13);
        assertThat(updatedHostGroup.getHosts()).extracting("hostName")
                .contains(hostsContext.formatNameKey(101), hostsContext.formatNameKey(102), hostsContext.formatNameKey(103));


        // delete HGs
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
        hc.delete(hostsContext2.formatNameKey(101));
        hc.delete(hostsContext2.formatNameKey(102));
        hc.delete(hostsContext2.formatNameKey(103));
    }

    // test creating some hosts that are not found
    @Test
    public void testHostsNotFound() {
        // create hosts
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 3);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 1);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoHost missing = new DtoHost(BULK_HOST_PREFIX + "-MISSING_HOST");
        missing.setAppType("VEMA");
        hostGroups.getHostGroups().get(0).addHost(missing);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getCount()).isEqualTo(context.getCount() + 1);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        assertThat(results.getResults()).extracting("status").contains("warning", "success");
        // validate writes of 3 good hosts to HG collection
        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(1);
        assertThat(dtoHostGroups.get(0).getHosts()).hasSize(hostsContext.getCount());
        // delete HGs
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    @Test
    public void testClear() {
        // create hosts
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 3);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 1);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(1);
        assertThat(dtoHostGroups.get(0).getHosts()).hasSize(hostsContext.getCount());
        // clear HGs
        hgc.clear(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(1);
        assertThat(dtoHostGroups.get(0).getHosts()).isNull();

        // recreate HG with hosts and test second entry point
        hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        results = hgc.post(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(1);
        assertThat(dtoHostGroups.get(0).getHosts()).hasSize(hostsContext.getCount());
        hgc.clear(HostGroupTestGenerator.reduceToNames(dtoHostGroups));
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(1);
        assertThat(dtoHostGroups.get(0).getHosts()).isNull();

        // delete HGs
        hgc.delete(HostGroupTestGenerator.reduceToNames(hostGroups.getHostGroups()));
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    @Test
    public void testDeleteHostsShouldRemoveFromHostGroup() {
        // create hosts
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 3);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 10);
        context.setReuseChildren(true);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
        for (DtoHostGroup group : dtoHostGroups) {
            assertThat(group.getHosts()).hasSize(3);
        }

        hc.delete(hostsContext.formatNameKey(2));
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        //HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
        for (DtoHostGroup group : dtoHostGroups) {
            assertThat(group.getHosts()).hasSize(2);
        }
        // delete HGs
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    @Test
    public void testQueries() {
        // create hosts
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 9);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 3);
        //context.setReuseChildren(true);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // query by joined hostname
        for (int ix = context.getStart(); ix <= context.getCount(); ix++) {
            List<DtoHostGroup> dtoHostGroups = hgc.query("hosts.hostName = '" + hostsContext.formatNameKey(ix) + "'", DtoDepthType.Shallow);
            assertThat(dtoHostGroups.size()).isEqualTo(1);
            assertThat(dtoHostGroups.get(0).getHosts()).hasSize(hostsContext.getCount() / context.getCount());
            HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
        }

        // test in query
        List<String> names = HostGroupTestGenerator.reduceToNames(new ArrayList(context.getEntities()));
        names.remove(context.getCount() - 1);
        String inQuery = "(\'" + StringUtils.join(names, "\',\'") + "\')";
        List<DtoHostGroup> dtoHostGroups = hgc.query("name in " + inQuery);
        assertThat(dtoHostGroups).hasSize(context.getCount() - 1);
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);

        // delete HGs
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    @Test
    public void testPagingAndListAndAutoComplete() {
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 2);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 100);
        context.setReuseChildren(true);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        int pageSize = 10;
        int start = 0;
        // query and page
        for (int ix = 0; ix < context.getCount() / pageSize; ix++) {
            List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, start, pageSize);
            assertThat(dtoHostGroups).hasSize(pageSize);
            HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
            start += pageSize;
        }
        // list and page
        start = 0;
        context.setSkipUnmatchedResults(true);
        for (int ix = 0; ix < context.getCount() / pageSize; ix++) {
            List<DtoHostGroup> dtoHostGroups = hgc.list(DtoDepthType.Shallow, start, pageSize);
            assertThat(dtoHostGroups).hasSize(pageSize);
            HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Shallow);
            start += pageSize;
        }

        // autocomplete 
        List<DtoName> suggestions = hgc.autocomplete(BULK_HG_PREFIX, 15);
        assertThat(suggestions).hasSize(15);
        suggestions = hgc.autocomplete(BULK_HG_PREFIX);
        assertThat(suggestions).hasSize(10);

        // delete HGs
        hgc.delete(hostGroups);
        List<DtoHostGroup> dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    static String HOST_MONITOR_STATUS[] = {
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UNSCHEDULED_DOWN,
            MonitorStatusBubbleUp.UP
    };

    @Test
    public void testFullDepthWithBubbleUp() {
        int groupCount = 40;
        HostClient hc = new HostClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 3, HOST_MONITOR_STATUS);
        // create host groups with hosts
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, groupCount);
        context.setReuseChildren(true);
        context.setSkipUnmatchedResults(true);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        List<DtoHostGroup> dtoHostGroups = hgc.list(DtoDepthType.Full);
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Full);

        // test bubble up status, one host down will make the group down...
        int matchedCount = 0;
        for (DtoHostGroup group : dtoHostGroups) {
            if (context.lookupResult(group.getName()) == null)
                continue;
            assertThat(group.getBubbleUpStatus()).isEqualTo(MonitorStatusBubbleUp.UNSCHEDULED_DOWN);
            matchedCount++;
        }
        assertThat(matchedCount).isEqualTo(groupCount);

        // now bring the bad host back up and he host group should reflect
        HOST_MONITOR_STATUS[1] = MonitorStatusBubbleUp.UP;
        hostsContext = createHosts(hc, 1, 3, HOST_MONITOR_STATUS);
        dtoHostGroups = hgc.list(DtoDepthType.Full);
        HostGroupTestGenerator.assertHostGroups(dtoHostGroups, context, DtoDepthType.Full);
        matchedCount = 0;
        for (DtoHostGroup group : dtoHostGroups) {
            if (context.lookupResult(group.getName()) == null)
                continue;
            assertThat(group.getBubbleUpStatus()).isEqualTo(MonitorStatusBubbleUp.UP);
            matchedCount++;
        }

        dtoHostGroups = hgc.list();
        assertThat(dtoHostGroups.size()).isGreaterThanOrEqualTo(context.getCount());

        // delete HGs
        hgc.delete(hostGroups);
        dtoHostGroups = hgc.query(QUERY_BULK_HOSTGROUPS, DtoDepthType.Shallow);
        assertThat(dtoHostGroups).hasSize(0);
        // cascade delete all bulk hosts
        hc.delete(new DtoHostList(new ArrayList(hostsContext.getEntities())));
    }

    protected IntegrationTestContext<DtoHost> createHosts(HostClient hc, int start, int count) {
        return createHosts(hc, start, count, null);
    }

    protected IntegrationTestContext<DtoHost> createHosts(HostClient hc, int start, int count, String[] statuses) {
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, start, count);
        if (statuses != null) {
            context.setMonitorStatuses(statuses);
        }
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        return context;
    }

    @Test
    public void testCommentsAreRetrievedForNOCBoard()  {
        HostClient hc = new HostClient(getDeploymentURL());
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        CommentClient cc = new CommentClient(getDeploymentURL());
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        IntegrationTestContext<DtoHost> hostsContext = createHosts(hc, 1, 3);
        IntegrationTestContext<DtoHostGroup> context = new IntegrationTestContext<DtoHostGroup>(BULK_HG_PREFIX, AGENT_ID, 1, 2);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(context, hostsContext);
        DtoOperationResults results = hgc.post(hostGroups);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        List<DtoHost> hosts = hc.query("agentid = '" + AGENT_ID + "'");
        IntegrationTestContext<DtoService> servicesContext = HostIT.populateHostServices(new DtoHostList(hosts), 5);
        for (DtoService service : servicesContext.getEntities()) {
            cc.addServiceComment(sc.lookup(service.getDescription(), service.getHostName()).getId(), "test-comment", "admin" );
        }
        List<DtoHostGroup> groups = hgc.query("agentid = '" + AGENT_ID + "'", DtoDepthType.Deep);
        for (DtoHostGroup hostGroup : groups) {
            for (DtoHost host : hostGroup.getHosts()) {
                for (DtoService service : host.getServices()) {
                    assertThat(service.getComments()).isNotNull();
                    assertThat(service.getComments()).isNotEmpty();
                    assertThat(service.getComments().get(0).getNotes()).isEqualTo("test-comment");
                    assertThat(service.getComments().get(0).getAuthor()).isEqualTo("admin");
                }
            }
        }
    }
}
