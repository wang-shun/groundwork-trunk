package org.groundwork.cloudhub.connectors.docker;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(DockerConfigurationProvider.NAME)
public class DockerConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "DockerConfigurationProvider";

    @Value("${synchronizer.services.docker.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_DOCKER              = "Docker Hypervisor";
    public static String MGMT_SERVER_DOCKER             = "Docker Management Server";

    // Application Type
    public static String APPLICATIONTYPE_DOCKER       = "DOCK";

    // Connector Constant
    public static String CONNECTOR_DOCKER             = "dock";

    // Prefixes
    public static String PREFIX_DOCKER_MGMT_SERVER      = "DOCK-M:";
    public static String PREFIX_DOCKER_HYPERVISOR       = "DOCK-H:";
    public static String PREFIX_DOCKER_STORAGE          = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_DOCKER_NETWORK          = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_DOCKER_CLUSTER          = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_DOCKER_DATACENTER       = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_DOCKER_RESOURCE_POOL    = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_DOCKER_VM_STORAGE       = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_DOCKER_VM_NETWORK       = ConnectorConstants.PREFIX_VM_NETWORK;

    public static final String DOCKER_ENGINE            = "engine";
    public static final String DOCKER_CONTAINER         = "container";

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new DockerConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return DockerConfiguration.class;
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
        return HYPERVISOR_DOCKER;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_DOCKER;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_DOCKER;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_DOCKER;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_DOCKER_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_DOCKER_HYPERVISOR;
            case Network:
                return PREFIX_DOCKER_NETWORK;
            case Cluster:
                return PREFIX_DOCKER_CLUSTER;
            case Storage:
                return PREFIX_DOCKER_STORAGE;
            case DataCenter:
                return PREFIX_DOCKER_DATACENTER;
            case ResourcePool:
                return PREFIX_DOCKER_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_DOCKER_VM_NETWORK;
            case VmStorage:
                return PREFIX_DOCKER_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String,InventoryType> prefixMap) {
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_NETWORK, InventoryType.Network);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_CLUSTER, InventoryType.Cluster);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_STORAGE, InventoryType.Datastore);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(DockerConfigurationProvider.PREFIX_DOCKER_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public String ensureHypervisorView(String name) {
        if (name != null && name.startsWith(PREFIX_DOCKER_MGMT_SERVER)) {
            String names[] = name.split(":");
            if (names.length > 1) {
                return PREFIX_DOCKER_HYPERVISOR + names[1];
            }
        }
        return name;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

}
