package org.groundwork.cloudhub.connectors.vmware2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dtaylor on 2/2/17.
 */
public class MetricCollectorSet {

    private Map<String,MetricCollectorInstance> instances = new HashMap<>();
    private String name;

    public MetricCollectorSet(String name) {
        this.name = name;
    }

    public void addInstance(String name, MetricCollectorInstance metricCollectorInstance) {
        instances.put(name, metricCollectorInstance);
    }

    public Collection<MetricCollectorInstance> getInstances() {
        return instances.values();
    }
}
