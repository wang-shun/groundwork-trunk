package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.metrics.*;
import org.groundwork.cloudhub.synthetics.SyntheticContext;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * VMWare Metrics processing services:
 *
 * <ul>
 *  <li>crushDownMetrics        - filter out metrics for VMs down or marked as not monitored
 *  <li>processMetrics          - process and translate metrics from VM representation and build a collection of GW consumable metrics
 *  <li>processSynthetics       - evaluate metric synthetic expressions for a VMWare resource
 *  <li>createMetricFromQuery   - Create a GW compatible metric from a query
 *  <li>mergePreviousMetricValues  - Merges in from Previous metrics state and value
 * </ul>
 */
@Component
public class MetricsUtils extends BaseMetricsUtil {

    private static Logger log = Logger.getLogger(MetricsUtils.class);

    public static final String POWER_STATE_PROPERTY = "summary.runtime.powerState";
    public static final String POWERED_OFF = "poweredOff";

    @Value("${synthetics.expressions.enabled}")
    private Boolean enableExpressions = true;

    @Autowired
    protected Synthetics synthetics;
    
    /**
     * Crush down metrics specific to VMware for all hosts and vms
     * Any metric that is not flagged as isMonitored will be removed from that list of metrics reported back to Groundwork server
     * Special cases are also made for suspended and unscheduled down VMs to remove metrics
     *
     * @param hostMap the collection of metrics to be filtered and modified
     */
    public void crushDownMetrics(Map<String, ? extends BaseHost> hostMap) {
        for (BaseHost host : hostMap.values()) {
            boolean crushHostMetrics = false;

            if (host.getMergeCount() > 0 && host.getRunState().equals(VMwareHost.UNSCHEDULED_DOWN) || host.getRunState().contains("SUSPEND")) {
                crushHostMetrics = true;
            }

            for (String metricName : host.getMetricPool().keySet())
                if (crushHostMetrics || !host.getMetric(metricName).isMonitored())
                    host.getMetricPool().remove(metricName);

            for (String configName : host.getConfigPool().keySet())
                if (crushHostMetrics || !host.getConfig(configName).isMonitored())
                    host.getConfigPool().remove(configName);

            for (BaseVM vmo : host.getVMPool().values()) {
                boolean crushVMMetrics = false;
                if (vmo.getMergeCount() > 0  && vmo.getRunState().equals(VMwareHost.UNSCHEDULED_DOWN) || vmo.getRunState().contains("SUSPEND")) {
                    crushVMMetrics = true;
                }

                for (String metricName : vmo.getMetricPool().keySet())
                    if (crushVMMetrics || !vmo.getMetric(metricName).isMonitored())
                        vmo.getMetricPool().remove(metricName);

                for (String configName : vmo.getConfigPool().keySet())
                    if (crushVMMetrics || !vmo.getConfig(configName).isMonitored())
                        vmo.getConfigPool().remove(configName);
            }
        }
    }

    /**
     * Process and translate metrics from VM representation and build a collection of GW consumable metrics
     *
     * @param resource the VMWare resource such as a VM, Hypervisor, Storage object
     * @param instance the metric results of a single collection pass for a VMWare resource
     * @param queryPool the metric query definitions
     * @param customNames the customized metric name translation table
     */
    public void processMetrics(DefaultMetricProvider resource, MetricCollectorInstance instance, Map<String, BaseQuery> queryPool, Map<String, String> customNames) {
        // Convert all properties to GW metrics
        for (Map.Entry<String,Object> pair : instance.getProperties().entrySet()) {
            String path = pair.getKey();
            Object value = pair.getValue();
            if (value == null) {
                continue;
            }
            BaseQuery vbq = queryPool.get(path);
            if (vbq == null) {
                continue;
            }
            BaseMetric vbm = createMetricFromQuery(path, vbq, customNames.get(path));
            vbm.setValue(value.toString());
            if (resource.isMetricMonitored(vbq)) {
                resource.putMetric(path, vbm);
            } else {
                resource.putConfig(path, vbm);
            }
        }
    }

