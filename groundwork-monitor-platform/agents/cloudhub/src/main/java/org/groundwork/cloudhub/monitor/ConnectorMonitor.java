package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.gwos.GWOSHostGroup;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.gwos.messages.SuspendedStatusMessages;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Monitor a Groundwork Connector
 * <p>
 * On completion of a successful monitoring run, send heart beats with each successful monitoring collection to a service
 * - status is set to OK
 * - value is set to 0
 * <p>
 * When there are failures on the connector (not GWOS failures)
 * the service's MonitorStatus is set to WARNING until threshold met
 * When the threshold is met, the MonitorStatus is set to CRITICAL
 * Events and notifications are sent with level Warning or Critical depending on threshold
 * <p>
 * Services are set to warning when retries count is less than retry limit
 * Services are set to critical when retries count is greater than or equal to retry limit
 * Retry limit comes from the configuration of the connector. If the configuration is -1, use a Cloudhub default property as retry limit
 * Each connector is stored as a service under the provisioned hostname for the connector
 * Each connector is name-spaced with a service name as connector-type + "-" + displayName
 */
@Service("ConnectorMonitor")
public class ConnectorMonitor {

    protected static Logger log = Logger.getLogger(ConnectorMonitor.class);

    public static final String SYSTEM_APPLICATION_TYPE = "SYSTEM";
    public static final String CLOUDHUB_ALIAS = "cloudhub";
    public static final String CONNECTOR_METRIC_TYPE = "connector";

    @Value("${monitoringRetryThreshold:5}")
    protected Integer monitoringRetryThreshold = 5;

    @Value("${connectorsHostGroup:CloudHub}")
    protected String connectorsHostGroup;
    @Value("${connectorsHostGroupDescription:Connectors Host Group}")
    protected String connectorsHostGroupDescription;


    public ConnectorMonitorState updateGroundworkConnector(GwosService gwosService, int retry, ConnectorMonitorState state) {
        ConnectionConfiguration configuration = gwosService.getConnection();
        if (configuration.getGwos().getMonitor() == false) {
            return state;
        }
        if (log.isInfoEnabled()) log.info("Updating Groundwork Connector (" + state.toString() + ")...");

        CloudhubAgentInfo agentInfo = gwosService.getAgentInfo();
        BaseHost host = new BaseHost(configuration.getGwos().getGwosServer());
        host.setRunningState(GwosStatus.UP.status);
        host.setPrevRunState(state.getLastHostState());

        // create the service with threshold logic
        String serviceName = buildServiceName(agentInfo, configuration);
        int threshold = configuration.getCommon().getConnectionRetries();
        if (threshold < 0) {
            threshold = monitoringRetryThreshold;
        }
        BaseMetric metric = new BaseMetric(serviceName, 1L, threshold, false, true, null);
        metric.setValue(Integer.toString(retry));
        metric.setLastState(state.getLastServiceState());
        metric.setMetricType(CONNECTOR_METRIC_TYPE);
        if (retry == ConnectorMonitorState.FORCE_MONITOR_SHUTDOWN) {
            metric.setCurrState(GwosServiceStatus.UNKNOWN.status);
            metric.setExplanation(new SuspendedStatusMessages().getServiceHypervisorMessage());
        }
        host.getMetricPool().put(serviceName, metric);

        List<BaseHost> hosts = new ArrayList<>();
        hosts.add(host);

        gwosService.modifyHypervisors(hosts, gwosService.getAgentInfo().getName(), new HashMap<String, String>(), true);
        updateHostGroup(gwosService);
        if (log.isInfoEnabled()) log.info("...Groundwork Connector updated");
        state.setLastHostState(GwosStatus.UP.status);
        state.setLastServiceState(metric.getCurrState());
        return state;
    }

    public static String buildServiceName(CloudhubAgentInfo agentInfo, ConnectionConfiguration configuration) {
        StringBuffer name = new StringBuffer();
        name.append(agentInfo.getConnectorName());
        name.append("-");
        name.append(configuration.getCommon().getDisplayName().replace(" ", "_"));
        return name.toString();
    }

    private void updateHostGroup(GwosService gwosService) {
        String hostName = gwosService.getConnection().getGwos().getGwosServer();
        GWOSHostGroup gwosHostGroup = new GWOSHostGroup(
                connectorsHostGroup,
                connectorsHostGroupDescription,
                CLOUDHUB_ALIAS,
                SYSTEM_APPLICATION_TYPE,
                CLOUDHUB_ALIAS);
        List<String> hosts = new LinkedList<>();
        hosts.add(hostName);
        gwosService.modifyHostGroup(gwosHostGroup, hosts);
    }

}
