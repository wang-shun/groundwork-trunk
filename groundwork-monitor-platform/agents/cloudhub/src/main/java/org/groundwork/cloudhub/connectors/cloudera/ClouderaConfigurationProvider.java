package org.groundwork.cloudhub.connectors.cloudera;

import org.groundwork.cloudhub.configuration.BaseConfigurationProvider;
import org.groundwork.cloudhub.configuration.ClouderaConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.monitor.MonitorAgentSynchronizerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service(ClouderaConfigurationProvider.NAME)
public class ClouderaConfigurationProvider extends BaseConfigurationProvider implements ConfigurationProvider {

    public static final String NAME = "ClouderaConfigurationProvider";

    @Value("${synchronizer.services.cloudera.enabled:false}")
    protected Boolean serviceSyncEnabled = false;

    private static final String HYPERVISOR_DISPLAY_NAME = "Cloudera Cluster";
    private static final String MGMT_SERVER_DISPLAY_NAME = "Cloudera Manager";

    private static final String CONNECTOR_NAME = "cloudera";
    private static final String APPLICATIONTYPE_NAME = "CLOUDERA";

    private static final String PREFIX_MGMT_SERVER = "CLOUDERA-M:"; // M for Management Server
    private static final String PREFIX_HYPERVISOR = "CLOUDERA-C:"; // C for controller
    private static final String PREFIX_STORAGE = ConnectorConstants.PREFIX_STORAGE;
    private static final String PREFIX_NETWORK = ConnectorConstants.PREFIX_NETWORK;
    private static final String PREFIX_CLUSTER = ConnectorConstants.PREFIX_CLUSTER;
    private static final String PREFIX_DATACENTER = ConnectorConstants.PREFIX_DATACENTER;
    private static final String PREFIX_RESOURCE_POOL = ConnectorConstants.PREFIX_POOL;
    private static final String PREFIX_VM_STORAGE = ConnectorConstants.PREFIX_VM_STORAGE;
    private static final String PREFIX_VM_NETWORK = ConnectorConstants.PREFIX_VM_NETWORK;

    public static final String CLOUDERA_CLUSTER = "CLUSTER";
    public static final String CLOUDERA_HOST = "HOST";
    public static final String CLOUDERA_SERVICE_HBASE = "HBASE";
    public static final String CLOUDERA_SERVICE_HDFS = "HDFS";
    public static final String CLOUDERA_SERVICE_HIVE = "HIVE";
    public static final String CLOUDERA_SERVICE_HUE = "HUE";
    public static final String CLOUDERA_SERVICE_IMPALA = "IMPALA";
    public static final String CLOUDERA_SERVICE_KSINDEXER = "KS_INDEXER";
    public static final String CLOUDERA_SERVICE_OOZIE = "OOZIE";
    public static final String CLOUDERA_SERVICE_SOLR = "SOLR";
    public static final String CLOUDERA_SERVICE_SPARK = "SPARK_ON_YARN";
    public static final String CLOUDERA_SERVICE_ZOOKEEPER = "ZOOKEEPER";

    public static final String CLOUDERA_SERVICE_YARN = "YARN";
    public static final String CLOUDERA_SERVICE_KAFKA = "KAFKA";
    
    public static List<ConfigurationView> createDefaultViews() {
        List<ConfigurationView> views = new LinkedList<>();
        views.add(new ConfigurationView(CLOUDERA_CLUSTER, true, false));
        views.add(new ConfigurationView(CLOUDERA_HOST, true, false));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_HBASE, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_HDFS, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_HIVE, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_HUE, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_IMPALA, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_KSINDEXER, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_OOZIE, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_SOLR, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_SPARK, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_ZOOKEEPER, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_YARN, true, true));
        views.add(new ConfigurationView(CLOUDERA_SERVICE_KAFKA, true, true));
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
        ClouderaConfiguration cc = new ClouderaConfiguration();
        return cc;
    }

    @Override
    public Class getImplementingClass() {
        return ClouderaConfiguration.class;
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
        return serviceType == null || serviceType.isEmpty() || serviceType.equals(CLOUDERA_HOST) || serviceType.equals(CLOUDERA_CLUSTER);
    }

    @Override
    public boolean isHostAlsoHostGroup(MonitorAgentSynchronizerService.SynchronizedResource resource) {
        if (resource.getType().equals(MonitorAgentSynchronizerService.SynchronizedResourceType.HOST)) {
            BaseHost host = resource.getHost();
            if (host != null) {
                ClouderaHost clouderaHost = (ClouderaHost)host;
                return clouderaHost.getServiceType() != null && clouderaHost.getServiceType().equals(ClouderaConfigurationProvider.CLOUDERA_CLUSTER);
            }
        }
        return true;
    }

    @Override
    public boolean isSynchronizeServicesEnabled() {
        return serviceSyncEnabled;
    }


}
