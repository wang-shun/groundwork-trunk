package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoService;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HostTestGenerator extends IntegrationTestGenerator {

    public final static String DESC = "Description ";
    public final static String DEVICE = "Device-";

    public final static String APP_TYPE = "VEMA";
    public final static String MONITOR_STATUS = MonitorStatusBubbleUp.UP;
    public final static String MONITOR_SERVER = "localhost";
    public final static String CHECK_TYPE = "ACTIVE";
    public final static String STATE_TYPE = "HARD";

    public static DtoHostList buildHostInserts(IntegrationTestContext<DtoHost> context) {
        DtoHostList hosts = new DtoHostList();
        int max =  context.getStart() + context.getCount();
        int baseIndex = 0;
        for (int ix = context.getStart(); ix < max; ix++) {
            DtoHost host = new DtoHost();
            String index = String.format(FORMAT_NUMBER_SUFFIX, ix);
            String hostName = context.getPrefix() + context.getDelimiter() + index;
            host.setHostName(hostName);
            host.setDescription(DESC + hostName);
            host.setAgentId(context.getAgentId()); //AGENT + prefix);
            if (context.getMonitorStatuses() != null) {
                host.setMonitorStatus(context.getMonitorStatuses()[baseIndex]);
            }
            else {
                host.setMonitorStatus(MONITOR_STATUS);
            }
            host.setAppType(APP_TYPE);
            host.setDeviceIdentification(hostName);
            host.setDeviceDisplayName(DEVICE + hostName);
            host.setMonitorServer(MONITOR_SERVER);
            Date[] lastCheckTimes = (Date[])context.getValuesForProperty("LastCheckTime");
            if (lastCheckTimes != null) {
                host.setLastCheckTime(lastCheckTimes[baseIndex]);
                host.setNextCheckTime(new Date(lastCheckTimes[baseIndex].getTime() + 60000L));
            }
            else {
                host.setLastCheckTime(new Date());
                host.setNextCheckTime(new Date(host.getLastCheckTime().getTime() + 60000L));
            }
            host.setCheckType(CHECK_TYPE);
            host.setStateType(STATE_TYPE);
            // properties
            host.putProperty("LastStateChange", new Date());
            Double[] executionTimes = (Double[])context.getValuesForProperty("ExecutionTime");
            if (executionTimes != null) {
                host.putProperty("ExecutionTime", executionTimes[baseIndex]);
            }
            else {
                host.putProperty("ExecutionTime", 106504.29);
            }
            host.putProperty("ScheduledDowntimeDepth", 0);
            host.putProperty("isProblemAcknowledged", true);
            host.putProperty("UpdatedBy", "monitor-admin");
            host.putProperty("Comments", "Host has been verified and restarted");
            hosts.add(host);
            context.addResult(hostName, host);
            baseIndex = baseIndex + 1;
        }

        return hosts;
    }

    protected static final String SHALLOW_SKIP_INSERTED_HOST_FIELDS[] =  {"id", "appTypeDisplayName",
            "bubbleUpStatus",
            "serviceAvailability",
            "monitorServer",
            "lastStateChange",
            "lastPlugInOutput",
            // Depth = Full fields:
            "device", "hostStatus", "hostGroups", "applicationType", "statistics", "services", "serviceCount"};

    protected static final String SIMPLE_INCLUDE_INSERTED_HOST_FIELDS[] =  {"hostName", "description", "appType", "agentId" };
    // TODO: sync also returns services and 3 properties: Alias, Notes, Parent
    protected static final String SYNC_INCLUDE_INSERTED_HOST_FIELDS[] =  {"hostName", "appType", "deviceIdentification" };

    public static void assertHosts(List<DtoHost> dtoHosts, IntegrationTestContext<DtoHost> context) {
        assertHosts(dtoHosts, context, DtoDepthType.Shallow);
    }

    public static void assertHosts(List<DtoHost> dtoHosts, IntegrationTestContext<DtoHost> context, DtoDepthType depthType) {
        try (AutoCloseableSoftAssertions soft = new AutoCloseableSoftAssertions()) {
            for (DtoHost host : dtoHosts) {
                DtoHost hostBefore = context.lookupResult(host.getHostName());
                if (hostBefore == null && context.getSkipUnmatchedResults()) {
                    continue;
                }
                assertThat(hostBefore).isNotNull();
                String comment = String.format("Comparing Bulk Host %s", host.getHostName());
                if (depthType == null) {
                    depthType = DtoDepthType.Shallow;
                }
                switch (depthType) {
                    case Simple:
                        assertThat(host).as(comment).isEqualToComparingOnlyGivenFields(SIMPLE_INCLUDE_INSERTED_HOST_FIELDS);
                        break;
                    case Shallow:
                    case Deep:
                    case Full:
                        assertThat(host).as(comment).isEqualToIgnoringGivenFields(hostBefore, SHALLOW_SKIP_INSERTED_HOST_FIELDS);
                        break;
                    case Sync:
                        assertThat(host).as(comment).isEqualToComparingOnlyGivenFields(SYNC_INCLUDE_INSERTED_HOST_FIELDS);
                        break;
                }
            }
        }
    }

    public static void assertHostGroups(List<DtoHost> hosts, List<DtoHostGroup> hostGroups)  {
        assertThat(hosts).size().isGreaterThan(0);
        List<String> sourceNames = HostGroupTestGenerator.reduceToNames(hostGroups);
        for (DtoHost host : hosts) {
            List<String> names = HostGroupTestGenerator.reduceToNames(host.getHostGroups());
            assertThat(names).containsOnlyElementsOf(sourceNames);
        }
    }

    public static void assertServices(List<DtoHost> hosts, IntegrationTestContext<DtoService> context, int expectedCount)  {
        for (DtoHost host : hosts) {
            assertThat(host.getServiceCount()).isEqualTo(expectedCount) ;
            for (DtoService service : host.getServices()) {
                DtoService serviceBefore = context.lookupResult(ServiceTestGenerator.makeServiceKey(host.getHostName(), service.getDescription()));
                assertThat(serviceBefore).isNotNull();
                assertThat(service).as("Comparing Bulk Service %s", ServiceTestGenerator.makeServiceKey(host.getHostName(), service.getDescription())).isEqualToIgnoringGivenFields(serviceBefore, ServiceTestGenerator.SHALLOW_SKIP_INSERTED_SERVICE_FIELDS);
            }
        }
    }

}
