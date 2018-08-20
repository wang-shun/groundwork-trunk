package org.groundwork.cloudhub.connectors.vmware2;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareConfigurationProvider;
import org.groundwork.cloudhub.connectors.vmware.VMwareConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.connectors.vmware.VMwareInventoryBrowser.InventoryType;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.connectors.vmware.VmWareNetwork;
import org.groundwork.cloudhub.connectors.vmware.VmWareStorage;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VMware Connector mach 2
 * 6.5 docs
 * https://code.vmware.com/apis/196/vsphere#https%3A%2F%2Fvdc-repo.vmware.com%2Fvmwb-repository%2Fdcr-public%2F6b586ed2-655c-49d9-9029-bc416323cb22%2Ffa0b429a-a695-4c11-b7d2-2cbc284049dc%2Fdoc%2Findex-do_types.html
 */
@Service(VmWareConnector2.NAME)
@Scope("prototype")
public class VmWareConnector2 extends VMwareConnector implements MonitoringConnector, ManagementConnector {

    public static final String NAME = "VmWareConnector2";

    private static Logger log = Logger.getLogger(VmWareConnector2.class);

    @Autowired
    protected VmWareCollector vmWareCollector;
    @Autowired
    protected VirtualMachineConverter vmConverter;
    @Autowired
    protected HostMetricConverter hostConverter;
    @Autowired
    protected StorageMetricConverter storageConverter;
    @Autowired
    protected NetworkMetricConverter networkConverter;
    @Autowired
    protected MetricsUtils metricsUtils;
    @Resource(name = ProfileService.NAME)
    protected ProfileService profileService;

    private DefaultMetricProvider defaultVM = new VMwareVM("__default__");
    private DefaultMetricProvider defaultHost = new VMwareHost("__default__");
    private DefaultMetricProvider defaultStorage = new VmWareStorage("__default__");
    private DefaultMetricProvider defaultNetwork = new VmWareNetwork("__default__");

