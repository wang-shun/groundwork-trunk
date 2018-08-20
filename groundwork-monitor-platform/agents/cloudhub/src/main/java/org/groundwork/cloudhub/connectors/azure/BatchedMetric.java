package org.groundwork.cloudhub.connectors.azure;

import com.microsoft.azure.management.monitor.MetricDefinition;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;

public class BatchedMetric {
    private BaseQuery query;
    private BaseMetric baseMetric;
    private MetricDefinition definition;
    private String metricName;

    public BatchedMetric(BaseQuery query, MetricDefinition definition, String metricName) {
        this.query = query;
        this.baseMetric = new BaseMetric(
                query.getQuery(),
                query.getWarning(),
                query.getCritical(),
                query.isGraphed(),
                query.isMonitored(),
                query.getCustomName());
        this.definition = definition;
        this.metricName = metricName;
    }

    public BaseQuery getQuery() {
        return query;
    }

    public void setQuery(BaseQuery query) {
        this.query = query;
    }

    public BaseMetric getBaseMetric() {
        return baseMetric;
    }

    public void setBaseMetric(BaseMetric baseMetric) {
        this.baseMetric = baseMetric;
    }

    public MetricDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(MetricDefinition definition) {
        this.definition = definition;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
