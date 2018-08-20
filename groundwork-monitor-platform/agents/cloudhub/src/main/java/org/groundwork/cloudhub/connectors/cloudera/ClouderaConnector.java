package org.groundwork.cloudhub.connectors.cloudera;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.DataView;
import com.cloudera.api.model.*;
import com.cloudera.api.v14.RootResourceV14;
import com.cloudera.api.v14.ServicesResourceV14;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ClouderaConnection;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.base.DiscoveryConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.MetricProvider;
import org.groundwork.cloudhub.metrics.MetricsPostProcessor;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(ClouderaConnector.NAME)
@Scope("prototype")
public class ClouderaConnector extends BaseConnector implements DiscoveryConnector {

    public static final String HOST_SERVICE_IS_IN_A_STOPPED_STATE = "Host is in a stopped state";
    @Resource(name = ConnectorFactory.NAME)
    protected ConnectorFactory connectorFactory;

    @Autowired
    protected MetricsPostProcessor postProcessor;

    @Autowired
    protected MetricNamesProvider namesProvider;

    @Autowired
    protected HealthAggregator healthAggregator;

    @Value("${cloudera.servicename.by.roletype}")
    protected boolean enableServiceNameByRoleType = false;

    private static Logger log = Logger.getLogger(org.groundwork.cloudhub.connectors.cloudera.ClouderaConnector.class);

    public static final String NAME = "ClouderaConnector";

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private DataCenterInventory inventory;

    private RootResourceV14 rootResource;

    public static class HealthInfo {

        public HealthInfo(String gwosStatus) {
            this.gwosStatus = gwosStatus;
        }

        public HealthInfo(String gwosStatus, String runStateExtra) {
            this.gwosStatus = gwosStatus;
            this.runStateExtra = runStateExtra;
        }

        String gwosStatus;
        String runStateExtra;
    }

    private static final Map<ApiHealthSummary,HealthInfo> API_HEALTH_SUMMARY_TO_HOST_STATE = ImmutableMap.<ApiHealthSummary, HealthInfo> builder()
            .put(ApiHealthSummary.HISTORY_NOT_AVAILABLE, new HealthInfo(GwosStatus.UNREACHABLE.status))
            .put(ApiHealthSummary.NOT_AVAILABLE, new HealthInfo(GwosStatus.UNREACHABLE.status))
            .put(ApiHealthSummary.DISABLED, new HealthInfo(GwosStatus.UNREACHABLE.status, "Host health summary has been Disabled"))
            .put(ApiHealthSummary.GOOD, new HealthInfo(GwosStatus.UP.status))
            // CLOUDHUB-351: was WARNING
            .put(ApiHealthSummary.CONCERNING, new HealthInfo(GwosStatus.UP.status, "Host is up, but health state is Concerning"))
            .put(ApiHealthSummary.BAD, new HealthInfo(GwosStatus.UP.status, "Host is up, but health state is Bad"))
            .build();

    private static final Map<ApiHealthSummary,String> API_HEALTH_SUMMARY_TO_METRIC_STATE = ImmutableMap.<ApiHealthSummary, String>builder()
            .put(ApiHealthSummary.HISTORY_NOT_AVAILABLE, BaseMetric.sUnknown)
            .put(ApiHealthSummary.NOT_AVAILABLE, BaseMetric.sUnknown)
            .put(ApiHealthSummary.DISABLED, BaseMetric.sUnknown)
            .put(ApiHealthSummary.GOOD, BaseMetric.sOK)
            .put(ApiHealthSummary.CONCERNING, BaseMetric.sWarning)
            .put(ApiHealthSummary.BAD, BaseMetric.sCritical)
            .build();
    
