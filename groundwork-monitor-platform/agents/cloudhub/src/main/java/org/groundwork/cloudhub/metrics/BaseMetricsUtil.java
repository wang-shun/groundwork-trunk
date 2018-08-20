package org.groundwork.cloudhub.metrics;

import org.groundwork.agents.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseMetricsUtil {

    /**
     * Create a GW compatible metric from a query
     *
     * @param name the query name of the metric
     * @param vbq the base query definition
     * @param customName the customized metric name translation table
     * @return
     */
    public BaseMetric createMetricFromQuery(String name, BaseQuery vbq, String customName) {
        BaseMetric vbm = new BaseMetric(
                name,
                vbq.getWarning(),
                vbq.getCritical(),
                vbq.isGraphed(),
                vbq.isMonitored(),
                customName
        );
        if (vbq.getServiceType() != null) {
            vbm.setMetricType(vbq.getServiceType());
        }
        if (vbq.isTraced())
            vbm.setTrace();
        return vbm;
    }

    /**
     * Merges in from Previous metrics the following values:
     *  1. prevLastState
     *  2. lastValue
     *
     * @param prevMetrics the metrics gathered in the previous collection run for a VMWare resource
     * @param metrics the metrics gathered in the currrent collection run for a VMWare resource
     */
    public void mergePreviousMetricValues(Map<String, BaseMetric> prevMetrics, Map<String, BaseMetric> metrics) {
        for (BaseMetric metric : metrics.values()) {
            BaseMetric prevMetric = prevMetrics.get(metric.getQuerySpec());
            if (prevMetric != null) {
                String lastState = prevMetric.getCurrState();
                metric.setLastState((lastState == null) ? "" : lastState);
                String lastValue = prevMetric.getCurrValue();
                metric.setLastValue((lastValue == null) ? null : lastValue); // backward compat, use null
            }
        }
    }

    /*
    TODO: improve on this. Represent metricValue in BaseMetric. Unfortunately we store these as Strings
            public class MetricValue {
                MetricType type; // the dataType of the metric, support Integer, Long, String, Boolean
                MetricUnit unit;
                Object value; // would prefer this to be Number, but Health values can be Strings
     */
    public static Map<String,Object> createMetricMap(Map<String,BaseMetric> pool) {
        Map<String,Object> contextObjects = new HashMap();
        Number number;
        for (Map.Entry<String,BaseMetric> entry : pool.entrySet()) {
            try {
                String currValue = entry.getValue().getCurrValue();
                currValue = (StringUtils.isEmpty(currValue)) ? "0" : currValue;
                if (Character.isLetter(currValue.charAt(0))) {
                    contextObjects.put(entry.getKey(), currValue);
                    continue;
                }
                number = (currValue.indexOf(".") > -1) ? Double.parseDouble(currValue) : Long.parseLong(currValue);
            }
            catch (Exception e) {
                number = 0L;
            }
            contextObjects.put(entry.getKey(), number);
        }
        return contextObjects;
    }

}
