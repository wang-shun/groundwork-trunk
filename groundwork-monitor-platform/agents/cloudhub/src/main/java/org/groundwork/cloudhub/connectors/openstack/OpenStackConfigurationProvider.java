package org.groundwork.cloudhub.connectors.openstack;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.OpenStackConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(OpenStackConfigurationProvider.NAME)
public class OpenStackConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "OpenStackConfigurationProvider";

    @Value("${synchronizer.services.openstack.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_OPENSTACK              = "OpenStack Hypervisor";
    public static String MGMT_SERVER_OPENSTACK             = "OpenStack Management Server";

    // Application Type
    public static String APPLICATIONTYPE_OPENSTACK    = "OS";

    // Connector Constant
    public static String CONNECTOR_OPENSTACK          = "os";

    // Prefixes
    public static String PREFIX_OPENSTACK_MGMT_SERVER      = "OS-M:";
    public static String PREFIX_OPENSTACK_HYPERVISOR       = "OS-H:";
    public static String PREFIX_OPENSTACK_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_OPENSTACK_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_OPENSTACK_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_OPENSTACK_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_OPENSTACK_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_OPENSTACK_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_OPENSTACK_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new OpenStackConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return OpenStackConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_OPENSTACK;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_OPENSTACK;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_OPENSTACK;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_OPENSTACK;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_OPENSTACK_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_OPENSTACK_HYPERVISOR;
            case Network:
                return PREFIX_OPENSTACK_NETWORK;
            case Cluster:
                return PREFIX_OPENSTACK_CLUSTER;
            case Storage:
                return PREFIX_OPENSTACK_STORAGE;
            case DataCenter:
                return PREFIX_OPENSTACK_DATACENTER;
            case ResourcePool:
                return PREFIX_OPENSTACK_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_OPENSTACK_VM_NETWORK;
            case VmStorage:
                return PREFIX_OPENSTACK_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_NETWORK, InventoryType.Network);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_CLUSTER, InventoryType.Cluster);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_STORAGE, InventoryType.Datastore);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(OpenStackConfigurationProvider.PREFIX_OPENSTACK_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
