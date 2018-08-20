package org.groundwork.cloudhub.metrics;

/**
 * Defines the contract for providing default metric lists
 */
public interface DefaultMetricProvider extends MetricProvider {

    /**
     * Provide the default synthetic list
     *
     * @return
     */
    BaseQuery[] getDefaultSyntheticList();

    /**
     * Provide a default metric list
     * @return
     */
    BaseQuery[] getDefaultMetricList();

    /**
     * Provide a default config list
     * @return
     */
    BaseQuery[] getDefaultConfigList();

    /**
     * Retrieve a synthetic by handle
     *
     * @param handle
     * @return
     */
    BaseSynthetic getSynthetic(String handle);

    /**
     * Determine if a profile metric should be collected from the virtualization system
     * Usually configs and metrics are collected, synthetics are not
     *
     * @param query
     * @return true if is override
     */
    boolean isMetricCollected(BaseQuery query);

    /**
     * Determine if a profile metric should be placed in the Query pool or not
     * This is a first level filter to remove any overloaded source types
     * For instance Storage and Network source types can be filtered out here
     * Synthetics should have a source type and be included in this filtering
     *
     * @param query
     * @return true if is required to go in pool
     */
    boolean isMetricPoolable(BaseQuery query);

    /**
     * Determine if a metric is monitored or is it a config when building the result set
     * This check is used when building the set of Metrics to be returned to Groundwork server
     *
     * @param query
     * @return
     */
    boolean isMetricMonitored(BaseQuery query);

    /**
     * Add or replace a config by key
     *
     * @param key
     * @param value
     */
    void putConfig(String key, BaseMetric value);

    /**
     * Lookup a metric by key
     *
     * @param key
     * @return
     */
    String getValueByKey(String key);
}