    private static final Map<ApiEntityStatus,HealthInfo> API_ENTITY_HEALTH_SUMMARY_TO_HOST_STATE = ImmutableMap.<ApiEntityStatus, HealthInfo>builder()
            .put(ApiEntityStatus.UNKNOWN, new HealthInfo(GwosStatus.UNREACHABLE.status))
            .put(ApiEntityStatus.NONE, new HealthInfo(GwosStatus.UNREACHABLE.status))
            .put(ApiEntityStatus.STOPPED, new HealthInfo(GwosStatus.DOWN.status, "Host has been Stopped"))
            .put(ApiEntityStatus.DOWN, new HealthInfo(GwosStatus.DOWN.status))
            // CLOUDHUB-351: was WARNING(4)
            .put(ApiEntityStatus.UNKNOWN_HEALTH, new HealthInfo(GwosStatus.UP.status, "Host is up, but has Unknown health state"))
            .put(ApiEntityStatus.DISABLED_HEALTH, new HealthInfo(GwosStatus.UP.status, "Host is up, but health state checking is Disabled"))
            .put(ApiEntityStatus.CONCERNING_HEALTH, new HealthInfo(GwosStatus.UP.status, "Host is up, but health state is Concerning"))
            .put(ApiEntityStatus.BAD_HEALTH, new HealthInfo(GwosStatus.UP.status, "Host is up, but health state is Bad"))

            .put(ApiEntityStatus.GOOD_HEALTH, new HealthInfo(GwosStatus.UP.status))
            .put(ApiEntityStatus.STARTING, new HealthInfo(GwosStatus.PENDING.status))
            .put(ApiEntityStatus.STOPPING, new HealthInfo(GwosStatus.DOWN.status))
            // CLOUDHUB-351: was WARNING
            .put(ApiEntityStatus.HISTORY_NOT_AVAILABLE, new HealthInfo(GwosStatus.UP.status, "Host is up, but health history is not available"))
            .build();
    
    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        long startTime = System.currentTimeMillis();
        connectionState = ConnectionState.CONNECTING;
        ClouderaConnection connection = (ClouderaConnection) monitorConnection;

        if (log.isDebugEnabled()) log.debug("server=" + connection.getServer() + " user=" + connection.getUsername());
        ClouderaManagerClientBuilder builder =
                new ClouderaManagerClientBuilder()
                        .withHost(connection.getServer())
                        .withPort(connection.getPort())
                        .withUsernamePassword(connection.getUsername(), connection.getPassword())
                        .withConnectionTimeout(connection.getTimeoutMs(), TimeUnit.MILLISECONDS);
        try {
            ApiRootResource apiRootResource = builder.build();
            rootResource = apiRootResource.getRootV14();

            if (log.isDebugEnabled()) {
                log.debug("currentVersion=" + apiRootResource.getCurrentVersion());
            }
            // make sure connection is valid
            ApiClusterList clusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
            for (ApiCluster cluster : clusters) {
                if (connection.getPrefixServiceNames() && !StringUtils.isEmpty(cluster.getDisplayName()) && cluster.getDisplayName().contains(" ")) {
                    throw new ConnectorException("Cluster Display name is not valid, no spaces allowed: [" + cluster.getDisplayName() + "]");
                }
            }
            
            namesProvider.build(rootResource.getTimeSeriesResource());
        } catch (Exception e) {
            log.error("Exception while connecting to " + connection.getServer() + ": " + e);
            connectionState = ConnectionState.DISCONNECTED;
            throw new ConnectorException("Unable to connect to cloudera: " + e.getMessage(), e);
        }
        connectionState = ConnectionState.CONNECTED;
        if (log.isDebugEnabled()) log.debug("Cloudera connect() completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void disconnect() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Disconnecting from cloudera");
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("opening connection to " + monitorConnection.getServer());
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("closing connection");
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    public MonitoringState collectMetricsByQuery(MonitoringState priorState, List<BaseQuery> hostQueries, List<BaseQuery> serviceQueries, List<BaseQuery> customQueries) throws ConnectorException {
        long startTime = System.currentTimeMillis();

        if (priorState == null) priorState = new MonitoringState();

        MonitoringState monitoringState = priorState;

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("Cloudera: collectMetrics(): not connected");
            return priorState;
        }

