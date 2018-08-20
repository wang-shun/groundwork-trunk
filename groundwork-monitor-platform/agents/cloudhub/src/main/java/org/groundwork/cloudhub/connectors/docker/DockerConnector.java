/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors.docker;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.groundwork.cloudhub.connectors.docker.client.*;
import org.groundwork.cloudhub.connectors.vmware.VMwareConfigurationProvider;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.*;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service(DockerConnector.NAME)
@Scope("prototype")
public class DockerConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public static final String HOST_SYNTHETIC_PREFIX = "host:";
    private static Logger log = Logger.getLogger(DockerConnector.class);

    public static final String NAME = "DockerConnector";
    public static final String METRIC_NOT_FOUND = "Docker Metric not found ";

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private DockerConnection connection = null;
    private DataCenterInventory inventory = null;
    private String cAdvisorVersion;
    private int apiLevel = 0;

    @Resource(name = ProfileService.NAME)
    protected ProfileService profileService;

    @Autowired
    protected MetricsUtils metricsUtils;
    @Autowired
    protected MetricsPostProcessor postProcessor;

    @Value("${docker.stats.sample.size:3}")
    protected int statsSampleSize;

    @Value("${docker.v2.enabled:false}")
    protected boolean v2Enabled;

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        connectionState = ConnectionState.CONNECTING;
        connection = ((DockerConnection) monitorConnection);
        try {
            VersionClient versionClient = new VersionClient(connection);
            cAdvisorVersion = versionClient.getVersionInfo();
            apiLevel = (StringUtils.isEmpty(cAdvisorVersion) ? 1  : 2);
        }
        catch (Exception e) {
            log.info("Docker: failed to connect to version API: " + e.getMessage());
            apiLevel = 1;
        }
        try {
            MachineClient client = new MachineClient(connection, apiLevel);
            DockerMachineInfo info = client.getMachineInfo();
            connectionState = ConnectionState.CONNECTED;
        }
        catch (Exception e) {
            if (e instanceof ConnectorException)
                throw e;
            connectionState = ConnectionState.FAILED;
            throw e;
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException
    {
        long startTime = System.currentTimeMillis();
        MonitoringState monitoringState = new MonitoringState();
        if (priorState == null)
            priorState = new MonitoringState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("Docker: collectMetrics(): not connected");
            return priorState;
        }

        gatherInventory();

        MetricViewDefinitions engineView = new MetricViewDefinitions(new ConfigurationView(DockerConfigurationProvider.DOCKER_ENGINE, true, false), hostQueries, true, true);
        MetricViewDefinitions containerView = new MetricViewDefinitions(new ConfigurationView(DockerConfigurationProvider.DOCKER_CONTAINER, true, false), vmQueries, true, true);

        IMetricsClient metricsClient = (v2Enabled && apiLevel > 1) ? new MetricsClient2(connection, statsSampleSize) : new MetricsClient(connection, apiLevel);
        for (InventoryContainerNode engine : inventory.getHypervisors().values()) {
            DockerHost host = new DockerHost(engine.getName());
            DockerHost priorHost = (DockerHost)priorState.hosts().get(engine.getName());
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }
            // Gather engine metrics
            engineView.getQueryMap();
            Set<String> engineQueryNames = MetricsUtils.filterSynthetics(engineView.getQueryMap());
            List<DockerMetricInfo> engineMetrics = metricsClient.retrieveDockerEngineMetrics(engine.getName(), engineQueryNames);
            processMetrics(engineMetrics, engineView, host, priorHost);

            // process synthetics
            postProcessor.processSynthetics(host, engineView, monitoringState.getState());

            // gather container metrics
            for (VirtualMachineNode container : engine.getVms().values()) {
                DockerVM dockerVM = new DockerVM(container.getName());
                dockerVM.setSystemName(container.getSystemName());
                DockerVM priorVM = null;
                if (priorHost != null) {
                    priorVM = (DockerVM)priorHost.getVM(container.getName());
                    if (priorVM != null) {
                        dockerVM.setPrevRunState(priorVM.getRunState());
                    }
                }
                Set<String> containerQueryNames = MetricsUtils.filterSynthetics(containerView.getQueryMap());
                List<DockerMetricInfo> metrics = metricsClient.retrieveContainerMetrics(container.getName(), container.getSystemName(), containerQueryNames);
                processMetrics(metrics, containerView, dockerVM, priorVM);

                // process synthetics
                postProcessor.processSynthetics(dockerVM, containerView, monitoringState.getState());

                dockerVM.setRunState(GwosStatus.UP.status);
                dockerVM.setRunExtra(GwosStatus.UP.status);
                host.putVM(container.getName(), dockerVM);
            }

            host.setRunState(GwosStatus.UP.status);
            host.setRunExtra(GwosStatus.UP.status);
            monitoringState.hosts().put(engine.getName(), host);
        }

        // clean up pass, remove unmonitored metrics
        metricsUtils.crushDownMetrics(monitoringState.hosts());

        if (log.isDebugEnabled()) {
            log.debug("Docker collectMetrics completed in " + (startTime - System.currentTimeMillis()) + " ms");
        }
        for (MetricFaultInfo metric : metricsClient.getMetricFaults()) {
            BaseQuery baseQuery = metric.isContainer() ? containerView.getQueryMap().get(metric.getQuery()) : engineView.getQueryMap().get(metric.getQuery());
            if (baseQuery == null || baseQuery.isMonitored() == false) {
                continue;
            }
            log.error("Docker Metric not found for query: " + metric.getQuery());
            monitoringState.events().add(new MonitoringEvent(metric.getHost(), metric.getQuery(), METRIC_NOT_FOUND + metric.getQuery()));
        }
        return monitoringState;
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (connection == null) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new DockerInventoryBrowser(connection, getApiLevel());
        InventoryOptions options = new InventoryOptions(true, false ,false ,false, false, "");
        inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public String getcAdvisorVersion() {
        return cAdvisorVersion;
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        CloudHubProfile profile = (CloudHubProfile) profileService.readProfileTemplate(VirtualSystem.DOCKER);
        List<String> names = new LinkedList<>();
        if (serviceType.equals(VMwareConfigurationProvider.VM)) {
            for (Metric metric : profile.getVm().getMetrics()) {
                if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                    names.add(metric.getName());
                }
            }
        } else if (serviceType.equals(VMwareConfigurationProvider.HYPERVISOR)) {
            for (Metric metric : profile.getVm().getMetrics()) {
                if (StringUtils.isEmpty(metric.getSourceType()) && (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic))) {
                    names.add(metric.getName());
                }
            }
        }
        return names;
    }

    protected void processMetrics(List<DockerMetricInfo> metrics, MetricViewDefinitions view, MetricProvider current, MetricProvider prior) {
        for (DockerMetricInfo metric : metrics) {
            BaseQuery query = view.getQueryMap().get(metric.meter);
            if (query != null) {
                String translatedMetricName = query.getQuery();
                BaseMetric baseMetric = new BaseMetric(query, translatedMetricName);
                baseMetric.setCustomName(query.getCustomName());
                baseMetric.setValue(metric.metricToString());
                if (prior != null) {
                    BaseMetric priorMetric = prior.getMetric(translatedMetricName);
                    if (priorMetric != null) {
                        baseMetric.setLastState(priorMetric.getCurrState());
                    }
                }
                current.putMetric(translatedMetricName, baseMetric);
            }
        }
    }
}