    /**
     * Evaluate metric synthetic expressions for a VMWare resource, setting the evaluated value on the metric collection of the resource
     *
     * @param resource the VMWare resource such as a VM, Hypervisor, Storage object
     * @param sourceType the synthetic sourceType, null if vm or hypervisor
     * @param queryPool the metric query definitions
     * @param customNames the customized metric name translation table
     */
    public void processSynthetics(MetricCollectorInstance collector, DefaultMetricProvider resource, SourceType sourceType, Map<String, BaseQuery> queryPool, Map<String, String> customNames)  {

        SyntheticContext context = synthetics.createContext(collector.getProperties());
        for (Map.Entry<String,BaseQuery> entry : queryPool.entrySet()) {
            String query = entry.getKey();
            BaseQuery vbq = entry.getValue();
            if (!isSynthetic(vbq, sourceType))
                continue;
            BaseSynthetic vbs;
            BaseMetric vbm = createMetricFromQuery(query, vbq, customNames.get(query));

            // is it an old style or new style synthetic
            if (enableExpressions && !StringUtils.isEmpty(vbq.getExpression())) {
                // user-defined expression
                try {
                    Number syntheticValue = synthetics.evaluate(context, vbq.getExpression());
                    if (!StringUtils.isEmpty(vbq.getFormat())) {
                        vbm.setValueOnly(synthetics.format(syntheticValue,vbq.getFormat()));
                    }
                    else {
                        vbm.setValueOnly(syntheticValue.toString());
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("computing new style synthetic metric: " + vbq.getQuery() + ": " + syntheticValue + " == " + vbm.getCurrValue());
                    }
                }
                catch(CloudHubException e) {
                    vbm.setValueOnly("0");
                    vbm.setCurrState(BaseMetric.sUnknown);
                    if (resource.isRunning(collector)) {
                        String key = (e.getAdditional() == null ? "" : e.getAdditional());
                        String message = e.getMessage() + "(" + collector.getProperty("name") + ":" + query + ")";
                        collector.addException(key, message);
                        log.error(">>>" + message);
                    }
                }
            }
            else
            {
                // hard-coded old style metric
                try {
                    String result = "uncomputed";
                    if ((vbs = resource.getSynthetic(query)) != null) {
                        String value1 = resource.getValueByKey(vbs.getLookup1());
                        String value2 = resource.getValueByKey(vbs.getLookup2());
                        result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("computing old style synthetic metric: " + vbq.getQuery() + ": " + result);
                    }
                    vbm.setValue(result);
                }
                catch(CloudHubException e) {
                    collector.addException(query, e.getMessage());
                }
            }
            resource.putMetric(query, vbm);
        }
    }

    public static boolean isRunning(MetricCollectionState collector) {
        Object o = collector.getProperty(POWER_STATE_PROPERTY);
        if (o != null) {
            if (o instanceof HostSystemPowerState) {
                HostSystemPowerState powerState = (HostSystemPowerState) collector.getProperty(POWER_STATE_PROPERTY);
                if (powerState != null) {
                    String powerStateValue = powerState.value();
                    if (powerStateValue != null && powerStateValue.equalsIgnoreCase(POWERED_OFF)) {
                        return false;
                    }
                }
            }
            else {
                VirtualMachinePowerState powerState = (VirtualMachinePowerState) collector.getProperty(POWER_STATE_PROPERTY);
                if (powerState != null) {
                    String powerStateValue = powerState.value();
                    if (powerStateValue != null && powerStateValue.equalsIgnoreCase(POWERED_OFF)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isSynthetic(BaseQuery baseQuery, SourceType sourceType) {
        if (baseQuery.getComputeType() != null && baseQuery.getComputeType().equals(ComputeType.synthetic)) {
            if (sourceType == null) {
                if (baseQuery.getSourceType() == null) {
                    return true;
                }
                return baseQuery.getSourceType().equals(SourceType.diagnostics) || baseQuery.getSourceType().equals(SourceType.compute);
            }
            return sourceType.equals(baseQuery.getSourceType()) || sourceType.name().equals(baseQuery.getServiceType());
        }
        return false;
    }

    public static Set<String> filterSynthetics(Map<String,BaseQuery> queries) {
        Set<String> names = new HashSet<>();
        for (Map.Entry<String,BaseQuery> entry : queries.entrySet()) {
            if (entry.getValue().getComputeType() == null || !entry.getValue().getComputeType().equals(ComputeType.synthetic)) {
                names.add(entry.getKey());
            }
        }
        return names;
    }
}
