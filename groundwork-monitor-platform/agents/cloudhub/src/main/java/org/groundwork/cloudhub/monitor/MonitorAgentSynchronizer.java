package org.groundwork.cloudhub.monitor;

import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.MonitoringState;

import java.util.List;

public interface MonitorAgentSynchronizer {

    static final String NAME = "MonitorAgentSynchronizer";

    /**
     * Synchronize additions and deletions of monitored entities between GWOS Server Inventory and Monitored Inventory
     * Additions and Deletions are called based on synchronization algorithms determining drifts in monitored inventory
     * Note these additions are deletions to the GWOS inventory are called immediately from within this method
     * Results are returned in DataCenterSyncResult containing counts of all inventory drifts
     *
     * @param configuration the configuration of the virtualization server and GWOS server
     * @param agentInfo the basic information about the virtualization agent
     * @param monitoringState
     * @param syncResult
     * @return
     */
    MonitoringState synchronize(
            ConnectionConfiguration configuration,
            CloudhubAgentInfo agentInfo,
            MonitoringState monitoringState,
            DataCenterSyncResult syncResult);

    /**
     * Synchronize additions and deletions of inventory items between GWOS Server Inventory and Monitored Inventory
     * Additions and Deletions are called based on synchronization algorithms determining drifts in monitored inventory
     * Results are returned in DataCenterSyncResult containing counts of all inventory drifts
     * NOTE: this method should eventually replace synchronize method above. Currently it does not execute adds, deletes
     *
     * @param monitoredInventory the inventory to be synchronized from the remote virtual management system
     * @param gwosInventory the inventory to be synchronized from the GWOS Server
     * @param configuration the configuration of the virtualization server and GWOS server
     * @param agentInfo the basic information about the virtualization agent
     * @param gwosService the service to handle GWOS persistence
     * @return the results (counts) of all added and deleted inventory
     */
    DataCenterSyncResult synchronizeInventory(DataCenterInventory monitoredInventory,
                                              DataCenterInventory gwosInventory,
                                              ConnectionConfiguration configuration,
                                              CloudhubAgentInfo agentInfo,
                                              GwosService gwosService);

    /**
     * Filter temporary or otherwise undesirable hypervisors by name
     *
     * @param hypervisors the list of hypervisors to filter
     * @return a new list of hypervisors created by this method
     */
    List<BaseHost> filterHypervisors(List<BaseHost> hypervisors);

}
