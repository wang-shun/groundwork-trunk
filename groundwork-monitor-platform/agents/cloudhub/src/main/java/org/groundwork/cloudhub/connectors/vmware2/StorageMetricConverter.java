package org.groundwork.cloudhub.connectors.vmware2;

import org.groundwork.cloudhub.connectors.vmware.VmWareStorage;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.utils.Conversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StorageMetricConverter extends VmWareBaseConverter {

    public static final String SUMMARY_UNCOMMITTED = "summary.uncommitted";
    public static final String SUMMARY_CAPACITY = "summary.capacity";
    public static final String SUMMARY_FREE_SPACE = "summary.freeSpace";
    @Autowired
    protected MetricsUtils metricsUtils;

    public VmWareStorage convert(MetricCollectorInstance instance, Map<String, BaseQuery> queryPool, Map<String, String> customNames) {
        String name = (String) instance.getProperty(PROP_NAME);
        VmWareStorage storage = new VmWareStorage(name);
        storage.setSystemName(instance.getName());

        Boolean accessible = true;
        Object value = instance.getProperty("summary.accessible");
        if (value instanceof Boolean) {
            accessible = (Boolean) value;
        } else {
            accessible = Boolean.parseBoolean(value.toString());
        }
        String url = (String) instance.getProperty("summary.url");
        if (url != null) {
            storage.setRunExtra(url);
        }
        String type = (String) instance.getProperty("summary.type");
        if (url != null) {
            storage.setDescription(type);
        }

        // Convert all properties to GW metrics
        metricsUtils.processMetrics(storage, instance, queryPool, customNames);
        postProcess(storage, SUMMARY_CAPACITY);
        postProcess(storage, SUMMARY_UNCOMMITTED);
        postProcess(storage, SUMMARY_FREE_SPACE);

        // If inaccessible, clear values
        // TODO: this could be computed ... 
        for (BaseMetric metric : storage.getMetricPool().values()) {
            metric.setValueOnly(accessible ? metric.getCurrValue() : null);
            if (!accessible) {
                metric.setCurrentState();
            }
        }
        for (BaseMetric metric : storage.getConfigPool().values()) {
            metric.setValueOnly(accessible ? metric.getCurrValue() : null);
            if (!accessible) {
                metric.setCurrentState();
            }
        }
        alwaysAddCommitted(storage, queryPool.get(SUMMARY_UNCOMMITTED), customNames.get(SUMMARY_UNCOMMITTED), accessible);

        // Process Synthetics
        metricsUtils.processSynthetics(instance, storage, SourceType.storage, queryPool, customNames);

        // TODO: improve on getMonitorStateByStatus, there are properties being processed outside converter
        storage.setRunningState(storage.getMonitorStateByStatus());

        return storage;
    }

    // TODO: post processing, should be accomplished with synthetics
    protected void postProcess(VmWareStorage storage, String query) {
        BaseMetric m = storage.getMetric(query);
        if (m != null) {
            m.setValueOnly(Conversion.byte2MB(m.getCurrValue()));
        }
    }

    // TODO: Default values could be synthesized
    protected void alwaysAddCommitted(VmWareStorage vmStorage, BaseQuery baseQuery,  String customName, boolean accessible) {
        if (!vmStorage.getMetricPool().containsKey(SUMMARY_UNCOMMITTED) &&
                vmStorage.getMetricPool().containsKey(SUMMARY_CAPACITY) &&
                vmStorage.getMetricPool().containsKey(SUMMARY_FREE_SPACE)) {
            // create summary.uncommitted metric
            BaseMetric vbm = metricsUtils.createMetricFromQuery(SUMMARY_UNCOMMITTED, baseQuery, customName);

            // return unknown status if not accessible
            vbm.setValue(accessible ? "0" : null);
            // add summary.uncommitted metric
            vmStorage.putMetric(SUMMARY_UNCOMMITTED, vbm);
        }
    }
}