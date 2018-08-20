package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.assertj.core.api.Condition;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.client.ServiceGroupClient;
import org.groundwork.rs.dto.*;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.filter.NotFilter.not;

public class ServiceIT extends AbstractIntegrationTest {

    public static final String QUERY_BULK_SERVICES = "description like '" + BULK_SERVICE_PREFIX + "%'";

    @Test
    public void testBulkCrudOnDataMembersAndProperties() {
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        int expectedHostCount = 2;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, expectedHostCount);
        int expectedCount = 20;
        // test inserts
        IntegrationTestContext<DtoService> serviceContext = null;
        String lastHostName = null;
        for (DtoHost host : hosts.getHosts()) {
            IntegrationTestContext<DtoService> context = populateServices(sc, expectedCount, host.getHostName());
            List<DtoService> services = sc.query("hostname = '" + host.getHostName() + "'");
            assertThat(services).hasSize(expectedCount);
            ServiceTestGenerator.assertServices(services, context);
            serviceContext = context;
            lastHostName = host.getHostName();
        }
        // test counts
        List<DtoService> dtoServices = sc.query(QUERY_BULK_SERVICES);
        assertThat(dtoServices).hasSize(expectedCount * expectedHostCount);
        // assert we now have ids
        assertThat(dtoServices).filteredOn("id", not(null));

        // test updates
        DtoService update = serviceContext.lookupResult(ServiceTestGenerator.makeServiceKey(lastHostName, serviceContext.formatNameKey(1)));
        assertThat(update).isNotNull();
        // update the values in the context directly
        update.setDomain("NEW_DOMAIN");
        update.setMetricType("hypervisor");
        update.setCheckType("PASSIVE");
        update.setStateType("UNKNOWN");
        update.setNextCheckTime(ServiceTestGenerator.dateWithoutMilliseconds(new Date()));
        update.putProperty("ExecutionTime", 4000.5); // new prop
        update.putProperty("LastPluginOutput", "22.output"); // update prop
        // test state change
        update.setMonitorStatus(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        update.setLastStateChange(ServiceTestGenerator.dateWithoutMilliseconds(new Date()));
        DtoServiceList updates = new DtoServiceList();
        updates.add(update);
        // update database ...
        DtoOperationResults results = sc.post(updates);
        assertThat(results.getSuccessful()).isEqualTo(1);
        // validate updates
        List<DtoService> services = sc.query("hostname = '" + lastHostName + "'");
        ServiceTestGenerator.assertServices(services, serviceContext);

        // cascade delete all bulk hosts
        hc.delete(hosts);

        // verify delete
        dtoServices = sc.query(QUERY_BULK_SERVICES);
        assertThat(dtoServices).hasSize(0);

    }

    @Test
    public void testAutocomplete() throws Exception {
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        int expectedHostCount = 1;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, expectedHostCount);
        String hostName = hosts.getHosts().get(0).getHostName();
        // create test data
        IntegrationTestContext<DtoService> context1 = populateServices(sc, 12, hostName, BULK_SERVICE_PREFIX);
        IntegrationTestContext<DtoService> context2 = populateServices(sc, 10, hostName, BULK_SERVICE_PREFIX2);


        // build comparison array for batch 1, could really use java8 ...
        List<DtoName> names1 = new ArrayList<>();
        for (DtoService service : context1.getEntities()) {
            // could use java8 here ahem
            names1.add(new DtoName(service.getDescription()));
        }
        List<DtoName> names2 = new ArrayList<>();
        for (DtoService service : context2.getEntities()) {
            // could use java8 here ahem
            names2.add(new DtoName(service.getDescription()));
        }

        // let the autoname cache catch up..
        Thread.sleep(250);

        // test auto complete on first batch
        List<DtoName> suggestions = sc.autocomplete(BULK_SERVICE_PREFIX, 12); // limit defaults to 10
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(context1.getCount());
        assertThat(suggestions).containsAll(names1);

        // test auto complete on second batch
        suggestions = sc.autocomplete(BULK_SERVICE_PREFIX2);
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(context2.getCount());
        assertThat(suggestions).containsAll(names2);


        suggestions = sc.autocomplete("zzz");
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(0);

        // cascade delete all bulk hosts
        hc.delete(hostName);

        // let the autoname cache catch up..
        Thread.sleep(250);

