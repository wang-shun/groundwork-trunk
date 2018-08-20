package org.groundwork.cloudhub.gwos;

import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AbstractGwosServiceTest {

    protected void createHostAndServices(ConnectionConfiguration configuration, String connectionString,
                                         String hostName, String[] serviceNames, MetricType metricType) {
        HostClient hostClient = new HostClient(connectionString);
        assert hostClient.lookup(hostName) == null;
        DtoHostList updates = new DtoHostList();
        DtoHost host1 = new DtoHost();
        host1.setHostName(hostName);
        host1.setDescription(hostName);
        host1.setMonitorStatus("OK");
        host1.setDeviceIdentification(host1.getHostName());
        host1.setAgentId(configuration.getCommon().getAgentId());
        updates.add(host1);
        DtoOperationResults results = hostClient.post(updates);
        assert results.getFailed() == 0;
        assert results.getSuccessful() == 1;

        ServiceClient serviceClient = new ServiceClient(connectionString);
        DtoServiceList services = new DtoServiceList();
        for (String serviceName : serviceNames) {
            DtoService service = buildServiceUpdate(configuration, serviceName, hostName, metricType);
            services.add(service);
        }
        DtoOperationResults results2 = serviceClient.post(services);
        assert results2.getFailed() == 0;
        assert results2.getSuccessful() > 0;
    }

    protected DtoService buildServiceUpdate(ConnectionConfiguration configuration, String serviceName, String hostName, MetricType metricType) {
        DtoService service = new DtoService();
        service.setDescription(serviceName);
        service.setHostName(hostName);
        service.setMonitorServer(hostName);
        service.setDeviceIdentification(hostName);
        service.setAgentId(configuration.getCommon().getAgentId());
        service.setMetricType(metricType.name());
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        Calendar last = new GregorianCalendar();
        Calendar next = new GregorianCalendar();
        Calendar stateChange = new GregorianCalendar();
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(175.4));
        service.putProperty("ExecutionTime", new Double(200.5));
        service.putProperty("LastPluginOutput", "1.output");
        return service;
    }

}
