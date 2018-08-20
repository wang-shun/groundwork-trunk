package org.groundwork.cloudhub.connectors.vmware2;

import org.groundwork.cloudhub.connectors.vmware.VMwareInventoryBrowser.InventoryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtaylor on 2/1/17.
 */
public class PropertyCollectorSpec {

    private final InventoryType inventoryType;
    private List<String> metrics = new ArrayList<>();

    public PropertyCollectorSpec(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public void addMetric(String metric) {
        metrics.add(metric);
    }
}