        if (log.isDebugEnabled()) log.debug("Cloudera collectMetrics completed in " + (System.currentTimeMillis() - startTime) + " ms");
        return monitoringState;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState, List<BaseQuery> hostQueries, List<BaseQuery> serviceQueries, List<BaseQuery> customQueries) throws ConnectorException {
        long startTime = System.currentTimeMillis();

        if (priorState == null) priorState = new MonitoringState();

        MonitoringState monitoringState = priorState;
        monitoringState.resetState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("Cloudera: collectMetrics(): not connected");
            return priorState;
        }

        if (inventory == null) {
            inventory = gatherInventory();
        }
        ClouderaMetricCollector collector = new ClouderaMetricCollector(rootResource, postProcessor, enableServiceNameByRoleType);

        Map<String, MetricViewDefinitions> serviceDefinitions = new HashMap<>();
        for (ConfigurationView view : collectionMode.getViews()) {
            if (view.isService() && view.isEnabled()) {
                serviceDefinitions.put(view.getName(), new MetricViewDefinitions(view, serviceQueries));
            }
        }
        MetricViewDefinitions hostView = new MetricViewDefinitions(ClouderaConfigurationProvider.getView(collectionMode.getViews(), ClouderaConfigurationProvider.CLOUDERA_HOST), hostQueries);
        MetricViewDefinitions clusterView = new MetricViewDefinitions(ClouderaConfigurationProvider.getView(collectionMode.getViews(), ClouderaConfigurationProvider.CLOUDERA_CLUSTER), hostQueries);

        if (hostView.isEnabled()) {
            collectHostMetrics(monitoringState, hostView, collector);
        }

        ApiClusterList clusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
        for (ApiCluster apiCluster : clusters) {
            ClouderaHost cluster = null;
            if (clusterView.isEnabled()) {
                cluster = collectClusterMetrics(monitoringState, clusterView, apiCluster, collector);
            }
            collectServiceMetrics(cluster, monitoringState, serviceDefinitions, apiCluster, collector);
        }

        // optimize logging, consolidate errors
        if (monitoringState.getExceptions().size() > 0) {
            for (Map.Entry<String,String> e : monitoringState.getExceptions().entrySet()) {
                log.warn("Cloudera metric expression error: " + e.getValue());
            }
        }

        if (log.isInfoEnabled()) log.info("Cloudera collectMetrics completed in " + (System.currentTimeMillis() - startTime) + " ms");
        return monitoringState;
    }

    public void collectHostMetrics(MonitoringState monitoringState, MetricViewDefinitions metricDefinitions, ClouderaMetricCollector collector) {
        ApiHostList hosts = rootResource.getHostsResource().readHosts(DataView.FULL_WITH_HEALTH_CHECK_EXPLANATION);
        for (ApiHost apiHost : hosts) {
            String hostName = apiHost.getHostname();
            ClouderaHost host = monitoringState.hosts().containsKey(hostName) ? (ClouderaHost)monitoringState.hosts().get(hostName) : new ClouderaHost(hostName, ClouderaConfigurationProvider.CLOUDERA_HOST);
            host.setDescription(hostName);
            host.setIpAddress(apiHost.getIpAddress());
            translateHealthForHostService(host, apiHost.getHealthSummary(), null);
            collectHealthCheckMetrics(apiHost.getHealthChecks(), host, metricDefinitions, null);

            // collect metrics
            collector.collect(monitoringState.getState(), host, metricDefinitions, "entityName", apiHost.getHostId());
            postProcessor.processSynthetics(host, metricDefinitions, monitoringState.getState());

            // update state
            monitoringState.hosts().put(hostName, host);
            if (log.isDebugEnabled()) log.debug("Added host: " + hostName);
        }
    }

    public ClouderaHost collectClusterMetrics(MonitoringState monitoringState, MetricViewDefinitions metricDefinitions, ApiCluster apiCluster, ClouderaMetricCollector collector) {
        String hostName = getValidClusterName(apiCluster);
        ClouderaHost host = monitoringState.hosts().containsKey(hostName) ? (ClouderaHost)monitoringState.hosts().get(hostName) : new ClouderaHost(hostName, ClouderaConfigurationProvider.CLOUDERA_CLUSTER);
        host.setDescription(hostName);
        translateHealthForHostEntity(host, apiCluster.getEntityStatus());

        // collect metrics
        collector.collect(monitoringState.getState(), host, metricDefinitions, "clusterName", apiCluster.getName());
        postProcessor.processSynthetics(host, metricDefinitions, monitoringState.getState());

        // update state
        monitoringState.hosts().put(hostName, host);
        if (log.isDebugEnabled()) log.debug("Added cluster: " + hostName);
        return host;
    }

    public void collectServiceMetrics(ClouderaHost cluster, MonitoringState monitoringState, Map<String, MetricViewDefinitions> serviceDefinitions, ApiCluster apiCluster, ClouderaMetricCollector collector) {

        ServicesResourceV14 servicesResource = rootResource.getClustersResource().getServicesResource(apiCluster.getName());
        ApiServiceList services = servicesResource.readServices(DataView.FULL_WITH_HEALTH_CHECK_EXPLANATION);
        for (ApiService service : services) {
            MetricViewDefinitions view = serviceDefinitions.get(service.getType());
            if (view == null) continue;
            if (!view.isEnabled()) continue;

            MetricProvider provider;
            String name;
            if (cluster == null) {
                // add ClouderaService to Manager Host Group
                String hostName = (collectionMode.isDoPrefixServiceNames()) ? prefixServiceName(apiCluster, service) : service.getName();
                ClouderaHost host = monitoringState.hosts().containsKey(hostName) ? (ClouderaHost)monitoringState.hosts().get(hostName) : new ClouderaHost(hostName, ClouderaConfigurationProvider.CLOUDERA_CLUSTER);
                host.setDescription(hostName);
                translateHealthForHostService(host, service.getHealthSummary(), service.getServiceState());


                provider = host;
                name = host.getHostName();
                monitoringState.hosts().put(hostName, host);
            }
            else {
                // add ClouderaService to Cluster HostGroup
                String vmName = (collectionMode.isDoPrefixServiceNames()) ? prefixServiceName(apiCluster, service) : service.getName();
                ClouderaVM vm = cluster.getVMPool().containsKey(vmName) ? (ClouderaVM) cluster.getVM(vmName) : new ClouderaVM(vmName);
                translateHealthForHostService(vm, service.getHealthSummary(), service.getServiceState());
                provider = vm;
                name = vmName;
                cluster.putVM(name, vm);
            }

            collectHealthCheckMetrics(service.getHealthChecks(), provider, view, servicesResource);

            // collect metrics
            collector.collect(monitoringState.getState(), provider, view, "serviceType", service.getName(), "clusterName", apiCluster.getName());
            postProcessor.processSynthetics(provider, view, monitoringState.getState());

            if (log.isDebugEnabled()) log.debug("Added ClouderaService: " + name);
        }

    }

    public void collectHealthCheckMetrics(List<ApiHealthCheck> healthChecks, MetricProvider provider, MetricViewDefinitions metricViewDefinitions, ServicesResourceV14 servicesResource) {
        List<ApiHealthCheck> healthChecks2 = new ArrayList<>();
        if (healthChecks == null || healthChecks.size() == 0) {
            // sometimes health checks are not returned when service is down
            if (metricViewDefinitions.getViewName().equalsIgnoreCase(ClouderaConfigurationProvider.CLOUDERA_SERVICE_KAFKA)) {
                // KAFKA doesn't provide aggregated metrics. Go build them...
                healthChecks2 = healthAggregator.processKafkaHealthChecks(servicesResource);
            }
            else {
                for (BaseQuery query : metricViewDefinitions.getQueries()) {
                    if (query.getComputeType().equals(ComputeType.health)) {
                        ApiHealthCheck healthCheck = new ApiHealthCheck(query.getServiceName(), ApiHealthSummary.BAD);
                        healthCheck.setExplanation(ClouderaMetricCollector.SERVICE_HEALTH_CHECK_NOT_OPERATIONAL);
                        healthChecks2.add(healthCheck);
                    }
                }
            }
        }
        else {
            healthChecks2 = healthChecks;
        }
        for (ApiHealthCheck healthCheck : healthChecks2) {
            String metricName = healthCheck.getName();
            BaseQuery query = metricViewDefinitions.getMetric(metricName);
            if (query == null) {
                continue;
            }
            BaseMetric metric = new BaseMetric(
                    metricName,
                    query.getWarning(),
                    query.getCritical(),
                    query.isGraphed(),
                    query.isMonitored(),
                    query.getCustomName()
            );
            metric.setMetricType(query.getServiceType());
            if (!query.isMonitored()) {
                metric.setConfigFlag(true);
            }
            if (healthCheck.getExplanation() != null) {
                metric.setExplanation(healthCheck.getExplanation());
            }
            metric.setValueOnly("0");
            metric.setCurrState(API_HEALTH_SUMMARY_TO_METRIC_STATE.get(healthCheck.getSummary()));
            BaseMetric priorMetric = provider.getMetricPool().get(metricName);
            if (priorMetric != null) {
                metric.setLastState(priorMetric.getCurrState());
            }
            provider.getMetricPool().put(metric.getServiceName(), metric);
        }

    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Gathering inventory");
        if (connectionState != ConnectionState.CONNECTED) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new ClouderaInventoryBrowser(rootResource, collectionMode.getViews());
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        return inventoryBrowser.gatherInventory(options);
    }


    protected String getValidClusterName(ApiCluster cluster) {
        String displayName = cluster.getDisplayName();
        if (StringUtils.isEmpty(displayName) || displayName.contains(" ")) {
            return cluster.getName();
        }
        return displayName;
    }

    protected String prefixServiceName(ApiCluster cluster, ApiService service) {
        return getValidClusterName(cluster) + "-" + service.getName();
    }

    public List<String> listHosts(MonitorConnection connection) {
        if (rootResource == null) {
            connect(connection);
        }
        ApiHostList apiHosts = rootResource.getHostsResource().readHosts(DataView.SUMMARY);
        List<String> hosts = new LinkedList<String>();
        for (ApiHost apiHost : apiHosts) {
            String hostName = apiHost.getHostname();
            hosts.add(hostName);
        }
        return hosts;
    }

    public List<String> listClusters(MonitorConnection connection) {
        if (rootResource == null) {
            connect(connection);
        }
        ApiClusterList apiClusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
        List<String> clusters = new LinkedList<String>();
        for (ApiCluster apiCluster : apiClusters) {
            String clusterName = getValidClusterName(apiCluster);
            clusters.add(clusterName);
        }
        return clusters;
    }

    public Set<String> listServices(MonitorConnection connection) {
        if (rootResource == null) {
            connect(connection);
        }
        Set<String> services = new HashSet<>();
        ApiClusterList apiClusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
        for (ApiCluster apiCluster : apiClusters) {
            ApiServiceList apiServices = rootResource.getClustersResource().getServicesResource(apiCluster.getName()).readServices(DataView.SUMMARY);
            for (ApiService service : apiServices) {
                services.add(service.getName());
            }
        }
        return services;
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        if (rootResource == null) {
            connect(configuration.getConnection());
        }
        if (rootResource != null) {
            return namesProvider.getListByServiceType(serviceType, rootResource.getTimeSeriesResource());
        }
        throw new CloudHubException("no connection available to retrieve metric metadata");
    }

    public List<String> listHealthCheckNames(String serviceType) {
        return namesProvider.getHealthChecksByServiceType(serviceType);
    }

    private void translateHealthForHostEntity(ClouderaHost host, ApiEntityStatus healthState) {

        HealthInfo info = API_ENTITY_HEALTH_SUMMARY_TO_HOST_STATE.get(healthState);
        if (info != null) {
            host.setRunState(info.gwosStatus);
            if (info.runStateExtra != null) {
                host.setRunExtra(info.runStateExtra);
            }
        }
    }

    private void translateHealthForHostService(ClouderaHost host, ApiHealthSummary healthState, ApiServiceState serviceState) {
        if (isServiceStopped(serviceState)) {
            host.setRunState(GwosStatus.UNSCHEDULED_DOWN.status);
            host.setRunExtra(HOST_SERVICE_IS_IN_A_STOPPED_STATE);
        } else {
            HealthInfo info = API_HEALTH_SUMMARY_TO_HOST_STATE.get(healthState);
            if (info != null) {
                host.setRunState(info.gwosStatus);
                if (info.runStateExtra != null) {
                    host.setRunExtra(info.runStateExtra);
                }
            }
        }
    }

    private void translateHealthForHostService(ClouderaVM vm, ApiHealthSummary healthState, ApiServiceState serviceState) {

        if (isServiceStopped(serviceState)) {
            vm.setRunState(GwosStatus.UNSCHEDULED_DOWN.status);
            vm.setRunExtra(HOST_SERVICE_IS_IN_A_STOPPED_STATE);
        } else {
            HealthInfo info = API_HEALTH_SUMMARY_TO_HOST_STATE.get(healthState);
            if (info != null) {
                vm.setRunState(info.gwosStatus);
                if (info.runStateExtra != null) {
                    vm.setRunExtra(info.runStateExtra);
                }
            }
        }
    }

    private boolean isServiceStopped(ApiServiceState serviceState) {
        if (serviceState == null) return false;
        return serviceState.equals(ApiServiceState.STOPPED) || serviceState.equals(ApiServiceState.STOPPING);
    }


}
