package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.connectors.vmware.VmWareStorage;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.utils.Conversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.groundwork.cloudhub.connectors.vmwarevi.PropertyListManager.PROP_NAME;

/**
 * Collects metrics for VMware Virtual machines
 *
 */
@Component
public class StorageMetricCollector {

    private static Logger log = Logger.getLogger(StorageMetricCollector.class);

    @Autowired
    private PropertyListManager propertyListManager;

    private DefaultMetricProvider defaults = new VmWareStorage("__default__");

    public Map<String, VmWareStorage> collectMetrics(List<BaseQuery> hostQueries, InventoryNavigator navigator, CollectorTimer timer) throws Exception {

        if (log.isDebugEnabled()) timer.start("Storage-metrics");

        Map<String, BaseQuery> storageQueryPool = new ConcurrentHashMap();
        Map<String, VmWareStorage> storagePool = new ConcurrentHashMap();
        // CLOUDHUB-296: custom names
        Map<String, String> customHostNames = new ConcurrentHashMap<>();
        for (BaseQuery query : hostQueries) {
            customHostNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        if (log.isDebugEnabled()) timer.start("Storage-inventory");
        ManagedEntity[] managedEntities = navigator.searchManagedEntities(InventoryType.Datastore.name());
        Set<String> propertySet = propertyListManager.createPropertyNames(storageQueryPool, hostQueries, defaults);

        String[] propertyNames = propertySet.toArray(new String[0]);
        Hashtable[] properties = PropertyCollectorUtil.retrieveProperties(managedEntities, InventoryType.Datastore.name(), propertyNames);

        if (log.isDebugEnabled()) timer.stopStart("Storage-inventory", "Storage-main") ;

        for (int ix = 0; ix < managedEntities.length; ix++) {

            VmWareStorage datastore = createDatastore(managedEntities[ix].getMOR().getVal(), properties[ix]);
            boolean accessible = true;
            Object accessibleValue = properties[ix].get("summary.accessible");
            if (accessibleValue instanceof Boolean) {
                accessible = (Boolean) accessibleValue;
            } else {
                accessible = Boolean.parseBoolean(accessibleValue.toString());
            }

            for (int iy = 0; iy < propertyNames.length; iy++) {
                String path = propertyNames[iy];
                Object value = properties[ix].get(path);
                if (value == null) {
                    continue;
                }
                BaseQuery vbq = storageQueryPool.get(path);
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
                if (path.equals("summary.capacity") ||
                        path.equals("summary.uncommitted") ||
                        path.equals("summary.freeSpace")) {
                    strValue = Conversion.byte2MB(strValue);
                }
                vbm.setValue(accessible ? strValue.toString() : null);

                if (defaults.isMetricMonitored(vbq)) {
                    datastore.putMetric(path, vbm);
                } else {
                    datastore.putConfig(path, vbm);
                }
            }

            if (!datastore.getMetricPool().containsKey("summary.uncommitted") &&
                    datastore.getMetricPool().containsKey("summary.capacity") &&
                    datastore.getMetricPool().containsKey("summary.freeSpace")) {
                // create summary.uncommitted metric
                String path = "summary.uncommitted";
                BaseQuery vbq = storageQueryPool.get(path);
                BaseMetric vbm = new BaseMetric(
                        "summary.uncommitted",
                        vbq.getWarning(),
                        vbq.getCritical(),
                        vbq.isGraphed(),
                        vbq.isMonitored(),
                        customHostNames.get(path)
                );
                // return unknown status if not accessible
                vbm.setValue(accessible ? "0" : null);
                // add summary.uncommitted metric
                datastore.putMetric("summary.uncommitted", vbm);
            }
            // Synthetics
            for (String query : storageQueryPool.keySet()) {
                if (!query.startsWith("syn.storage"))
                    continue;  // move along, not one of ours.

                BaseSynthetic vbs;
                BaseQuery vbq = storageQueryPool.get(query);
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
                    String value1 = datastore.getValueByKey(vbs.getLookup1());
                    String value2 = datastore.getValueByKey(vbs.getLookup2());

                    result = String.valueOf(vbs.compute(value1, value2)) + "%";
                }
                vbm.setValue(result);

                if (vbq.isTraced())
                    vbm.setTrace();

                datastore.putMetric(query, vbm);
            }
            datastore.setRunState(datastore.getMonitorStateByStatus());
            storagePool.put(datastore.getHostName(), datastore);
        }
        propertyListManager.crushMetrics(storagePool, storageQueryPool);

        if (log.isDebugEnabled()) {
            timer.stop("Storage-core");
            timer.stop("Storage-metrics");
        }
        return storagePool;
    }

    /**
     * Create a Host object and handle all special cases, set all hard-wired properties on that Host
     *
     * @param properties the dynamic properties queried for a given host
     * @return a new CloudHub host
     */
    private VmWareStorage createDatastore(String systemName, Hashtable properties) {
        String name = (String) properties.get(PROP_NAME);
        VmWareStorage storage = new VmWareStorage(name);
        storage.setSystemName(systemName);
        String url = (String) properties.get("summary.url");
        if (url != null) {
            storage.setRunExtra(url);
        }
        String type = (String) properties.get("summary.type");
        if (url != null) {
            storage.setDescription(type);
        }
        return storage;
    }

}
