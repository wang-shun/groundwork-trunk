/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors.netapp;

import netapp.manage.NaServer;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.NetAppConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.groundwork.cloudhub.connectors.netapp.client.MetricsClient;
import org.groundwork.cloudhub.connectors.netapp.client.SystemClient;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
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
import org.groundwork.cloudhub.metrics.MonitoringEvent;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(NetAppConnector.NAME)
@Scope("prototype")
public class NetAppConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    private static Logger log = Logger.getLogger(org.groundwork.cloudhub.connectors.netapp.NetAppConnector.class);

    public static final String NAME = "NetAppConnector";
    public static final String METRIC_NOT_FOUND = "NetApp Metric not found ";

    public static final int NETAPP_MAJOR_VERSION = 1;
    public static final int NETAPP_MINOR_VERSION = 15;

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private NetAppConnection connection = null;
    private DataCenterInventory inventory = null;

    private NaServer server = null;
    private NetAppSystemInfo systemInfo = null;

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        connectionState = ConnectionState.CONNECTING;
        connection = ((NetAppConnection) monitorConnection);
        try {
            server = new NaServer(monitorConnection.getServer(), NETAPP_MAJOR_VERSION, NETAPP_MINOR_VERSION);
            server.setServerType(NaServer.SERVER_TYPE_FILER);
            if (((NetAppConnection) monitorConnection).isSslEnabled()) {
                server.setStyle(NaServer.STYLE_CERTIFICATE);
                server.setTransportType(NaServer.TRANSPORT_TYPE_HTTPS);
            }
            else {
                server.setStyle(NaServer.STYLE_LOGIN_PASSWORD);
                server.setTransportType(NaServer.TRANSPORT_TYPE_HTTP);
                server.setAdminUser(((NetAppConnection) monitorConnection).getUsername(), ((NetAppConnection) monitorConnection).getPassword());

            }
            SystemClient systemClient = new SystemClient(server);
            systemInfo = systemClient.getSystemInfo();
            connectionState = ConnectionState.CONNECTED;
        }
        catch (Exception e) {
            connectionState = ConnectionState.FAILED;
            throw new ConnectorException("Failed to connect to host: " + monitorConnection.getServer(), e);
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
            log.error("NetApp: collectMetrics(): not connected");
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

        Map<String, BaseQuery> mergedHypervisorQueries = mergeHypervisorMetrics(hostQueries);
        Map<String, BaseQuery> mergedVmQueries = mergeVMMetrics(vmQueries);
        MetricsClient metricsClient = new MetricsClient(server);
        Map<String,NetAppHost> controllerMetrics = metricsClient.gatherControllerMetrics(mergedHypervisorQueries);
        Map<String,NetAppHost> vServerMetrics = metricsClient.gatherVServerMetrics();
        Map<String,NetAppVM> volumeMetrics = metricsClient.gatherVolumeMetrics(mergedVmQueries);
        Map<String,NetAppVM> aggregateMetrics = metricsClient.gatherAggregateMetrics(mergedVmQueries);
        for (InventoryContainerNode controllerOrVServer : inventory.getHypervisors().values()) {
            NetAppHost host = controllerMetrics.get(controllerOrVServer.getName());
            if (host == null) {
                host = vServerMetrics.get(controllerOrVServer.getName());
            }
            if (host == null) {
                if (log.isInfoEnabled()) {
                    log.info("NetApp node: " + controllerOrVServer.getName() + " not found, skipping...");
                }
                continue;
            }

            BaseHost priorHost = priorState.hosts().get(controllerOrVServer.getName());
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }

            if (priorHost != null) {
                for (BaseMetric metric : host.getMetricPool().values()) {
                    BaseMetric priorMetric = priorHost.getMetricPool().get(metric.getQuerySpec());
                    if (!host.getRunState().equals(GwosStatus.UP.status)) {
                        metric.setCurrState(getMetricStatus(host.getRunState()));
                    }
                    if (priorMetric != null) {
                        metric.setLastState(priorMetric.getCurrState());
                    }
                }
            }

            host.setRunState(controllerOrVServer.getStatus());
            host.setRunExtra(controllerOrVServer.getStatus());
            monitoringState.hosts().put(controllerOrVServer.getName(), host);

            // Gather VM metrics
            for (VirtualMachineNode node : controllerOrVServer.getVms().values()) {
                NetAppNode vmNode = (NetAppNode)node;
                BaseVM vm = null;
                if (vmNode.isVolume()) {
                    vm = volumeMetrics.get(vmNode.getName());
                }
                else {
                    vm = aggregateMetrics.get(vmNode.getName());
                }
                if (vm != null) {
                    host.putVM(vm.getVMName(), vm);
                    BaseVM priorVM = null;
                    if (priorHost != null) {
                        priorVM = priorHost.getVM(vm.getVMName());
                        if (priorVM != null) {
                            vm.setPrevRunState(priorVM.getRunState());
                            for (BaseMetric metric : vm.getMetricPool().values()) {
                                BaseMetric priorMetric = priorVM.getMetricPool().get(metric.getQuerySpec());

                                if (!vm.getRunState().equals(GwosStatus.UP.status)) {
                                    metric.setCurrState(getMetricStatus(vm.getRunState()));
                                }

                                if (priorMetric != null) {
                                    metric.setLastState(priorMetric.getCurrState());
                                }
                            }
                        }
                    }
                    for (ExtendedSynthetic synthetic : NetAppVM.getSyntheticDefinitions()) {
                        computeSecondarySynthetics(mergedVmQueries, vm, synthetic, priorVM);
                    }
                }
            } // end VMs

            // compute host synthetic metrics
            for (ExtendedSynthetic synthetic : NetAppHost.getSyntheticDefinitions()) {
                computePrimarySynthetics(mergedHypervisorQueries, host, synthetic, priorHost);
            }
        }

        // clean up pass, remove unmonitored metrics
        pushDownMetrics(monitoringState, monitoredHostMetrics, monitoredVmMetrics);
        pushDownAggregates(monitoringState);

        if (log.isDebugEnabled()) {
            log.debug("NetApp collectMetrics completed in " + (startTime - System.currentTimeMillis()) + " ms");
        }
        for (MetricFaultInfo metric : metricsClient.getMetricFaults()) {
            monitoringState.events().add(new MonitoringEvent(metric.getHost(), metric.getQuery(), METRIC_NOT_FOUND + metric.getQuery()));
        }
        return monitoringState;
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (connection == null) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new NetAppInventoryBrowser(server);
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    private Map<String, BaseQuery> mergeHypervisorMetrics(List<BaseQuery> hostQueries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : NetAppHost.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : NetAppHost.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : NetAppHost.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (hostQueries != null) {
            for (BaseQuery accessor : hostQueries) // LAST to ensure OVERRIDE capability.
                queryPool.put(accessor.getQuery(), accessor);
        }
        return queryPool;
    }

    private Map<String, BaseQuery> mergeVMMetrics(List<BaseQuery> containerQueries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : NetAppVM.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : NetAppVM.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : NetAppVM.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (containerQueries != null) {
            for (BaseQuery accessor : containerQueries) // LAST to ensure OVERRIDE capability.
                queryPool.put(accessor.getQuery(), accessor);
        }
        return queryPool;
    }

    protected int pushDownAggregates(MonitoringState hostPool) {
        int count = 0;
        for (BaseHost host : hostPool.hosts().values()) {
            // not necessary to push down hypervisor metrics
            for (BaseVM baseVM : host.getVMPool().values()) {
                List<String> deletes = new ArrayList<>();
                NetAppVM vm = (NetAppVM)baseVM;
                if (vm.isVolume()) {
                    for (BaseMetric metric : vm.getMetricPool().values()) {
                        if (metric.getQuerySpec().startsWith("aggr.") || metric.getQuerySpec().startsWith("syn.aggregate")) {
                            deletes.add(metric.getQuerySpec());
                        }
                    }
                }
                else {
                    for (BaseMetric metric : vm.getMetricPool().values()) {
                        if (metric.getQuerySpec().startsWith("volume.") || metric.getQuerySpec().startsWith("syn.volume")) {
                            deletes.add(metric.getQuerySpec());
                        }
                    }
                }
                for (String metric : deletes) {
                    vm.getMetricPool().remove(metric);
                    count++;
                }
            }

        }
        return count;
    }

    protected String getMetricStatus(String hostStatus) {
        if (GwosStatus.UP.status.equals(hostStatus)) {
            return BaseMetric.sOK;
        } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sCritical;
        } else if (GwosStatus.UNREACHABLE.status.equals(hostStatus)) {
            return BaseMetric.sUnknown;
        } else if (GwosStatus.SCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sScheduledDown;
        } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sScheduledDown;
        } else if (GwosStatus.PENDING.status.equals(hostStatus)) {
            return BaseMetric.sPending;
        } else if (GwosStatus.DOWN.status.equals(hostStatus)) {
            return BaseMetric.sPoweredDown;
        }
        return BaseMetric.sWarning;
    }

}
