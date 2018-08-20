package org.groundwork.rs.it;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceKey;
import org.groundwork.rs.dto.DtoServiceList;
import org.groundwork.rs.dto.DtoStateTransition;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.groundwork.rs.it.ServiceTestGenerator.makeServiceKey;

/**
 * EventIT
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class EventIT extends AbstractIntegrationTest {

    private static final DateFormat DATE_FORMAT_US = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");

    @Test
    public void testEventStateTransitions() throws Exception {
        HostClient hc = new HostClient(getDeploymentURL());
        ServiceClient sc = new ServiceClient(getDeploymentURL());
        EventClient ec = new EventClient(getDeploymentURL());

        DtoHostList hosts = null;
        try {
            long now = System.currentTimeMillis();
            String startDate = DATE_FORMAT_US.format(new Date(now-3600000L));
            String endDate = DATE_FORMAT_US.format(new Date(now));
            // ensure state transitions do not exist
            String hostName = BULK_HOST_PREFIX+"-00001";
            List<DtoStateTransition> stateTransitions = ec.getStateTransitions(hostName, null, startDate, endDate);
            assertThat(stateTransitions.size()).isEqualTo(0);
            // populate hosts, services, and events
            hosts = populateHosts(hc);
            DtoServiceList allServices = new DtoServiceList();
            for (DtoHost host : hosts.getHosts()) {
                populateEvents(ec, host.getHostName(), null);
                DtoServiceList services = populateServices(sc, host.getHostName());
                allServices.getServices().addAll(services.getServices());
                for (DtoService service : services.getServices()) {
                    populateEvents(ec, service.getHostName(), service.getDescription());
                }
            }
            assertThat(hosts.getHosts().get(0).getHostName()).isEqualTo(hostName);
            // wait for state transitions to be available in current window
            for (long waitStart = System.currentTimeMillis(); System.currentTimeMillis()-waitStart < 30000L;) {
                now = System.currentTimeMillis();
                startDate = DATE_FORMAT_US.format(new Date(now - 3600000L));
                endDate = DATE_FORMAT_US.format(new Date(now));
                stateTransitions = ec.getStateTransitions(hostName, null, startDate, endDate);
                if (stateTransitions.size() == 3) {
                    break;
                }
                Thread.sleep(1000);
            }
            assertThat(stateTransitions).hasSize(3);
            // test host state transitions query
            stateTransitions = ec.getStateTransitions(hostName, null, startDate, endDate);
            assertHostStateTransitions(hostName, stateTransitions);
            // test service state transitions query
            String serviceHostName = allServices.getServices().get(0).getHostName();
            String serviceName = allServices.getServices().get(0).getDescription();
            stateTransitions = ec.getStateTransitions(serviceHostName, serviceName, startDate, endDate);
            assertServiceStateTransitions(serviceHostName, serviceName, stateTransitions);
            // test hosts and services state transitions query
            List<DtoServiceKey> hostServiceKeys = new ArrayList<>();
            for (DtoHost host : hosts.getHosts()) {
                hostServiceKeys.add(new DtoServiceKey(null, host.getHostName()));
            }
            for (DtoService service : allServices.getServices()) {
                hostServiceKeys.add(new DtoServiceKey(service.getDescription(), service.getHostName()));
            }
            Map<DtoServiceKey, List<DtoStateTransition>> stateTransitionsMap = ec.getStateTransitions(hostServiceKeys,
                    startDate, endDate);
            assertThat(stateTransitionsMap).isNotNull();
            assertThat(stateTransitionsMap).hasSize(6);
            assertThat(stateTransitionsMap).containsKey(new DtoServiceKey(null, hostName));
            stateTransitions = stateTransitionsMap.get(new DtoServiceKey(null, hostName));
            assertHostStateTransitions(hostName, stateTransitions);
            assertThat(stateTransitionsMap).containsKey(new DtoServiceKey(serviceName, serviceHostName));
            stateTransitions = stateTransitionsMap.get(new DtoServiceKey(serviceName, serviceHostName));
            assertServiceStateTransitions(serviceHostName, serviceName, stateTransitions);
        } finally {
            // cleanup hosts, services, and events
            if (hosts != null) {
                hc.delete(hosts);
            }
        }
    }

    private DtoHostList populateHosts(HostClient hc) {
        IntegrationTestContext<DtoHost> context = new IntegrationTestContext<>(BULK_HOST_PREFIX, AGENT_ID, 1, 2, null,
                new String[]{MonitorStatusBubbleUp.UP, MonitorStatusBubbleUp.UP});
        DtoHostList hosts = HostTestGenerator.buildHostInserts(context);
        DtoOperationResults results = hc.post(hosts);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        return hosts;
    }

    private DtoServiceList populateServices(ServiceClient sc, String hostName) {
        IntegrationTestContext<DtoService> context = new IntegrationTestContext<>(BULK_SERVICE_PREFIX, AGENT_ID, 1, 2,
                hostName, new String[]{MonitorStatusBubbleUp.OK, MonitorStatusBubbleUp.OK});
        DtoServiceList services = ServiceTestGenerator.buildServiceInserts(context);
        DtoOperationResults results = sc.post(services);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
        return services;
    }

    private void populateEvents(EventClient ec, String hostName, String serviceName) {
        IntegrationTestContext<DtoEvent> context;
        if (serviceName == null) {
            // host events
            context = new IntegrationTestContext<>(null, AGENT_ID, 1, 4, hostName,
                    new String[]{MonitorStatusBubbleUp.PENDING, MonitorStatusBubbleUp.UP, MonitorStatusBubbleUp.DOWN,
                            MonitorStatusBubbleUp.UP});
        } else {
            // service events
            context = new IntegrationTestContext<>(null, AGENT_ID, 1, 4, makeServiceKey(hostName, serviceName),
                    new String[]{MonitorStatusBubbleUp.PENDING, MonitorStatusBubbleUp.OK, MonitorStatusBubbleUp.CRITICAL,
                            MonitorStatusBubbleUp.OK});
        }
        DtoEventList events = EventTestGenerator.buildEventInserts(context);
        DtoOperationResults results = ec.post(events);
        assertThat(results.getSuccessful()).isEqualTo(context.getCount());
    }

    private void assertHostStateTransitions(String hostName, List<DtoStateTransition> stateTransitions) {
        assertThat(stateTransitions).isNotNull();
        assertThat(stateTransitions).hasSize(3);
        assertThat(stateTransitions.get(0).getHostName()).isEqualTo(hostName);
        assertThat(stateTransitions.get(0).getServiceName()).isNull();
        assertThat(stateTransitions.get(0).getFromStatus()).isNotNull();
        assertThat(stateTransitions.get(0).getFromStatus().getName()).isEqualTo(MonitorStatusBubbleUp.PENDING);
        assertThat(stateTransitions.get(0).getFromTransitionDate());
        assertThat(stateTransitions.get(0).getToStatus()).isNotNull();
        assertThat(stateTransitions.get(0).getToStatus().getName()).isEqualTo(MonitorStatusBubbleUp.UP);
        assertThat(stateTransitions.get(0).getToTransitionDate()).isNotNull();
        assertThat(stateTransitions.get(0).getDurationInState()).isNotNull();
        assertThat(stateTransitions.get(1).getFromStatus().getName()).isEqualTo(MonitorStatusBubbleUp.UP);
        assertThat(stateTransitions.get(1).getToStatus().getName()).isIn(MonitorStatusBubbleUp.DOWN,
                MonitorStatusBubbleUp.UNSCHEDULED_DOWN);
        assertThat(stateTransitions.get(2).getFromStatus().getName()).isIn(MonitorStatusBubbleUp.DOWN,
                MonitorStatusBubbleUp.UNSCHEDULED_DOWN);
        assertThat(stateTransitions.get(2).getToStatus().getName()).isEqualTo(MonitorStatusBubbleUp.UP);
    }

    private void assertServiceStateTransitions(String serviceHostName, String serviceName,
                                               List<DtoStateTransition> stateTransitions) {
        assertThat(stateTransitions).isNotNull();
        assertThat(stateTransitions).hasSize(3);
        assertThat(stateTransitions.get(0).getHostName()).isEqualTo(serviceHostName);
        assertThat(stateTransitions.get(0).getServiceName()).isEqualTo(serviceName);
        assertThat(stateTransitions.get(0).getFromStatus()).isNotNull();
        assertThat(stateTransitions.get(0).getFromStatus().getName()).isEqualTo(MonitorStatusBubbleUp.PENDING);
        assertThat(stateTransitions.get(0).getFromTransitionDate());
        assertThat(stateTransitions.get(0).getToStatus()).isNotNull();
        assertThat(stateTransitions.get(0).getToStatus().getName()).isEqualTo(MonitorStatusBubbleUp.OK);
        assertThat(stateTransitions.get(0).getToTransitionDate()).isNotNull();
        assertThat(stateTransitions.get(0).getDurationInState()).isNotNull();
        assertThat(stateTransitions.get(1).getFromStatus().getName()).isEqualTo(MonitorStatusBubbleUp.OK);
        assertThat(stateTransitions.get(1).getToStatus().getName()).isIn(MonitorStatusBubbleUp.CRITICAL,
                MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        assertThat(stateTransitions.get(2).getFromStatus().getName()).isIn(MonitorStatusBubbleUp.CRITICAL,
                MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        assertThat(stateTransitions.get(2).getToStatus().getName()).isEqualTo(MonitorStatusBubbleUp.OK);
    }
}