    public VmWareConnector2() {
        super();
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorResults,
                                          final List<BaseQuery> hostQueries, final List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {

        if (priorResults == null)    // safety check BEFORE any return()
            priorResults = new MonitoringState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("collectMetrics(): not connected");
            return priorResults;
        }
        priorResults.events().clear();

        CollectorTimer timer = new CollectorTimer("CollectMetrics");

        Map<String, VMwareVM> vmPool = new ConcurrentHashMap<>();
        Map<String, VMwareHost> hostPool = new ConcurrentHashMap<>();

        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.VMWARE);
        // CLOUDHUB-296: custom names
        Map<String, String> customHostNames = new ConcurrentHashMap<>();
        for (BaseQuery query : hostQueries) {
            customHostNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }
        Map<String, String> customVmNames = new ConcurrentHashMap<>();
        for (BaseQuery query : vmQueries) {
            customVmNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        // Create Property Specs
        List<PropertyCollectorSpec> retrievalSpecs = new ArrayList<PropertyCollectorSpec>();

        // Create Property Specs for Virtual machines
        PropertyCollectorSpec vmSpec = new PropertyCollectorSpec(InventoryType.VirtualMachine);
        Map<String, BaseQuery> vmQueryPool = createQueryPool(vmQueries, vmSpec, defaultVM);
        retrievalSpecs.add(vmSpec);
        // CLOUDHUB-333: add support for snapshots
        snapshotService.configureCollectorSpec(vmQueryPool, vmSpec);

        // Create Property Specs for Hosts
        PropertyCollectorSpec hostSpec = new PropertyCollectorSpec(InventoryType.HostSystem);
        Map<String, BaseQuery> hostQueryPool = createQueryPool(hostQueries, hostSpec, defaultHost);
        retrievalSpecs.add(hostSpec);

        // Create Property Specs for Storage
        Map<String, BaseQuery> storageQueryPool = new ConcurrentHashMap<String, BaseQuery>();
        if (collectionMode.isDoStorageDomains()) {
            PropertyCollectorSpec storageSpec = new PropertyCollectorSpec(InventoryType.Datastore);
            storageQueryPool = createQueryPool(hostQueries, storageSpec, defaultStorage);
            retrievalSpecs.add(storageSpec);
        }

        // Create Property Specs for Networks
        Map<String, BaseQuery> networkQueryPool = new ConcurrentHashMap<String, BaseQuery>();
        ;
        if (collectionMode.isDoNetworks()) {
            PropertyCollectorSpec networkSpec = new PropertyCollectorSpec(InventoryType.Network);
            networkQueryPool = createQueryPool(hostQueries, networkSpec, defaultNetwork);
            retrievalSpecs.add(networkSpec);
        }

        // Collect metrics from VMware
        if (log.isDebugEnabled()) timer.start("collect-props");
        MetricCollectionResult result = vmWareCollector.collectMetrics(retrievalSpecs, serviceContent, vimPort);
        if (log.isDebugEnabled()) timer.stop("collect-props");

        // convert and synthesize VM metrics
        MetricCollectorSet vms = result.getMetrics(InventoryType.VirtualMachine.name());
        Map<String, String> vmExceptions = new HashMap<>();
        if (vms != null) {
            if (log.isDebugEnabled()) timer.start("process-vms");
            for (MetricCollectorInstance instance : vms.getInstances()) {

                VMwareVM vm = vmConverter.convert(instance, vmQueryPool, customVmNames);
                vmPool.put(vm.getSystemName(), vm);

                // consolidate expression exceptions into one exception per query/expression error
                if (instance.getExceptions().size() > 0) {
                    for (Map.Entry<String, String> e : instance.getExceptions().entrySet()) {
                        vmExceptions.put(e.getKey(), e.getValue());
                    }
                }
            }
            if (log.isDebugEnabled()) timer.stop("process-vms");
        }
        // if any consolidated exceptions, log them...
        if (vmExceptions.size() > 0) {
            for (Map.Entry<String, String> e : vmExceptions.entrySet()) {
                log.error("VM metric/expression: " + e.getValue());
            }
        }

        // convert and synthesize Host metrics
        MetricCollectorSet hosts = result.getMetrics(InventoryType.HostSystem.name());
        Map<String, String> hostExceptions = new HashMap<>();
        if (hosts != null) {
            if (log.isDebugEnabled()) timer.start("process-hosts");
            for (MetricCollectorInstance instance : hosts.getInstances()) {

                VMwareHost host = hostConverter.convert(instance, hostQueryPool, customHostNames, vmPool);
                hostPool.put(host.getSystemName(), host);

                // consolidate expression exceptions into one exception per query/expression error
                if (instance.getExceptions().size() > 0) {
                    for (Map.Entry<String, String> e : instance.getExceptions().entrySet()) {
                        hostExceptions.put(e.getKey(), e.getValue());
                    }
                }
            }
            if (log.isDebugEnabled()) timer.stop("process-hosts");
        }
        // if any consolidated exceptions, log them...
        if (hostExceptions.size() > 0) {
            for (Map.Entry<String, String> e : hostExceptions.entrySet()) {
                log.error("Host metric/expression: " + e.getValue());
            }
        }

        // Replace vmware system names with required real host names
        Map<String, String> hostSystemNames = new HashMap();
        for (VMwareHost host : hostPool.values()) {
            hostSystemNames.put(host.getSystemName(), host.getHostName());
        }
        for (VMwareVM vm : vmPool.values()) {
            vm.setHypervisor(hostSystemNames.get(vm.getHypervisor()));
        }

        if (collectionMode.isDoStorageDomains()) {
            // convert and synthesize Storage metrics
            if (log.isDebugEnabled()) timer.start("process-storage");
            MetricCollectorSet datastores = result.getMetrics(InventoryType.Datastore.name());
            if (datastores != null) {
                Map<String, VmWareStorage> storageHosts = new ConcurrentHashMap<String, VmWareStorage>();
                Map<String, String> storageExceptions = new HashMap<>();
                for (MetricCollectorInstance instance : datastores.getInstances()) {

                    VmWareStorage store = storageConverter.convert(instance, storageQueryPool, customHostNames);
                    String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmStorage) + store.getHostName();
                    store.setHostName(prefixedName);
                    storageHosts.put(prefixedName, store);
                    hostPool.put(prefixedName, store);

                    // consolidate expression exceptions into one exception per query/expression error
                    if (instance.getExceptions().size() > 0) {
                        for (Map.Entry<String, String> e : instance.getExceptions().entrySet()) {
                            storageExceptions.put(e.getKey(), e.getValue());
                        }
                    }
                }
                // if any consolidated exceptions, log them...
                if (storageExceptions.size() > 0) {
                    for (Map.Entry<String, String> e : storageExceptions.entrySet()) {
                        log.error("Storage metric/expression: " + e.getValue());
                    }
                }
            }
            if (log.isDebugEnabled()) timer.stop("process-storage");
        }

