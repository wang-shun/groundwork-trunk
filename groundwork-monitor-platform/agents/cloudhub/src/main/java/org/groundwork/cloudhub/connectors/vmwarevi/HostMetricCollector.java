package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.ManagedObjectReference;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_BOOTTIME;
import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_HOST_MODEL;
import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_HOST_UPTIME;
import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_NAME;
import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_VM;

/**
 * Collects metrics for VMware Virtual machines
 *
 */
@Component
public class HostMetricCollector {

    private static Logger log = Logger.getLogger(HostMetricCollector.class);

    @Autowired
    private PropertyListManager propertyListManager;
    @Autowired
    protected MetricsUtils metricsUtils;

    private DefaultMetricProvider defaults = new VMwareHost("__default__");

    public Map<String, VMwareHost> collectMetrics(List<BaseQuery> hostQueries, Map<String, VMwareVM> vmPool, InventoryNavigator navigator, CollectorTimer timer) throws Exception {

        if (log.isDebugEnabled()) timer.start("Host-metrics");

        Map<String, BaseQuery> hostQueryPool = new ConcurrentHashMap();
        Map<String, VMwareHost> hostPool = new ConcurrentHashMap();
        // CLOUDHUB-296: custom names
        Map<String, String> customHostNames = new ConcurrentHashMap<>();
        for (BaseQuery query : hostQueries) {
            customHostNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        if (log.isDebugEnabled()) timer.start("Host-inventory");
        ManagedEntity[] managedEntities = navigator.searchManagedEntities(InventoryType.HostSystem.name());
        Set<String> propertySet = propertyListManager.createPropertyNames(hostQueryPool, hostQueries, defaults);

        String[] propertyNames = propertySet.toArray(new String[0]);
        Hashtable[] properties = PropertyCollectorUtil.retrieveProperties(managedEntities, InventoryType.HostSystem.name(), propertyNames);

        if (log.isDebugEnabled()) timer.stopStart("Host-inventory", "Host-core") ;

        for (int ix = 0; ix < managedEntities.length; ix++) {
            VMwareHost host = createHost(managedEntities[ix].getMOR().getVal(), properties[ix], vmPool);
            for (int iy = 0; iy < propertyNames.length; iy++) {
                String path = propertyNames[iy];
                Object value = properties[ix].get(path);
                if (value == null) {
                    continue;
                }
                BaseQuery vbq = hostQueryPool.get(path);
                if (vbq == null) {
                    continue;
                }
                BaseMetric vbm = metricsUtils.createMetricFromQuery(path, vbq, customHostNames.get(path));
                vbm.setValue(value.toString());

                if (defaults.isMetricMonitored(vbq)) {
                    host.putMetric(path, vbm);
                } else {
                    host.putConfig(path, vbm);
                }
            }
            // Synthetics
            for (String query : hostQueryPool.keySet()) {
                if (!query.startsWith("syn.host."))
                    continue;  // move along, not one of ours.

                BaseSynthetic vbs;
                BaseQuery vbq = hostQueryPool.get(query);
                BaseMetric vbm = metricsUtils.createMetricFromQuery(query, vbq, customHostNames.get(query));
                String result = "uncomputed";
                if ((vbs = defaults.getSynthetic(query)) != null) {
                    String value1 = host.getValueByKey(vbs.getLookup1());
                    String value2 = host.getValueByKey(vbs.getLookup2());
                    result = String.valueOf(vbs.compute(value1, value2)) + "%";
                }
                vbm.setValue(result);
                host.putMetric(query, vbm);
            }
            host.setRunState(host.getMonitorState());
            hostPool.put(host.getHostName(), host);
        }
        if (log.isDebugEnabled()) {
            timer.stop("Host-core");
            timer.stop("Host-metrics");
        }

        return hostPool;
    }

    /**
     * Create a Host object and handle all special cases, set all hard-wired properties on that Host
     *
     * @param properties the dynamic properties queried for a given host
     * @return a new CloudHub host
     */
    private VMwareHost createHost(String systemName, Hashtable properties, Map<String, VMwareVM> vmPool) {
        String name = (String) properties.get(PROP_NAME);
        VMwareHost host = new VMwareHost(name);
        host.setSystemName(systemName);

        // Host VMs association
        ManagedObjectReference[] references = (ManagedObjectReference[]) properties.get(PROP_VM);
        if (references != null) {
            for (ManagedObjectReference vmRef : references) {
                VMwareVM vm = vmPool.get(vmRef.getVal());
                if (vm != null) {
                    host.putVM(vm.getVMName(), vm);
                }
            }
        }
        // Boot Time
        Object bootTime = properties.get(PROP_BOOTTIME);
        if (bootTime != null && bootTime instanceof Calendar) {
            Calendar date = (Calendar) bootTime;
            host.setBootDate(date);
        }
        // Up Time
        Integer upTime = (Integer) properties.get(PROP_HOST_UPTIME);
        if (upTime != null) {
            host.setLastUpdate(upTime.toString());
        }
        // Hardware Model
        String hardwareModel = (String) properties.get(PROP_HOST_MODEL);
        if (hardwareModel != null) {
            host.setDescription(hardwareModel);
        }
        return host;
    }
}
