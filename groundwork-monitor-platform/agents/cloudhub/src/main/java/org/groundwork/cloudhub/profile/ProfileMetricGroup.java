package org.groundwork.cloudhub.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.CaseFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtaylor on 5/23/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileMetricGroup {

    private String name;
    private String displayName;
    private List<UIMetric> metrics;
    private MetricType metricType;

    public ProfileMetricGroup() {
        this.metrics = new ArrayList<>();
    }

    public ProfileMetricGroup(String serviceType, MetricType metricType) {
        this.name = serviceType;
        this.metrics = new ArrayList<>();
        this.displayName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, serviceType);
        this.metricType = metricType;
    }

    public ProfileMetricGroup(String serviceType, MetricType metricType, String displayName) {
        this.name = serviceType;
        this.metrics = new ArrayList<>();
        this.displayName = displayName;
        this.metricType = metricType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<UIMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<UIMetric> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(UIMetric metric) {
        metrics.add(metric);
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }
}
