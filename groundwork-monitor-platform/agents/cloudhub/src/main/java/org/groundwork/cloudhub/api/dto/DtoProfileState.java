package org.groundwork.cloudhub.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DtoProfileState {

    private List<DtoMetricRemoved> metricsRemoved = new ArrayList<>();
    private Boolean isConnected = false;

    public DtoProfileState() {
    }

    public DtoProfileState(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public List<DtoMetricRemoved> getMetricsRemoved() {
        return metricsRemoved;
    }

    public void setMetricsRemoved(List<DtoMetricRemoved> metricsRemoved) {
        this.metricsRemoved = metricsRemoved;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }
}
