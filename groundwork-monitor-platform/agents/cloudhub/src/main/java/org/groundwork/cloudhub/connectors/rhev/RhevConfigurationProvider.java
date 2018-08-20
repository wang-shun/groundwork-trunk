package org.groundwork.cloudhub.connectors.rhev;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.RedhatConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(RhevConfigurationProvider.NAME)
public class RhevConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "RhevConfigurationProvider";

    @Value("${synchronizer.services.rhev.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String MGMT_SERVER_RHEV             = "RHEV management server";
    public static String HYPERVISOR_RHEV              = "RHEV hypervisor";

    // Application Type
    public static String APPLICATIONTYPE_RHEV         = "CHRHEV";

    // Connector Constant
    public static String CONNECTOR_RHEV               = "rhev";

    // ----------------------------------------------------------------------
    // RED HAT ENTITIES
    //
    //  Management server - collects information about a RHEV vm/host collection
    //  Hypervisor        - hardware host
    //  Network           - logical connected network
    //  Cluster           - cluster of VMs
    //  Storage Domain    - that VMs might be attached to
    //  Data Center       - largest entity, the "data center"
    // ----------------------------------------------------------------------

    // Prefixes
    public static String PREFIX_RHEV_MGMT_SERVER      = "RHEV-M:";
    public static String PREFIX_RHEV_HYPERVISOR       = "RHEV-H:";
    public static String PREFIX_RHEV_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_RHEV_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_RHEV_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_RHEV_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_RHEV_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_RHEV_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_RHEV_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new RedhatConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return RedhatConfiguration.class;
    }


    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_RHEV;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_RHEV;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_RHEV;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_RHEV;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_RHEV_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_RHEV_HYPERVISOR;
            case Network:
                return PREFIX_RHEV_NETWORK;
            case Cluster:
                return PREFIX_RHEV_CLUSTER;
            case Storage:
                return PREFIX_RHEV_STORAGE;
            case DataCenter:
                return PREFIX_RHEV_DATACENTER;
            case ResourcePool:
                return PREFIX_RHEV_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_RHEV_VM_NETWORK;
            case VmStorage:
                return PREFIX_RHEV_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_NETWORK, InventoryType.Network);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_CLUSTER, InventoryType.Cluster);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_STORAGE, InventoryType.Datastore);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(RhevConfigurationProvider.PREFIX_RHEV_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }


}
