package org.groundwork.cloudhub.connectors.nedi;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.NediConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(NediConfigurationProvider.NAME)
public class NediConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "NediConfigurationProvider";

    @Value("${synchronizer.services.nedi.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    // Display Names
    public static String HYPERVISOR_NEDI = "NeDi Hypervisor";
    public static String MGMT_SERVER_NEDI = "NeDi Management Server";

    // Application Type
    public static String APPLICATIONTYPE_NEDI = "NEDI";

    // Connector Constant
    public static String CONNECTOR_NEDI = "nedi";

    // Prefixes
    public static String PREFIX_NEDI_MGMT_SERVER = "NEDI-M:";
    public static String PREFIX_NEDI_HYPERVISOR = "NEDI-H:";
    public static String PREFIX_NEDI_STORAGE = ConnectorConstants.PREFIX_STORAGE;
    public static String PREFIX_NEDI_NETWORK = ConnectorConstants.PREFIX_NETWORK;
    public static String PREFIX_NEDI_CLUSTER = ConnectorConstants.PREFIX_CLUSTER;
    public static String PREFIX_NEDI_DATACENTER = ConnectorConstants.PREFIX_DATACENTER;
    public static String PREFIX_NEDI_RESOURCE_POOL = ConnectorConstants.PREFIX_POOL;
    public static String PREFIX_NEDI_VM_STORAGE = ConnectorConstants.PREFIX_VM_STORAGE;
    public static String PREFIX_NEDI_VM_NETWORK = ConnectorConstants.PREFIX_VM_NETWORK;

    public static final String NEDI_HOST = "HOST";

    @Override
    public ConnectionConfiguration createConfiguration() {
        return new NediConfiguration();
    }

    @Override
    public Class getImplementingClass() {
        return NediConfiguration.class;
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
        return HYPERVISOR_NEDI;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_NEDI;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_NEDI;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_NEDI;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_NEDI_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_NEDI_HYPERVISOR;
            case Network:
                return PREFIX_NEDI_NETWORK;
            case Cluster:
                return PREFIX_NEDI_CLUSTER;
            case Storage:
                return PREFIX_NEDI_STORAGE;
            case DataCenter:
                return PREFIX_NEDI_DATACENTER;
            case ResourcePool:
                return PREFIX_NEDI_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_NEDI_VM_NETWORK;
            case VmStorage:
                return PREFIX_NEDI_VM_STORAGE;
        }
        return null;
    }

    protected void initPrefixMap(Map<String, InventoryType> prefixMap) {
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_NETWORK, InventoryType.Network);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_CLUSTER, InventoryType.Cluster);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_STORAGE, InventoryType.Datastore);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(NediConfigurationProvider.PREFIX_NEDI_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    @Override
    public String ensureHypervisorView(String name) {
        if (name != null && name.startsWith(PREFIX_NEDI_MGMT_SERVER)) {
            String names[] = name.split(":");
            if (names.length > 1) {
                return PREFIX_NEDI_HYPERVISOR + names[1];
            }
        }
        return name;
    }

    @Override
    public boolean isHostAlsoHostGroup(MonitorAgentSynchronizerService.SynchronizedResource resource) {
        if (resource == null) return false;
        if (resource.getName() != null && resource.getName().startsWith(PREFIX_NEDI_MGMT_SERVER)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

    @Override
    public boolean isPrimaryMetric(String serviceType) {
        return false;
    }
}