        // verify cache cleared
        suggestions = hc.autocomplete(BULK_HOST_PREFIX, 20);
        assertThat(suggestions).hasSize(0);
        suggestions = hc.autocomplete(BULK_HOST_PREFIX2);
        assertThat(suggestions).hasSize(0);

    }

    @Test
    public void testListAndPaging() throws Exception {
        int total = 14;
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        int expectedHostCount = 1;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, expectedHostCount);
        String hostName = hosts.getHosts().get(0).getHostName();
        // create test data
        IntegrationTestContext<DtoService> context = populateServices(sc, total, hostName, BULK_SERVICE_PREFIX);


        // list by all depths
        context.setSkipUnmatchedResults(true);
        List<DtoService> dtoServices = sc.list();
        assertThat(dtoServices).filteredOn(serviceFilter).hasSize(total);
        ServiceTestGenerator.assertServices(dtoServices, context);

        // paging testing
        int pageSize = 10;
        int start = 0;
        dtoServices = sc.query("agentid = 'IT_AGENT'", start, pageSize);
        assertThat(dtoServices).filteredOn(serviceFilter).hasSize(pageSize);
        ServiceTestGenerator.assertServices(dtoServices, context, DtoDepthType.Simple);
        start += pageSize;
        dtoServices = sc.query("agentid = 'IT_AGENT'", start, pageSize);
        assertThat(dtoServices).filteredOn(serviceFilter).hasSize(total - pageSize);
        ServiceTestGenerator.assertServices(dtoServices, context);

        // list by host name
        dtoServices = sc.list(hostName);
        assertThat(dtoServices).hasSize(total);
        ServiceTestGenerator.assertServices(dtoServices, context);

        hc.delete(hosts);
    }

    @Test
    public void testLookup() throws Exception {
        int total = 20;
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        int expectedHostCount = 1;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, expectedHostCount);
        String hostName = hosts.getHosts().get(0).getHostName();
        // create test data
        IntegrationTestContext<DtoService> context = populateServices(sc, total, hostName, BULK_SERVICE_PREFIX);
        Integer id = 0;
        for (int ix = 0; ix < total; ix++) {
            String serviceName = context.formatNameKey(ix + 1);
            DtoService service = sc.lookup(serviceName, hostName);
            assertThat(service).isNotNull();
            ServiceTestGenerator.assertService(service, context);
            id = service.getId();
        }
        // test query by id
        List<DtoService> services = sc.query("id=" + id);
        assertThat(services).hasSize(1);

        DtoService shouldExist = sc.lookup(id);
        assertThat(shouldExist).isNotNull();

        DtoService shouldNotExist = sc.lookup(999_999_999);
        assertThat(shouldNotExist).isNull();

        DtoService notFound = sc.lookup("Not", "Found");
        assertThat(notFound).isNull();
        
        hc.delete(hostName);
    }

    static String SERVICE_MONITOR_STATUS[] = {
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.WARNING,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL

    };

    @Test
    public void testQueryMonitorStatus() throws Exception {
        int hostCount = 3;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, hostCount);
        int servicesPerHost = SERVICE_MONITOR_STATUS.length;
        ServiceClient sc = new ServiceClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);

        // create test data
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, servicesPerHost);
        context.setMonitorStatuses(SERVICE_MONITOR_STATUS);
        for (DtoHost host : hosts.getHosts()) {
            executeInserts(sc, context, host, servicesPerHost);
        }
        // test out queries by status across multiple hosts
        List<DtoService> dtoServices = sc.query("monitorStatus = '" + MonitorStatusBubbleUp.OK  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoServices).hasSize(6 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);
        dtoServices = sc.query("monitorStatus = '" + MonitorStatusBubbleUp.WARNING  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoServices).hasSize(1 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);
        dtoServices = sc.query("monitorStatus = '" + MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL  +"' and agentId = '" + AGENT_ID + "'");
        assertThat(dtoServices).hasSize(3 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);

        hc.delete(hosts);
    }

    static Double HOST_EXECUTION_TIME[] = {
            2010.0,
            2000.0, 42000.0, 2010.0, 42010.0,
            2020.0, 42020.0, 2030.0, 43030.0,
            42010.0
    };
    static String SERVICE_MONITOR_STATUS2[] = {
            MonitorStatusBubbleUp.WARNING,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.OK,
            MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL
    };

    @Test
    public void testQueryProperty() throws Exception {
        int hostCount = 3;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, hostCount);
        int servicesPerHost = SERVICE_MONITOR_STATUS.length;
        ServiceClient sc = new ServiceClient(getDeploymentURL());

        // create test data
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, servicesPerHost);
        context.setPropertyValues("ExecutionTime", HOST_EXECUTION_TIME);
        context.setMonitorStatuses(SERVICE_MONITOR_STATUS2);
        for (DtoHost host : hosts.getHosts()) {
            executeInserts(sc, context, host, servicesPerHost);
        }

        List<DtoService> dtoServices = sc.query("(property.ExecutionTime between 2000 and 10000 and (monitorStatus = 'OK') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoServices).hasSize(4 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);
        dtoServices = sc.query("(property.ExecutionTime between 2000 and 10000 and (monitorStatus <> 'OK') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoServices).hasSize(1 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);

        dtoServices = sc.query("(property.ExecutionTime between 42000 and 52000 and (monitorStatus = 'OK') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoServices).hasSize(4 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);
        dtoServices = sc.query("(property.ExecutionTime between 42000 and 52000 and (monitorStatus <> 'OK') and (agentId = '" + AGENT_ID + "')) order by property.ExecutionTime");
        assertThat(dtoServices).hasSize(1 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);

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
    public void testQueryDateRanges() throws Exception {
        int hostCount = 4;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, hostCount);
        int servicesPerHost = HOST_LAST_CHECK_TIME.length;
        ServiceClient sc = new ServiceClient(getDeploymentURL());

        // create test data
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, servicesPerHost);
        context.setPropertyValues("LastCheckTime", HOST_LAST_CHECK_TIME);
        context.setMonitorStatuses(SERVICE_MONITOR_STATUS2);
        for (DtoHost host : hosts.getHosts()) {
            executeInserts(sc, context, host, servicesPerHost);
        }

        List<DtoService> dtoServices = sc.query("year(lastCheckTime) = 2018 and month(lastCheckTime) between 6 and 11 and agentId = '" + AGENT_ID + "' order by lastCheckTime");
        assertThat(dtoServices).hasSize(6 * hostCount);
        ServiceTestGenerator.assertServices(dtoServices, context);

        hc.delete(hosts);
    }

    @Test
    public void testAsync() throws Exception {
        int hostCount = 5;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, hostCount);
        int servicesPerHost = SERVICE_MONITOR_STATUS.length;
        ServiceClient sc = new ServiceClient(getDeploymentURL());

        // create test data
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, servicesPerHost);
        context.setPropertyValues("LastCheckTime", HOST_LAST_CHECK_TIME);
        context.setMonitorStatuses(SERVICE_MONITOR_STATUS);
        context.setPropertyValues("ExecutionTime", HOST_EXECUTION_TIME);
        for (DtoHost host : hosts.getHosts()) {
            context.setOwner(host.getHostName());
            DtoServiceList services = ServiceTestGenerator.buildServiceInserts(context);
            DtoOperationResults results = sc.postAsync(services);
            assertThat(results.getSuccessful()).isEqualTo(1);
        }

        Thread.sleep(2000);

        // validate inserts async per host
        for (DtoHost host : hosts.getHosts()) {
            List<DtoService> dtoServices = sc.query("hostname = '" + host.getHostName() + "'");
            assertThat(dtoServices.size()).as("async service count of %d", context.getCount()).isEqualTo(context.getCount());
            ServiceTestGenerator.assertServices(dtoServices, context);
        }

        // assert all values are correct across hosts
        List<DtoService> dtoServices = sc.query("servicedescription like '" + BULK_SERVICE_PREFIX + "%'");
        assertThat(dtoServices.size()).as("async service count of %d", context.getResultsSize()).isEqualTo(context.getResultsSize());
        ServiceTestGenerator.assertServices(dtoServices, context);

        hc.delete(hosts);
    }

    @Test
    public void testDeletes() throws Exception {
        int hostCount = 4;
        HostClient hc = new HostClient(getDeploymentURL());
        DtoHostList hosts = populateHosts(hc, hostCount);
        int servicesPerHost = SERVICE_MONITOR_STATUS.length;
        ServiceClient sc = new ServiceClient(getDeploymentURL());

        // create test data
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(BULK_SERVICE_PREFIX, AGENT_ID, 1, servicesPerHost);
        context.setPropertyValues("LastCheckTime", HOST_LAST_CHECK_TIME);
        context.setMonitorStatuses(SERVICE_MONITOR_STATUS);
        context.setPropertyValues("ExecutionTime", HOST_EXECUTION_TIME);
        DtoServiceList[] batches = new DtoServiceList[hostCount];
        int ix = 0;
        for (DtoHost host : hosts.getHosts()) {
            batches[ix] = executeInserts(sc, context, host, servicesPerHost);
            ix++;
        }

        Thread.sleep(500);

        // delete by service list
        sc.delete(batches[0]);
        int expectedSize = servicesPerHost * (hostCount - 1);
        List<DtoService> dtoServices = sc.query("servicedescription like '" + BULK_SERVICE_PREFIX + "%'");
        assertThat(dtoServices.size()).as("after delete(1) service count of %d", expectedSize).isEqualTo(expectedSize);

        // delete by service list of strings and host name
        List<String> serviceNames = ServiceTestGenerator.reduceToNames(batches[1].getServices());
        sc.delete(serviceNames, hosts.getHosts().get(1).getHostName());
        expectedSize = servicesPerHost * (hostCount - 2);
        dtoServices = sc.query("servicedescription like '" + BULK_SERVICE_PREFIX + "%'");
        assertThat(dtoServices.size()).as("after delete(2) service count of %d", expectedSize).isEqualTo(expectedSize);

        // delete by service, name pair
        serviceNames = ServiceTestGenerator.reduceToNames(batches[2].getServices());
        String hostName = hosts.getHosts().get(2).getHostName();
        for (String serviceName : serviceNames) {
            sc.delete(serviceName, hostName);
        }
        expectedSize = servicesPerHost * (hostCount - 3);
        dtoServices = sc.query("servicedescription like '" + BULK_SERVICE_PREFIX + "%'");
        assertThat(dtoServices.size()).as("after delete(2) service count of %d", expectedSize).isEqualTo(expectedSize);
        ServiceTestGenerator.assertServices(batches[3].getServices(), context);

        hc.delete(hosts);
    }

    protected DtoServiceList executeInserts(ServiceClient sc, IntegrationTestContext<DtoService> context, DtoHost host, int servicesPerHost) {
        context.setOwner(host.getHostName());
        DtoServiceList services = ServiceTestGenerator.buildServiceInserts(context);
        DtoOperationResults results = sc.post(services);
        assertThat(results.getSuccessful()).isEqualTo(servicesPerHost);
        return services;
    }

    Condition<DtoService> serviceFilter = new Condition<DtoService>() {
        @Override
        public boolean matches(DtoService service) {
            return service.getDescription().startsWith(BULK_SERVICE_PREFIX);
        }
    };


    static DtoHostList populateHosts(HostClient hc, int count) {
        // create the owning hosts
        IntegrationTestContext<DtoHost> hostContext = new IntegrationTestContext(BULK_HOST_PREFIX, AGENT_ID, 1, count);
        DtoHostList hosts = HostTestGenerator.buildHostInserts(hostContext);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(hostContext.getCount());
        return hosts;
    }

    static IntegrationTestContext<DtoService> populateServices(ServiceClient sc, int expectedCount, String hostName) {
        return populateServices(sc, expectedCount, hostName, null, null);
    }


    static IntegrationTestContext<DtoService> populateServices(ServiceClient sc, int expectedCount, String hostName, String prefix) {
        return populateServices(sc, expectedCount, hostName, prefix, null);
    }

    static IntegrationTestContext<DtoService> populateServices(ServiceClient sc, int expectedCount, String hostName, String prefix, String[] statuses) {
        String servicePrefix = (prefix == null) ? BULK_SERVICE_PREFIX : prefix;
        IntegrationTestContext<DtoService> context = new IntegrationTestContext(servicePrefix, AGENT_ID, 1, expectedCount);
        context.setOwner(hostName);
        if (statuses != null) {
            context.setMonitorStatuses(statuses);
        }
        DtoServiceList services = ServiceTestGenerator.buildServiceInserts(context);
        DtoOperationResults results = sc.post(services);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        return context;
    }

}
