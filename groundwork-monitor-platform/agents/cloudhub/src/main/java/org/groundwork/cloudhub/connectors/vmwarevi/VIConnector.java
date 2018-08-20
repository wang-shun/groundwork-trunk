package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.mo.Folder;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ServiceInstance;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.VmwareConnection;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.connectors.vmware.VmWareNetwork;
import org.groundwork.cloudhub.connectors.vmware.VmWareStorage;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(VIConnector.NAME)
@Scope("prototype")
public class VIConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public static final String NAME = "VIConnector";

    private static Logger log = Logger.getLogger(VIConnector.class);

    private ConnectionState connectionState = ConnectionState.NASCENT;
    private ServiceInstance serviceInstance;

    @Autowired
    private VirtualMachineMetricCollector vmCollector;
    @Autowired
    private HostMetricCollector hostCollector;
    @Autowired
    private StorageMetricCollector storageCollector;
    @Autowired
    private NetworkMetricCollector networkCollector;
    @Autowired
    protected MetricsUtils metricsUtils;

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void testConnection(MonitorConnection monitorConnection) throws ConnectorException {
        try {
            connect(monitorConnection);
            disconnect();
        } catch (Exception e) {
            connectionState = ConnectionState.FAILED;
            throw new ConnectorException(e);
        }
    }

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        VmwareConnection connection = (VmwareConnection) monitorConnection;
        try {
            connectionState = ConnectionState.CONNECTING;
            serviceInstance = new ServiceInstance(new URL(connection.getUrl()), connection.getUsername(), connection.getPassword(), true);
            connectionState = ConnectionState.CONNECTED;
        } catch (Exception e) {
            connectionState = ConnectionState.FAILED;
            throw new ConnectorException(e);
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            try {
                connectionState = ConnectionState.DISCONNECTED;
                serviceInstance.getServerConnection().logout();
            } catch (Exception e) {
                log.error("Failed to disconnect", e);
            }
        }
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        InventoryBrowser inventoryBrowser = new VIInventoryBrowser(serviceInstance);
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        DataCenterInventory inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorResults, List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries) throws ConnectorException {

        try {
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.VMWARE);
            if (priorResults == null)    // safety check BEFORE any return()
                priorResults = new MonitoringState();

            if (connectionState != ConnectionState.CONNECTED) {
                log.error("collectMetrics(): not connected");
                return priorResults;
            }

            CollectorTimer timer = new CollectorTimer("CollectMetrics");
            Folder rootFolder = serviceInstance.getRootFolder();
            InventoryNavigator navigator = new InventoryNavigator(rootFolder);
            // Collect VM metrics
            Map<String, VMwareVM> vmPool = vmCollector.collectMetrics(vmQueries, navigator, timer);
            // Collect Host metrics
            Map<String, VMwareHost> hostPool = hostCollector.collectMetrics(hostQueries, vmPool, navigator, timer);
            Map<String, String> hostSystemNames = new HashMap();

            // Replace vmware system names with required real host names
            for (VMwareHost host : hostPool.values()) {
                hostSystemNames.put(host.getSystemName(), host.getHostName());
            }
            for (VMwareVM vm : vmPool.values()) {
                vm.setHypervisor(hostSystemNames.get(vm.getHypervisor()));
            }

            if (collectionMode.isDoStorageDomains()) {
                Map<String, VmWareStorage> storageMap = storageCollector.collectMetrics(hostQueries, navigator, timer);
                Map<String, VmWareStorage> storageHosts = new ConcurrentHashMap<String, VmWareStorage>();
                for (String storageName : storageMap.keySet()) {
                    VmWareStorage storage = storageMap.get(storageName);
                    String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmStorage) + storageName;
                    storage.setHostName(prefixedName);
                    storageHosts.put(prefixedName, storage);
                }
                for (VmWareStorage storage : storageHosts.values()) {
                    priorResults.hosts().put(storage.getHostName(), storage);
                }
            }

            if (collectionMode.isDoNetworks()) {
                Map<String, VmWareNetwork> networkMap = networkCollector.collectMetrics(hostQueries, navigator, timer);
                Map<String, VmWareNetwork> networkHosts = new ConcurrentHashMap<String, VmWareNetwork>();
                for (String networkName : networkMap.keySet()) {
                    VmWareNetwork network = networkMap.get(networkName);
                    String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmNetwork) + networkName;
                    network.setHostName(prefixedName);
                    networkHosts.put(prefixedName, network);
                }
                for (VmWareNetwork network : networkHosts.values()) {
                    priorResults.hosts().put(network.getHostName(), network);
                }
            }

                // build result from pool
            for (VMwareHost host : hostPool.values()) {
                priorResults.hosts().put(host.getHostName(), host);
            }
            metricsUtils.crushDownMetrics(priorResults.hosts());

            if (log.isDebugEnabled()) {
                log.debug(timer.end());
            }
            return priorResults;

        } catch (Exception e) {
            throw new ConnectorException("Failed to collect metrics", e);
        }
    }




}
