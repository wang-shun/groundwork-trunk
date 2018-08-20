package org.groundwork.cloudhub.connectors.openshift;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.OpenShiftConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(OpenShiftConfigurationProvider.NAME)
public class OpenShiftConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "OpenShiftConfigurationProvider";

    @Value("${synchronizer.services.openshift.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_OPENSHIFT              = "OpenShift Hypervisor";
    public static String MGMT_SERVER_OPENSHIFT             = "OpenShift Management Server";

    // Application Type
    public static String APPLICATIONTYPE_OPENSHIFT    = "SHIFT";

    // Connector Constant
    public static String CONNECTOR_OPENSHIFT          = "openshift";

    // Prefixes
    public static String PREFIX_OPENSHIFT_MGMT_SERVER      = "ROS-M:";
    public static String PREFIX_OPENSHIFT_HYPERVISOR       = "ROS-H:";
    public static String PREFIX_OPENSHIFT_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_OPENSHIFT_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_OPENSHIFT_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_OPENSHIFT_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_OPENSHIFT_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_OPENSHIFT_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_OPENSHIFT_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new OpenShiftConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return OpenShiftConfiguration.class;
    }

    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_OPENSHIFT;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_OPENSHIFT;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_OPENSHIFT;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_OPENSHIFT;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_OPENSHIFT_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_OPENSHIFT_HYPERVISOR;
            case Network:
                return PREFIX_OPENSHIFT_NETWORK;
            case Cluster:
                return PREFIX_OPENSHIFT_CLUSTER;
            case Storage:
                return PREFIX_OPENSHIFT_STORAGE;
            case DataCenter:
                return PREFIX_OPENSHIFT_DATACENTER;
            case ResourcePool:
                return PREFIX_OPENSHIFT_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_OPENSHIFT_VM_NETWORK;
            case VmStorage:
                return PREFIX_OPENSHIFT_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_NETWORK, InventoryType.Network);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_CLUSTER, InventoryType.Cluster);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_STORAGE, InventoryType.Datastore);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(OpenShiftConfigurationProvider.PREFIX_OPENSHIFT_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
