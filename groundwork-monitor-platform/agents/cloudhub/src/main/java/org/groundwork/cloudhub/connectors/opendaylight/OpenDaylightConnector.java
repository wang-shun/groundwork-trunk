package org.groundwork.cloudhub.connectors.opendaylight;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.OpenDaylightConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.opendaylight.client.AuthClient;
import org.groundwork.cloudhub.connectors.opendaylight.client.MetricInfo;
import org.groundwork.cloudhub.connectors.opendaylight.client.PortClient;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service(OpenDaylightConnector.NAME)
@Scope("prototype")
public class OpenDaylightConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    private static Logger log = Logger.getLogger(OpenDaylightConnector.class);

    public static final String NAME = "OpenDaylightConnector";
    private ConnectionState connectionState = ConnectionState.NASCENT;

    private OpenDaylightConnection connection = null;
    private DataCenterInventory inventory = null;

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        AuthClient.AuthResponse response = null;
        try {
            connection = (OpenDaylightConnection) monitorConnection;
            AuthClient auth = new AuthClient(connection);
            response = auth.login();
            if (log.isDebugEnabled()) {
                log.debug("authenticated to OpenDayLight: " + response.getStatus());
            }
            if (response.getStatus() == Response.Status.OK) {
                connectionState = ConnectionState.CONNECTED;
            } else {
                connectionState = ConnectionState.DISCONNECTED;
            }
        } catch (Exception e) {
            connectionState = ConnectionState.DISCONNECTED;
            Response.Status status = (response == null) ? Response.Status.INTERNAL_SERVER_ERROR : response.getStatus();
            log.error("Failed to connect", e);
            throw new ConnectorException("Failed to authenticate to OpenDayLight: " + status);
        }
        if (connectionState != ConnectionState.CONNECTED) {
            Response.Status status = (response == null) ? Response.Status.INTERNAL_SERVER_ERROR : response.getStatus();
            throw new ConnectorException("Failed to authenticate to OpenDayLight: " + status);
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
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (connection == null) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new OpenDaylightInventoryBrowser(connection);
        InventoryOptions options = new InventoryOptions(true, false, false, false, false, "");
        inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {
        long startTime = System.currentTimeMillis();
        MonitoringState monitoringState = new MonitoringState();
        if (priorState == null)
            priorState = new MonitoringState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("OpenDaylight: collectMetrics(): not connected");
            return priorState;
        }

        if (inventory == null) {
            gatherInventory();
        }

        // set up final filtering of queries
        Map<String, BaseQuery> monitoredHostMetrics = new ConcurrentHashMap<String, BaseQuery>();
        for (BaseQuery query : hostQueries) {
            monitoredHostMetrics.put(query.getQuery(), query);
        }
        Map<String, BaseQuery> monitoredVmMetrics = new ConcurrentHashMap<String, BaseQuery>();
        for (BaseQuery query : vmQueries) {
            monitoredVmMetrics.put(query.getQuery(), query);
        }

        Map<String, BaseQuery> mergedControllerQueries = mergeControllerMetrics(hostQueries);
        Map<String, BaseQuery> mergedSwitchQueries = mergeSwitchMetrics(vmQueries);
        PortClient portClient = new PortClient(connection);
        Map<String, VmMetricInfo> vmMetrics = gatherVMMetrics(portClient, mergedSwitchQueries.keySet());
        for (InventoryContainerNode controller : inventory.getHypervisors().values()) {
            OpenDaylightHost host = new OpenDaylightHost(controller.getName());
            BaseHost priorHost = priorState.hosts().get(controller.getName());
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }
            // Gather controller metrics
            List<MetricInfo> controllerMetrics = portClient.getHyperVisorStatistics(controller.getName());
            for (MetricInfo controllerMetric : controllerMetrics) {
                BaseQuery query = mergedControllerQueries.get(controllerMetric.meter);
                if (query != null) {
                    String translatedMetricName = /* "host." + */ query.getQuery();
                    BaseMetric baseMetric = new BaseMetric(query, translatedMetricName);
                    baseMetric.setValue(controllerMetric.metricToString());
                    if (priorHost != null) {
                        BaseMetric priorMetric = priorHost.getMetric(translatedMetricName);
                        if (priorMetric != null) {
                            baseMetric.setLastState(priorMetric.getCurrState());
                        }
                    }
                    host.putMetric(translatedMetricName, baseMetric);
                }
            }

            // compute host synthetic metrics
            for (ExtendedSynthetic synthetic : OpenDaylightHost.getSyntheticDefinitions()) {
                computePrimarySynthetics(mergedControllerQueries, host, synthetic, priorHost);
            }

            // gather switch metrics
            for (VirtualMachineNode vm : controller.getVms().values()) {
                OpenDaylightVM odvm = new OpenDaylightVM(vm.getName());
                BaseVM priorVM = null;
                if (priorHost != null) {
                    priorVM = priorHost.getVM(vm.getName());
                    if (priorVM != null) {
                        odvm.setPrevRunState(priorVM.getRunState());
                    }
                }
                int queriesOverThreshold = 0; // total number of queries over threshold for this VM
                int queries = 0; // total queries (duplicates per port)
                VmMetricInfo vmMetric = vmMetrics.get(vm.getName());
                if (vmMetric != null) {
                    for (BaseQuery query : mergedSwitchQueries.values()) {
                        // walk through each query type
                        List<MetricInfo> metricList = vmMetric.byQuery.get(query.getQuery());
                        if (metricList != null) {
                            // walk through each metric for query type
                            for (MetricInfo metric : metricList) {
                                String translatedMetricName = metric.meter;
                                BaseMetric baseMetric = new BaseMetric(query, translatedMetricName);
                                baseMetric.setValue(metric.metricToString());
                                if (priorVM != null) {
                                    BaseMetric priorMetric = priorVM.getMetric(translatedMetricName);
                                    if (priorMetric != null) {
                                        baseMetric.setLastState(priorMetric.getCurrState());
                                    }
                                }
                                odvm.putMetric(translatedMetricName, baseMetric);
                                queriesOverThreshold += OpenDaylightStatus.getWarningCounts(metric, query);
                                queries++;
                            }
                        }
                    }
                }

                // compute VM synthetic metrics
                for (ExtendedSynthetic synthetic : OpenDaylightVM.getSyntheticDefinitions()) {
                    computeSecondarySynthetics(mergedSwitchQueries, odvm, synthetic, priorVM);
                }

                String runState = OpenDaylightStatus.convertToGroundworkStatus(queriesOverThreshold, queries);
                odvm.setRunState(runState);
                odvm.setRunExtra(vm.getStatus());
                host.putVM(vm.getName(), odvm);
            }
            host.setRunState(OpenDaylightStatus.convertToGroundworkStatus(0, 0));
            host.setRunExtra(controller.getStatus());
            monitoringState.hosts().put(controller.getName(), host);
        }

        // clean up pass, remove unmonitored metrics
        pushDownMetrics(monitoringState, monitoredHostMetrics, monitoredVmMetrics);

        if (log.isDebugEnabled()) {
            log.debug("Open Daylight collectMetrics completed in " + (startTime - System.currentTimeMillis()) + " ms");
        }
        return monitoringState;
    }

    public class VmMetricInfo {
        final String name; // vm name
        // query -> List<MetricInfo>
        final Map<String, List<MetricInfo>> byQuery = new HashMap<String, List<MetricInfo>>();

        VmMetricInfo(String name) {
            this.name = name;
        }

    }

    /**
     * Builds a map of all metrics by VM. VM Name maps to a List of all MetricInfo for
     *
     * @param portClient
     * @return
     */
    public Map<String, VmMetricInfo> gatherVMMetrics(PortClient portClient, Set<String> queries) {
        Map<String, VmMetricInfo> metricMap = new HashMap();
        List<MetricInfo> metrics = portClient.retrieveMetrics(queries);
        for (MetricInfo metric : metrics) {
            VmMetricInfo vmMetric = metricMap.get(metric.resource);
            if (vmMetric == null) {
                vmMetric = new VmMetricInfo(metric.resource);
                metricMap.put(metric.resource, vmMetric);
            }
            List<MetricInfo> metricList = vmMetric.byQuery.get(metric.query);
            if (metricList == null) {
                metricList = new ArrayList<MetricInfo>();
                vmMetric.byQuery.put(metric.query, metricList);
            }
            metricList.add(metric);
        }
        return metricMap;
    }

    private Map<String, BaseQuery> mergeControllerMetrics(List<BaseQuery> hostQueries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : OpenDaylightHost.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenDaylightHost.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenDaylightHost.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (hostQueries != null) {
            for (BaseQuery accessor : hostQueries) // LAST to ensure OVERRIDE capability.
                queryPool.put(accessor.getQuery(), accessor);
        }
        return queryPool;
    }

    private Map<String, BaseQuery> mergeSwitchMetrics(List<BaseQuery> switchQueries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : OpenDaylightVM.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenDaylightVM.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenDaylightVM.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (switchQueries != null) {
            for (BaseQuery accessor : switchQueries) // LAST to ensure OVERRIDE capability.
                queryPool.put(accessor.getQuery(), accessor);
        }
        return queryPool;
    }

    /**
     * Strip off service name prefix and return only metric name
     *
     * @param fullService
     * @return pure metric name with service prefix stripped
     */
    protected String stripServiceNamePrefix(String fullService) {
        if (fullService == null)
            return "";
        int index = fullService.lastIndexOf('-');
        if (index == -1 || (index >= fullService.length() - 1))
            return fullService;
        return fullService.substring(index+1);
    }

}
