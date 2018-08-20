package org.groundwork.cloudhub.connectors.openstack;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.OpenStackConnection;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.connectors.base.MetricFaultInfo;
import org.groundwork.cloudhub.connectors.openstack.client.AuthClient;
import org.groundwork.cloudhub.connectors.openstack.client.CapabilityInfo;
import org.groundwork.cloudhub.connectors.openstack.client.CeilometerClient;
import org.groundwork.cloudhub.connectors.openstack.client.MetricInfo;
import org.groundwork.cloudhub.connectors.openstack.client.NovaClient;
import org.groundwork.cloudhub.connectors.openstack.client.TenantInfo;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(OpenStackConnector.NAME)
@Scope("prototype")
public class OpenStackConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    private static Logger log = Logger.getLogger(OpenStackConnector.class);

    public static final String TAG_CEILOMETER_ENABLED = "ceilometerEnabled";

    public static final String NAME = "OpenStackConnector";
    public static final String METRIC_NOT_FOUND = "OpenStack Metric not found ";

    private ConnectionState connectionState = ConnectionState.NASCENT;

    private OpenStackConnection connection = null;
    private DataCenterInventory inventory = null;
    private boolean ceilometerEnabled = false;
    private boolean supportsJunoMicroServices = true;

    private static ConcurrentHashMap<String,Pattern> patterns = new ConcurrentHashMap<>();
    private static final String SYNTHETIC_DEFAULT_PATTERN = "^.*(\\(.+\\)).*";

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        AuthClient.AuthResponse response = null;
        boolean authOK = false;
        boolean novaOK = false;
        boolean ceilometerOK = false;
        supportsJunoMicroServices = true; // reset on connect
        try {
            connection = (OpenStackConnection) monitorConnection;
            TenantInfo tenantInfo = new TenantInfo(null, connection.getTenantId(), connection.getTenantName());
            AuthClient auth = new AuthClient(connection);
            auth.logout(connection.getServer()); //  clear cache
            response = auth.login(tenantInfo);
            if (log.isDebugEnabled()) {
                log.debug("authenticated to OpenStack: " + response.getStatus());
            }
            authOK = true;
            if (response.getStatus() == Response.Status.OK) {

                NovaClient nova = new NovaClient(connection);
                nova.listHypervisors(); // will throw exception if cannot connect
                novaOK = true;
                CeilometerClient ceilometer = new CeilometerClient(connection);
                //ceilometer.retrieveMetrics("cpu_util");
                // DST: 7.1.1: removing ceilometer.retrieveMetricDescriptions();, it was returning 40K individual metrics
                //List<MetricMetaInfo> metrics = ceilometer.retrieveMetricDescriptions();
                CapabilityInfo caps = ceilometer.retrieveCapabilities();
                if (caps.getApiCapabilities().size() == 0) {
                    List<MetricInfo> testMeters = ceilometer.retrieveMetrics("cpu_util");
                    if (testMeters.size() == 0) {
                        String msg = "Disabling Ceilometer. Could not connect to Telemetry Service";
                        log.warn(msg);
                    }
                    else {
                        ceilometerEnabled = true;
                    }
                }
                else {
                    ceilometerEnabled = true;
                }
                connectionState = ConnectionState.CONNECTED;
            }
            else {
                connectionState = ConnectionState.DISCONNECTED;
            }
        }
        catch (Exception e) {
            connectionState = ConnectionState.DISCONNECTED;
            if (!authOK) {
                Response.Status status = (response == null) ? Response.Status.INTERNAL_SERVER_ERROR : response.getStatus();
                String msg = "Failed to Authenticate to OpenStack: " + e.getMessage() + ", status: " + status;
                log.error(msg, e);
                throw new ConnectorException(msg, e);
            }
            if (!novaOK) {
                String msg = "Failed to connect to Nova Compute Service: " + e.getMessage();
                log.error(msg, e);
                throw new ConnectorException(msg, e);
            }
            if (!ceilometerOK) {
                String msg = "Disabling Ceilometer. Could not connect to Telemetry Service: " + e.getMessage();
                log.warn(msg, e);
                connectionState = ConnectionState.CONNECTED;
            }
        }
        if (connectionState != ConnectionState.CONNECTED) {
            Response.Status status = (response == null) ? Response.Status.INTERNAL_SERVER_ERROR : response.getStatus();
            throw new ConnectorException("Failed to authenticate to OpenStack: " + status);
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        connectionState = ConnectionState.DISCONNECTED;
        if (connection != null) {
            AuthClient auth = new AuthClient(connection);
            auth.logout(connection.getServer());
        }
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

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
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (connection == null) {
            throw new ConnectorException("no connection, can't browse inventory");
        }
        InventoryBrowser inventoryBrowser = new OpenStackInventoryBrowser(connection);
        InventoryOptions options = new InventoryOptions(true, false ,false ,false, false, "");
        inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException
    {
        long startTime = System.currentTimeMillis();
        MonitoringState monitoringState = new MonitoringState();
        if (priorState == null)
            priorState = new MonitoringState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("OpenStack: collectMetrics(): not connected");
            return priorState;
        }

        if (inventory == null) {
            gatherInventory();
        }

        // set up final filtering of queries
        Map<String, BaseQuery> monitoredHostMetrics = new ConcurrentHashMap<String, BaseQuery>();
        for (BaseQuery query : hostQueries) {
            monitoredHostMetrics.put(query.getQuery(), query);
        }
        Map<String, BaseQuery> monitoredVmMetrics = new ConcurrentHashMap<String, BaseQuery>();
        for (BaseQuery query : vmQueries) {
            monitoredVmMetrics.put(query.getQuery(), query);
        }

        Map<String, BaseQuery> mergedHypervisorQueries = mergeHypervisorMetrics(hostQueries);
        Map<String, BaseQuery> mergedVMQueries = mergeVMMetrics(vmQueries);

        NovaClient nova = new NovaClient(connection);

        // gather all Ceilometer metrics
        Map<String, VmOsMetricInfo> vmMetrics = null;
        if (ceilometerEnabled) {
            CeilometerClient ceilometer = new CeilometerClient(connection);
            vmMetrics = gatherCeilometerMetrics(ceilometer, mergedVMQueries.values());
        }
        else {
            vmMetrics = new HashMap<String, VmOsMetricInfo>();
        }

        // walk through hypervisors v2/%s/servers/detail
        for (InventoryContainerNode hypervisor : inventory.getHypervisors().values()) {
            OpenStackHost host = new OpenStackHost(hypervisor.getName());
            BaseHost priorHost = priorState.hosts().get(hypervisor.getName());
            if (priorHost != null) {
                host.setPrevRunState(priorHost.getRunState());
            }

            // Gather hypervisor metrics
            List<MetricInfo> metrics = nova.getHyperVisorStatistics(hypervisor.getName(), mergedHypervisorQueries.keySet());
            for (MetricInfo metric : metrics) {
                BaseQuery query = mergedHypervisorQueries.get(metric.meter);
                if (query != null) {
                    String translatedMetricName = /* "host." + */ query.getQuery();
                    BaseMetric baseMetric = new BaseMetric(query, translatedMetricName);
                    baseMetric.setCustomName(query.getCustomName());
                    baseMetric.setValue(metric.metric);
                    if (priorHost != null) {
                        BaseMetric priorMetric = priorHost.getMetric(translatedMetricName);
                        if (priorMetric != null) {
                            baseMetric.setLastState(priorMetric.getCurrState());
                        }
                    }
                    host.putMetric(translatedMetricName, baseMetric);
                }
            }

            // compute host synthetic metrics
            for (ExtendedSynthetic synthetic : OpenStackHost.getSyntheticDefinitions()) {
                computePrimarySynthetics(mergedHypervisorQueries, host, synthetic, priorHost);
            }

            // gather VM metrics
            for (VirtualMachineNode vm : hypervisor.getVms().values()) {
                OpenStackVM osvm = new OpenStackVM(vm.getName());
                BaseVM priorVM = null;
                if (priorHost != null) {
                    priorVM = priorHost.getVM(vm.getName());
                    if (priorVM != null) {
                        osvm.setPrevRunState(priorVM.getRunState());
                    }
                }

                if (supportsJunoMicroServices) {
                    vmMetrics = gatherMetricsFromDiagnostics(nova, vm, vmMetrics, mergedVMQueries.values());
                }

                VmOsMetricInfo vmMetric = vmMetrics.get(vm.getSystemName());
                if (vmMetric != null) {
                       // walk through each query type
                      for (List<MetricInfo> metricList : vmMetric.byQuery.values()) {
                        if (metricList != null && metricList.size() > 0) {
                            // get the most recent metric (array list should only be one, but ceilometer setting
                            // may get out of sync with cloudhub setting in OpenStackConnection)
                            MetricInfo metric = metricList.get(0);
                            if (metric != null) {
                                String translatedMetricName = /* "vm." + */ metric.meter;
                                BaseQuery query = mergedVMQueries.get(metric.query);
                                if (query != null) {
                                    BaseMetric baseMetric = new BaseMetric(query, translatedMetricName);
                                    baseMetric.setCustomName(query.getCustomName());
                                    baseMetric.setValue(metric.metric);
                                    if (priorVM != null) {
                                        BaseMetric priorMetric = priorVM.getMetric(translatedMetricName);
                                        if (priorMetric != null) {
                                            baseMetric.setLastState(priorMetric.getCurrState());
                                        }
                                    }
                                    osvm.putMetric(translatedMetricName, baseMetric);
                                }
                                else {
                                    log.error("Query " + metric.query + " not found for VM " + vm.getName() );
                                }
                            }
                        }
                    }
                    // compute VM synthetic metrics
                    for (ExtendedSynthetic synthetic : OpenStackVM.getSyntheticDefinitions()) {
                        Map<String,String> synMatches = matchQueryNames(synthetic.getLookup1(), osvm.getMetricPool());
                        for (Map.Entry<String,String> entry : synMatches.entrySet()) {
                            computeSecondarySynthetics(mergedVMQueries, osvm, synthetic, priorVM, entry.getKey(), entry.getValue());
                        }
                    }
                }
                osvm.setRunState(GwosStatus.UP.status);
                osvm.setRunExtra(vm.getStatus());
                host.putVM(vm.getName(), osvm);
            }
            host.setRunState(hypervisor.getStatus());
            host.setRunExtra(hypervisor.getStatus());
            monitoringState.hosts().put(hypervisor.getName(), host);
        }

        // Remove all metrics that are not monitored
        pushDownUnMonitoredMetrics(monitoringState, monitoredHostMetrics, monitoredVmMetrics);

        if (log.isDebugEnabled()) {
            log.debug("Open Stack collectMetrics completed in " + (startTime - System.currentTimeMillis()) + " ms");
        }

        for (MetricFaultInfo metric : nova.getMetricFaults()) {
            monitoringState.events().add(new MonitoringEvent(metric.getHost(), metric.getQuery(), METRIC_NOT_FOUND + metric.getQuery()));
        }

        return monitoringState;
    }

    protected void computeSecondarySynthetics(Map<String, ? extends BaseQuery> queryPool,
                                              BaseVM vm, ExtendedSynthetic synthetic, BaseVM priorVM,
                                              String sequence, String lookupQuery) {
        BaseQuery vbq = queryPool.get(synthetic.getHandle());
        if (vbq != null && vbq.isMonitored()) {
            String syntheticKey = makeConcreteQueryName(synthetic.getHandle(), sequence);
            BaseMetric vbm = new BaseMetric(vbq, syntheticKey);
            vbm.setCustomName(vbq.getCustomName());
            String value1 = vm.getValueByKey(lookupQuery);
            String value2 = vm.getValueByKey(synthetic.getLookup2());
            String result = String.valueOf(synthetic.compute(value1, value2)) + ((synthetic.isPercent()) ? "%" : "");
            vbm.setValue(result);

            if (priorVM != null) {
                BaseMetric priorMetric = priorVM.getMetric(syntheticKey);
                if (priorMetric != null) {
                    vbm.setLastState(priorMetric.getCurrState());
                }
            }

            if (vbq.isTraced())
                vbm.setTrace();

            vm.putMetric(syntheticKey, vbm);
        }
    }
    /**
     * Builds a map of all metrics by VM. VM Name maps to a List of all MetricInfo for
     *
     */
    public Map<String, VmOsMetricInfo> gatherCeilometerMetrics(CeilometerClient ceilometer, Collection<BaseQuery> queries) {
        Map<String, VmOsMetricInfo> metricMap = new HashMap();
        for (BaseQuery query : queries) {
            if (query.getSourceType() == SourceType.ceilometer) {
                List<MetricInfo> metrics = ceilometer.retrieveMetrics(query.getQuery());
                for (MetricInfo metric : metrics) {
                    VmOsMetricInfo vmMetric = metricMap.get(metric.resource);
                    if (vmMetric == null) {
                        vmMetric = new VmOsMetricInfo(metric.resource);
                        metricMap.put(metric.resource, vmMetric);
                    }
                    List<MetricInfo> metricList = vmMetric.byQuery.get(metric.meter);
                    if (metricList == null) {
                        metricList = new ArrayList<MetricInfo>();
                        vmMetric.byQuery.put(metric.meter, metricList);
                    }
                    metricList.add(metric);
                }
            }
        }
        return metricMap;
    }

    public Map<String, VmOsMetricInfo> gatherMetricsFromDiagnostics(NovaClient nova,
                                                                    VirtualMachineNode vm,
                                                                    Map<String, VmOsMetricInfo> metricMap,
                                                                    Collection<BaseQuery> queries) {
        try {
            List<MetricInfo> metrics = nova.getNovaServerDiagnostics(vm.getSystemName(), vm.getName(), queries);
            for (MetricInfo metric : metrics) {
                VmOsMetricInfo vmMetric = metricMap.get(metric.resource);
                if (vmMetric == null) {
                    vmMetric = new VmOsMetricInfo(metric.resource);
                    metricMap.put(metric.resource, vmMetric);
                }
                List<MetricInfo> metricList = vmMetric.byQuery.get(metric.meter);
                if (metricList == null) {
                    metricList = new ArrayList<MetricInfo>();
                    vmMetric.byQuery.put(metric.meter, metricList);
                }
                metricList.add(metric);
            }
        }
        catch (Exception e) {
            log.error ("Failed to retrieve VM diagnostics for VM: " + vm.getName(), e);
            supportsJunoMicroServices = false;
        }
        return metricMap;
    }

    public class VmOsMetricInfo {
        final String name; // vm name
        // query -> List<MetricInfo>
        final Map<String, List<MetricInfo>> byQuery = new HashMap<String, List<MetricInfo>>();

        VmOsMetricInfo(String name) {
            this.name = name;
        }

    }

    private Map<String, BaseQuery> mergeHypervisorMetrics(List<BaseQuery> hostQueries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();

        for (BaseQuery accessor : OpenStackHost.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenStackHost.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenStackHost.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (hostQueries != null) {
            for (BaseQuery accessor : hostQueries)
                queryPool.put(accessor.getQuery(), accessor);
        }
        return queryPool;
    }

    private Map<String, BaseQuery> mergeVMMetrics(List<BaseQuery> queries) {
        Map<String, BaseQuery> queryPool = new ConcurrentHashMap<>();

        for (BaseQuery accessor : OpenStackVM.getDefaultMetrics())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenStackVM.getDefaultConfigs())
            queryPool.put(accessor.getQuery(), accessor);

        for (BaseQuery accessor : OpenStackVM.getSynthetics())
            queryPool.put(accessor.getQuery(), accessor);

        if (queries != null) {
            for (BaseQuery accessor : queries) {
                queryPool.put(accessor.getQuery(), accessor);
            }
        }
        return queryPool;
    }

    protected int pushDownUnMonitoredMetrics(MonitoringState hostPool,
                                              Map<String, BaseQuery> monitoredHostMetrics,
                                              Map<String, ? extends BaseQuery> monitoredVmMetrics) {
        int count = 0;
        for (BaseHost host : hostPool.hosts().values()) {

            for (BaseMetric metric : host.getMetricPool().values()) {
                String metricBaseName = stripServiceNamePrefix(metric.getQuerySpec());
                if (monitoredHostMetrics.get(metricBaseName) == null) {
                    host.getMetricPool().remove(metric.getQuerySpec());
                    count++;
                }
            }
            for (BaseVM vm : host.getVMPool().values()) {
                for (BaseMetric metric : vm.getMetricPool().values()) {
                    String metricBaseName = (metric.getQueryRegex() == null) ? metric.getQuerySpec() : metric.getQueryRegex();
                    if (monitoredVmMetrics.get(metricBaseName) == null) {
                        vm.getMetricPool().remove(metric.getQuerySpec());
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public String makeConcreteQueryName(String query, String sequence) {
        Pattern pattern = patterns.get(SYNTHETIC_DEFAULT_PATTERN);
        if (pattern == null) {
            pattern = Pattern.compile(SYNTHETIC_DEFAULT_PATTERN);
            patterns.put(query, pattern);
        }
        Matcher m = pattern.matcher(query);
        if (m.matches()) {
            String text = m.group(1);
            return query.replace(text, sequence);
        }
        return null;
    }

    public Map<String,String> matchQueryNames(String query, ConcurrentHashMap<String,BaseMetric> metricPool) {
        Map<String,String> matches = new HashMap<>();
        Pattern pattern = patterns.get(query);
        if (pattern == null) {
            pattern = Pattern.compile(query);
            patterns.put(query, pattern);
        }
        for (String key : metricPool.keySet()) {
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    String match = matcher.group(1);
                    if (match != null) {
                        matches.put(match, key);
                    }
                }
            }
        }
        return matches;
    }

    public String queryConnectorInfo(String tag) {
        if (tag.equals(TAG_CEILOMETER_ENABLED)) {
            return (ceilometerEnabled) ? "true" : "false";
        }
        return super.queryConnectorInfo(tag);
    }

}
