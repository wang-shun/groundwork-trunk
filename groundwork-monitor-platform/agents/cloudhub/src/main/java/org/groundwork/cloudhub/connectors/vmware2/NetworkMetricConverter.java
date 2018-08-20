package org.groundwork.cloudhub.connectors.vmware2;

import org.groundwork.cloudhub.connectors.vmware.VmWareNetwork;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NetworkMetricConverter extends VmWareBaseConverter {

    public static final String SUMMARY_ACCESSIBLE = "summary.accessible";
    @Autowired
    protected MetricsUtils metricsUtils;

    public VmWareNetwork convert(MetricCollectorInstance instance, Map<String, BaseQuery> queryPool, Map<String, String> customNames) {
        String name = (String) instance.getProperty(PROP_NAME);
        VmWareNetwork network = new VmWareNetwork(name);
        network.setSystemName(instance.getName());

        String ipPool = (String) instance.getProperty("summary.ipPoolName");
        if (ipPool == null) {
            ipPool = "";
        }
        network.setDescription(instance.getName());

        // Convert all properties to GW metrics
        metricsUtils.processMetrics(network, instance, queryPool, customNames);

        String accessible = processAccessible(network,  (Boolean)instance.getProperty(SUMMARY_ACCESSIBLE), queryPool.get(SUMMARY_ACCESSIBLE), customNames);

        // Process Synthetics
        metricsUtils.processSynthetics(instance, network, SourceType.network, queryPool, customNames);

        ipPool = (ipPool.equals("")) ? "no pools configured" : ipPool;
        network.setRunExtra(accessible + " - " + ipPool);
        // TODO: improve on getMonitorStateByStatus, there are properties being processed outside converter
        network.setRunningState(network.getMonitorStateByStatus());
        return network;
    }

    private String processAccessible(VmWareNetwork network, Boolean accessible, BaseQuery vbq, Map<String, String> customNames) {
        String value = (accessible == null || !accessible.toString().toLowerCase().equals("true")) ? "0" : "1";
        BaseMetric vbm = metricsUtils.createMetricFromQuery(SUMMARY_ACCESSIBLE, vbq, customNames.get(SUMMARY_ACCESSIBLE));
        vbm.setValue(value.toString());
        network.putMetric(SUMMARY_ACCESSIBLE, vbm);
        String access = (accessible == null) ? "" : accessible.toString();
        return (access.equalsIgnoreCase("false") || access.equals("")) ? "not accessible" : "accessible";
    }

}