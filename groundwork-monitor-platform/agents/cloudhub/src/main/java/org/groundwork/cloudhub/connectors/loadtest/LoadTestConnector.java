/*
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.cloudhub.connectors.loadtest;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.LoadTestConnection;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.ProfileType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoadTestConnector
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Service(LoadTestConnector.NAME)
@Scope("prototype")
public class LoadTestConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public final static String NAME = "LoadTestConnector";

    private static Logger log = Logger.getLogger(LoadTestConnector.class);

    private ConnectionState connectionState = ConnectionState.NASCENT;
    private LoadTestConnection connection;
    private long connectorStarted = 0L;
    private Map<String,Double> metricValuePeriodMillis = new HashMap<String,Double>();

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        // disconnect if connected
        disconnect();
        // open or reopen connection
        connection = (LoadTestConnection) monitorConnection;
        connectionState = ConnectionState.CONNECTED;
        connectorStarted = System.currentTimeMillis();
    }

    @Override
    public void disconnect() throws ConnectorException {
        // close connection if connected
        if (connectionState == ConnectionState.CONNECTED) {
            connectorStarted = 0L;
            connectionState = ConnectionState.DISCONNECTED;
            connection = null;
        }
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState, List<BaseQuery> hostQueries,
                                          List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {
        // validate connection state
        if (priorState == null) {
            priorState = new MonitoringState();
        }
        if ((connection == null) || (connectionState != ConnectionState.CONNECTED)) {
            return priorState;
        }
        // get inventory
        DataCenterInventory inventory = gatherInventory();
        // create and populate new monitoring state
        MonitoringState monitoringState = new MonitoringState();
        // populate hosts monitoring state
        for (InventoryContainerNode hypervisor : inventory.getHypervisors().values()) {
            // create host monitoring state
            LoadTestHost host = new LoadTestHost(hypervisor.getName());
            // lookup previous host state
            BaseHost priorHost = priorState.hosts().get(hypervisor.getName());
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }
            // simulate host state
            GwosStatus hostStatus = ((Math.random() < ((double)connection.getHostsDownPercent())/100.0) ?
                    GwosStatus.UNSCHEDULED_DOWN : GwosStatus.UP);

            // generate host monitored metrics
            for (BaseQuery hostQuery : hostQueries) {
                // generate host metric
                BaseMetric hostMetric = new BaseMetric(hostQuery);
                if (hostStatus == GwosStatus.UP) {
                    hostMetric.setValue(generateMetricValue(hostMetric));
                } else {
                    hostMetric.setCurrState(BaseMetric.sUnknown);
                }
                // lookup previous host metric state
                if (priorHost != null) {
                    BaseMetric priorMetric = priorHost.getMetric(hostQuery.getQuery());
                    if (priorMetric != null) {
                        hostMetric.setLastState(priorMetric.getCurrState());
                    }
                }
                // save host metric in monitoring state
                host.putMetric(hostQuery.getQuery(), hostMetric);
            }

            // populate virtual machine monitoring state
            for (VirtualMachineNode virtualMachine : hypervisor.getVms().values()) {
                // create virtual machine monitoring state
                LoadTestVM vm = new LoadTestVM(virtualMachine.getName());
                // lookup previous virtual machine state
                BaseVM priorVM = null;
                if (priorHost != null) {
                    priorVM = priorHost.getVM(virtualMachine.getName());
                    if (priorVM != null) {
                        vm.setPrevRunState(priorVM.getRunState());
                    }
                }
                // simulate virtual machine state
                GwosStatus vmStatus = (((hostStatus == GwosStatus.UNSCHEDULED_DOWN) ||
                        ((Math.random() < ((double)connection.getHostsDownPercent())/100.0))) ?
                        GwosStatus.UNSCHEDULED_DOWN : GwosStatus.UP);

                // generate virtual machine monitored metrics
                for (BaseQuery vmQuery : vmQueries) {
                    // generate virtual machine metric
                    BaseMetric vmMetric = new BaseMetric(vmQuery);
                    if (vmStatus == GwosStatus.UP) {
                        vmMetric.setValue(generateMetricValue(vmMetric));
                    } else {
                        vmMetric.setCurrState(BaseMetric.sUnknown);
                    }
                    // lookup previous virtual machine metric state
                    if (priorVM != null) {
                        BaseMetric priorMetric = priorVM.getMetric(vmQuery.getQuery());
                        if (priorMetric != null) {
                            vmMetric.setLastState(priorMetric.getCurrState());
                        }
                    }
                    // save virtual machine metric in monitoring state
                    vm.putMetric(vmQuery.getQuery(), vmMetric);
                }

                // save virtual machine status in monitoring state
                virtualMachine.setStatus(vmStatus.status);
                vm.setRunState(vmStatus.status);
                vm.setRunExtra(vmStatus.status);
                host.putVM(virtualMachine.getName(), vm);
            }

            // save host status in monitoring state
            hypervisor.setStatus(hostStatus.status);
            host.setRunState(hostStatus.status);
            host.setRunExtra(hostStatus.status);
            monitoringState.hosts().put(hypervisor.getName(), host);
        }
        return monitoringState;
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        // validate connection state
        if ((connection == null) || (connectionState != ConnectionState.CONNECTED)) {
            throw new ConnectorException("Not connected");
        }
        // create or recreate inventory in the event connection configuration has changed
        InventoryOptions options = new InventoryOptions(true, false, false, false, false, "");
        DataCenterInventory inventory = new DataCenterInventory(options);
        // create hypervisor host groups and virtual machine hosts
        int vmsPerHypervisor = Math.max(((connection.getHostGroups() > 0) ?
                connection.getHosts()/connection.getHostGroups() : connection.getHosts()), 1);
        InventoryContainerNode hypervisorNode = null;
        for (int vmIndex = 0; (vmIndex < connection.getHosts()); vmIndex++) {
            if (vmIndex%vmsPerHypervisor == 0) {
                int hvIndex = vmIndex/vmsPerHypervisor;
                hypervisorNode = new InventoryContainerNode("loadtest-hypervisor-"+hvIndex);
                hypervisorNode.setStatus(GwosStatus.PENDING.status);
                inventory.getHypervisors().put(hypervisorNode.getName(), hypervisorNode);
            }
            VirtualMachineNode virtualMachineNode = new VirtualMachineNode("loadtest-vm-"+vmIndex, null);
            virtualMachineNode.setStatus(GwosStatus.PENDING.status);
            hypervisorNode.putVM(virtualMachineNode.getName(), virtualMachineNode);
            inventory.getVirtualMachines().put(virtualMachineNode.getName(), virtualMachineNode);
        }
        return inventory;
    }

    @Override
    public HubProfile readProfile() throws CloudHubException {
        // validate connection state
        if ((connection == null) || (connectionState != ConnectionState.CONNECTED)) {
            throw new ConnectorException("Not connected");
        }
        // create or recreate profile in the event connection configuration has changed
        CloudHubProfile profile = new CloudHubProfile(ProfileType.loadtest, null);
        // create hypervisor and virtual machine metrics
        int metricsPerHypervisor = Math.max(connection.getServices(), 1);
        int metricIndex = 0;
        for (int limit = metricsPerHypervisor/2; (metricIndex < limit); metricIndex++) {
            profile.getHypervisor().addMetric(generateMetric(metricIndex, "hypervisor", "Hypervisor", 90.0, 100.0));
        }
        for (int limit = (metricsPerHypervisor*4)/5; (metricIndex < limit); metricIndex++) {
            profile.getHypervisor().addMetric(generateMetric(metricIndex, "hypervisor", "Hypervisor", -1.0, 100.0));
        }
        for (int limit = metricsPerHypervisor; (metricIndex < limit); metricIndex++) {
            profile.getHypervisor().addMetric(generateMetric(metricIndex, "hypervisor", "Hypervisor", -1.0, -1.0));
        }
        int metricsPerVM = Math.max(connection.getServices(), 1);
        metricIndex = 0;
        for (int limit = metricsPerVM/2; (metricIndex < limit); metricIndex++) {
            profile.getVm().addMetric(generateMetric(metricIndex, "vm", "VM", 90.0, 100.0));
        }
        for (int limit = (metricsPerVM*4)/5; (metricIndex < limit); metricIndex++) {
            profile.getVm().addMetric(generateMetric(metricIndex, "vm", "VM", -1.0, 100.0));
        }
        for (int limit = metricsPerVM; (metricIndex < limit); metricIndex++) {
            profile.getVm().addMetric(generateMetric(metricIndex, "vm", "VM", -1.0, -1.0));
        }
        return profile;
    }

    @Override
    public void openConnection(MonitorConnection connection) throws ConnectorException {
        connect(connection);
    }

    @Override
    public void closeConnection() throws ConnectorException {
        disconnect();
    }

    /**
     * Generates critical, warning, and normal metric values for specified
     * metric. Non-critical values are generated to follow a sine function
     * with a period of greater than a minute and less than an hour starting
     * when connector is opened/connected.
     *
     * @param metric metric definition
     * @return generated metric value
     */
    private String generateMetricValue(BaseMetric metric) {
        long value = 0L;
        if (connectorStarted != 0L) {
            // simulate critical metric value if critical threshold defined
            if ((metric.getThresholdCritical() > 0) &&
                    (Math.random() < ((double)connection.getServicesCriticalPercent())/100.0)) {
                value = metric.getThresholdCritical()+(long)(Math.random()*(double)metric.getThresholdCritical()/2.0);
            } else {
                // compute "normal" max value based on metric thresholds,
                // (includes warning level values if warning threshold defined)
                long maxValue = 100L;
                if (metric.getThresholdCritical() > 0) {
                    if (metric.getThresholdWarning() > 0) {
                        maxValue = (metric.getThresholdCritical() + metric.getThresholdWarning()) / 2;
                    } else {
                        maxValue = (metric.getThresholdCritical() * 9) / 10;
                    }
                } else if (metric.getThresholdWarning() > 0) {
                    maxValue = (metric.getThresholdWarning() * 11) / 10;
                }
                // compute sine based value with period based on metric query spec
                Double periodMillis = metricValuePeriodMillis.get(metric.getQuerySpec());
                if (periodMillis == null) {
                    periodMillis = Math.random()*2400000.0+1200000.0;
                    metricValuePeriodMillis.put(metric.getQuerySpec(), periodMillis);
                }
                double x = (double) (System.currentTimeMillis() - connectorStarted) / periodMillis * 2.0 * Math.PI;
                value = Math.round(((double) maxValue / 2.0 * Math.sin(x)) + ((double) maxValue / 2.0));
            }
        }
        return Long.toString(value);
    }

    /**
     * Generate load test profile metric.
     *
     * @param metricIndex metric index
     * @param name load test metric name
     * @param description load test metric description
     * @param thresholdWarning warning threshold or -1.0
     * @param thresholdCritical critical threshold or -1.0
     * @return load test metric
     */
    private Metric generateMetric(int metricIndex, String name, String description, double thresholdWarning,
                                  double thresholdCritical) {
        return new Metric("loadtest-"+name+"-metric-"+metricIndex, description+" Metric "+metricIndex,
                true, true, thresholdWarning, thresholdCritical, null, null, null, null, null, null);
    }
}
