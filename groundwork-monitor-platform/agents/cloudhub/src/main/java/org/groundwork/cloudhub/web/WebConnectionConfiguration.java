package org.groundwork.cloudhub.web;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;

public class WebConnectionConfiguration {

    private ConnectionConfiguration configuration;
    private Integer monitorExceptionCount = 0;

    public WebConnectionConfiguration(ConnectionConfiguration configuration, Integer monitorExceptionCount) {
        this.configuration = configuration;
        this.monitorExceptionCount = monitorExceptionCount;
    }

    public CommonConfiguration getCommon() {
        return configuration.getCommon();
    }

    public GWOSConfiguration getGwos() {
        return configuration.getGwos();
    }

    public MonitorConnection getConnection() { return configuration.getConnection(); }

    public Integer getMonitorExceptionCount() { return monitorExceptionCount; }

}
