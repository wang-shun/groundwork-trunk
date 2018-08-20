package org.groundwork.cloudhub.connectors.azure;

import org.groundwork.cloudhub.configuration.AzureConfiguration;
import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service(AzureConfigurationProvider.NAME)
public class AzureConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "AzureConfigurationProvider";

    @Value("${synchronizer.services.azure.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    public static final String HYPERVISOR_DISPLAY_NAME = "Azure Resource Group";
    public static final String MGMT_SERVER_DISPLAY_NAME = "Azure Subscription";

    public static final String CONNECTOR_NAME = "azure";
    public static final String APPLICATIONTYPE_NAME = "AZURE";

    public static final String PREFIX_MGMT_SERVER = "AZURE-M:"; // M for Management Server
    public static final String PREFIX_RESOURCE_GROUP = "AZURE-G:"; // G for Group
    public static final String PREFIX_HYPERVISOR = "AZURE-C:"; // C for controller
    public static final String PREFIX_STORAGE = ConnectorConstants.PREFIX_STORAGE;
    public static final String PREFIX_NETWORK = ConnectorConstants.PREFIX_NETWORK;
    public static final String PREFIX_CLUSTER = ConnectorConstants.PREFIX_CLUSTER;
    public static final String PREFIX_DATACENTER = ConnectorConstants.PREFIX_DATACENTER;
    public static final String PREFIX_RESOURCE_POOL = ConnectorConstants.PREFIX_POOL;
    public static final String PREFIX_VM_STORAGE = ConnectorConstants.PREFIX_VM_STORAGE;
    public static final String PREFIX_VM_NETWORK = ConnectorConstants.PREFIX_VM_NETWORK;

    public static final String AZURE_CLUSTER = "CLUSTER";
    public static final String AZURE_HOST = "HOST";
    public static final String AZURE_RESOURCE_GROUP = "RGROUP";
    public static final String AZURE_VIRTUAL_MACHINES = "Microsoft.Compute/virtualMachines";
    public static final String AZURE_COSMOS_DBS = "Microsoft.DocumentDb/databaseAccounts";
    public static final String AZURE_STORAGE_ACCOUNTS = "Microsoft.Storage/storageAccounts";
    public static final String AZURE_SQL_SERVERS = "Microsoft.Sql/servers";
    public static final String AZURE_SQL_DATABASES = "Microsoft.Sql/servers/databases";
    public static final String AZURE_WEBSITES = "Microsoft.Web/sites";


    public static List<ConfigurationView> createDefaultViews() {
        List<ConfigurationView> views = new LinkedList<>();
        views.add(new ConfigurationView(AZURE_VIRTUAL_MACHINES, true, true));
        views.add(new ConfigurationView(AZURE_COSMOS_DBS, true, true));
        views.add(new ConfigurationView(AZURE_STORAGE_ACCOUNTS, false, true));
        views.add(new ConfigurationView(AZURE_SQL_DATABASES, true, true));
        views.add(new ConfigurationView(AZURE_WEBSITES, true, true));
        views.add(new ConfigurationView(AZURE_SQL_SERVERS, true, false));
        return views;
    }

    public static ConfigurationView getView(List<ConfigurationView> views, String viewName) {
        for (ConfigurationView view : views) {
            if (view.getName().equals(viewName)) {
                return view;
            }
        }
        return null;
    }

    @Override
    public ConnectionConfiguration createConfiguration() {
        AzureConfiguration cc = new AzureConfiguration();
        return cc;
    }

    @Override
    public Class getImplementingClass() {
        return AzureConfiguration.class;
    }
    
    @Override
    public String getHypervisorDisplayName() {
        return HYPERVISOR_DISPLAY_NAME;
    }

    @Override
    public String getManagementServerDisplayName() {
        return MGMT_SERVER_DISPLAY_NAME;
    }

    @Override
    public String getConnectorName() {
        return CONNECTOR_NAME;
    }

    @Override
    public String getApplicationType() {
        return APPLICATIONTYPE_NAME;
    }

    @Override
    public String getPrefix(PrefixType prefixType) {
        switch (prefixType) {
            case ManagementServer:
                return PREFIX_MGMT_SERVER;
            case Hypervisor:
                return PREFIX_HYPERVISOR;
            case Network:
                return PREFIX_NETWORK;
            case Cluster:
                return PREFIX_CLUSTER;
            case Storage:
                return PREFIX_STORAGE;
            case DataCenter:
                return PREFIX_DATACENTER;
            case ResourcePool:
                return PREFIX_RESOURCE_POOL;
            case VmNetwork:
                return PREFIX_VM_NETWORK;
            case VmStorage:
                return PREFIX_VM_STORAGE;
        }
        return null;
    }

    @Override
    protected void initPrefixMap(Map<String, InventoryType> prefixMap) {
        prefixMap.put(PREFIX_HYPERVISOR, InventoryType.Hypervisor);
        prefixMap.put(PREFIX_MGMT_SERVER, InventoryType.Hypervisor);
        prefixMap.put(PREFIX_NETWORK, InventoryType.Network);
        prefixMap.put(PREFIX_CLUSTER, InventoryType.Cluster);
        prefixMap.put(PREFIX_STORAGE, InventoryType.Datastore);
        prefixMap.put(PREFIX_DATACENTER, InventoryType.DataCenter);
        prefixMap.put(PREFIX_RESOURCE_POOL, InventoryType.ResourcePool);
    }

    public boolean isPrimaryMetric(String serviceType) {
        return serviceType == null || serviceType.isEmpty() || serviceType.equals(AZURE_HOST) || serviceType.equals(AZURE_CLUSTER);
    }

    @Override
    public boolean isHostAlsoHostGroup(MonitorAgentSynchronizerService.SynchronizedResource resource) {
        if (resource == null) return false;
        if (resource.getName().startsWith(PREFIX_RESOURCE_GROUP) || resource.getName().startsWith(PREFIX_MGMT_SERVER)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }

    @Override
    public boolean isValidManagementServerHostGroup(String hostGroupName) {
        if (hostGroupName == null || hostGroupName != null && hostGroupName.startsWith(PREFIX_MGMT_SERVER)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSimpleHostGroupName(String hgName) {
        return true;
    }

    @Override
    public String getHostGroupDescription(CloudhubAgentInfo agentInfo, String hostName) {
        return (hostName.startsWith(AzureConfigurationProvider.PREFIX_MGMT_SERVER))
                ? AzureConfigurationProvider.MGMT_SERVER_DISPLAY_NAME : AzureConfigurationProvider.HYPERVISOR_DISPLAY_NAME;
    }

}
