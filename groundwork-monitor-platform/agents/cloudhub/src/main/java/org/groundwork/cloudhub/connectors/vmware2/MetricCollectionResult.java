package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.DynamicProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricCollectionResult {

    private Map<String,MetricCollectorSet> metrics = new HashMap();

    public MetricCollectionResult() {
    }

    public MetricCollectorSet getMetrics(String inventoryType) {
        return metrics.get(inventoryType);
    }

    public MetricCollectorInstance addInstance(String inventoryType, String systemName, List<DynamicProperty> propSet) {
        MetricCollectorInstance instance = new MetricCollectorInstance(systemName, propSet);
        MetricCollectorSet set = metrics.get(inventoryType);
        if (set == null) {
            set = new MetricCollectorSet(inventoryType);
            metrics.put(inventoryType, set);
        }
        set.addInstance(systemName, instance);
        return instance;
    }
    
}
