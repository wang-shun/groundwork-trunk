package org.groundwork.cloudhub.connectors.cisco;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.CiscoConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(CiscoConfigurationProvider.NAME)
public class CiscoConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "CiscoConfigurationProvider";

    @Value("${synchronizer.services.cisco.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_CISCO              = "CISCO Hypervisor";
    public static String MGMT_SERVER_CISCO             = "CISCO Management Server";

    // Application Type
    public static String APPLICATIONTYPE_CISCO        = "CISCO";

    // Connector Constant
    public static String CONNECTOR_CISCO              = "cisco";

    // Prefixes
    public static String PREFIX_CISCO_MGMT_SERVER      = "CISCO-M:";
    public static String PREFIX_CISCO_HYPERVISOR       = "CISCO-H:";
    public static String PREFIX_CISCO_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_CISCO_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_CISCO_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_CISCO_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_CISCO_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_CISCO_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_CISCO_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;



    @Override
    public ConnectionConfiguration createConfiguration() {
        return new CiscoConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return CiscoConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_CISCO;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_CISCO;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_CISCO;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_CISCO;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_CISCO_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_CISCO_HYPERVISOR;
            case Network:
                return PREFIX_CISCO_NETWORK;
            case Cluster:
                return PREFIX_CISCO_CLUSTER;
            case Storage:
                return PREFIX_CISCO_STORAGE;
            case DataCenter:
                return PREFIX_CISCO_DATACENTER;
            case ResourcePool:
                return PREFIX_CISCO_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_CISCO_VM_NETWORK;
            case VmStorage:
                return PREFIX_CISCO_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_NETWORK, InventoryType.Network);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_CLUSTER, InventoryType.Cluster);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_STORAGE, InventoryType.Datastore);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(CiscoConfigurationProvider.PREFIX_CISCO_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
