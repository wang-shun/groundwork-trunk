package org.groundwork.cloudhub.connectors.opendaylight;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.OpenDaylightConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(OpenDaylightConfigurationProvider.NAME)
public class OpenDaylightConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "OpenDaylightConfigurationProvider";

    @Value("${synchronizer.services.opendaylight.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_OPENDAYLIGHT              = "Open Daylight Hypervisor";
    public static String MGMT_SERVER_OPENDAYLIGHT             = "Open Daylight Management Server";

    // Application Type
    public static String APPLICATIONTYPE_OPENDAYLIGHT = "ODL";

    // Connector Constant
    public static String CONNECTOR_OPENDAYLIGHT       = "odl";

    // Prefixes
    public static String PREFIX_OPENDAYLIGHT_MGMT_SERVER      = "ODL-M:";
    public static String PREFIX_OPENDAYLIGHT_HYPERVISOR       = "ODL-H:";
    public static String PREFIX_OPENDAYLIGHT_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_OPENDAYLIGHT_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_OPENDAYLIGHT_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_OPENDAYLIGHT_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_OPENDAYLIGHT_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_OPENDAYLIGHT_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_OPENDAYLIGHT_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new OpenDaylightConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return OpenDaylightConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_OPENDAYLIGHT;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_OPENDAYLIGHT;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_OPENDAYLIGHT;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_OPENDAYLIGHT;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_OPENDAYLIGHT_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_OPENDAYLIGHT_HYPERVISOR;
            case Network:
                return PREFIX_OPENDAYLIGHT_NETWORK;
            case Cluster:
                return PREFIX_OPENDAYLIGHT_CLUSTER;
            case Storage:
                return PREFIX_OPENDAYLIGHT_STORAGE;
            case DataCenter:
                return PREFIX_OPENDAYLIGHT_DATACENTER;
            case ResourcePool:
                return PREFIX_OPENDAYLIGHT_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_OPENDAYLIGHT_VM_NETWORK;
            case VmStorage:
                return PREFIX_OPENDAYLIGHT_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_NETWORK, InventoryType.Network);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_CLUSTER, InventoryType.Cluster);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_STORAGE, InventoryType.Datastore);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(OpenDaylightConfigurationProvider.PREFIX_OPENDAYLIGHT_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
