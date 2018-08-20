package org.groundwork.connectors.solarwinds.status;

public enum MonitorProperty {

    PerformanceData,
    LastPluginOutput;

    public String value() {
        return name();
    }
}
