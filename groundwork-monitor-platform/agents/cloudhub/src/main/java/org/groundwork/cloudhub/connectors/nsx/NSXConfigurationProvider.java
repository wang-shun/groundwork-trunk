package org.groundwork.cloudhub.connectors.nsx;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.NSXConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(NSXConfigurationProvider.NAME)
public class NSXConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "NSXConfigurationProvider";

    @Value("${synchronizer.services.nsx.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_NSX              = "NSX Hypervisor";
    public static String MGMT_SERVER_NSX             = "NSX Management Server";

    // Application Type
    public static String APPLICATIONTYPE_NSX          = "NSX";

    // Connector Constant
    public static String CONNECTOR_NSX                = "nsx";

    // Prefixes
    public static String PREFIX_NSX_MGMT_SERVER      = "NSX-M:";
    public static String PREFIX_NSX_HYPERVISOR       = "NSX-H:";
    public static String PREFIX_NSX_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_NSX_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_NSX_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_NSX_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_NSX_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_NSX_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_NSX_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new NSXConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return NSXConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_NSX;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_NSX;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_NSX;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_NSX;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_NSX_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_NSX_HYPERVISOR;
            case Network:
                return PREFIX_NSX_NETWORK;
            case Cluster:
                return PREFIX_NSX_CLUSTER;
            case Storage:
                return PREFIX_NSX_STORAGE;
            case DataCenter:
                return PREFIX_NSX_DATACENTER;
            case ResourcePool:
                return PREFIX_NSX_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_NSX_VM_NETWORK;
            case VmStorage:
                return PREFIX_NSX_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_NETWORK, InventoryType.Network);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_CLUSTER, InventoryType.Cluster);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_STORAGE, InventoryType.Datastore);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(NSXConfigurationProvider.PREFIX_NSX_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
