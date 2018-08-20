package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.assertj.core.api.Condition;
import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.filter.NotFilter.not;
import static org.assertj.core.util.Lists.newArrayList;

public class HostIT extends AbstractIntegrationTest {

    public static final String QUERY_BULK_HOSTS = "hostname like '" + BULK_HOST_PREFIX + "%'";
    public static final String QUERY_BULK_HOSTS2 = "hostname like '" + BULK_HOST_PREFIX2 + "%'";
    public static final String QUERY_BULK_DEVICES = "identification like '" + BULK_HOST_PREFIX + "%'";

    public static final String ZHOST_001 = "ZHOST-001";
    public static final String ZHG_001 = "ZHG-001";

    @Test
    public void testBulkCrudOnDataMembersAndProperties() {
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 20);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        int propertiesSize = hosts.getHosts().get(0).getProperties().size();
        HostClient hc = new HostClient(getDeploymentURL());
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        
        // assert by query
        List<DtoHost> dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%'");
        assertThat(dtoHosts.size()).as("bulk host count of %d", context.getCount()).isEqualTo(context.getCount());

        // assert values are correct
        HostTestGenerator.assertHosts(dtoHosts, context);

        // update one DTO
        DtoHost update = context.lookupResult(context.formatNameKey(1));
        assertThat(update).isNotNull();
        // update the values in the context directly
        update.setDescription("MODIFIED FOR UPDATE");
        update.putProperty("ExecutionTime", 4000.5);
        DtoHostList updates = new DtoHostList();
        updates.add(update);
        // update database ...
        results = hc.post(updates);
        assertThat(results.getCount()).isEqualTo(1);
        assertThat(results.getSuccessful()).isEqualTo(1);

        // assert the updates from server
        dtoHosts = hc.query(QUERY_BULK_HOSTS);
        assertThat(dtoHosts.size()).as("bulk host count of %d", context.getCount()).isEqualTo(context.getCount());
        // assert we now have IDs
        assertThat(dtoHosts).filteredOn("id", not(null)).hasSize(context.getCount());
        // assert updates
        HostTestGenerator.assertHosts(dtoHosts, context);

        // add a new property
        update.putProperty("MaxAttempts",  50);
        results = hc.post(updates);
        assertThat(results.getSuccessful()).isEqualTo(1);
        // test lookup on modified host
        DtoHost lookup =  hc.lookup(context.formatNameKey(1));
        assertThat(lookup).isNotNull();
        assertThat(lookup.getProperties()).hasSize(propertiesSize + 1);

        // verify corresponding devices created
        DeviceClient dc = new DeviceClient(getDeploymentURL());
        List<DtoDevice> dtoDevices = dc.query(QUERY_BULK_DEVICES);
        assertThat(dtoDevices.size()).as("bulk device count of %d", context.getCount()).isEqualTo(context.getCount());

        // cascade delete all bulk hosts
        hc.delete(hosts);

