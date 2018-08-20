package org.groundwork.cloudhub.profile;

import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileServiceState implements Serializable {

    private static Logger log = Logger.getLogger(ProfileServiceState.class);

    private Set<String> primary = new HashSet<String>();
    private Set<String> secondary = new HashSet<String>();
    private Set<String> custom = new HashSet<String>();
    private Map<String, UIMetric> primaryMetrics = new HashMap<>();
    private Map<String, UIMetric> secondaryMetrics = new HashMap<>();
    private Map<String, UIMetric> customMetrics = new HashMap<>();

    public void reset() {
        primary.clear();
        secondary.clear();
        custom.clear();
        primaryMetrics.clear();
        secondaryMetrics.clear();
        customMetrics.clear();
    }

    public void addMetrics(List<UIMetric> primaries, List<UIMetric> secondaries) {
        addMetrics(primaries, secondaries, null, false);
    }

    public void addMetrics(List<UIMetric> primaries, List<UIMetric> secondaries, List<UIMetric> customs, boolean usePrefix) {
        if (primaries != null) {
            for (UIMetric metric : primaries) {
                String metricName = getMetricName(metric, usePrefix);
                if (metric.isMonitored()) {
                    primary.add(metricName);
                }
                primaryMetrics.put(metric.getName(), metric);
            }
        }
        if (secondaries != null) {
            for (UIMetric metric : secondaries) {
                String metricName = getMetricName(metric, usePrefix);
                if (metric.isMonitored()) {
                    secondary.add(metricName);
                }
                secondaryMetrics.put(metric.getName(), metric);
            }
        }
        if (customs != null) {
            for (UIMetric metric : customs) {
                String metricName = getMetricName(metric, usePrefix);
                if (metric.isMonitored()) {
                    custom.add(metricName);
                }
                customMetrics.put(metric.getName(), metric);
            }
        }

    }

    public List<String> determineDeletedPrimaryServices(List<UIMetric> metrics, boolean usePrefix) {
        List<String> result = new LinkedList<>();
        if (metrics != null) {
            for (UIMetric metric : metrics) {
                String metricName = getMetricName(metric, usePrefix);
                if (!metric.isMonitored() && primary.contains(metricName)) {
                    result.add(metricName);
                }
                else {
                    UIMetric oldMetric = primaryMetrics.get(metric.getName());
                    if (oldMetric != null) {
                        String oldMetricName = getMetricName(oldMetric, usePrefix);
                        String newMetricName = getMetricName(metric, usePrefix);
                        if (!oldMetricName.equals(newMetricName)) {
                            result.add(oldMetricName);
                            if (log.isInfoEnabled()) {
                                log.info("adding PRIMARY metric to be deleted: " + oldMetricName + ", new: " + newMetricName);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<String> determineDeletedSecondaryServices(List<UIMetric> metrics, boolean usePrefix) {
        List<String> result = new LinkedList<>();
        if (metrics != null) {
            for (UIMetric metric : metrics) {
                String metricName = getMetricName(metric, usePrefix);
                if (!metric.isMonitored() && secondary.contains(metricName)) {
                    result.add(metricName);
                }
                else {
                    UIMetric oldMetric = secondaryMetrics.get(metric.getName());
                    if (oldMetric != null) {
                        String oldMetricName = getMetricName(oldMetric, usePrefix);
                        String newMetricName = getMetricName(metric, usePrefix);
                        if (!oldMetricName.equals(newMetricName)) {
                            result.add(oldMetricName);
                            if (log.isInfoEnabled()) {
                                log.info("adding SECONDARY metric to be deleted: " + oldMetricName + ", new: " + newMetricName);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<String> determineDeletedCustomServices(List<UIMetric> metrics, boolean usePrefix) {
        List<String> result = new LinkedList<>();
        if (metrics != null) {
            for (UIMetric metric : metrics) {
                String metricName = getMetricName(metric, usePrefix);
                if (!metric.isMonitored() && custom.contains(metricName)) {
                    result.add(metricName);
                }
                else {
                    UIMetric oldMetric = customMetrics.get(metric.getName());
                    if (oldMetric != null) {
                        String oldMetricName = getMetricName(oldMetric, usePrefix);
                        String newMetricName = getMetricName(metric, usePrefix);
                        if (!oldMetricName.equals(newMetricName)) {
                            result.add(oldMetricName);
                            if (log.isInfoEnabled()) {
                                log.info("adding CUSTOM metric to be deleted: " + oldMetricName + ", new: " + newMetricName);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    protected String getMetricName(UIMetric metric, boolean usePrefix) {
        if (!StringUtils.isEmpty(metric.getCustomName())) {
            if (usePrefix) {
                String[] substrings = metric.getName().split("\\.");
                String customName = (substrings.length == 2) ?
                        substrings[0] + "." + metric.getCustomName() : metric.getCustomName();
                return customName;
            }
            else {
                return metric.getCustomName();
            }
        }
        return metric.getName();
    }

    public Map<String, UIMetric> getPrimaryMetrics() {
        return primaryMetrics;
    }

    public void setPrimaryMetrics(Map<String, UIMetric> primaryMetrics) {
        this.primaryMetrics = primaryMetrics;
    }

    public Map<String, UIMetric> getSecondaryMetrics() {
        return secondaryMetrics;
    }

    public void setSecondaryMetrics(Map<String, UIMetric> secondaryMetrics) {
        this.secondaryMetrics = secondaryMetrics;
    }

    public Map<String, UIMetric> getCustomMetrics() {
        return customMetrics;
    }

    public void setCustomMetrics(Map<String, UIMetric> customMetrics) {
        this.customMetrics = customMetrics;
    }
}
