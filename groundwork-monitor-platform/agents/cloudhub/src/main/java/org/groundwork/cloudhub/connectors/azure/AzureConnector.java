package org.groundwork.cloudhub.connectors.azure;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.MetricDefinition;
import com.microsoft.azure.management.monitor.MetricDefinitions;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.SubscriptionState;
import com.microsoft.rest.LogLevel;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.AzureConnection;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.connectors.azure.health.AzureHealthProcessor;
import org.groundwork.cloudhub.connectors.azure.health.HealthInfo;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.base.DiscoveryConnector;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MetricsPostProcessor;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service(AzureConnector.NAME)
@Scope("prototype")
public class AzureConnector extends BaseConnector implements DiscoveryConnector {

    public static final String NAME = "AzureConnector";

    private static Logger log = Logger.getLogger(AzureConnector.class);
    private static Logger azureLog = Logger.getLogger(Azure.class);

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private Azure azure;

    private DataCenterInventory inventory;

    @Autowired
    protected MetricsPostProcessor postProcessor;

    @Autowired
    protected AzureHealthProcessor healthProcessor;

    @Autowired
    protected MetricsUtils metricsUtils;

    protected static final Boolean optimized = true;
    protected Boolean enableResourceGroups;

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

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        long startTime = System.currentTimeMillis();
        connectionState = ConnectionState.CONNECTING;
        AzureConnection connection = (AzureConnection) monitorConnection;
        if (connection.getCredentialsFile() == null) {
            throw new ConnectorException("Credentials file not provided");
        }
        enableResourceGroups = connection.getEnableResourceGroups();
        try {

            final File credFile = new File(connection.getCredentialsFile());
            azure = Azure.configure().
                    withLogLevel(azureLog.isDebugEnabled() ? LogLevel.BASIC : LogLevel.NONE).
                    authenticate(credFile).withDefaultSubscription();
                    //authenticate(credFile).withSubscription((((AzureConnection) monitorConnection).getSubscription()));

            // TODO: build metric names-  namesProvider.build(rootResource.getTimeSeriesResource());

        } catch (Exception e) {
            log.error("Exception while connecting to " + connection.getServer() + ": " + e);
            connectionState = ConnectionState.DISCONNECTED;
            throw new ConnectorException("Unable to connect to Azure: " + e.getMessage(), e);
        }
        connectionState = ConnectionState.CONNECTED;
        if (log.isDebugEnabled())
            log.debug("Azure connect() completed in " + (System.currentTimeMillis() - startTime) + " ms");

    }

    @Override
    public void disconnect() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Disconnecting from Azure");
        connectionState = ConnectionState.DISCONNECTED;
        // TODO: azure.logout
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        if (azure == null) {
            connect(configuration.getConnection());
        }
        GenericResource targetResource = null;
        PagedList<GenericResource> genericResources = azure.genericResources().list();
        for (GenericResource genericResource : genericResources) {
            if (serviceType.equalsIgnoreCase(genericResource.type())) {
                targetResource = genericResource;
                break;
            }
        }
        if (targetResource != null) {
            List<String> metricNames = new ArrayList<>();
            List<MetricDefinition> metricDefs = azure.metricDefinitions().listByResource(targetResource.id());
            for (MetricDefinition metricDef : metricDefs) {
                metricNames.add(metricDef.name().localizedValue());
            }
            return metricNames;
        }
        throw new CloudHubException("no connection available to retrieve metric metadata");
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Gathering inventory");
        if (connectionState != ConnectionState.CONNECTED) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new AzureInventoryBrowser(collectionMode.getViews());
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        return inventoryBrowser.gatherInventory(options);
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState, List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries) throws ConnectorException {
        long startTime = System.currentTimeMillis();

        if (priorState == null) priorState = new MonitoringState();
        MonitoringState monitoringState = new MonitoringState();
        monitoringState.resetState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("Azure: collectMetrics(): not connected");
            return priorState;
        }

        if (inventory == null) {
            inventory = gatherInventory();
        }

        AzureMetricCollector metricCollector = new AzureMetricCollector(postProcessor);
        Map<String, MetricViewDefinitions> serviceDefinitions = new HashMap<>();
        for (ConfigurationView view : collectionMode.getViews()) {
            if (view.isService() && view.isEnabled()) {
                serviceDefinitions.put(view.getName(), new MetricViewDefinitions(view, vmQueries, false));
            }
        }

        // Get or construct a AzureHost based on current subscription name
        Subscription subscription = azure.getCurrentSubscription();
        String hostName = AzureConfigurationProvider.PREFIX_MGMT_SERVER + subscription.displayName();
        AzureHost host = (AzureHost) monitoringState.hosts().get(hostName);
        BaseHost priorHost = priorState.hosts().get(hostName);
        if (priorHost == null) {
            priorHost = new AzureHost(hostName, AzureConfigurationProvider.AZURE_CLUSTER);
        }
        if (host == null) {
            host = new AzureHost(hostName, AzureConfigurationProvider.AZURE_CLUSTER);
            host.setTransient(true);
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }
            monitoringState.hosts().put(hostName, host);
        }
        host.setRunningState(mapSubscriptionStatus(subscription.state()));
        MetricDefinitions metricDefinitions = azure.metricDefinitions();
        Map<String, Map<String, Object>> servicesByType = new HashMap<>();
        PagedList<GenericResource> subscriptResources = azure.genericResources().list();
        for (GenericResource azureResource : subscriptResources) {
            MetricViewDefinitions view = serviceDefinitions.get(azureResource.type());
            if (null != view) {
                if (log.isDebugEnabled()) log.debug("processing azure resource " + azureResource.name());
                AzureVM vm = new AzureVM(azureResource.name());

                Map<String, Object> resourceMap = servicesByType.get(azureResource.type());
                if (resourceMap == null) {
                    resourceMap = healthProcessor.buildResources(azureResource.type(), azure);
                    servicesByType.put(azureResource.type(), resourceMap);
                }
                Object resource = resourceMap.get(azureResource.id().toLowerCase());
                if (resource != null) {
                    HealthInfo health = healthProcessor.healthCheck(azureResource.type(), resource);
                    vm.setRunningState(health.getGwosStatus());
                    vm.setRunExtra(health.getRunStateExtra());
                } else {
                    String message = "Could not map resource of type " + azureResource.type() + " for instance " + azureResource.id();
                    log.error(message);
                    vm.setRunningState(GwosStatus.UNREACHABLE.status);
                    vm.setRunExtra(message);
                }
                BaseVM priorVM = priorHost.getVM(vm.getName());
                if (priorVM != null) {
                    vm.setPrevRunState(priorVM.getPrevRunState());
                }
                host.getVMPool().put(vm.getName(), vm);

                // resource groups
                if (enableResourceGroups) {
                    String resourceGroupName = azureResource.resourceGroupName();
                    if (!StringUtils.isEmpty(resourceGroupName)) {
                        String hostGroupName = AzureConfigurationProvider.PREFIX_RESOURCE_GROUP + resourceGroupName;
                        AzureHost resourceGroup = (AzureHost)monitoringState.hosts().get(hostGroupName);
                        if (resourceGroup == null) {
                            resourceGroup = new AzureHost(hostGroupName, AzureConfigurationProvider.AZURE_RESOURCE_GROUP);
                            resourceGroup.setTransient(true);
                            BaseHost priorGroup = priorState.hosts().get(hostGroupName);
                            if (priorGroup != null) {
                                resourceGroup.setPrevRunState(priorGroup.getRunState());
                            }
                            monitoringState.hosts().put(hostGroupName, resourceGroup);
                        }
                        resourceGroup.getVMPool().put(vm.getName(), vm);
                    }
                }

                long startCollectTime = System.currentTimeMillis();
                if (optimized) {
                    metricCollector.collectOptimized(metricDefinitions, azureResource, vm, monitoringState.getState(), view);
                }
                else {
                    metricCollector.collect(metricDefinitions, azureResource, vm, monitoringState.getState(), view);
                }
                // process synthetics
                postProcessor.processSynthetics(vm, view, monitoringState.getState());

                // merge previous metrics
                if (priorVM != null) {
                    metricsUtils.mergePreviousMetricValues(vm.getMetricPool(), priorVM.getMetricPool());
                }
                if (log.isDebugEnabled()) {
                    log.debug("\t" + azureResource.name() + " metrics collected in " + (System.currentTimeMillis() - startCollectTime) + " ms");
                }
            } else {
                if (log.isDebugEnabled()) log.debug("skipping azure resource " + azureResource.name());
            }
        }

        if (log.isInfoEnabled())
            log.info("Azure collectMetrics completed in " + (System.currentTimeMillis() - startTime) + " ms");
        return monitoringState;
    }

    public List<String> listHosts(MonitorConnection connection) {
        return new LinkedList<>();
    }

    public List<String> listClusters(MonitorConnection connection) {
        return new LinkedList<>();
    }

    private static Set<String> EXCLUSION_LIST = new HashSet<>(Arrays.asList("Microsoft.Network/networkSecurityGroups",
                                                                            "Microsoft.Network/virtualNetworks",
                                                                            "Microsoft.Compute/disks"));

    public Set<String> listServices(MonitorConnection connection) {
        if (azure == null) {
            connect(connection);
        }
        Set<String> services = new HashSet<>();
        PagedList<GenericResource> subscriptResources = azure.genericResources().list();
        for (GenericResource azureResource : subscriptResources) {
            if (EXCLUSION_LIST.contains(azureResource.type())) {
                continue;
            }
            services.add(azureResource.type());
        }
        return services;
    }

    private String mapSubscriptionStatus(SubscriptionState state) {
        switch (state) {
            case ENABLED:
                return GwosStatus.UP.name();
            case WARNED:
            case PAST_DUE:
                return GwosStatus.WARNING.name();
            case DISABLED:
            case DELETED:
            default:
                return GwosStatus.DOWN.name();
        }
    }

}
