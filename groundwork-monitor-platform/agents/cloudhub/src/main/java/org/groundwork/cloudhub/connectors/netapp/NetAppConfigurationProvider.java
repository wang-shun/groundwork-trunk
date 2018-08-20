package org.groundwork.cloudhub.connectors.netapp;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.NetAppConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(NetAppConfigurationProvider.NAME)
public class NetAppConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "NetAppConfigurationProvider";

    @Value("${synchronizer.services.netapp.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_NETAPP             = "NetApp Cluster";
    public static String MGMT_SERVER_NETAPP             = "NetApp Management Server";

    // Application Type
    public static String APPLICATIONTYPE_NETAPP       = "NETAPP";

    // Connector Constant
    public static String CONNECTOR_NETAPP             = "netapp";

    // Prefixes
    public static String PREFIX_NETAPP_MGMT_SERVER      = "NETAPP-M:"; // M for Management Server
    public static String PREFIX_NETAPP_HYPERVISOR       = "NETAPP-C:"; // C for controller
    public static String PREFIX_NETAPP_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_NETAPP_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_NETAPP_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_NETAPP_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_NETAPP_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_NETAPP_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_NETAPP_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    public static String NETAPP_VOLUMES_HOSTGROUP       = "volumes";
    public static String NETAPP_AGGREGATE_HOSTGROUP     = "aggregates";
    public static String NETAPP_VOLUMES_HOSTGROUP_FULL  = PREFIX_NETAPP_STORAGE + "volumes";
    public static String NETAPP_AGGREGATE_HOSTGROUP_FULL= PREFIX_NETAPP_NETWORK + "aggregates";


    @Override
    public ConnectionConfiguration createConfiguration() {
        return new NetAppConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return NetAppConfiguration.class;
    }

    @Override
    public String encryptPassword(ConnectionConfiguration configuration) throws CloudHubException {
        // no passwords supported
        return "";
    }

    @Override
    public String decryptPassword(ConnectionConfiguration configuration) throws CloudHubException {
        // no passwords supported
        return "";
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_NETAPP;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_NETAPP;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_NETAPP;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_NETAPP;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_NETAPP_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_NETAPP_HYPERVISOR;
            case Network:
                return PREFIX_NETAPP_NETWORK;
            case Cluster:
                return PREFIX_NETAPP_CLUSTER;
            case Storage:
                return PREFIX_NETAPP_STORAGE;
            case DataCenter:
                return PREFIX_NETAPP_DATACENTER;
            case ResourcePool:
                return PREFIX_NETAPP_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_NETAPP_VM_NETWORK;
            case VmStorage:
                return PREFIX_NETAPP_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_NETWORK, InventoryType.Network);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_CLUSTER, InventoryType.Cluster);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_STORAGE, InventoryType.Datastore);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(NetAppConfigurationProvider.PREFIX_NETAPP_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isLogicalView(String hostGroupName) {
        return (hostGroupName.equals(NETAPP_VOLUMES_HOSTGROUP_FULL) || hostGroupName.equals(NETAPP_AGGREGATE_HOSTGROUP_FULL));
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