        if (collectionMode.isDoNetworks()) {
            // convert and synthesize Network metrics
            if (log.isDebugEnabled()) timer.start("process-network");
            MetricCollectorSet networks = result.getMetrics(InventoryType.Network.name());
            if (networks != null) {
                Map<String, VmWareNetwork> networkHosts = new ConcurrentHashMap<String, VmWareNetwork>();
                Map<String, String> networkExceptions = new HashMap<>();
                for (MetricCollectorInstance instance : networks.getInstances()) {

                    VmWareNetwork network = networkConverter.convert(instance, networkQueryPool, customHostNames);
                    String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmNetwork) + network.getHostName();
                    network.setHostName(prefixedName);
                    networkHosts.put(prefixedName, network);
                    hostPool.put(prefixedName, network);

                    // consolidate expression exceptions into one exception per query/expression error
                    if (instance.getExceptions().size() > 0) {
                        for (Map.Entry<String, String> e : instance.getExceptions().entrySet()) {
                            networkExceptions.put(e.getKey(), e.getValue());
                        }
                    }
                }
                // if any consolidated exceptions, log them...
                if (networkExceptions.size() > 0) {
                    for (Map.Entry<String, String> e : networkExceptions.entrySet()) {
                        log.error("Network metric/expression: " + e.getValue());
                    }
                }
            }
            if (log.isDebugEnabled()) timer.stop("process-network");
        }

        if (log.isDebugEnabled()) timer.start("process-merge");
        if (priorResults.hosts().size() == 0) {
            for (VMwareHost host : hostPool.values()) {
                priorResults.hosts().put(host.getHostName(), host);
            }
        } else {
            // merge in lastStatus and lastValue from previous run
            for (VMwareHost host : hostPool.values()) {
                VMwareHost priorHost = (VMwareHost) priorResults.hosts().get(host.getHostName());
                if (priorHost != null) {
                    String priorRunState = priorHost.getRunState();
                    host.setPrevRunState((priorRunState == null) ? "" : priorRunState);
                    host.incrementMergeCount();
                    metricsUtils.mergePreviousMetricValues(priorHost.getMetricPool(), host.getMetricPool());
                    for (BaseVM vm : host.getVMPool().values()) {
                        BaseVM priorVM = priorHost.getVM(vm.getVMName());
                        if (priorVM != null) {
                            String priorVmRunState = priorVM.getRunState();
                            vm.incrementMergeCount();
                            vm.setPrevRunState((priorVmRunState == null) ? "" : priorVmRunState);
                            metricsUtils.mergePreviousMetricValues(priorVM.getMetricPool(), vm.getMetricPool());
                        }
                    }
                }
            }
            priorResults = new MonitoringState();
            for (VMwareHost host : hostPool.values()) {
                priorResults.hosts().put(host.getHostName(), host);
            }
        }
        if (log.isDebugEnabled()) timer.stop("process-merge");

        if (log.isDebugEnabled()) timer.start("process-crush");
        metricsUtils.crushDownMetrics(priorResults.hosts());
        if (log.isDebugEnabled()) timer.stop("process-crush");

        if (log.isDebugEnabled()) {
            log.debug(timer.end());
        }
        return priorResults;
    }


    /**
     * Build a query pool from defaults provider and configuration profile. vmSpec is also created here
     *
     * @param queries
     * @param spec
     * @param metricProvider
     * @return
     */
    protected Map<String, BaseQuery> createQueryPool(List<BaseQuery> queries, PropertyCollectorSpec spec, DefaultMetricProvider metricProvider) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery query : metricProvider.getDefaultMetricList()) {
            queryPool.put(query.getQuery(), query);
            if (query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic)) {
                spec.getMetrics().add(query.getQuery());
            }
        }
        for (BaseQuery query : metricProvider.getDefaultConfigList()) {
            queryPool.put(query.getQuery(), query);
            if (query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic)) {
                spec.getMetrics().add(query.getQuery());
            }
        }
        for (BaseQuery query : metricProvider.getDefaultSyntheticList()) {
            queryPool.put(query.getQuery(), query);
            if (query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic)) {
                spec.getMetrics().add(query.getQuery());
            }
        }
        // this is LAST in order to OVERRIDE the above defaults
        for (BaseQuery query : queries) {
            if (metricProvider.isMetricPoolable(query)) {
                queryPool.put(query.getQuery(), query);
                if (metricProvider.isMetricCollected(query)) {
                    spec.getMetrics().add(query.getQuery());
                }
            }
        }
        return queryPool;
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        CloudHubProfile profile = (CloudHubProfile) profileService.readProfileTemplate(VirtualSystem.VMWARE);
        List<String> names = new LinkedList<>();
        if (serviceType.equals(VMwareConfigurationProvider.VM)) {
            for (Metric metric : profile.getVm().getMetrics()) {
                if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                    names.add(metric.getName());
                }
            }
        } else if (serviceType.equals(VMwareConfigurationProvider.HYPERVISOR)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (StringUtils.isEmpty(metric.getSourceType()) && (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic))) {
                    names.add(metric.getName());
                }
            }
        } else if (serviceType.equals(VMwareConfigurationProvider.STORAGE)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (!StringUtils.isEmpty(metric.getSourceType()) && metric.getSourceType().equals(VMwareConfigurationProvider.STORAGE)) {
                    if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                        names.add(metric.getName());
                    }
                }
            }
        } else if (serviceType.equals(VMwareConfigurationProvider.NETWORK)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (!StringUtils.isEmpty(metric.getSourceType()) && metric.getSourceType().equals(VMwareConfigurationProvider.NETWORK)) {
                    if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                        names.add(metric.getName());
                    }
                }
            }
        }
        return names;
    }
}