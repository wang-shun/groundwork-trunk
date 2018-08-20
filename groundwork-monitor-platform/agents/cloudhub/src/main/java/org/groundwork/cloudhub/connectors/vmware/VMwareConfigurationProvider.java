package org.groundwork.cloudhub.connectors.vmware;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(VMwareConfigurationProvider.NAME)
public class VMwareConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    @Value("${synchronizer.services.vmware.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String MGMT_SERVER_VMWARE           = "vSphere management server";
    public static String HYPERVISOR_VMWARE            = "ESXi hypervisor";

    // Connector Constant
    public static String CONNECTOR_VMWARE             = "vmware";

    // Application Type
    public static String APPLICATIONTYPE_VMWARE       = "VEMA";

    // Prefixes
    public static String PREFIX_VMWARE_MGMT_SERVER    = "VSS:";
    public static String PREFIX_VMWARE_HYPERVISOR     = "ESX:";
    public static String PREFIX_VMWARE_NETWORK        = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_VMWARE_CLUSTER        = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_VMWARE_STORAGE        = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_VMWARE_DATACENTER     = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_VMWARE_RESOURCE_POOL  = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_VMWARE_VM_NETWORK     = ConnectorConstants.PREFIX_VM_NETWORK;
    public static String PREFIX_VMWARE_VM_STORAGE     = ConnectorConstants.PREFIX_VM_STORAGE;

    // profile metric types
    public static final String HYPERVISOR = "hypervisor";
    public static final String VM = "vm";
    public static final String STORAGE = "storage";
    public static final String NETWORK = "network";
    
    public static final String NAME = "VMwareConfigurationProvider";

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new VmwareConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return VmwareConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_VMWARE;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_VMWARE;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_VMWARE;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_VMWARE;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_VMWARE_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_VMWARE_HYPERVISOR;
            case Network:
                return PREFIX_VMWARE_NETWORK;
            case Cluster:
                return PREFIX_VMWARE_CLUSTER;
            case Storage:
                return PREFIX_VMWARE_STORAGE;
            case DataCenter:
                return PREFIX_VMWARE_DATACENTER;
            case ResourcePool:
                return PREFIX_VMWARE_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_VMWARE_VM_NETWORK;
            case VmStorage:
                return PREFIX_VMWARE_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_NETWORK, InventoryType.Network);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_CLUSTER, InventoryType.Cluster);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_STORAGE, InventoryType.Datastore);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(VMwareConfigurationProvider.PREFIX_VMWARE_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isValidManagementServerHostGroup(String hostGroupName) {
        // see CLOUDHUB-322
//        if (hostGroupName == null || (hostGroupName != null && hostGroupName.startsWith(PREFIX_VMWARE_VM_NETWORK))) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}

