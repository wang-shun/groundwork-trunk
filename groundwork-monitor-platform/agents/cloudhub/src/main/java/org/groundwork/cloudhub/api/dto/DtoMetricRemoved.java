package org.groundwork.cloudhub.api.dto;

/**
 * Created by dtaylor on 6/16/17.
 */
public class DtoMetricRemoved {
    private String metric;
    private String serviceType;
    private Boolean isMonitored;

    public DtoMetricRemoved() {
    }

    public DtoMetricRemoved(String metric, String serviceType, Boolean isMonitored) {
        this.metric = metric;
        this.serviceType = serviceType;
        this.isMonitored = isMonitored;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Boolean getMonitored() {
        return isMonitored;
    }

    public void setMonitored(Boolean monitored) {
        isMonitored = monitored;
    }

    @Override
    public String toString() {
        return serviceType + "-" + metric + " : " + isMonitored;
    }
}
