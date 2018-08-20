package org.groundwork.cloudhub.metrics;

import java.util.Map;

public interface MetricProvider {

    /**
     * Return the name of the provider, usually host or VM name
     * @return
     */
    String getName();
    
    /**
     * Add or replace a metric by key
     *
     * @param key
     * @param value
     */
    void putMetric(String key, BaseMetric value);

    /**
     * Get a metric
     *
     * @param key
     * @return
     */
    BaseMetric getMetric(String key);

    /**
     * Return a map of metric names to values
     *
     * @return map of metric names to values
     */
    Map<String,Object> createMetricMap();

    Map<String, BaseMetric> getMetricPool();

    boolean isRunning(MetricCollectionState collector);

}
