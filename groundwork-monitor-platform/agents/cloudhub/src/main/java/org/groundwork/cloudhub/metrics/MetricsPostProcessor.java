package org.groundwork.cloudhub.metrics;

import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.synthetics.SyntheticContext;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic Metrics post-processing services:
 *
 * <ul>
 *  <li>crushDownMetrics        - filter out metrics for VMs down or marked as not monitored
 *  <li>processSynthetics       - evaluate metric synthetic expressions for a VMWare resource
 *  <li>createMetricFromQuery   - Create a GW compatible metric from a query
 *  <li>mergePreviousMetricValues  - Merges in from Previous metrics state and value
 * </ul>
 */
@Component
public class MetricsPostProcessor extends BaseMetricsUtil {

    private static Logger log = Logger.getLogger(MetricsPostProcessor.class);

    @Value("${synthetics.expressions.enabled}")
    protected Boolean enableExpressions = true;

    @Autowired
    protected Synthetics synthetics;

    /**
     * Crush down metrics generically for all hosts and vms
     * Any metric that is not flagged as isMonitored will be removed from that list of metrics reported back to Groundwork server
     * Special cases are also made for suspended and unscheduled down VMs to remove metrics
     *
     * @param hostMap the collection of metrics to be filtered and modified
     */
    public void crushDownMetrics(Map<String, ? extends BaseHost> hostMap, boolean hostProcessing) {
        for (BaseHost host : hostMap.values()) {
            boolean crushHostMetrics = false;

            if (hostProcessing && (host.getRunState().equals(GwosStatus.UNSCHEDULED_DOWN.name()) || host.getRunState().contains("SUSPEND"))) {
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
                if (hostProcessing && (vmo.getRunState().equals(GwosStatus.UNSCHEDULED_DOWN.name()) || vmo.getRunState().contains("SUSPEND"))) {
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
     * Evaluate metric synthetic expressions for a VMWare resource, setting the evaluated value on the metric collection of the resource
     *
     * @param metricProvider the host being processed for synthetics
     * @param view the view's query definitions
     */
    public void processSynthetics(MetricProvider metricProvider, MetricViewDefinitions view, MetricCollectionState state)  {

        Map<String,BaseQuery> synths = new HashMap<>();
        for (Map.Entry<String,BaseQuery> queryEntry : view.getQueryMap().entrySet()) {
            BaseQuery vbq = queryEntry.getValue();
            if (vbq.getComputeType().equals(ComputeType.synthetic)) {
                synths.put(queryEntry.getKey(), queryEntry.getValue());
            }
        }
        // optimize, only process synthetics
        if (synths.size() == 0) {
            return;
        }

        // fill the synthetic context with all values
        Map<String,Object> contextObjects = metricProvider.createMetricMap();

        // create a synthetic context
        SyntheticContext context = synthetics.createContext(contextObjects);
        for (Map.Entry<String,BaseQuery> queryEntry : synths.entrySet()) {
            BaseQuery vbq = queryEntry.getValue();
            String query = queryEntry.getKey();
            BaseMetric vbm = createMetricFromQuery(query, vbq, vbq.getCustomName());
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
                handleSyntheticException(vbm, e, metricProvider, query, state);
            }
            metricProvider.putMetric(query, vbm);
        }
    }

    public void handleSyntheticException(BaseMetric metric, CloudHubException e, MetricProvider metricProvider, String query, MetricCollectionState state) {
        metric.setValueOnly("0");
        metric.setCurrState(BaseMetric.sUnknown);
        if (metricProvider.isRunning(state)) {
            String key = (e.getAdditional() == null ? "" : e.getAdditional());
            String message = e.getMessage() + "(" + metricProvider.getName() + ":" + query + ")";
            state.addException(key, message);
        }
    }

    /**
     * The format method is called after the expression is evaluated. The responsibility of matching expression return values
     * with formatting strings is up to the end user. For example, if <code>evaluate</code> returns an Integer, the
     * corresponding format parameter must have a match Java format statement such as "%d", similar for double values "%f"
     *
     * The format statement is a Java format string. It should have only one value substitution. Examples:
     *
     * For example, evaluate returns an Integer percentage, the format statement substitutes the value and adds the percent sign:
     *      <code>%d%%</code>
     * For example, evaluate returns a Double number as MB, the format statement substitutes the value ands MB postfix:
     *      <code>%f.2MB</code>
     *
     * @param value the Number subclassed value such as Integer, Long, Double to be formatted
     * @param format a Java formatting expression for a single value plus additional formatting. Should have only one value substitution
     * @return the formatted string created by applying parameter <code>format</code> to parameter <code>value</code>
     * @throws CloudHubException to normalize all variants of exceptions. The format string is added to the exception as additional information
     */
    public String format(Number value, String format) throws CloudHubException {
        return synthetics.format(value, format);
    }

}