        // verify delete
        dtoHosts = hc.query(QUERY_BULK_HOSTS);
        assertThat(dtoHosts.size()).isEqualTo(0);
        // verify cascade delete
        dtoDevices = dc.query(QUERY_BULK_DEVICES);
        assertThat(dtoDevices.size()).isEqualTo(0);
    }

    @Test
    public void testAutocomplete() throws Exception {
        IntegrationTestContext<DtoHost> context1 = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 12);
        DtoHostList hosts1 = HostTestGenerator.buildHostInserts(context1);
        IntegrationTestContext<DtoHost> context2 = new IntegrationTestContext(BULK_HOST_PREFIX2, AGENT_ID, 1, 10);
        DtoHostList hosts2 = HostTestGenerator.buildHostInserts(context2);

        // create first batch of prefixed hosts
        HostClient hc = new HostClient(getDeploymentURL());
        DtoOperationResults results = hc.post(hosts1);
        assertThat(results.getCount()).isEqualTo(context1.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context1.getCount());

        // create second batch of prefixed hosts
        results = hc.post(hosts2);
        assertThat(results.getCount()).isEqualTo(context2.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context2.getCount());

        // build comparison array for batch 1, could really use java8 ...
        List<DtoName> names1 = new ArrayList<>();
        for (DtoHost h : context1.getEntities()) {
            // could use java8 here ahem
            names1.add(new DtoName(h.getHostName()));
        }
        List<DtoName> names2 = new ArrayList<>();
        for (DtoHost h : context2.getEntities()) {
            // could use java8 here ahem
            names2.add(new DtoName(h.getHostName()));
        }

        // let the autoname cache catch up..
        Thread.sleep(250);

        // test auto complete on first batch
        List<DtoName> suggestions = hc.autocomplete(BULK_HOST_PREFIX, 20); // limit defaults to 10
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(context1.getCount());
        assertThat(suggestions).containsAll(names1);

        // test auto complete on second batch
        suggestions = hc.autocomplete(BULK_HOST_PREFIX2);
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(context2.getCount());
        assertThat(suggestions).containsAll(names2);


        suggestions = hc.autocomplete("zzz");
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(0);

        // cascade delete all bulk hosts
        hc.delete(hosts1);
        hc.delete(hosts2);

        // verify delete
        List<DtoHost> dtoHosts1 = hc.query(QUERY_BULK_HOSTS);
        assertThat(dtoHosts1.size()).isEqualTo(0);
        List<DtoHost> dtoHosts2 = hc.query(QUERY_BULK_HOSTS2);
        assertThat(dtoHosts2.size()).isEqualTo(0);

        // let the autoname cache catch up..
        Thread.sleep(250);

        // verify cache cleared
        suggestions = hc.autocomplete(BULK_HOST_PREFIX, 20);
        assertThat(suggestions).hasSize(0);
        suggestions = hc.autocomplete(BULK_HOST_PREFIX2);
        assertThat(suggestions).hasSize(0);

    }

    @Test
    public void testLookupDepthFullWithHostGroups() {

        // first create hosts
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 2);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL());
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // create host groups and assign host to host groups
        IntegrationTestContext<DtoHostGroup> hgContext = new IntegrationTestContext(BULK_HG_PREFIX, AGENT_ID, 1, 2);
        DtoHostGroupList hostGroups = HostGroupTestGenerator.buildHostGroupInserts(hgContext);
        for (DtoHostGroup hostGroup : hostGroups.getHostGroups()) {
            HostGroupTestGenerator.addHostsToHostGroup(hostGroup, hosts);
        }
        HostGroupClient hgc = new HostGroupClient(getDeploymentURL());
        results = hgc.post(hostGroups);
        assertThat(results.getSuccessful()).isEqualTo(hgContext.getCount());

        // assert full depth by lookup of first host
        String hostName = hosts.getHosts().get(0).getHostName();
        DtoHost host1 = hc.lookup(hostName, DtoDepthType.Full);
        assertThat(host1).isNotNull();
        List<DtoHost> newHostList = newArrayList(host1);
        HostTestGenerator.assertHosts(newHostList, context);
        // assert HostGroups added is same as host's collection of host groups
        HostTestGenerator.assertHostGroups(newHostList, hostGroups.getHostGroups());

        // assert by query
        List<DtoHost> dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%'", DtoDepthType.Full);
        assertThat(dtoHosts).hasSize(hosts.size());
        // assert queried values are correct
        HostTestGenerator.assertHosts(dtoHosts, context);
        // assert HostGroups added is same as host's collection of host groups
        HostTestGenerator.assertHostGroups(dtoHosts, hostGroups.getHostGroups());

        // test filter by Host Groups
        List<String> hgNames = HostGroupTestGenerator.reduceToNames(hostGroups.getHostGroups());
        dtoHosts = hc.filterByHostGroups(hgNames, DtoDepthType.Full);
        assertThat(dtoHosts).hasSize(hosts.size());
        // assert queried values are correct
        HostTestGenerator.assertHosts(dtoHosts, context);
        // assert HostGroups added is same as host's collection of host groups
        HostTestGenerator.assertHostGroups(dtoHosts, hostGroups.getHostGroups());

        String[] notFound = new String[] {"__not_xxfound"};
        dtoHosts = hc.filterByHostGroups(Arrays.asList(notFound), DtoDepthType.Full);
        assertThat(dtoHosts).isEmpty();

        // delete cleanup
        hc.delete(hosts);
        hgc.delete(hostGroups);

        // assert deletion worked on host
        host1 = hc.lookup(hostName, DtoDepthType.Full);
        assertThat(host1).isNull();
    }

    static String SERVICE_MONITOR_STATUS[] = {
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK
    };

    @Test
    public void testLookupDepthFullWithServices() {

        // first create hosts
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 2);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getCount()).isEqualTo(context.getCount());
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // create services and assign services to each host
        int expectedCount = 5;
        IntegrationTestContext<DtoService> serviceContext = populateHostServices(hosts, expectedCount);
        
        // assert full depth by lookup of first host
        String hostName = hosts.getHosts().get(0).getHostName();
        DtoHost host1 = hc.lookup(hostName, DtoDepthType.Full);
        assertThat(host1).isNotNull();
        List<DtoHost> newHostList = newArrayList(host1);
        HostTestGenerator.assertHosts(newHostList, context);
        // assert HostGroups added is same as host's collection of host groups
        HostTestGenerator.assertServices(newHostList, serviceContext, expectedCount);
        // assert Host service stats based on monitor status
        assertHostServiceStatistics(host1, "80", expectedCount, 4, 1);

        // assert by query
         List<DtoHost> dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%'", DtoDepthType.Full);
        assertThat(dtoHosts).hasSize(hosts.size());
        HostTestGenerator.assertHosts(dtoHosts, context);
        // assert HostGroups added is same as host's collection of host groups
        HostTestGenerator.assertServices(dtoHosts, serviceContext, expectedCount);
        // assert Host service stats based on monitor status
        for (DtoHost dtoHost : dtoHosts) {
            assertHostServiceStatistics(dtoHost, "80", expectedCount, 4, 1);
        }

        // cleanup
        hc.delete(hosts);

        // assert deletion worked on host
        host1 = hc.lookup(hostName, DtoDepthType.Full);
        assertThat(host1).isNull();
    }

    @Test
    public void testQueryByService() throws Exception {
        int expectedHostCount = 4;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, expectedHostCount);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());

        // create services and assign services to each host
        int expectedCount = 3;
        populateHostServices(hosts, expectedCount);
        List<DtoHost> hostsList = hc.query("serviceStatuses.serviceDescription = '" + BULK_SERVICE_PREFIX + "-00002'");
        assertThat(hostsList).hasSize(expectedHostCount);
        hc.delete(hosts);
    }

    static IntegrationTestContext<DtoService> populateHostServices(DtoHostList hosts, int expectedCount) {
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        IntegrationTestContext<DtoService> serviceContext = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, expectedCount);
        for (DtoHost host : hosts.getHosts()) {
            serviceContext.setOwner(host.getHostName());
            serviceContext.setMonitorStatuses(SERVICE_MONITOR_STATUS);
            DtoServiceList services = ServiceTestGenerator.buildServiceInserts(serviceContext);
            DtoOperationResults results = sc.post(services);
            assertThat(results.getSuccessful()).isEqualTo(serviceContext.getCount());
        }
        return serviceContext;
    }

    @Test
    public void testHostCreationAsync() throws Exception {
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 50);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.postAsync(hosts);
        assertThat(results.getSuccessful()).isEqualTo(1); // only 1 record on async returned

        Thread.sleep(2000);

        List<DtoHost> dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%'");
        assertThat(dtoHosts.size()).as("async host count of %d", context.getCount()).isEqualTo(context.getCount());

        // assert values are correct
        HostTestGenerator.assertHosts(dtoHosts, context);

        hc.delete(hosts);
    }

    @Test
    public void testHostQueryAndPaging() throws Exception {
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, 23);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(23);
        // paging testing
        int pageSize = 10;
        int start = 0;
        List<DtoHost> dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%' order by hostname", DtoDepthType.Shallow, start, pageSize);
        assertThat(dtoHosts.size()).as("paging host count of %d", context.getCount()).isEqualTo(pageSize);
        HostTestGenerator.assertHosts(dtoHosts, context);
        start += pageSize;
        dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%' order by hostname", start, pageSize);
        assertThat(dtoHosts.size()).as("paging host count of %d", context.getCount()).isEqualTo(pageSize);
        HostTestGenerator.assertHosts(dtoHosts, context);
        start += pageSize;
        dtoHosts = hc.query("hostname like '" + BULK_HOST_PREFIX + "%' order by hostname", start, pageSize);
        assertThat(dtoHosts.size()).as("paging host count of %d", context.getCount()).isEqualTo(3);
        HostTestGenerator.assertHosts(dtoHosts, context);
        hc.delete(hosts);
    }

    @Test
    public void testListDepthsAndPaging() throws Exception {
        int total = 14;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, total);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(total);

        // list by all depths
        context.setSkipUnmatchedResults(true);
        listByDepth(hc, DtoDepthType.Simple, context, total);
        listByDepth(hc, DtoDepthType.Sync, context, total);
        listByDepth(hc, DtoDepthType.Full, context, total);
        listByDepth(hc, DtoDepthType.Deep, context, total);
        listByDepth(hc, null, context, total); // shallow

        // paging testing
        int pageSize = 10;
        int start = 0;
        List<DtoHost> dtoHosts = hc.query("agentid = 'IT_AGENT'", DtoDepthType.Simple, start, pageSize);
        assertThat(dtoHosts).filteredOn(hostFilter).hasSize(pageSize);
        HostTestGenerator.assertHosts(dtoHosts, context, DtoDepthType.Simple);
        start += pageSize;
        dtoHosts = hc.query("agentid = 'IT_AGENT'", DtoDepthType.Simple, start, pageSize);
        assertThat(dtoHosts).filteredOn(hostFilter).hasSize(total - pageSize);
        HostTestGenerator.assertHosts(dtoHosts, context, DtoDepthType.Simple);

        hc.delete(hosts);
    }

    static String HOST_MONITOR_STATUS[] = {
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UNSCHEDULED_DOWN,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.WARNING,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UNSCHEDULED_DOWN,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UNSCHEDULED_DOWN

    };

    @Test
    public void testQueryMonitorStatus() throws Exception {
        int batch = 10;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, batch);
        context.setMonitorStatuses(HOST_MONITOR_STATUS);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(batch); // only 1 record on async returned

        List<DtoHost> dtoHosts = hc.query("monitorStatus = '" + MonitorStatusBubbleUp.UP  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoHosts).hasSize(6);
        HostTestGenerator.assertHosts(dtoHosts, context);
        dtoHosts = hc.query("monitorStatus = '" + MonitorStatusBubbleUp.WARNING  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoHosts).hasSize(1);
        HostTestGenerator.assertHosts(dtoHosts, context);
        dtoHosts = hc.query("monitorStatus = '" + MonitorStatusBubbleUp.UNSCHEDULED_DOWN  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoHosts).hasSize(3);
        HostTestGenerator.assertHosts(dtoHosts, context);
        
        hc.delete(hosts);
    }

    static Double HOST_EXECUTION_TIME[] = {
            2010.0,
            2000.0, 42000.0, 2010.0, 42010.0,
            2020.0, 42020.0, 2030.0, 43030.0,
            42010.0
    };

    static String HOST_MONITOR_STATUS2[] = {
            MonitorStatusBubbleUp.WARNING,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UP,
            MonitorStatusBubbleUp.UNSCHEDULED_DOWN
    };

    @Test
    public void testQueryProperty() throws Exception {
        int batch = 10;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, batch);
        context.setMonitorStatuses(HOST_MONITOR_STATUS2);
        context.setPropertyValues("ExecutionTime", HOST_EXECUTION_TIME);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(batch); // only 1 record on async returned

        List<DtoHost> dtoHosts = hc.query("(property.ExecutionTime between 2000 and 10000 and (monitorStatus = 'UP') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoHosts).hasSize(4);
        HostTestGenerator.assertHosts(dtoHosts, context);
        dtoHosts = hc.query("(property.ExecutionTime between 2000 and 10000 and (monitorStatus <> 'UP') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoHosts).hasSize(1);
        HostTestGenerator.assertHosts(dtoHosts, context);

        dtoHosts = hc.query("(property.ExecutionTime between 42000 and 52000 and (monitorStatus = 'UP') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoHosts).hasSize(4);
        HostTestGenerator.assertHosts(dtoHosts, context);
        dtoHosts = hc.query("(property.ExecutionTime between 42000 and 52000 and (monitorStatus <> 'UP') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoHosts).hasSize(1);
        HostTestGenerator.assertHosts(dtoHosts, context);
        
        hc.delete(hosts);
    }

    static Date HOST_LAST_CHECK_TIME[] = {
            new GregorianCalendar(2019, Calendar.DECEMBER,21).getTime(),
            new GregorianCalendar(2018, Calendar.JANUARY,21).getTime(),
            new GregorianCalendar(2018, Calendar.DECEMBER,21).getTime(),
            new GregorianCalendar(2018, Calendar.JULY,21).getTime(),
            new GregorianCalendar(2018, Calendar.AUGUST,21).getTime(),

            new GregorianCalendar(2019, Calendar.OCTOBER,21).getTime(),
            new GregorianCalendar(2018, Calendar.JUNE,21).getTime(),
            new GregorianCalendar(2018, Calendar.NOVEMBER,21).getTime(),
            new GregorianCalendar(2018, Calendar.JULY,21).getTime(),
            new GregorianCalendar(2018, Calendar.AUGUST,21).getTime(),

    };


    @Test
    public void testQueryDateRange() throws Exception {
        int batch = 10;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, batch);
        context.setPropertyValues("LastCheckTime", HOST_LAST_CHECK_TIME);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(batch); // only 1 record on async returned

        List<DtoHost> dtoHosts = hc.query("year(lastCheckTime) = 2018 and month(lastCheckTime) between 6 and 11 and agentId = '" + AGENT_ID + "' order by lastCheckTime");
        assertThat(dtoHosts).hasSize(6);
        HostTestGenerator.assertHosts(dtoHosts, context);

        hc.delete(hosts);
    }
    

    @Test
    public void testHostWithSpecialCharacters() throws Exception {
        testSpecialCharacterInHostName("Z HOST"); // space
        testSpecialCharacterInHostName("Z+HOST"); // plus sign
    }

    public void testSpecialCharacterInHostName(String prefix) throws Exception {

        int batch = 2;
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext(prefix, AGENT_ID, 1, batch);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        HostClient hc = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(batch); // only 1 record on async returned

        // test space in lookup
        assertThat(hc.lookup(context.formatNameKey(1))).isNotNull();
        assertThat(hc.lookup(context.formatNameKey(2))).isNotNull();

        // test space in query
        List<DtoHost> dtoHosts = hc.query("hostname like '" + prefix + context.getDelimiter() +  "%'");
        assertThat(dtoHosts).hasSize(batch);
        HostTestGenerator.assertHosts(dtoHosts, context);

        hc.delete(hosts);

        assertThat(hc.lookup(context.formatNameKey(1))).isNull();
        assertThat(hc.lookup(context.formatNameKey(2))).isNull();

        dtoHosts = hc.query("hostname like '" + prefix + context.getDelimiter() +  "%'");
        assertThat(dtoHosts).hasSize(0);

    }

    /**
     * Assert statistics and status of Host from calculated service values
     *
     * @param host
     * @param expectedAvailability
     * @param expectedCount
     * @param expectedOK
     * @param expectedDown
     */
    public static void assertHostServiceStatistics(DtoHost host, String expectedAvailability, int expectedCount, int expectedOK, int expectedDown) {
        assertThat(host.getServiceCount()).isEqualTo(expectedCount);
        assertThat(host.getServices()).hasSize(expectedCount);
        assertThat(host.getServiceAvailability()).isEqualTo(expectedAvailability);
        assertThat(countStatisticsByType(host.getStatistics(), MonitorStatusBubbleUp.OK)).isEqualTo(expectedOK);
        assertThat(countStatisticsByType(host.getStatistics(), MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL)).isEqualTo(expectedDown);
        assertThat(host.getBubbleUpStatus()).isEqualTo(MonitorStatusBubbleUp.UNSCHEDULED_DOWN);
    }

    protected void listByDepth(HostClient hc, DtoDepthType depth, IntegrationTestContext context, int total) {
        List<DtoHost> dtoHosts;
        if (depth == null) {
            dtoHosts = hc.list();
        }
        else {
            dtoHosts = hc.list(depth);
        }
        assertThat(dtoHosts).filteredOn(hostFilter).hasSize(total);
        HostTestGenerator.assertHosts(dtoHosts, context, depth);
    }

    Condition<DtoHost> hostFilter = new Condition<DtoHost>() {
        @Override
        public boolean matches(DtoHost host) {
            return host.getHostName().startsWith(BULK_HOST_PREFIX);
        }
    };

}
