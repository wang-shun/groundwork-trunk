package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.connectors.vmware.VmWareNetwork;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects metrics for VMware Virtual machines
 *
 */
@Component
public class NetworkMetricCollector {

    private static Logger log = Logger.getLogger(NetworkMetricCollector.class);

    @Autowired
    private PropertyListManager propertyListManager;

    private DefaultMetricProvider defaults = new VmWareNetwork("__default__");

    public Map<String, VmWareNetwork> collectMetrics(List<BaseQuery> hostQueries, InventoryNavigator navigator, CollectorTimer timer) throws Exception {

        if (log.isDebugEnabled()) timer.start("Network-metrics");

        Map<String, BaseQuery> networkQueryPool = new ConcurrentHashMap();
        Map<String, VmWareNetwork> networkPool = new ConcurrentHashMap();
        // CLOUDHUB-296: custom names
        Map<String, String> customHostNames = new ConcurrentHashMap<>();
        for (BaseQuery query : hostQueries) {
            customHostNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        if (log.isDebugEnabled()) timer.start("Network-inventory");
        ManagedEntity[] managedEntities = navigator.searchManagedEntities(InventoryType.Network.name());
        Set<String> propertySet = propertyListManager.createPropertyNames(networkQueryPool, hostQueries, defaults);

        String[] propertyNames = propertySet.toArray(new String[0]);
        Hashtable[] properties = PropertyCollectorUtil.retrieveProperties(managedEntities, InventoryType.Network.name(), propertyNames);

        if (log.isDebugEnabled()) timer.stopStart("Network-inventory", "Network-main") ;

        for (int ix = 0; ix < managedEntities.length; ix++) {

            VmWareNetwork network = createNetwork(managedEntities[ix].getMOR().getVal(), properties[ix]);
            String ipPool = null;
            String accessible = null;
            for (int iy = 0; iy < propertyNames.length; iy++) {
                boolean forceMetric = false;
                String path = propertyNames[iy];
                Object value = properties[ix].get(path);
                if (value == null) {
                    continue;
                }
                if (path.equals("summary.ipPoolName")) {
                    ipPool = value.toString();
                }
                else if (path.equals("summary.accessible")) {
                    accessible = value.toString();
                    if (value == null || !value.toString().toLowerCase().equals("true")) {
                        value = "0";
                    }
                    else {
                        value = "1";
                    }
                    forceMetric = true;
                }
                BaseQuery vbq = networkQueryPool.get(path);
                if (vbq == null) {
                    continue;
                }
                BaseMetric vbm = new BaseMetric(
                        path,
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        customHostNames.get(path)
                );
                if (vbq.isTraced())
                    vbm.setTrace();
                String strValue = value.toString();
                vbm.setValue(strValue.toString());

                if (forceMetric || defaults.isMetricMonitored(vbq)) {
                    network.putMetric(path, vbm);
                } else {
                    network.putConfig(path, vbm);
                }
            }

            // Synthetics
            for (String query : networkQueryPool.keySet()) {
                if (!query.startsWith("syn.network."))
                    continue;  // move along, not one of ours.

                BaseSynthetic vbs;
                BaseQuery vbq = networkQueryPool.get(query);
                BaseMetric vbm = new BaseMetric(
                        query,
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        customHostNames.get(query)
                );
                String result = "uncomputed";
                if ((vbs = defaults.getSynthetic(query)) != null) {
                    String value1 = network.getValueByKey(vbs.getLookup1());
                    String value2 = network.getValueByKey(vbs.getLookup2());

                    result = String.valueOf(vbs.compute(value1, value2)) + "%";
                }
                vbm.setValue(result);

                if (vbq.isTraced())
                    vbm.setTrace();

                network.putMetric(query, vbm);
            }
            network.setDescription(managedEntities[ix].getMOR().getVal());
            if (accessible == null) {
                accessible = "";
            }
            if (ipPool == null) {
                ipPool = "";
            }
            accessible = (accessible.equalsIgnoreCase("false") || accessible.equals("")) ? "not accessible" : "accessible";
            ipPool = (ipPool.equals("")) ? "no pools configured" : ipPool;
            network.setRunExtra(accessible + " - " + ipPool);
            network.setRunState(network.getMonitorStateByStatus());
            networkPool.put(network.getHostName(), network);
        }
        propertyListManager.crushMetrics(networkPool, networkQueryPool);

        if (log.isDebugEnabled()) {
            timer.stop("Network-core");
            timer.stop("Network-metrics");
        }
        return networkPool;
    }

    /**
     * Create a Host object and handle all special cases, set all hard-wired properties on that Host
     *
     * @param properties the dynamic properties queried for a given host
     * @return a new CloudHub host
     */
    private VmWareNetwork createNetwork(String systemName, Hashtable properties) {
        String name = (String) properties.get(PropertyListManager.PROP_NAME);
        VmWareNetwork network = new VmWareNetwork(name);
        network.setSystemName(systemName);
        return network;
    }

}
